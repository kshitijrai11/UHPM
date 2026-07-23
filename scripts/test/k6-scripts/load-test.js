import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 50 },  // Ramp up to 50 virtual users
    { duration: '1m', target: 50 },   // Stay at 50 users
    { duration: '30s', target: 200 }, // Spike to 200 virtual users (Trigger Layer 1 & 2 Backpressure)
    { duration: '1m', target: 200 },  // Maintain spike
    { duration: '30s', target: 0 },   // Ramp down
  ],
  thresholds: {
    // 95% of requests must complete under 200ms
    http_req_duration: ['p(95)<200'],
    // Less than 1% of requests can fail
    http_req_failed: ['rate<0.01'],
  },
};

export default function () {
  const url = 'http://localhost:8081/api/v1/orders';
  
  const payload = JSON.stringify({
    userId: "user-123",
    items: [
      { productId: "prod-456", quantity: 2 },
      { productId: "prod-789", quantity: 1 }
    ],
    totalAmount: 150.00
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      // In a real environment, we'd pass a JWT token here
      // 'Authorization': 'Bearer <token>'
    },
  };

  const res = http.post(url, payload, params);

  check(res, {
    'status is 200 or 202': (r) => r.status === 200 || r.status === 202,
    'transaction time OK': (r) => r.timings.duration < 200,
  });

  sleep(1);
}
