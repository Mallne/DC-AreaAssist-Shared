import { createHash } from 'node:crypto';
import { mkdir, writeFile } from 'node:fs/promises';

type SourceFeature = { id?: string | number; properties: Record<string, unknown>; geometry: GeoJSON.Polygon | GeoJSON.MultiPolygon };
type DataSource = { url: string; path: string; map: (feature: SourceFeature) => GeoJSON.Feature };
type SourceCollection = { features: SourceFeature[] };
type ManifestEntry = { path: string; source: string; sha256: string; features: number };

const sources: DataSource[] = [
  {
    url: 'https://raw.githubusercontent.com/nvkelso/natural-earth-vector/v5.1.2/geojson/ne_110m_admin_0_countries.geojson',
    path: 'public/data/countries.geojson',
    map: (feature) => ({
      type: 'Feature', id: String(feature.properties.ADM0_A3),
      properties: { NAME: feature.properties.NAME, NAME_EN: feature.properties.NAME_EN, ISO_A3: feature.properties.ISO_A3, ADM0_A3: feature.properties.ADM0_A3 },
      geometry: feature.geometry,
    }),
  },
  {
    url: 'https://raw.githubusercontent.com/isellsoap/deutschlandGeoJSON/4090d4e1f89c1184b436b3d9ccaf332b4c5b43d2/2_bundeslaender/4_niedrig.geo.json',
    path: 'public/data/germany-states.geojson',
    map: (feature) => ({ type: 'Feature', id: String(feature.properties.id), properties: { id: feature.properties.id, name: feature.properties.name }, geometry: feature.geometry }),
  },
];

await mkdir('public/data', { recursive: true });
const manifest: { notice: string; data: ManifestEntry[] } = { notice: 'Natural Earth data is public domain. deutschlandGeoJSON is Unlicense.', data: [] };
for (const source of sources) {
  const response = await fetch(source.url);
  if (!response.ok) throw new Error(`Download failed: ${source.url} (${response.status})`);
  const input = await response.json() as SourceCollection;
  if (source.path.endsWith('germany-states.geojson')) {
    const expected = ['DE-BW', 'DE-BY', 'DE-BE', 'DE-BB', 'DE-HB', 'DE-HH', 'DE-HE', 'DE-MV', 'DE-NI', 'DE-NW', 'DE-RP', 'DE-SL', 'DE-SN', 'DE-ST', 'DE-SH', 'DE-TH'];
    const actual = input.features.map((feature) => feature.properties.id).sort();
    if (JSON.stringify(actual) !== JSON.stringify(expected.sort())) throw new Error(`Expected all 16 German states; received ${actual.length}.`);
  }
  const output = JSON.stringify({ type: 'FeatureCollection', features: input.features.map(source.map) });
  await writeFile(source.path, output);
  manifest.data.push({ path: source.path, source: source.url, sha256: createHash('sha256').update(output).digest('hex'), features: input.features.length });
}
await writeFile('public/data/manifest.json', `${JSON.stringify(manifest, null, 2)}\n`);
console.log(`Prepared ${manifest.data.map((item) => `${item.features} features → ${item.path}`).join(', ')}`);
