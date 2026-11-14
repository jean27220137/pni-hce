import { test, expect } from '@playwright/test';

test.describe('Establecimientos', () => {

  test('lista establecimientos registrados en el MPI', async ({ page }) => {
    await page.goto('/establecimientos');
    await expect(page.getByRole('heading', { name: /Establecimientos/i })).toBeVisible();
    // Table renders at least one row (requires MPI running with test data)
    const rows = page.locator('p-table tbody tr');
    await expect(rows.first()).toBeVisible({ timeout: 10000 });
  });

  test('navega desde la barra de navegación', async ({ page }) => {
    await page.goto('/buscar');
    await page.getByRole('link', { name: /Establecimientos/i }).click();
    await expect(page).toHaveURL(/\/establecimientos/);
  });
});
