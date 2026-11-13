import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'buscar', pathMatch: 'full' },
  {
    path: 'buscar',
    loadComponent: () =>
      import('./features/buscar-paciente/buscar-paciente.component').then(m => m.BuscarPacienteComponent)
  },
  {
    path: 'establecimientos',
    loadComponent: () =>
      import('./features/establecimientos/establecimientos.component').then(m => m.EstablecimientosComponent)
  }
];
