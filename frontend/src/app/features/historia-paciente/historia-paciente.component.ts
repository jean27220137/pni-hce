import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { CardModule } from 'primeng/card';
import { TagModule } from 'primeng/tag';
import { PanelModule } from 'primeng/panel';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { MessageModule } from 'primeng/message';
import { HistoriaService, HistoriaResponse, EstablecimientoResumen } from '../../core/services/historia.service';

type Estado = 'loading' | 'ok' | 'error' | 'forbidden';

const TIPO_SEVERITY: Record<string, 'success' | 'info' | 'warn' | 'danger'> = {
  HOSPITAL:     'danger',
  CENTRO_SALUD: 'warn',
  POSTA:        'info',
  CLINICA:      'success'
};

@Component({
  selector: 'app-historia-paciente',
  imports: [CardModule, TagModule, PanelModule, ProgressSpinnerModule, MessageModule],
  templateUrl: './historia-paciente.component.html',
  styleUrl: './historia-paciente.component.scss'
})
export class HistoriaPacienteComponent {
  private historia = inject(HistoriaService);
  private route = inject(ActivatedRoute);

  estado = signal<Estado>('loading');
  resultado = signal<HistoriaResponse | null>(null);
  errorMsg = signal('');
  tipoSeverity = TIPO_SEVERITY;

  constructor() {
    const dni = this.route.snapshot.paramMap.get('dni') ?? '';
    this.historia.obtenerHistoria(dni).subscribe({
      next: res => {
        this.resultado.set(res);
        this.estado.set('ok');
      },
      error: (err: HttpErrorResponse) => {
        if (err.status === 403) {
          this.estado.set('forbidden');
        } else {
          this.errorMsg.set(err.error?.detail ?? 'Error al obtener la historia clínica.');
          this.estado.set('error');
        }
      }
    });
  }

  condicionesDeBundle(resumen: EstablecimientoResumen): string[] {
    const bundle = resumen.resumenFhir as Record<string, unknown> | null;
    if (!bundle || !Array.isArray(bundle['entry'])) return [];
    return (bundle['entry'] as Record<string, unknown>[])
      .map(e => {
        const resource = e['resource'] as Record<string, unknown> | undefined;
        if (!resource || resource['resourceType'] !== 'Condition') return null;
        const coding = (resource['code'] as Record<string, unknown[]> | undefined)?.['coding'];
        if (!Array.isArray(coding) || coding.length === 0) return null;
        const c = coding[0] as Record<string, string>;
        return `${c['code']} – ${c['display']}`;
      })
      .filter((x): x is string => x !== null);
  }
}
