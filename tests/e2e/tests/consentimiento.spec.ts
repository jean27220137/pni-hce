import { test, expect } from '@playwright/test';

test.describe('Registrar Consentimiento', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto('/consentimiento');
  });

  test('muestra el formulario al entrar', async ({ page }) => {
    await expect(page.getByRole('heading', { name: /Registrar Consentimiento/i })).toBeVisible();
    await expect(page.locator('#dniConsent')).toBeVisible();
  });

  test('valida DNI inválido sin llamar al servidor', async ({ page }) => {
    await page.locator('#dniConsent').fill('123');
    await page.getByRole('button', { name: /Otorgar consentimiento/i }).click();
    await expect(page.locator('p-message[severity="error"]')).toBeVisible();
  });

  test('registra consentimiento GRANTED (requiere consent service)', async ({ page }) => {
    await page.route('**/api/v1/consent/pacientes/*', async route => {
      if (route.request().method() === 'PUT') {
        await route.fulfill({
          status: 201,
          contentType: 'application/json',
          body: JSON.stringify({ estado: 'GRANTED', actualizadoEn: '2026-11-13T15:00:00Z' })
        });
      } else {
        await route.continue();
      }
    });

    await page.locator('#dniConsent').fill('71234567');
    await page.getByRole('button', { name: /Otorgar consentimiento/i }).click();
    await expect(page.locator('p-message[severity="success"]')).toBeVisible({ timeout: 5000 });
    await expect(page.locator('p-message[severity="success"]')).toContainText('otorgado');
  });

  test('navega desde la barra de navegación', async ({ page }) => {
    await page.goto('/buscar');
    await page.getByRole('link', { name: /Consentimiento/i }).click();
    await expect(page).toHaveURL(/\/consentimiento/);
  });
});
