import test from 'node:test';
import assert from 'node:assert/strict';
import { makeExport, makeFilename, makeKotlinFilename, makeKotlinModel } from '../src/export.ts';
import { countVertices, type BoundaryGeometry } from '../src/geometry.ts';

test('export is a deterministic single-feature FeatureCollection', () => {
  const area = { id: 'DE-BY', iso: 'DE-BY', name: 'Bayern', type: 'state' as const, source: 'fixture', sourceVersion: '1' };
  const geometry: BoundaryGeometry = { type: 'Polygon', coordinates: [[[0, 0], [1, 0], [0, 1], [0, 0]]] };
  const one = makeExport(area, geometry, 40, 4);
  assert.equal(one, makeExport(area, geometry, 40, 4));
  const parsed = JSON.parse(one);
  assert.equal(parsed.features.length, 1);
  assert.deepEqual(parsed.features[0].geometry, geometry);
  assert.equal(parsed.features[0].properties.isoCode, 'DE-BY');
  assert.equal(parsed.features[0].properties.coverageModel, 'conservative-notch-simplification');
  assert.equal(parsed.features[0].properties.falsePositivesExpected, true);
});

test('filename is stable and sanitized', () => {
  assert.equal(makeFilename({ id: 'DE-BY', iso: 'DE-BY' }, 40), 'de-by-fidelity-40.geojson');
  assert.equal(makeFilename({ id: 'a / b' }, 100), 'a-b-fidelity-100.geojson');
});

test('Kotlin state export implements StateDefinition and preserves multipart rings', () => {
  const area = { id: 'DE-BY', iso: 'DE-BY', name: 'Bayern', type: 'state' as const, source: 'fixture', sourceVersion: '1' };
  const primary: BoundaryGeometry = {
    type: 'Polygon',
    coordinates: [
      [[0, 0], [1, 0], [0, 1], [0, 0]],
      [[0.2, 0.2], [0.3, 0.2], [0.2, 0.3], [0.2, 0.2]],
    ],
  };
  const multi: BoundaryGeometry = {
    type: 'MultiPolygon',
    coordinates: [primary.coordinates, [[[10, 10], [11, 10], [11, 11], [10, 10]]]],
  };
  const output = makeKotlinModel(area, multi, 40);
  assert.equal(makeKotlinFilename(area), 'de-by.kt');
  assert.match(output, /internal object Bayern : StateDefinition/);
  assert.match(output, /: StateDefinition/);
  assert.match(output, /override val iso3166_2: String = "DE-BY"/);
  assert.match(output, /override val boundary: MultiPolygon/);
  assert.match(output, /coordinates = listOf/);
  assert.equal((output.match(/Position\(/g) ?? []).length, countVertices(multi));
});
