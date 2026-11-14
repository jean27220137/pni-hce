import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ConsentimientoResponse {
  estado: 'GRANTED' | 'REVOKED';
  actualizadoEn: string;
}

export interface ConsentimientoRequest {
  estado: 'GRANTED' | 'REVOKED';
  establecimientoOrigen?: string;
  observacion?: string;
}

@Injectable({ providedIn: 'root' })
export class ConsentService {
  private http = inject(HttpClient);

  consultar(dni: string): Observable<ConsentimientoResponse> {
    return this.http.get<ConsentimientoResponse>(`/api/v1/consent/pacientes/${dni}`);
  }

  registrar(dni: string, req: ConsentimientoRequest): Observable<ConsentimientoResponse> {
    return this.http.put<ConsentimientoResponse>(`/api/v1/consent/pacientes/${dni}`, req);
  }
}
