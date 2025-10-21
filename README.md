# demo

This project is a small Spring Boot application that exposes APIs to load and query restaurant deals. It demonstrates parsing external JSON, normalising deal times against restaurant opening hours, filtering active deals by time, and computing the peak window by the maximum number of deals overlap.

## Environment

- Java: 21
- Spring Boot: 3.5.6
- springdoc OpenAPI (Swagger UI): 2.8.0

## Quick start — run locally

1. Run the main method inside IDE like (IntelliJ, VScode)

2. Build the project with Maven:

```bash
mvn -U clean package
```

3. Run the application (from project root):

```bash
mvn spring-boot:run
```

Or run the generated jar:

```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

By default the application will start on port 8080.

## Configuration

The external data source URL is configured via the `data.url` property in `src/main/resources/application.yml`. The application uses a simple `RestClient` bean configured in `RestClientConfig` to fetch the JSON feed.

## API (Swagger / OpenAPI)

This project includes springdoc OpenAPI and exposes a Swagger UI to explore the APIs.

- Swagger UI: http://localhost:8080/swagger-ui.html

Endpoints (under `/api/deals`):

- GET `/api/deals?timeOfDay=02:00PM` — Returns a list of deals active at the provided time. The `timeOfDay` parameter accepts formats like `2PM`, `02:00PM`, `14:00`, etc.
- GET `/api/deals/peak-window` — Returns the peak deal window (start and end) where the maximum number of deals overlap.

Response DTOs are defined in `src/main/java/com/demo/demo/DTO`.

## Design and implementation notes

High-level components:

- Controller: `DealController` — exposes the REST endpoints.
- Service: `DealServiceImpl` — where the business logic handles repository queries and implements the peak-window algorithm.
- Repository: `CachedHttpDealsRepository` — acts as a repository abstraction that loads deal data via DealLoader, normalizes it, 
and caches it in memory. This simulates a typical database-backed repository since the challenge data source is a static JSON file rather than a live database..
- HTTP loader: `DealLoader` — fetches the JSON feed from the configured URL, parses restaurants and deals, and converts them into `NormalisedDeal` entities.
- Utilities: `TimeUtils` — parses multiple time formats into `LocalTime` and formats times for responses.

Key implementation details:

- Normalisation: `DealLoader` reads restaurant-level `open`/`close` and deal-level `start`/`end` (some are `open`/`close`) fields and creates `NormalisedDeal` objects.
- Safety checks: When parsing each deal, the loader ensures the deal start is not before the restaurant open time, and the deal end is not after the restaurant close time. This prevents deals from being considered active outside restaurant hours.
- Filtering active deals: `CachedHttpDealsRepository.findAllActiveDeals(timeOfDay)` parses the `timeOfDay` query into a `LocalTime` and filters loaded `NormalisedDeal` objects by checking if the query time is within the deal's [start, end] inclusive range.
- Peak-window algorithm: `DealServiceImpl.getPeakDealWindow()` uses a sweep-line approach: it creates a sorted map of events (start +1, end -1), iterates the timeline, maintains the current overlap count, and records the time interval when overlap is maximal.

## Problem statement (in the dataset)

The challenge focuses on handling deals and restaurant opening hours where:

- Deals may have their own `start` and `end` times which may not always align with the parent restaurant's operating times.
- A deal without explicit `start`/`end` times may specify `open`/`close` at the deal level, or may omit deal-level times entirely.

This project addresses these issues by normalising every deal against its restaurant's opening hours so that:

- A deal start is adjusted to be no earlier than the restaurant open time.
- A deal end is adjusted to be no later than the restaurant close time.

This ensures we never consider a deal active while the restaurant is closed.

## Assumptions

The code follows these explicit assumptions:

1. Deal may have `start`/`end` or `open`/`close`, so the loader will attempt to try both fields. If those are also missing, the restaurant `open`/`close` times will be used instead.
2. If a parsed deal `start` is before the restaurant `open`, the effective start time is set to the restaurant `open` time.
3. If a parsed deal `end` is after the restaurant `close`, the effective end time is set to the restaurant `close` time.
4. If either the effective start or end time is null or not parsable, the deal is ignored when filtering by a specific query time (the restaurant may be closed permanently / old data).
5. Time parsing accepts multiple formats (e.g., `2PM`, `02:00PM`, `14:00`) via `TimeUtils.parseTime`. Unrecognised formats (e.g. `2`, `2.5`) will throw an exception during parsing.

These choices were made to keep deal-query semantics conservative and to avoid exposing deals during restaurant closed hours.

## Error handling & edge cases

- If the remote JSON fetch fails, `CachedHttpDealsRepository` will throw a runtime exception wrapping the root cause.
- If API `/api/deals` is called without `timeOfDay`, the repository returns all normalised deals.
- If there are no deals, or the computed peak window is degenerate, the `peak-window` endpoint returns null start/end values.

## Tests

This project contains unit tests that exercise parsing, normalisation, filtering and the peak-window algorithm. The main test classes are located under `src/test/java/com/demo/demo/`:

- `DealLoaderTest` — integration-style unit test that uses `okhttp3.mockwebserver.MockWebServer` to simulate the remote JSON feed. It verifies:
	- JSON parsing and flattening of restaurants → deals
	- Normalisation/clamping of deal start/end times against restaurant open/close

- `CachedHttpDealsRepositoryTest` — tests the repository behaviour by mocking `DealLoader`. It verifies:
	- Filtering by a query time (inclusive range checks)
	- Fallback behaviour when deal start/end are missing (uses restaurant open/close)
	- Invalid/unsupported time format handling (the repository wraps parse problems in a RuntimeException)

- `DealServiceTest` — tests the service layer by mocking `DealsRepository`. It verifies:
	- Mapping from `NormalisedDeal` to `DealResponse` (formatting times, fields)
	- `getPeakDealWindow()` behaviour for no deals and overlapping deals (expected peak window calculation)

Testing frameworks and helpers used:

- JUnit 5 (JUnit Jupiter)
- Mockito for mocking
- okhttp3 MockWebServer for HTTP feed simulation in `DealLoaderTest`
- Jackson `ObjectMapper` for JSON parsing in tests

Run all tests:

```bash
mvn test
```

Run a single test class (example):

```bash
mvn -Dtest=DealLoaderTest test
```

Run a single test method (example):

```bash
mvn -Dtest=DealLoaderTest#loadAll_NormalizesDealTimes_AndParsesFields test
```

Run tests from your IDE:

- Import the project into IntelliJ IDEA or VS Code and run the test classes directly from the editor.

## Task3 (Bonus)

PostgreSQL can be used for this challenge.

Why PostgreSQL gits this challenge?

1. For this challenge, time window filtering and peak time computation are first class, in this case, postgresql is the most natural fit because it supports concise queries.
2. I didn’t choose NoSQL because the structure of restaurant, deal, and cuisine data is not suited to rapid JSON ingestion or document-level reads.
3. PostgreSQL provides strong data integrity and analytical power. Its relational model and advanced query features make it ideal for ensuring consistent restaurant–deal relationships and performing time-window analytics with ease.

However, it has cons:
1. It's difficult to scale horizontally. PostgreSQL handles vertical scaling and read replicas easily, but scaling write operations across many servers is more complex than in NoSQL databases.
2. When the data structure changes often, you need to update the database schema (DDL). However, using the jsonb type can make schema changes more flexible, since you can store evolving fields without altering the table each time.
