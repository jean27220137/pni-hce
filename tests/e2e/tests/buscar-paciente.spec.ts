import { test, expect } from '@playwright/test';

test.describe('Buscar Paciente', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto('/buscar');
  });

  test('muestra formulario de búsqueda al entrar', async ({ page }) => {
    await expect(page.getByRole('heading', { name: /Buscar Paciente/i })).toBeVisible();
    await expect(page.locator('#dni')).toBeVisible();
    await expect(page.getByRole('button', { name: /Buscar/i })).toBeVisible();
  });

  test('valida DNI inválido sin llamar al servidor', async ({ page }) => {
    await page.locator('#dni').fill('123');
    await page.getByRole('button', { name: /Buscar/i }).click();
    await expect(page.locator('p-message')).toContainText('8 dígitos');
  });

  test('busca DNI con historial y muestra establecimientos', async ({ page }) => {
    // Requires MPI + Docker running. DNI 71234567 tiene vinculos pre-cargados en init SQL.
    await page.locator('#dni').fill('71234567');
    await page.getByRole('button', { name: /Buscar/i }).click();
    await expect(page.locator('.resultados-header')).toBeVisible({ timeout: 10000 });
    await expect(page.locator('.est-card')).toHaveCount(2); // posta + clínica
  });

  test('botón limpiar resetea el formulario', async ({ page }) => {
    await page.locator('#dni').fill('123');
    await page.getByRole('button', { name: /Buscar/i }).click();
    await expect(page.getByRole('button', { name: /Limpiar/i })).toBeVisible();
    await page.getByRole('button', { name: /Limpiar/i }).click();
    await expect(page.locator('#dni')).toHaveValue('');
    await expect(page.getByRole('button', { name: /Limpiar/i })).not.toBeVisible();
  });
});
