/**
 * Test de carga Gateway — flujo completo MPI + FHIR
 * Uso: k6 run tests/load/historia-load-test.js
 */
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

const errorRate      = new Rate('error_rate');
const latencyHistoria = new Trend('latency_historia_ms', true);

const DNIS = ['71234567', '45678901'];

export const options = {
  scenarios: {
    ramp_up: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 50  },
        { duration: '60s', target: 200 },
        { duration: '30s', target: 0   },
      ],
    },
  },
  thresholds: {
    'latency_historia_ms': ['p(95)<2000'],
    'error_rate':          ['rate<0.02'],
  },
};

export default function () {
  const dni = DNIS[Math.floor(Math.random() * DNIS.length)];
  const res = http.get(`${BASE_URL}/api/v1/pacientes/${dni}/historia`);

  const ok = check(res, {
    'status 200': r => r.status === 200,
    'tiene dni': r => {
      try { return JSON.parse(r.body).dni === dni; }
      catch { return false; }
    },
  });

  errorRate.add(!ok);
  if (res.status === 200) latencyHistoria.add(res.timings.duration);

  sleep(0.2);
}

export function handleSummary(data) {
  return {
    'tests/load/results/historia-load-test-summary.json': JSON.stringify(data, null, 2),
  };
}
