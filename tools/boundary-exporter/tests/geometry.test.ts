import test from 'node:test';
import assert from 'node:assert/strict';
import { buildCoverageGeometry, buildWireOptimizedCoverage, countVertices, isValidGeometry, raycastContains, type BoundaryGeometry, type PolygonCoordinates } from '../src/geometry.ts';

const geometry: BoundaryGeometry = {
  type: 'Polygon',
  coordinates: [[[-4, 0], [-3, 1], [-2, 0], [-1, 1], [0, 0], [1, 1], [2, 0], [3, 1], [4, 0], [4, -1], [3, -2], [2, -1], [1, -2], [0, -1], [-1, -2], [-2, -1], [-3, -2], [-4, -1], [-4, 0]]],
};

test('100% fidelity returns canonical coordinates and does not mutate input', () => {
  const original = structuredClone(geometry);
  const result = buildCoverageGeometry(geometry, 100);
  assert.deepEqual(result, geometry);
  assert.deepEqual(geometry, original);
});

test('coarse polygon cover contains source points and remains valid', () => {
  const result = buildCoverageGeometry(geometry, 30);
  assert.ok(isValidGeometry(result));
  assert.ok(countVertices(result) > 0);
  assert.ok(raycastContains(result, [0, 0]));
  assert.ok(raycastContains(result, [-4, 0]));
  assert.equal(raycastContains(result, [20, 20]), false);
});

test('increasing fidelity progressively tightens the conservative cover', () => {
  const low = buildCoverageGeometry(geometry, 50);
  const medium = buildCoverageGeometry(geometry, 70);
  const high = buildCoverageGeometry(geometry, 90);

  assert.notDeepEqual(low, medium);
  assert.notDeepEqual(medium, high);
  for (const cover of [low, medium, high]) {
    assert.ok(isValidGeometry(cover));
    assert.ok(geometry.coordinates[0].every(([longitude, latitude]) =>
      raycastContains(cover, [longitude, latitude])));
  }
});

test('very low fidelity can use a three-point enclosing model', () => {
  const result = buildCoverageGeometry(geometry, 1);
  assert.equal(countVertices(result), 4);
  assert.ok(isValidGeometry(result));
  assert.ok(geometry.coordinates[0].every(([longitude, latitude]) =>
    raycastContains(result, [longitude, latitude])));
});

test('wire optimizer chooses a data-driven tradeoff for the aggregate bundle', () => {
  const manyPoints: BoundaryGeometry = {
    type: 'Polygon',
    coordinates: [[...Array.from({ length: 120 }, (_, index) => {
      const angle = (index / 120) * Math.PI * 2;
      return [Math.cos(angle) * (2 + (index % 3) * 0.01), Math.sin(angle) * (2 + (index % 3) * 0.01)];
    }), [2.01, 0]]],
  };
  const bundle = [manyPoints, manyPoints, manyPoints];
  const result = buildWireOptimizedCoverage(manyPoints, bundle);
  assert.ok(result.bundleWireBytes > result.wireBytes);
  assert.ok(result.fidelity >= 1 && result.fidelity <= 100);
  assert.ok(isValidGeometry(result.geometry));
});

test('coarse cover keeps islands and may fill holes; exact fidelity preserves hole semantics', () => {
  const polygonWithHole = [...geometry.coordinates, [[-1, -1], [0, -1], [0, 0], [-1, -1]]];
  const island: PolygonCoordinates = [[[9, 9], [10, 9], [10, 10], [9, 9]]];
  const multi: BoundaryGeometry = { type: 'MultiPolygon', coordinates: [polygonWithHole, island] };
  const coarse = buildCoverageGeometry(multi, 1);
  const exact = buildCoverageGeometry(multi, 100);
  assert.ok(isValidGeometry(coarse));
  assert.ok(raycastContains(coarse, [2, 0]));
  assert.ok(raycastContains(coarse, [9.5, 9.5]));
  assert.equal(raycastContains(exact, [-0.75, -0.9]), false);
});

test('every fidelity covers source vertices and handles antimeridian coordinates', () => {
  const antimeridian: BoundaryGeometry = {
    type: 'Polygon',
    coordinates: [[[179, 10], [-179, 10], [-178, 12], [179, 13], [179, 10]]],
  };
  for (const fidelity of [1, 20, 40, 60, 80, 99, 100]) {
    const cover = buildCoverageGeometry(geometry, fidelity);
    assert.ok(geometry.coordinates[0].every(([longitude, latitude]) => raycastContains(cover, [longitude, latitude])));
  }
  const result = buildCoverageGeometry(antimeridian, 40);
  assert.ok(isValidGeometry(result));
  assert.ok(raycastContains(result, [180, 11]));
  assert.ok(raycastContains(result, [-179.5, 11]));
  assert.equal(raycastContains(result, [0, 11]), false);
});

test('invalid open and non-finite rings are rejected', () => {
  assert.equal(isValidGeometry({ type: 'Polygon', coordinates: [[[0, 0], [1, 0], [0, 1]]] }), false);
  assert.equal(isValidGeometry({ type: 'Polygon', coordinates: [[[0, 0], [1, 0], [0, 1], [NaN, 0]]] }), false);
});
