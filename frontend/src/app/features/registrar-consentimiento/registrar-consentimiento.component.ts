import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { SelectButtonModule } from 'primeng/selectbutton';
import { TextareaModule } from 'primeng/textarea';
import { MessageModule } from 'primeng/message';
import { CardModule } from 'primeng/card';
import { TagModule } from 'primeng/tag';
import { ConsentService } from '../../core/services/consent.service';

type Estado = 'idle' | 'loading' | 'ok' | 'error';

@Component({
  selector: 'app-registrar-consentimiento',
  imports: [FormsModule, InputTextModule, ButtonModule, SelectButtonModule, TextareaModule, MessageModule, CardModule, TagModule],
  templateUrl: './registrar-consentimiento.component.html',
  styleUrl: './registrar-consentimiento.component.scss'
})
export class RegistrarConsentimientoComponent {
  private consent = inject(ConsentService);

  dni = signal('');
  estadoSeleccionado = signal<'GRANTED' | 'REVOKED'>('GRANTED');
  establecimientoOrigen = signal('');
  observacion = signal('');

  estado = signal<Estado>('idle');
  mensajeOk = signal('');
  errorMsg = signal('');

  estadoOpciones = [
    { label: 'Otorgar consentimiento', value: 'GRANTED' },
    { label: 'Revocar consentimiento', value: 'REVOKED' }
  ];

  registrar(): void {
    const d = this.dni().trim();
    if (!/^\d{8}$/.test(d)) {
      this.errorMsg.set('El DNI debe tener exactamente 8 dígitos.');
      this.estado.set('error');
      return;
    }

    this.estado.set('loading');

    this.consent.registrar(d, {
      estado: this.estadoSeleccionado(),
      establecimientoOrigen: this.establecimientoOrigen().trim() || undefined,
      observacion: this.observacion().trim() || undefined
    }).subscribe({
      next: res => {
        const accion = res.estado === 'GRANTED' ? 'otorgado' : 'revocado';
        this.mensajeOk.set(`Consentimiento ${accion} correctamente para DNI ${d}.`);
        this.estado.set('ok');
      },
      error: (err: HttpErrorResponse) => {
        this.errorMsg.set(err.error?.detail ?? 'Error al registrar el consentimiento.');
        this.estado.set('error');
      }
    });
  }

  limpiar(): void {
    this.dni.set('');
    this.estadoSeleccionado.set('GRANTED');
    this.establecimientoOrigen.set('');
    this.observacion.set('');
    this.estado.set('idle');
  }
}
