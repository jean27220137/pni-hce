/**
 * Test de carga Consent — consulta y registro de consentimiento
 * Uso: k6 run tests/load/consent-load-test.js
 * Requisitos: CONS-RNF-01 — p95 < 100ms
 */
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8083';

const errorRate       = new Rate('error_rate');
const latencyConsulta = new Trend('latency_consulta_ms', true);
const latencyRegistro = new Trend('latency_registro_ms', true);

// DNIs de prueba con consentimientos pre-cargados
const DNIS = ['71234567', '45678901', '48900123'];

export const options = {
  scenarios: {
    consultas: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '20s', target: 50  },
        { duration: '40s', target: 200 },
        { duration: '20s', target: 0   },
      ],
    },
  },
  thresholds: {
    'latency_consulta_ms': ['p(95)<100'],
    'latency_registro_ms': ['p(95)<150'],
    'error_rate':          ['rate<0.01'],
  },
};

export default function () {
  const dni = DNIS[Math.floor(Math.random() * DNIS.length)];
  const headers = { 'Content-Type': 'application/json' };

  // GET — consulta consentimiento
  const getRes = http.get(`${BASE_URL}/api/v1/consent/pacientes/${dni}`);
  const getOk = check(getRes, {
    'GET status 200 o 404': r => r.status === 200 || r.status === 404,
  });
  errorRate.add(!getOk);
  latencyConsulta.add(getRes.timings.duration);

  // PUT — registrar/actualizar consentimiento (1 de cada 5 iteraciones)
  if (__ITER % 5 === 0) {
    const estado = Math.random() > 0.2 ? 'GRANTED' : 'REVOKED';
    const putRes = http.put(
      `${BASE_URL}/api/v1/consent/pacientes/${dni}`,
      JSON.stringify({ estado, establecimientoOrigen: 'POSTA0001' }),
      { headers }
    );
    const putOk = check(putRes, {
      'PUT status 200 o 201': r => r.status === 200 || r.status === 201,
    });
    errorRate.add(!putOk);
    latencyRegistro.add(putRes.timings.duration);
  }

  sleep(0.1);
}

export function handleSummary(data) {
  return {
    'tests/load/results/consent-load-test-summary.json': JSON.stringify(data, null, 2),
  };
}
