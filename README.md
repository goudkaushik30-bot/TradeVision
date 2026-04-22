# TradeVision — Production-Grade Stock Trading & Portfolio Management Platform

A cloud-native, microservices-based stock trading and portfolio management platform built with Java 17, Spring Boot 3, Angular 14, Apache Kafka, PostgreSQL, and Redis.

## Architecture

```
                    ┌──────────────────────────────────────────────┐
                    │             Angular 14 Frontend               │
                    │         (Bootstrap 5, TypeScript)             │
                    └─────────────────┬────────────────────────────┘
                                      │ HTTP/REST
                    ┌─────────────────▼────────────────────────────┐
                    │          NGINX / K8s Ingress                  │
                    └──┬──────┬──────┬──────┬──────┬───────────────┘
                       │      │      │      │      │
          ┌────────────▼┐ ┌───▼────┐ │  ┌──▼─────┐ │
          │ user-service│ │ stock  │ │  │ trade  │ │
          │   :8081     │ │ :8082  │ │  │ :8083  │ │
          └─────────────┘ └────────┘ │  └────────┘ │
                                     │             │
                          ┌──────────▼──┐  ┌───────▼──────┐
                          │ portfolio   │  │ alert-service │
                          │   :8084     │  │    :8085      │
                          └──────┬──────┘  └───────┬───────┘
                                 │                 │
              ┌──────────────────▼─────────────────▼───────────────┐
              │              Apache Kafka                           │
              │   Topics: trade-events, stock-price-events          │
              └──────────────────────────────────────────────────────┘
              ┌─────────────────────┐      ┌────────────────────────┐
              │   PostgreSQL 15     │      │      Redis 7            │
              │  (5 databases)      │      │  (caching layer)        │
              └─────────────────────┘      └────────────────────────┘
```

## Tech Stack

| Layer         | Technology                                           |
|---------------|------------------------------------------------------|
| Frontend      | Angular 14, TypeScript, Bootstrap 5                 |
| Backend       | Java 17, Spring Boot 3.2, Spring Security            |
| Auth          | JWT (jjwt 0.11.5), BCrypt                           |
| Messaging     | Apache Kafka 7.5 (Confluent)                        |
| Database      | PostgreSQL 15, Spring Data JPA / Hibernate           |
| Caching       | Redis 7, Spring Cache                               |
| DevOps        | Docker, Kubernetes, Jenkins CI/CD                   |
| Build         | Maven 3.9 (multi-module)                            |

## Microservices

| Service             | Port | Responsibility                                    |
|---------------------|------|---------------------------------------------------|
| `user-service`      | 8081 | Authentication (JWT), user management             |
| `stock-service`     | 8082 | Stock data, watchlists, Kafka price events        |
| `trade-service`     | 8083 | Trade execution, history, Kafka trade events      |
| `portfolio-service` | 8084 | Portfolio positions, P&L calculation (Kafka consumer) |
| `alert-service`     | 8085 | Price alerts, triggered by Kafka stock events     |

## API Endpoints

### Authentication (`user-service`)
```
POST /api/auth/register    Register new user
POST /api/auth/login       Login, returns JWT
GET  /api/users/me         Current user profile
GET  /api/users/{id}       Get user by ID (ADMIN)
PUT  /api/users/{id}       Update user
```

### Stocks (`stock-service`)
```
GET    /api/stocks                  All stocks
GET    /api/stocks/{symbol}         Stock by symbol
GET    /api/stocks/search?q=        Search stocks
GET    /api/stocks/watchlist        User watchlist
POST   /api/stocks/watchlist        Add to watchlist
DELETE /api/stocks/watchlist/{sym}  Remove from watchlist
```

### Trades (`trade-service`)
```
POST   /api/trades          Execute trade (BUY/SELL)
GET    /api/trades          Trade history
GET    /api/trades/{id}     Trade by ID
DELETE /api/trades/{id}     Cancel trade (PENDING only)
```

### Portfolio (`portfolio-service`)
```
GET /api/portfolio             All positions
GET /api/portfolio/summary     Portfolio summary with P&L
GET /api/portfolio/{symbol}    Position by symbol
```

### Alerts (`alert-service`)
```
POST   /api/alerts          Create price alert
GET    /api/alerts          List alerts (?activeOnly=true)
DELETE /api/alerts/{id}     Delete alert
```

## Running with Docker Compose

### Prerequisites
- Docker 24+ and Docker Compose v2
- 8GB RAM recommended

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/your-org/TradeVision.git
cd TradeVision

# 2. Build all Maven modules
mvn clean package -DskipTests

# 3. Set JWT secret (optional — defaults provided for dev)
export JWT_SECRET=your-production-secret-256-bits-min

# 4. Start all services
docker compose up -d

# 5. Check service health
docker compose ps
```

Services available:
- Frontend:          http://localhost:4200
- User Service:      http://localhost:8081/actuator/health
- Stock Service:     http://localhost:8082/actuator/health
- Trade Service:     http://localhost:8083/actuator/health
- Portfolio Service: http://localhost:8084/actuator/health
- Alert Service:     http://localhost:8085/actuator/health

## Running Locally (without Docker)

### Prerequisites
- Java 17+, Maven 3.9+
- PostgreSQL 15 running locally
- Redis 7 running locally
- Kafka + Zookeeper running locally (or use Docker for infra only)

```bash
# Start only infrastructure
docker compose up -d postgres redis zookeeper kafka

# Build
mvn clean package -DskipTests

# Run each service (in separate terminals)
java -jar user-service/target/user-service-1.0.0.jar
java -jar stock-service/target/stock-service-1.0.0.jar
java -jar trade-service/target/trade-service-1.0.0.jar
java -jar portfolio-service/target/portfolio-service-1.0.0.jar
java -jar alert-service/target/alert-service-1.0.0.jar

# Run frontend
cd frontend && npm install && npm start
```

## Kubernetes Deployment

```bash
# Create namespace and config
kubectl apply -f k8s/namespace.yml
kubectl apply -f k8s/configmap.yml

# Deploy infrastructure
kubectl apply -f k8s/postgres-deployment.yml
kubectl apply -f k8s/redis-deployment.yml
kubectl apply -f k8s/kafka-deployment.yml

# Deploy microservices
kubectl apply -f k8s/user-service-deployment.yml
kubectl apply -f k8s/stock-service-deployment.yml
kubectl apply -f k8s/trade-service-deployment.yml
kubectl apply -f k8s/portfolio-service-deployment.yml
kubectl apply -f k8s/alert-service-deployment.yml
kubectl apply -f k8s/frontend-deployment.yml

# Set up ingress
kubectl apply -f k8s/ingress.yml

# Or apply everything at once
kubectl apply -f k8s/
```

## Environment Variables

| Variable            | Default                              | Description                |
|---------------------|--------------------------------------|----------------------------|
| `DB_HOST`           | `localhost`                          | PostgreSQL hostname         |
| `DB_PORT`           | `5432`                               | PostgreSQL port             |
| `DB_NAME`           | varies per service                   | Database name               |
| `DB_USERNAME`       | `postgres`                           | Database user               |
| `DB_PASSWORD`       | `postgres`                           | Database password           |
| `REDIS_HOST`        | `localhost`                          | Redis hostname              |
| `REDIS_PORT`        | `6379`                               | Redis port                  |
| `KAFKA_SERVERS`     | `localhost:9092`                     | Kafka bootstrap servers     |
| `JWT_SECRET`        | *(dev default, 256-bit)*             | JWT signing secret          |
| `JWT_EXPIRATION`    | `86400000` (24h)                     | Token expiry in ms          |

## CI/CD (Jenkins)

The `Jenkinsfile` defines a pipeline with stages:
1. **Checkout** — pulls latest code
2. **Build** — `mvn clean package -DskipTests`
3. **Test** — `mvn test`
4. **Code Quality** — SonarQube analysis (main branch)
5. **Docker Build & Push** — builds and pushes all service images
6. **Deploy to Staging** — auto-deploy on `develop` branch
7. **Deploy to Production** — manual approval gate on `main` branch

Configure Jenkins credentials:
- `kubeconfig` — Kubernetes config file
- `docker-registry-credentials` — Docker registry username/password

## Kafka Topics

| Topic                 | Producer       | Consumer(s)                        |
|-----------------------|----------------|------------------------------------|
| `trade-events`        | trade-service  | portfolio-service                  |
| `stock-price-events`  | stock-service  | alert-service                      |

## Project Structure

```
TradeVision/
├── pom.xml                    # Parent POM (Spring Boot 3.2, Java 17)
├── common/                    # Shared: DTOs, exceptions, JWT, Kafka events
├── user-service/              # Auth + user management (port 8081)
├── stock-service/             # Stock data + watchlists (port 8082)
├── trade-service/             # Trade execution (port 8083)
├── portfolio-service/         # Portfolio + P&L (port 8084)
├── alert-service/             # Price alerts (port 8085)
├── frontend/                  # Angular 14 SPA
├── k8s/                       # Kubernetes manifests
├── scripts/                   # Utility scripts
├── docker-compose.yml
└── Jenkinsfile
```

## License

MIT License — see [LICENSE](LICENSE).

Full-stack stock trading &amp; portfolio tracker | Java 17 · Spring Boot 3 · Angular 14 · Kafka · PostgreSQL · Docker · Kubernetes · AWS
