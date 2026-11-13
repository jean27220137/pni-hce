/**
 * Test de carga MPI — MPI-RNF-01
 * Objetivo: p95 < 200 ms (con caché), p95 < 500 ms (sin caché)
 * Uso: k6 run tests/load/mpi-load-test.js
 *      k6 run --env BASE_URL=http://host:8081 tests/load/mpi-load-test.js
 */
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8081';

// Métricas personalizadas
const errorRate   = new Rate('error_rate');
const cacheHits   = new Rate('cache_hit_rate');
const latencyDb   = new Trend('latency_db_ms',    true);
const latencyCache = new Trend('latency_cache_ms', true);

// DNIs de prueba distribuidos en las tres particiones
const DNIS = [
  '12345678', '23456789', '34567890',   // partición 0 (00-39)
  '45678901', '56789012', '67890123',   // partición 1 (40-69)
  '71234567', '82345678', '91234567',   // partición 2 (70-99)
];

export const options = {
  scenarios: {
    ramp_up: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 100  },
        { duration: '60s', target: 500  },   // pico: 500 VUs concurrentes
        { duration: '30s', target: 500  },
        { duration: '30s', target: 0    },
      ],
    },
  },
  thresholds: {
    // MPI-RNF-01
    'latency_cache_ms': ['p(95)<200'],
    'latency_db_ms':    ['p(95)<500'],
    'error_rate':       ['rate<0.01'],    // < 1 % errores
    'http_req_duration':['p(95)<500'],
  },
};

export default function () {
  const dni = DNIS[Math.floor(Math.random() * DNIS.length)];
  const url = `${BASE_URL}/api/v1/mpi/pacientes/${dni}/establecimientos`;

  const res = http.get(url, { tags: { endpoint: 'buscarPaciente' } });

  const ok = check(res, {
    'status 200': r => r.status === 200,
    'tiene establecimientos': r => {
      try { return Array.isArray(JSON.parse(r.body).establecimientos); }
      catch { return false; }
    },
  });

  errorRate.add(!ok);

  if (res.status === 200) {
    try {
      const body = JSON.parse(res.body);
      if (body.fuente === 'cache') {
        cacheHits.add(1);
        latencyCache.add(res.timings.duration);
      } else {
        cacheHits.add(0);
        latencyDb.add(res.timings.duration);
      }
    } catch (_) { /* ignore parse error */ }
  }

  sleep(0.1);
}

export function handleSummary(data) {
  return {
    'tests/load/results/mpi-load-test-summary.json': JSON.stringify(data, null, 2),
  };
}
