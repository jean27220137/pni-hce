import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface EstablecimientoResumen {
  codigoRenipress: string;
  nombre: string;
  tipo: string;
  sector: string;
  ultimaAtencion: string;
  disponible: boolean;
  resumenFhir: Record<string, unknown> | null;
}

export interface HistoriaResponse {
  dni: string;
  establecimientos: EstablecimientoResumen[];
}

@Injectable({ providedIn: 'root' })
export class HistoriaService {
  private http = inject(HttpClient);

  obtenerHistoria(dni: string): Observable<HistoriaResponse> {
    return this.http.get<HistoriaResponse>(`/api/v1/pacientes/${dni}/historia`);
  }
}
