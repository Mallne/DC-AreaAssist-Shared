import { buildCoverageGeometry, buildWireOptimizedCoverage, countVertices, isValidGeometry, type BoundaryGeometry } from './geometry.ts';

interface GeometryRequest { id: number; geometry: BoundaryGeometry; bundleGeometries?: BoundaryGeometry[]; fidelity: number; auto: boolean }

self.onmessage = ({ data }: MessageEvent<GeometryRequest>) => {
  const { id, geometry, bundleGeometries, fidelity, auto } = data;
  try {
    const optimized = auto ? buildWireOptimizedCoverage(geometry, bundleGeometries ?? [geometry]) : null;
    const result = optimized?.geometry ?? buildCoverageGeometry(geometry, fidelity);
    if (!isValidGeometry(result)) throw new Error('The coverage model did not pass geometry checks.');
    const wireBytes = optimized?.wireBytes ?? new TextEncoder().encode(JSON.stringify(result)).length;
    const bundled = bundleGeometries ?? [geometry];
    const bundleWireBytes = optimized?.bundleWireBytes ?? new TextEncoder().encode(JSON.stringify(
      bundled.map((item) => buildCoverageGeometry(item, fidelity)),
    )).length;
    self.postMessage({ id, geometry: result, vertices: countVertices(result), fidelity: optimized?.fidelity ?? fidelity,
      wireBytes, bundleWireBytes });
  } catch (error) {
    self.postMessage({ id, error: error instanceof Error ? error.message : 'Unable to build the coverage model.' });
  }
};
