import { defineConfig } from '@playwright/test';
import dotenv from 'dotenv';
import { fileURLToPath } from 'node:url';

dotenv.config({
  path: fileURLToPath(new URL('.env', import.meta.url)),
  override: false,
  quiet: true,
});

const codexBaseUrl = new URL(process.env.CODEX_BASE_URL ?? 'http://127.0.0.1:8081');
if (codexBaseUrl.hostname === 'api.areaassist.mallne.cloud') {
  throw new Error('Playwright contract tests must not target the production Codex instance.');
}

export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  reporter: 'list',
  // Keep tests local by default; never send test traffic to a deployed Codex.
  use: { baseURL: codexBaseUrl.toString() },
});
