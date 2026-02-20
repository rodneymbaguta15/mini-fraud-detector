# Mini Fraud Detector API (Spring Boot)

Mini Fraud Detector is an explainable fraud risk scoring API for card-like transaction payloads.  
It scores each request, returns a risk band, and explains *why* the score was assigned.

## Features

- Fraud risk scoring with human-readable reasons
- Request validation with centralized/global error handling
- OpenAPI/Swagger UI documentation
- Rules endpoint to inspect active scoring configuration
- Unit and controller tests for scoring, request mapping, and API behavior

## Tech Stack

- Java 21
- Spring Boot 3.2
- Spring Web + Validation
- Springdoc OpenAPI (Swagger UI)
- JUnit 5 + Spring Boot Test + MockMvc
- Maven Wrapper (`mvnw`)

## API Endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/health` | Basic health check (`{"status":"ok"}`) |
| POST | `/api/v1/fraud/check` | Evaluate a transaction and return score, risk level, reasons, and evaluation timestamp |
| GET | `/api/v1/fraud/rules` | Return active scoring thresholds, points, risk bands, high-risk countries, and night window |

## Scoring Rules

The service adds points for each matching rule, caps total score at `100`, then maps score to a risk band.

### Rule thresholds and points

- **Amount-based points**
  - `amount >= 300` → `+10` (Moderate amount)
  - `amount >= 1000` → `+25` (High amount)
  - `amount >= 2000` → `+35` (Very high amount)
- **Device trust**
  - `deviceTrusted == false` → `+20`
- **Country risk**
  - `country` in configured high-risk list (`MM`, `GH`, `KE`, `ZA`, `BR`, `CY` by default) → `+25`
- **Time window (UTC)**
  - timestamp hour in configured night window (`00:00` through `05:00` UTC by default) → `+10`

### Risk bands

- `LOW`: `0-29`
- `MEDIUM`: `30-69`
- `HIGH`: `70-100`

## Run Locally

```bash
# 1) Start the API
./mvnw spring-boot:run

# 2) (optional) Build package
./mvnw clean package
```

API runs on `http://localhost:8080` by default.

## Example Requests

### 1) Fraud check (`POST /api/v1/fraud/check`)

```bash
curl -X POST "http://localhost:8080/api/v1/fraud/check" \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "tx-123",
    "userId": "user-42",
    "amount": 1200.00,
    "currency": "USD",
    "merchantCategory": "electronics",
    "country": "MM",
    "timestamp": "2026-01-01T01:30:00Z",
    "deviceTrusted": false,
    "paymentMethod": "CARD",
    "ipAddress": "203.0.113.7"
  }'
```

Sample response:

```json
{
  "transactionId": "tx-123",
  "riskScore": 80,
  "riskLevel": "HIGH",
  "reasons": [
    "High amount (>= 1000)",
    "Untrusted device",
    "High-risk country: MM",
    "Transaction time is unusual (01:00 UTC in 00:00–05:00 UTC)"
  ],
  "evaluatedAt": "2026-01-01T12:00:00Z"
}
```

### 2) Rules (`GET /api/v1/fraud/rules`)

```bash
curl "http://localhost:8080/api/v1/fraud/rules"
```

## Swagger UI

After starting the app, open:

- `http://localhost:8080/swagger-ui/index.html`

## Testing

```bash
./mvnw clean test
```

## Notes

- This project intentionally focuses on stateless fraud scoring and explainability.
- Persistence and transaction history analysis are optional future enhancements (not implemented in the current version).
