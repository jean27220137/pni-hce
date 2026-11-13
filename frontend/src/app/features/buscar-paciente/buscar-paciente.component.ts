import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { TagModule } from 'primeng/tag';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { MessageModule } from 'primeng/message';
import { MpiService, VinculoPacienteResponse } from '../../core/services/mpi.service';

type Estado = 'idle' | 'loading' | 'ok' | 'error';

const TIPO_SEVERITY: Record<string, 'success' | 'info' | 'warn' | 'danger'> = {
  HOSPITAL:      'danger',
  CENTRO_SALUD:  'warn',
  POSTA:         'info',
  CLINICA:       'success'
};

const SECTOR_LABEL: Record<string, string> = {
  MINSA:    'MINSA',
  ESSALUD:  'EsSalud',
  PRIVADO:  'Privado',
  FFAA_PNP: 'FF.AA./PNP'
};

@Component({
  selector: 'app-buscar-paciente',
  imports: [FormsModule, InputTextModule, ButtonModule, CardModule, TagModule, ProgressSpinnerModule, MessageModule],
  templateUrl: './buscar-paciente.component.html',
  styleUrl: './buscar-paciente.component.scss'
})
export class BuscarPacienteComponent {
  private mpi = inject(MpiService);

  dni = signal('');
  estado = signal<Estado>('idle');
  resultado = signal<VinculoPacienteResponse | null>(null);
  errorMsg = signal('');

  tipoSeverity = TIPO_SEVERITY;
  sectorLabel = SECTOR_LABEL;

  buscar(): void {
    const d = this.dni().trim();
    if (!/^\d{8}$/.test(d)) {
      this.errorMsg.set('El DNI debe tener exactamente 8 dígitos.');
      this.estado.set('error');
      return;
    }

    this.estado.set('loading');
    this.resultado.set(null);

    this.mpi.buscarPaciente(d).subscribe({
      next: res => {
        this.resultado.set(res);
        this.estado.set('ok');
      },
      error: (err: HttpErrorResponse) => {
        this.errorMsg.set(err.error?.detail ?? 'Error al consultar el MPI.');
        this.estado.set('error');
      }
    });
  }

  limpiar(): void {
    this.dni.set('');
    this.estado.set('idle');
    this.resultado.set(null);
  }
}
