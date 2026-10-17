export type Position = number[];
export type Ring = Position[];
export type PolygonCoordinates = Ring[];
export type BoundaryGeometry =
  | { type: 'Polygon'; coordinates: PolygonCoordinates }
  | { type: 'MultiPolygon'; coordinates: PolygonCoordinates[] };

export function countVertices(geometry: BoundaryGeometry): number {
  let count = 0;
  const visit = (coordinates: unknown): void => {
    if (!Array.isArray(coordinates)) return;
    if (typeof coordinates[0] === 'number') count += 1;
    else coordinates.forEach(visit);
  };
  visit(geometry.coordinates);
  return count;
}

function pointOnSegment(point: Position, a: Position, b: Position): boolean {
  const cross = (point[1] - a[1]) * (b[0] - a[0]) - (point[0] - a[0]) * (b[1] - a[1]);
  if (Math.abs(cross) > 1e-10) return false;
  return point[0] >= Math.min(a[0], b[0]) - 1e-10 && point[0] <= Math.max(a[0], b[0]) + 1e-10
    && point[1] >= Math.min(a[1], b[1]) - 1e-10 && point[1] <= Math.max(a[1], b[1]) + 1e-10;
}

function pointInRing(point: Position, ring: Ring): boolean {
  let inside = false;
  for (let i = 0, j = ring.length - 1; i < ring.length; j = i, i += 1) {
    const a = ring[j]; const b = ring[i];
    if (pointOnSegment(point, a, b)) return true;
    if ((b[1] > point[1]) !== (a[1] > point[1])
      && point[0] < ((a[0] - b[0]) * (point[1] - b[1])) / (a[1] - b[1]) + b[0]) inside = !inside;
  }
  return inside;
}

function pointInPolygon(point: Position, polygon: PolygonCoordinates): boolean {
  if (polygon.length === 0 || !pointInRing(point, polygon[0])) return false;
  for (const hole of polygon.slice(1)) {
    if (hole.some((position, index) => index > 0 && pointOnSegment(point, hole[index - 1], position))) return true;
    if (pointInRing(point, hole)) return false;
  }
  return true;
}

/** Ray-cast against a Polygon/MultiPolygon; points on edges count as inside. */
export function raycastContains(geometry: BoundaryGeometry, point: Position): boolean {
  const polygons = geometry.type === 'Polygon' ? [geometry.coordinates] : geometry.coordinates;
  return polygons.some((polygon) => {
    const unwrapped = unwrapPolygon(polygon);
    const longitudes = unwrapped[0].map(([longitude]) => longitude);
    const center = (Math.min(...longitudes) + Math.max(...longitudes)) / 2;
    let longitude = point[0];
    while (longitude - center > 180) longitude -= 360;
    while (longitude - center < -180) longitude += 360;
    return pointInPolygon([longitude, point[1]], unwrapped);
  });
}

function unwrapRing(ring: Ring): Ring {
  if (ring.length < 2) return ring.map((position) => [...position]);
  const points = [ring[0].slice()];
  for (const position of ring.slice(1, -1)) {
    let longitude = position[0];
    const previous = points.at(-1)![0];
    while (longitude - previous > 180) longitude -= 360;
    while (longitude - previous < -180) longitude += 360;
    points.push([longitude, position[1]]);
  }
  points.push([...points[0]]);
  return points;
}

function unwrapPolygon(polygon: PolygonCoordinates): PolygonCoordinates {
  const outer = unwrapRing(polygon[0]);
  const center = (Math.min(...outer.map((point) => point[0])) + Math.max(...outer.map((point) => point[0]))) / 2;
  const holes = polygon.slice(1).map((ring) => {
    const result = unwrapRing(ring);
    const ringCenter = (Math.min(...result.map((point) => point[0])) + Math.max(...result.map((point) => point[0]))) / 2;
    const shift = Math.round((center - ringCenter) / 360) * 360;
    return result.map(([longitude, latitude]) => [longitude + shift, latitude]);
  });
  return [outer, ...holes];
}

function turn(a: Position, b: Position, c: Position): number {
  return (b[0] - a[0]) * (c[1] - a[1]) - (b[1] - a[1]) * (c[0] - a[0]);
}

function convexHull(points: Ring): Ring {
  const sorted = points.slice().sort((a, b) => a[0] - b[0] || a[1] - b[1]);
  const lower: Ring = [];
  for (const point of sorted) {
    while (lower.length >= 2 && turn(lower.at(-2)!, lower.at(-1)!, point) <= 0) lower.pop();
    lower.push(point);
  }
  const upper: Ring = [];
  for (const point of [...sorted].reverse()) {
    while (upper.length >= 2 && turn(upper.at(-2)!, upper.at(-1)!, point) <= 0) upper.pop();
    upper.push(point);
  }
  return [...lower.slice(0, -1), ...upper.slice(0, -1)];
}

function ringArea(ring: Ring): number {
  return Math.abs(signedArea(ring));
}

/** Find a compact enclosing k-gon by minimizing area over rotated regular support directions. */
function minimumAreaEnclosingKgon(points: Ring, sides: number): Ring {
  const rotationSteps = 96;
  let best: Ring = [];
  let bestArea = Number.POSITIVE_INFINITY;
  for (let step = 0; step < rotationSteps; step += 1) {
    const offset = step * Math.PI / (sides * rotationSteps);
    const supports = Array.from({ length: sides }, (_, index) => {
      const angle = offset + index * 2 * Math.PI / sides;
      const nx = Math.cos(angle); const ny = Math.sin(angle);
      return { nx, ny, distance: Math.max(...points.map(([x, y]) => nx * x + ny * y)) };
    });
    const candidate: Ring = supports.map((line, index) => {
      const next = supports[(index + 1) % sides];
      const determinant = line.nx * next.ny - line.ny * next.nx;
      return [
        (line.distance * next.ny - line.ny * next.distance) / determinant,
        (line.nx * next.distance - line.distance * next.nx) / determinant,
      ];
    });
    if (candidate.some(([x, y]) => !Number.isFinite(x) || !Number.isFinite(y))) continue;
    const area = ringArea(candidate);
    if (area < bestArea) { bestArea = area; best = candidate; }
  }
  return best;
}

function signedArea(ring: Ring): number {
  return ring.reduce((sum, point, index) => {
    const next = ring[(index + 1) % ring.length];
    return sum + point[0] * next[1] - next[0] * point[1];
  }, 0) / 2;
}

function segmentsIntersect(a: Position, b: Position, c: Position, d: Position): boolean {
  const abC = turn(a, b, c); const abD = turn(a, b, d);
  const cdA = turn(c, d, a); const cdB = turn(c, d, b);
  return ((abC > 1e-12 && abD < -1e-12) || (abC < -1e-12 && abD > 1e-12))
    && ((cdA > 1e-12 && cdB < -1e-12) || (cdA < -1e-12 && cdB > 1e-12));
}

function removalIsSimple(ring: Ring, index: number): boolean {
  const previous = ring[(index - 1 + ring.length) % ring.length];
  const next = ring[(index + 1) % ring.length];
  for (let edge = 0; edge < ring.length; edge += 1) {
    const edgeNext = (edge + 1) % ring.length;
    if (edge === index || edgeNext === index || edgeNext === (index - 1 + ring.length) % ring.length) continue;
    if (segmentsIntersect(previous, next, ring[edge], ring[edgeNext])) return false;
  }
  return true;
}

/**
 * Remove inward-notch vertices to reduce point count without cutting across the source area.
 * Reflex-only removal fills a notch rather than shaving off an outer corner. Fidelity sets a
 * target point budget; parts that have no safe reflex vertices stop above that target.
 */
function conservativeOuterRing(sourceRing: Ring, fidelity: number): Ring {
  const points = unwrapRing(sourceRing).slice(0, -1);
  if (points.length <= 3) return sourceRing.map((position) => [...position]);
  const orientation = Math.sign(signedArea(points));
  const targetCount = Math.max(3, Math.ceil(3 + (points.length - 3) * ((fidelity - 1) / 99)));
  const hull = convexHull(points);
  if (targetCount < hull.length) {
    const compact = minimumAreaEnclosingKgon(points, targetCount);
    const normalized = compact.map(([longitude, latitude]) => {
      let wrapped = ((longitude + 180) % 360 + 360) % 360 - 180;
      if (wrapped === -180 && longitude > 0) wrapped = 180;
      return [wrapped, latitude];
    });
    normalized.push([...normalized[0]]);
    return normalized;
  }
  const simplified = [...points];

  while (simplified.length > targetCount) {
    let bestIndex = -1;
    let bestScore = Number.POSITIVE_INFINITY;
    for (let index = 0; index < simplified.length; index += 1) {
      const previous = simplified[(index - 1 + simplified.length) % simplified.length];
      const current = simplified[index];
      const next = simplified[(index + 1) % simplified.length];
      const bend = turn(previous, current, next) * orientation;
      if (bend >= -1e-12 || !removalIsSimple(simplified, index)) continue;
      const score = Math.abs(bend);
      if (score < bestScore) { bestScore = score; bestIndex = index; }
    }
    if (bestIndex < 0) break;
    simplified.splice(bestIndex, 1);
  }

  const normalized = simplified.map(([longitude, latitude]) => {
    let wrapped = ((longitude + 180) % 360 + 360) % 360 - 180;
    if (wrapped === -180 && longitude > 0) wrapped = 180;
    return [wrapped, latitude];
  });
  normalized.push([...normalized[0]]);
  return normalized;
}

/**
 * Build a conservative, point-budgeted outline. Coarse levels remove inward notches first and may
 * retain more points than their target when further removal could cut the source boundary. At
 * 100%, return the exact source geometry (including holes).
 */
export function buildCoverageGeometry(geometry: BoundaryGeometry, fidelity: number): BoundaryGeometry {
  const level = Math.max(1, Math.min(100, Math.round(fidelity)));
  if (level === 100) return structuredClone(geometry);
  const simplifyPolygon = (polygon: PolygonCoordinates): PolygonCoordinates =>
    polygon.length === 0 ? [] : [conservativeOuterRing(polygon[0], level)];
  if (geometry.type === 'Polygon') return { type: 'Polygon', coordinates: simplifyPolygon(geometry.coordinates) };
  return { type: 'MultiPolygon', coordinates: geometry.coordinates.map(simplifyPolygon) };
}

function geometryArea(geometry: BoundaryGeometry): number {
  const polygons = geometry.type === 'Polygon' ? [geometry.coordinates] : geometry.coordinates;
  return polygons.reduce((sum, polygon) => {
    const rings = polygon.map((ring) => Math.abs(signedArea(unwrapRing(ring))));
    return sum + Math.max(0, (rings[0] ?? 0) - rings.slice(1).reduce((holes, area) => holes + area, 0));
  }, 0);
}

/**
 * Pick the elbow in the aggregate bundle-size versus added-coverage curve. This avoids an
 * arbitrary per-boundary byte cap: every bundled state boundary contributes to the estimate.
 */
export function buildWireOptimizedCoverage(
  geometry: BoundaryGeometry,
  bundleGeometries: BoundaryGeometry[] = [geometry],
): { fidelity: number; geometry: BoundaryGeometry; wireBytes: number; bundleWireBytes: number } {
  const sourceArea = bundleGeometries.reduce((sum, item) => sum + geometryArea(item), 0);
  const levels = [1, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100];
  const candidates = levels.map((fidelity) => {
    const models = bundleGeometries.map((item) => buildCoverageGeometry(item, fidelity));
    const modelArea = models.reduce((sum, item) => sum + geometryArea(item), 0);
    const bundleWireBytes = new TextEncoder().encode(JSON.stringify(models)).length;
    return { fidelity, model: buildCoverageGeometry(geometry, fidelity), bundleWireBytes,
      areaError: sourceArea === 0 ? 0 : Math.max(0, (modelArea - sourceArea) / sourceArea) };
  });
  const minBytes = Math.min(...candidates.map((candidate) => candidate.bundleWireBytes));
  const maxBytes = Math.max(...candidates.map((candidate) => candidate.bundleWireBytes));
  const maxError = Math.max(...candidates.map((candidate) => candidate.areaError));
  let best = candidates.at(-1)!;
  let bestGain = 0;
  for (const candidate of candidates) {
    const sizeRatio = maxBytes === minBytes ? 1 : (candidate.bundleWireBytes - minBytes) / (maxBytes - minBytes);
    const gain = maxError === 0 ? 0 : maxError * (1 - sizeRatio) - candidate.areaError;
    if (gain > bestGain) { best = candidate; bestGain = gain; }
  }
  // With no useful elbow, prefer exact geometry rather than an arbitrary coarse level.
  if (bestGain === 0) best = candidates.at(-1)!;
  return {
    fidelity: best.fidelity,
    geometry: best.model,
    wireBytes: new TextEncoder().encode(JSON.stringify(best.model)).length,
    bundleWireBytes: best.bundleWireBytes,
  };
}

export function isValidGeometry(value: unknown): value is BoundaryGeometry {
  if (!value || typeof value !== 'object') return false;
  const geometry = value as { type?: unknown; coordinates?: unknown };
  if (geometry.type !== 'Polygon' && geometry.type !== 'MultiPolygon') return false;
  if (!Array.isArray(geometry.coordinates)) return false;
  const polygons: unknown[] = geometry.type === 'Polygon' ? [geometry.coordinates] : geometry.coordinates;
  if (!Array.isArray(polygons) || polygons.length === 0) return false;
  return polygons.every((polygon: unknown) => Array.isArray(polygon) && polygon.length > 0 && polygon.every((ring: unknown) => {
    if (!Array.isArray(ring) || ring.length < 4) return false;
    if (!ring.every((point: unknown) => Array.isArray(point) && point.length >= 2 && Number.isFinite(point[0]) && Number.isFinite(point[1]))) return false;
    if (ring[0][0] !== ring.at(-1)[0] || ring[0][1] !== ring.at(-1)[1]) return false;
    const unique = new Set(ring.slice(0, -1).map((point) => JSON.stringify(point)));
    return unique.size >= 3 && ring.every((point) => Array.isArray(point) && point.length >= 2
      && Number.isFinite(point[0]) && Number.isFinite(point[1])
      && point[0] >= -180 && point[0] <= 180 && point[1] >= -90 && point[1] <= 90);
  }));
}
