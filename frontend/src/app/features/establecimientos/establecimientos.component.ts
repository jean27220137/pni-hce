import { Component, inject, OnInit, signal } from '@angular/core';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { MessageModule } from 'primeng/message';
import { MpiService, EstablecimientoDto } from '../../core/services/mpi.service';

const TIPO_SEVERITY: Record<string, 'success' | 'info' | 'warn' | 'danger'> = {
  HOSPITAL:     'danger',
  CENTRO_SALUD: 'warn',
  POSTA:        'info',
  CLINICA:      'success'
};

@Component({
  selector: 'app-establecimientos',
  imports: [TableModule, TagModule, ProgressSpinnerModule, MessageModule],
  templateUrl: './establecimientos.component.html',
  styleUrl: './establecimientos.component.scss'
})
export class EstablecimientosComponent implements OnInit {
  private mpi = inject(MpiService);

  establecimientos = signal<EstablecimientoDto[]>([]);
  loading = signal(true);
  errorMsg = signal('');

  tipoSeverity = TIPO_SEVERITY;

  ngOnInit(): void {
    this.mpi.listarEstablecimientos().subscribe({
      next: data => {
        this.establecimientos.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.errorMsg.set('No se pudo cargar la lista de establecimientos.');
        this.loading.set(false);
      }
    });
  }
}
