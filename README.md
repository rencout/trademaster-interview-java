# Inventory Management Service

A clean, minimal backend service built with Java 17, Spring Boot 3, RabbitMQ, and PostgreSQL for managing inventory through event-driven architecture.

## Architecture Overview

The service implements an event-driven inventory management system with the following key components:

- **Domain Layer**: Core entities (InventoryItem, Event, BatchJob) with JPA annotations
- **Repository Layer**: Spring Data JPA repositories with custom query methods
- **Service Layer**: Business logic with strategy pattern for event processing
- **Controller Layer**: REST endpoints for event publishing and metrics
- **Message Layer**: RabbitMQ consumer with idempotency and retry logic
- **Batch Layer**: Scheduled processing of pending events in configurable chunks

### Design Patterns

The service implements two key design patterns for clean, maintainable code:

1. **Strategy Pattern**: Each event type (ORDER_PLACED, ORDER_CANCELLED, INVENTORY_ADJUSTED) has its own strategy implementation that encapsulates the specific business logic for that event type. This provides clean separation of concerns and makes it easy to add new event types.

2. **Factory Pattern**: The `EventStrategyFactory` automatically discovers and registers all available strategies, then provides a clean interface to retrieve the appropriate strategy for any given event type. This eliminates conditional logic and switch statements throughout the codebase.

**Trade-offs**: The Factory pattern adds a layer of indirection but provides better encapsulation and makes the system more maintainable. The Strategy pattern increases the number of classes but makes each one focused and testable.

## Tech Stack

- **Java 17** with Spring Boot 3.2.0
- **Spring Boot Starters**: Web, AMQP, Data JPA, Validation, Actuator
- **Database**: PostgreSQL 16 with Hibernate
- **Message Queue**: RabbitMQ 3 with management interface
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito
- **Utilities**: Lombok for clean code

## Key Features

### Event Processing
- **Strategy Pattern**: Different handlers for ORDER_PLACED, ORDER_CANCELLED, INVENTORY_ADJUSTED
- **Idempotency**: SHA-256 hash-based duplicate detection
- **Retry Logic**: Configurable retry attempts with exponential backoff
- **Dead Letter Queue**: Failed events after max retries

### Batch Processing
- **Scheduled Jobs**: Automatic processing of pending events
- **Chunk Processing**: Configurable batch sizes for performance
- **Statistics Tracking**: Detailed metrics for each batch run

### Health & Monitoring
- **Actuator Endpoints**: Built-in Spring Boot monitoring
- **Custom Health**: Simple `/health` endpoint
- **Metrics**: Aggregated database totals via `/events/metrics`

## Project Structure

```
src/main/java/com/trademaster/inventory/
├── domain/                 # JPA entities
├── repository/            # Spring Data repositories
├── service/               # Business logic services
├── controller/            # REST controllers
├── consumer/              # RabbitMQ consumer
└── config/                # Configuration classes
```

## Environment Variables

Copy `env.example` to `.env` and configure:

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | PostgreSQL host | `localhost` |
| `DB_PORT` | PostgreSQL port | `5432` |
| `DB_NAME` | Database name | `inventory` |
| `DB_USERNAME` | Database user | `postgres` |
| `DB_PASSWORD` | Database password | `postgres` |
| `RABBITMQ_HOST` | RabbitMQ host | `localhost` |
| `RABBITMQ_PORT` | RabbitMQ port | `5672` |
| `RABBITMQ_USERNAME` | RabbitMQ user | `guest` |
| `RABBITMQ_PASSWORD` | RabbitMQ password | `guest` |
| `CHUNK_SIZE` | Batch processing chunk size | `100` |
| `MAX_RETRIES` | Maximum retry attempts | `3` |
| `RETRY_DELAY_MS` | Retry delay in milliseconds | `5000` |
| `CONCURRENCY` | Processing concurrency | `1` |

## Running Locally

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker & Docker Compose

### Quick Start with Docker Compose

1. **Clone and navigate to project**
   ```bash
   cd trademaster-interview-java
   ```

2. **Start infrastructure services**
   ```bash
   docker-compose up -d postgres rabbitmq
   ```

3. **Wait for services to be healthy**
   ```bash
   docker-compose ps
   ```

4. **Run with Maven**
   ```bash
   mvn spring-boot:run
   ```

5. **Or build and run with Docker**
   ```bash
   docker-compose up --build
   ```

### Manual Setup

1. **Start PostgreSQL and RabbitMQ manually**
2. **Set environment variables** (see env.example)
3. **Run with Maven**
   ```bash
   mvn spring-boot:run
   ```

## API Endpoints

### Events
- `POST /events` - Publish inventory events
- `GET /events/metrics` - Get aggregated metrics

### Batch Jobs
- `GET /batches` - List all batch jobs
- `POST /batches/trigger` - Manually trigger batch processing

### Health
- `GET /health` - Service health status
- `GET /actuator/health` - Detailed health information

## Event Types

### ORDER_PLACED
Decrements inventory quantity by specified amount (defaults to 1)
```json
{
  "type": "ORDER_PLACED",
  "sku": "PRODUCT-123",
  "quantity": 2
}
```

### ORDER_CANCELLED
Increments inventory quantity by specified amount (defaults to 1)
```json
{
  "type": "ORDER_CANCELLED",
  "sku": "PRODUCT-123",
  "quantity": 1
}
```

### INVENTORY_ADJUSTED
Adjusts inventory by specified delta value
```json
{
  "type": "INVENTORY_ADJUSTED",
  "sku": "PRODUCT-123",
  "delta": 5
}
```

## Testing

Run unit tests:
```bash
mvn test
```

Tests cover main happy paths for:
- Event processing service
- REST controllers
- Strategy implementations

## Trade-offs & Design Decisions

### Simplicity Over Complexity
- **Single Responsibility**: Each class has one clear purpose
- **Strategy Pattern**: Clean separation of event type handling
- **Minimal Dependencies**: Only essential Spring Boot starters

### Performance Considerations
- **Chunk Processing**: Configurable batch sizes for memory efficiency
- **Database Queries**: Optimized with custom repository methods
- **Connection Pooling**: Default HikariCP configuration

### Reliability Features
- **Idempotency**: Prevents duplicate processing
- **Retry Logic**: Handles transient failures gracefully
- **Dead Letter Queue**: Captures permanently failed events

### Monitoring & Observability
- **Actuator**: Built-in Spring Boot monitoring
- **Custom Metrics**: Business-specific aggregations
- **Structured Logging**: Consistent log format with SLF4J

## Development

### Adding New Event Types
1. Add enum value to `EventType`
2. Implement new strategy class implementing `EventProcessingStrategy`
3. The `EventStrategyFactory` will automatically discover and register the new strategy

### Database Schema Changes
- JPA entities use `ddl-auto: update`
- Manual migrations for production deployments

### Configuration Changes
- All configurable values use environment variables
- Sensible defaults in `application.yml`
- Override via `.env` file or system properties

## Production Considerations

- **Database**: Use connection pooling and read replicas
- **RabbitMQ**: Configure clustering and persistence
- **Monitoring**: Add application metrics and alerting
- **Security**: Implement authentication and authorization
- **Scaling**: Consider horizontal scaling with load balancers
