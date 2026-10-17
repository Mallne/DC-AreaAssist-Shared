import { Map, NavigationControl, AttributionControl, LngLatBounds, setWorkerUrl } from 'maplibre-gl';
import type { GeoJSONSource } from 'maplibre-gl';
import mapWorkerUrl from 'maplibre-gl/dist/maplibre-gl-worker.mjs?worker&url';
import './styles.css';
import { countVertices, isValidGeometry, raycastContains, type BoundaryGeometry } from './geometry.ts';
import { makeExport, makeFilename, makeKotlinFilename, makeKotlinModel, type ExportArea } from './export.ts';
type AreaType = 'country' | 'state';
type BoundaryFeature = GeoJSON.Feature<GeoJSON.Polygon | GeoJSON.MultiPolygon, Record<string, unknown>>;
type AreaOption = ExportArea & { feature: BoundaryFeature };
type CoverageResult = { id: number; geometry?: BoundaryGeometry; vertices?: number; fidelity?: number; wireBytes?: number; bundleWireBytes?: number; error?: string };

const DATA: Record<AreaType, { url: string; source: string; version: string }> = {
  country: { url: `${import.meta.env.BASE_URL}data/countries.geojson`, source: 'Natural Earth', version: '5.1.2 · 1:110m' },
  state: { url: `${import.meta.env.BASE_URL}data/germany-states.geojson`, source: 'deutschlandGeoJSON', version: '4 · low resolution · 4090d4e' },
};
const byId = <T extends HTMLElement = HTMLElement>(id: string): T => {
  const element = document.getElementById(id);
  if (!element) throw new Error(`Missing required element: ${id}`);
  return element as T;
};
const typeInputs = [...document.querySelectorAll<HTMLInputElement>('input[name="areaType"]')];
const areaSearch = byId<HTMLInputElement>('areaSearch');
const areaSelect = byId<HTMLSelectElement>('areaSelect');
const fidelity = byId<HTMLInputElement>('fidelity');
const autoFidelity = byId<HTMLInputElement>('autoFidelity');
const download = byId<HTMLButtonElement>('download');
const downloadGeoJson = byId<HTMLButtonElement>('downloadGeoJson');
const worker = new Worker(new URL('./worker.ts', import.meta.url), { type: 'module' });
setWorkerUrl(mapWorkerUrl);
const empty: GeoJSON.FeatureCollection = { type: 'FeatureCollection', features: [] };
const map = new Map({
  container: 'map',
  center: [5, 25],
  zoom: 1.7,
  minZoom: 1,
  attributionControl: false,
  style: {
    version: 8,
    sources: {},
    layers: [{ id: 'background', type: 'background', paint: { 'background-color': '#edf1ed' } }],
  },
});
map.addControl(new NavigationControl({ showCompass: false }), 'bottom-right');
map.addControl(new AttributionControl({ compact: true, customAttribution: 'Boundary data: Natural Earth · Germany states: deutschlandGeoJSON' }), 'bottom-left');

let mapReady = false;
let areas: AreaOption[] = [];
let selected: AreaOption | null = null;
let currentGeometry: BoundaryGeometry | null = null;
let pendingId = 0;
let debounceTimer: ReturnType<typeof setTimeout>;
let latestBytes = '';
let latestName = '';
let latestGeoJson = '';
let latestGeoJsonName = '';
const setDownloadsDisabled = (disabled: boolean): void => {
  download.disabled = disabled;
  downloadGeoJson.disabled = disabled;
};

const setStatus = (message: string): void => { byId('status').textContent = message; };
const getType = (): AreaType => typeInputs.find((input) => input.checked)?.value as AreaType ?? 'country';
const displayName = (feature: BoundaryFeature, type: AreaType): string => {
  const props = feature.properties;
  const name = type === 'state' ? props.name : (props.NAME_EN || props.NAME || props.ADMIN);
  return typeof name === 'string' ? name : '';
};
const stableId = (feature: BoundaryFeature, type: AreaType): string => {
  const props = feature.properties;
  const id = type === 'state' ? props.id : (props.ADM0_A3 || props.ISO_A3 || props.NAME);
  return typeof id === 'string' ? id : '';
};
const isoCode = (feature: BoundaryFeature, type: AreaType): string => {
  const code = type === 'state' ? feature.properties.id : feature.properties.ISO_A3;
  return typeof code === 'string' && code !== '-99' ? code : '';
};

function addDataSources(): void {
  if (map.getSource('world')) return;
  map.addSource('world', { type: 'geojson', data: empty });
  map.addLayer({ id: 'world-fill', type: 'fill', source: 'world', paint: { 'fill-color': '#e6ece6', 'fill-opacity': 0.68 } });
  map.addLayer({ id: 'world-line', type: 'line', source: 'world', paint: { 'line-color': '#cbd5cc', 'line-width': 0.55 } });
  map.addSource('selected', { type: 'geojson', data: empty });
  map.addLayer({ id: 'selected-fill', type: 'fill', source: 'selected', paint: { 'fill-color': '#85ad91', 'fill-opacity': 0.72 } });
  map.addSource('original', { type: 'geojson', data: empty });
  map.addLayer({ id: 'original-line', type: 'line', source: 'original', layout: { visibility: 'none' }, paint: { 'line-color': '#b18a49', 'line-width': 1.4, 'line-dasharray': [2, 2] } });
}

map.on('load', () => {
  mapReady = true;
  addDataSources();
  if (areas.length) updateContext();
});

function setGeoJson(id: string, data: GeoJSON.Feature | GeoJSON.FeatureCollection): void {
  (map.getSource(id) as GeoJSONSource | undefined)?.setData(data);
}

function updateContext(): void {
  if (!mapReady) return;
  const data = getType() === 'country' && areas.length
    ? { type: 'FeatureCollection' as const, features: areas.map((area) => area.feature) }
    : empty;
  setGeoJson('world', data);
}

async function loadAreas(type: AreaType): Promise<void> {
  setStatus('Loading boundary data…');
  setDownloadsDisabled(true);
  try {
    const response = await fetch(DATA[type].url);
    if (!response.ok) throw new Error(`Boundary data unavailable (${response.status}).`);
    const collection = await response.json() as GeoJSON.FeatureCollection<GeoJSON.Polygon | GeoJSON.MultiPolygon, Record<string, unknown>>;
    if (collection.type !== 'FeatureCollection' || !Array.isArray(collection.features)) throw new Error('Boundary data is not a valid feature collection.');
    areas = collection.features.filter((feature): feature is BoundaryFeature => feature.geometry !== null && isValidGeometry(feature.geometry)).map((feature) => ({
      id: stableId(feature, type), iso: isoCode(feature, type), name: displayName(feature, type),
      type, source: DATA[type].source, sourceVersion: DATA[type].version, feature,
    })).filter((area) => area.id && area.name).sort((a, b) => a.name.localeCompare(b.name));
    if (!areas.length) throw new Error('No valid boundaries were found in this data file.');
    areaSearch.value = '';
    areaSearch.placeholder = type === 'country' ? 'Search countries…' : 'Search German states…';
    selected = null;
    currentGeometry = null;
    updateContext();
    setGeoJson('selected', empty);
    setGeoJson('original', empty);
    renderOptions();
    byId('dataNote').textContent = type === 'country' ? 'Generalized outline · Natural Earth 1:110m' : 'Generalized outline · deutschlandGeoJSON';
    byId('mapLabel').textContent = 'Choose an area to preview';
    byId('modelPoints').textContent = '—';
    byId('wireSize').textContent = '—';
    byId('byteCount').textContent = '—';
    setStatus(`${areas.length} ${type === 'country' ? 'countries' : 'German states'} available.`);
    if (type === 'country') {
      map.fitBounds([[-180, -60], [180, 82]], { padding: 35, duration: 0 });
    }
  } catch (error) {
    areas = [];
    areaSelect.replaceChildren();
    setStatus(error instanceof Error ? error.message : 'Unable to load boundary data.');
  }
}

function renderOptions(): void {
  const query = areaSearch.value.trim().toLocaleLowerCase();
  const filtered = areas.filter((area) => `${area.name} ${area.iso} ${area.id}`.toLocaleLowerCase().includes(query));
  areaSelect.replaceChildren(...filtered.map((area) => {
    const option = document.createElement('option');
    option.value = area.id;
    option.textContent = `${area.name}${area.iso ? ` · ${area.iso}` : ''}`;
    option.selected = selected?.id === area.id;
    return option;
  }));
}

function extendBounds(bounds: LngLatBounds, coordinates: unknown): void {
  if (!Array.isArray(coordinates)) return;
  if (typeof coordinates[0] === 'number' && typeof coordinates[1] === 'number') bounds.extend([coordinates[0], coordinates[1]]);
  else coordinates.forEach((child: unknown) => extendBounds(bounds, child));
}

function fitGeometry(geometry: BoundaryGeometry): void {
  const bounds = new LngLatBounds();
  extendBounds(bounds, geometry.coordinates);
  if (bounds.isEmpty()) return;
  // Fit around the antimeridian using a minimal longitude arc rather than spanning the globe.
  const longitudes: number[] = [];
  const collect = (node: unknown): void => {
    if (!Array.isArray(node)) return;
    if (typeof node[0] === 'number') longitudes.push(node[0]);
    else node.forEach(collect);
  };
  collect(geometry.coordinates);
  if (longitudes.length > 1) {
    const sorted = [...longitudes].sort((a, b) => a - b);
    let gap = -1; let gapIndex = 0;
    for (let i = 0; i < sorted.length; i += 1) {
      const next = i === sorted.length - 1 ? sorted[0] + 360 : sorted[i + 1];
      if (next - sorted[i] > gap) { gap = next - sorted[i]; gapIndex = i; }
    }
    const west = sorted[(gapIndex + 1) % sorted.length];
    let east = sorted[gapIndex];
    if (east < west) east += 360;
    if (east - west < 180) {
      const latWest = bounds.getSouthWest().lat;
      const latEast = bounds.getNorthEast().lat;
      map.fitBounds([[west, latWest], [east, latEast]], { padding: 48, maxZoom: 7, duration: 300 });
      return;
    }
  }
  map.fitBounds(bounds, { padding: 48, maxZoom: 7, duration: 300 });
}

function selectArea(area: AreaOption): void {
  selected = area;
  currentGeometry = null;
  pendingId += 1;
  setDownloadsDisabled(true);
  latestBytes = '';
  clearTimeout(debounceTimer);
  setGeoJson('selected', empty);
  setGeoJson('original', { type: 'Feature', properties: {}, geometry: area.feature.geometry });
  fitGeometry(area.feature.geometry);
  byId('mapLabel').textContent = `${area.name}${area.iso ? ` · ${area.iso}` : ''}`;
  renderOptions();
  compute();
}

function compute(): void {
  if (!selected) return;
  const area = selected;
  const id = ++pendingId;
  setDownloadsDisabled(true);
  setStatus('Updating outline…');
  const requested = Number(fidelity.value);
  debounceTimer = setTimeout(() => {
    const bundleGeometries = area.type === 'state'
      ? areas.map((candidate) => candidate.feature.geometry)
      : [area.feature.geometry];
    worker.postMessage({ id, geometry: area.feature.geometry, bundleGeometries, fidelity: requested, auto: autoFidelity.checked });
  }, 90);
}

worker.onmessage = ({ data }: MessageEvent<CoverageResult>) => {
  if (data.id !== pendingId || !selected) return;
  if (data.error) { currentGeometry = null; setStatus(data.error); return; }
  if (!isValidGeometry(data.geometry) || data.vertices === undefined || data.fidelity === undefined || data.wireBytes === undefined || data.bundleWireBytes === undefined) { currentGeometry = null; setStatus('This result is not valid GeoJSON; export is disabled.'); return; }
  currentGeometry = data.geometry;
  const feature: GeoJSON.Feature = { type: 'Feature', properties: {}, geometry: currentGeometry };
  setGeoJson('selected', feature);
  byId('modelPoints').textContent = data.vertices.toLocaleString();
  byId('wireSize').textContent = data.bundleWireBytes < 1024 ? `${data.bundleWireBytes} B` : `${(data.bundleWireBytes / 1024).toFixed(1)} KB`;
  const level = data.fidelity;
  fidelity.value = String(level);
  byId<HTMLOutputElement>('fidelityOutput').value = `${level}%`;
  byId<HTMLOutputElement>('fidelityOutput').textContent = autoFidelity.checked ? `Auto · ${level}%` : `${level}%`;
  latestBytes = makeKotlinModel(selected, currentGeometry, level);
  latestName = makeKotlinFilename(selected);
  latestGeoJson = makeExport(selected, currentGeometry, level, data.vertices);
  latestGeoJsonName = makeFilename(selected, level);
  const bytes = new TextEncoder().encode(latestBytes).length;
  byId('byteCount').textContent = bytes < 1024 ? `${bytes} B` : `${(bytes / 1024).toFixed(1)} KB`;
  setDownloadsDisabled(false);
  const originalCount = countVertices(selected.feature.geometry);
  if (level === 100) setStatus(`Exact source outline · ${originalCount.toLocaleString()} source vertices.`);
  else setStatus(`Conservative simplified outline · ${data.vertices.toLocaleString()} model points; outside points may be included.`);
};

worker.onerror = () => { currentGeometry = null; setDownloadsDisabled(true); setStatus('Geometry worker failed. Reload the page to try again.'); };

map.on('click', (event) => {
  const result = byId('raycastResult');
  if (!selected || !currentGeometry) { result.textContent = 'Select an area before testing a point.'; return; }
  const point: [number, number] = [event.lngLat.lng, event.lngLat.lat];
  const covered = raycastContains(currentGeometry, point);
  const qualifier = Number(fidelity.value) === 100 ? 'source area' : 'conservative outline';
  result.textContent = `${covered ? 'INSIDE' : 'OUTSIDE'} ${qualifier} · ${point[0].toFixed(4)}, ${point[1].toFixed(4)}`;
});

typeInputs.forEach((input) => input.addEventListener('change', () => loadAreas(getType())));
areaSearch.addEventListener('input', renderOptions);
areaSelect.addEventListener('change', () => {
  const area = areas.find((candidate) => candidate.id === areaSelect.value);
  if (area) selectArea(area);
});
fidelity.addEventListener('input', () => {
  autoFidelity.checked = false;
  byId<HTMLOutputElement>('fidelityOutput').value = `${fidelity.value}%`;
  byId<HTMLOutputElement>('fidelityOutput').textContent = `${fidelity.value}%`;
  compute();
});
autoFidelity.addEventListener('change', compute);
byId<HTMLInputElement>('showOriginal').addEventListener('change', (event) => {
  map.setLayoutProperty('original-line', 'visibility', (event.currentTarget as HTMLInputElement).checked && selected ? 'visible' : 'none');
});
const saveFile = (content: string, filename: string, contentType: string): void => {
  const url = URL.createObjectURL(new Blob([content], { type: contentType }));
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = filename;
  anchor.click();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
};
download.addEventListener('click', () => {
  if (currentGeometry && latestBytes && selected) saveFile(latestBytes, latestName, 'text/x-kotlin');
});
downloadGeoJson.addEventListener('click', () => {
  if (currentGeometry && latestGeoJson && selected) saveFile(latestGeoJson, latestGeoJsonName, 'application/geo+json');
});
byId('reset').addEventListener('click', () => {
  fidelity.value = '40';
  autoFidelity.checked = true;
  byId<HTMLOutputElement>('fidelityOutput').value = '40%';
  byId<HTMLOutputElement>('fidelityOutput').textContent = '40%';
  byId<HTMLInputElement>('showOriginal').checked = false;
  map.setLayoutProperty('original-line', 'visibility', 'none');
  areaSearch.value = '';
  selected = null;
  currentGeometry = null;
  pendingId += 1;
  setDownloadsDisabled(true);
  setGeoJson('selected', empty);
  setGeoJson('original', empty);
  byId('modelPoints').textContent = '—';
  byId('wireSize').textContent = '—';
  byId('byteCount').textContent = '—';
  byId('mapLabel').textContent = 'Choose an area to preview';
  renderOptions();
  setStatus('Select an area to get started.');
});

await loadAreas('country');
