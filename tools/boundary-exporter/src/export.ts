import type { BoundaryGeometry } from './geometry.ts';

export interface ExportArea {
  id: string;
  iso?: string;
  name: string;
  type: 'country' | 'state';
  source: string;
  sourceVersion: string;
}

export function makeExport(area: ExportArea, geometry: BoundaryGeometry, fidelity: number, vertices: number): string {
  const properties = {
    name: area.name,
    areaType: area.type,
    ...(area.iso ? { isoCode: area.iso } : {}),
    source: area.source,
    sourceVersion: area.sourceVersion,
    requestedFidelityPercent: fidelity,
    outputVertices: vertices,
    coverageModel: fidelity === 100 ? 'exact-source-geometry' : 'conservative-notch-simplification',
    falsePositivesExpected: fidelity < 100,
    note: 'Generalized administrative outline; not an authoritative legal or cadastral boundary.',
  };
  return JSON.stringify({
    type: 'FeatureCollection',
    features: [{ type: 'Feature', id: area.id, properties, geometry }],
  }, null, 2);
}

export function makeFilename(area: Pick<ExportArea, 'id' | 'iso'>, fidelity: number): string {
  const id = area.iso || area.id;
  const slug = id.toLowerCase().replace(/[^a-z0-9-]+/g, '-').replace(/^-|-$/g, '');
  return `${slug || 'area'}-fidelity-${fidelity}.geojson`;
}

function kotlinString(value: string): string {
  return `"${value.replace(/\\/g, '\\\\').replace(/"/g, '\\"').replace(/\$/g, '\\$').replace(/\r/g, '\\r').replace(/\n/g, '\\n')}"`;
}

function kotlinIdentifier(value: string): string {
  const words = value.normalize('NFKD').replace(/[\u0300-\u036f]/g, '').match(/[A-Za-z0-9]+/g) ?? [];
  const identifier = words.map((word) => word[0].toUpperCase() + word.slice(1)).join('');
  return /^[A-Za-z_]/.test(identifier) ? identifier : `Area${identifier}`;
}

/** Generate a Kotlin AdministrativeArea object using GeoKit's serializable GeoJSON model. */
export function makeKotlinModel(area: ExportArea, geometry: BoundaryGeometry, fidelity: number): string {
  const polygons = geometry.type === 'Polygon' ? [geometry.coordinates] : geometry.coordinates;
  const ring = (positions: number[][], indent: string): string => [
    `${indent}listOf(`,
    ...positions.map(([longitude, latitude]) => `${indent}    Position(${longitude}, ${latitude}),`),
    `${indent})`,
  ].join('\n');
  const geometrySource = polygons.map((polygon) => [
    '            listOf(',
    ...polygon.flatMap((positions) => [ring(positions, '                ') + ',']),
    '            ),',
  ].join('\n')).join('\n');
  const objectName = kotlinIdentifier(area.name);
  const state = area.type === 'state';
  const output = [
    'package cloud.mallne.dicentra.areaassist.model.areas',
    '',
    ...(state ? [] : ['import cloud.mallne.dicentra.areaassist.model.areas.AdministrativeArea']),
    'import org.maplibre.spatialk.geojson.MultiPolygon',
    'import org.maplibre.spatialk.geojson.Position',
    '',
    `/** Generated boundary model for ${area.name}.`,
    ` * Source: ${area.source} ${area.sourceVersion}. Fidelity: ${fidelity}%.`,
    ' * Generalized outline; not an authoritative legal or cadastral boundary.',
    ' * At fidelity below 100%, this is a conservative cover and may have false positives.',
    ' */',
    `internal object ${objectName} : ${state ? 'StateDefinition' : 'AdministrativeArea'} {`,
    state
      ? `    override val iso3166_2: String = ${kotlinString(area.iso || area.id)}`
      : `    override val id: String = ${kotlinString(area.iso || area.id)}`,
    '',
    '    override val boundary: MultiPolygon = MultiPolygon(',
    '        coordinates = listOf(',
    geometrySource,
    '        ),',
    '    )',
    '}',
    '',
  ];
  return output.join('\n');
}

export function makeKotlinFilename(area: Pick<ExportArea, 'id' | 'iso'>): string {
  const slug = (area.iso || area.id).toLowerCase().replace(/[^a-z0-9-]+/g, '-').replace(/^-|-$/g, '');
  return `${slug || 'area'}.kt`;
}
