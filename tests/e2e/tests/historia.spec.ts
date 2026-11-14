import { test, expect } from '@playwright/test';

test.describe('Historia Clínica', () => {

  test('accede a la historia clínica desde búsqueda (DNI con consentimiento)', async ({ page }) => {
    // Requires full stack: MPI + Gateway + Simulators + Consent service running
    // DNI 71234567 tiene consentimiento GRANTED y vinculos pre-cargados en init SQL
    await page.goto('/buscar');
    await page.locator('#dni').fill('71234567');
    await page.getByRole('button', { name: /Buscar/i }).click();

    const btnHistoria = page.getByRole('button', { name: /Ver historia clínica completa/i });
    await expect(btnHistoria).toBeVisible({ timeout: 10000 });
    await btnHistoria.click();

    await expect(page).toHaveURL(/\/historia\/71234567/);
    await expect(page.getByRole('heading', { name: /Historia Clínica/i })).toBeVisible({ timeout: 10000 });
    // Deben aparecer los 2 establecimientos
    await expect(page.locator('p-panel')).toHaveCount(2, { timeout: 10000 });
  });

  test('muestra mensaje de consentimiento requerido (403)', async ({ page }) => {
    // Intercept gateway response with 403
    await page.route('**/api/v1/pacientes/*/historia', async route => {
      await route.fulfill({
        status: 403,
        contentType: 'application/problem+json',
        body: JSON.stringify({
          title: 'Consentimiento requerido',
          detail: 'El paciente no ha otorgado consentimiento',
          status: 403
        })
      });
    });

    await page.goto('/historia/99999999');
    await expect(page.locator('p-message[severity="warn"]')).toContainText('consentimiento', { timeout: 5000 });
  });

  test('muestra spinner mientras carga', async ({ page }) => {
    // Delay gateway response to observe spinner
    await page.route('**/api/v1/pacientes/*/historia', async route => {
      await new Promise(r => setTimeout(r, 1500));
      await route.fulfill({ status: 404, body: '{}' });
    });

    await page.goto('/historia/12345678');
    await expect(page.locator('p-progressspinner')).toBeVisible();
  });
});
