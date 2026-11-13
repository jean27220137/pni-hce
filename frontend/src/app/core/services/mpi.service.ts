import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface EstablecimientoVinculo {
  codigoRenipress: string;
  nombre: string;
  tipo: string;
  sector: string;
  fhirBaseUrl: string;
  idLocal: string;
  ultimaAtencion: string;
}

export interface VinculoPacienteResponse {
  dni: string;
  establecimientos: EstablecimientoVinculo[];
  fuente: 'cache' | 'db';
}

export interface EstablecimientoDto {
  id: string;
  codigoRenipress: string;
  nombre: string;
  tipo: string;
  sector: string;
  fhirBaseUrl: string;
}

const API = '/api/v1/mpi';

@Injectable({ providedIn: 'root' })
export class MpiService {
  private http = inject(HttpClient);

  buscarPaciente(dni: string): Observable<VinculoPacienteResponse> {
    return this.http.get<VinculoPacienteResponse>(`${API}/pacientes/${dni}/establecimientos`);
  }

  listarEstablecimientos(): Observable<EstablecimientoDto[]> {
    return this.http.get<EstablecimientoDto[]>(`${API}/establecimientos`);
  }
}
