import test from 'node:test';
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { buildCoverageGeometry, countVertices, isValidGeometry, raycastContains, type BoundaryGeometry } from '../src/geometry.ts';

const states = ['DE-BW', 'DE-BY', 'DE-BE', 'DE-BB', 'DE-HB', 'DE-HH', 'DE-HE', 'DE-MV', 'DE-NI', 'DE-NW', 'DE-RP', 'DE-SL', 'DE-SN', 'DE-ST', 'DE-SH', 'DE-TH'];

test('bundled state data contains all 16 ISO-coded German states', async () => {
  const data = JSON.parse(await readFile(new URL('../public/data/germany-states.geojson', import.meta.url), 'utf8'));
  assert.equal(data.features.length, 16);
  assert.deepEqual(data.features.map((feature: { properties: { id: string } }) => feature.properties.id).sort(), [...states].sort());
  assert.ok(data.features.every((feature: { geometry: unknown }) => isValidGeometry(feature.geometry)));
});

test('coarse models cover every bundled state boundary sample', async () => {
  const data = JSON.parse(await readFile(new URL('../public/data/germany-states.geojson', import.meta.url), 'utf8'));
  for (const feature of data.features as Array<{ geometry: BoundaryGeometry }>) {
    const polygons = feature.geometry.type === 'Polygon' ? [feature.geometry.coordinates] : feature.geometry.coordinates;
    const sourceSamples: number[][] = [];
    for (const polygon of polygons) for (const ring of polygon) {
      for (let index = 1; index < ring.length; index += 1) {
        const previous = ring[index - 1]; const current = ring[index];
        sourceSamples.push(previous, [(previous[0] + current[0]) / 2, (previous[1] + current[1]) / 2]);
      }
    }
    for (const fidelity of [1, 40, 80, 99]) {
      const cover = buildCoverageGeometry(feature.geometry, fidelity);
      assert.ok(countVertices(cover) <= countVertices(feature.geometry), `fidelity ${fidelity} expanded vertex count`);
      assert.ok(sourceSamples.every((point) => raycastContains(cover, point)), `fidelity ${fidelity} omitted a source point`);
    }
  }
});

test('bundled country catalog has names and valid rough outlines', async () => {
  const data = JSON.parse(await readFile(new URL('../public/data/countries.geojson', import.meta.url), 'utf8'));
  assert.ok(data.features.length >= 170);
  assert.ok(data.features.every((feature: { properties: { NAME?: string }; geometry: unknown }) => feature.properties.NAME && isValidGeometry(feature.geometry)));
});
