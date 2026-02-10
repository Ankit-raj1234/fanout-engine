# High-Throughput Fan-Out Engine (Java)

## Overview
This project implements a streaming fan-out engine that ingests large files and distributes records to multiple sink types (REST, gRPC, MQ, Wide-column DB) with transformation, throttling, backpressure, retries, and observability.

## Setup
Requirements:
- Java 21+
- Maven 3.9+

Build:
```bash
mvn -q -DskipTests package
```

Run:
```bash
java -jar target/fanout-engine-1.0.0.jar application.yaml
```

Run tests:
```bash
mvn -q test
```

## Architecture
Data Flow:
1. `InputReader` streams from file (CSV/JSONL/FIXED) into a bounded source queue.
2. `Dispatcher` fans out each record into bounded per-sink queues.
3. Each sink has worker threads (virtual threads) that transform + send with rate limiting and retries.
4. Metrics are printed every 5 seconds.
See `docs/architecture.txt` for a compact diagram.

## Backpressure
- Bounded queues are used for the source and each sink.
- If any sink is slow, its queue fills and the dispatcher blocks, which also slows ingestion.

## Concurrency Model
- Java 21 virtual threads via `Executors.newVirtualThreadPerTaskExecutor()`.
- Each sink runs `workers` tasks for parallelism.

## Design Patterns
- Strategy: `Transformer` per sink type.
- Factory: `SinkFactory` / `TransformerFactory` for decoupled creation.
- Observer: `Metrics` scheduled logging.
Protobuf transformation uses `CodedOutputStream` wire encoding (no `protoc` required).

## Error Handling and DLQ
- Each record is retried up to `maxRetries` per sink.
- After final failure, the record is appended to `dlq/<sink>.jsonl`.

## Configuration
`application.yaml` (or `.json`) controls:
- input path and format
- queue sizes
- per-sink concurrency, rate limit, latency simulation, error rate

Example Fixed-width config:
```yaml
input:
  path: sample/input.fixed
  format: FIXED
  fixedWidthColumns:
    - name: id
      start: 0
      end: 4
    - name: name
      start: 4
      end: 20
```

## Assumptions
- Input records are small JSON objects or CSV rows.
- Timestamp is numeric if provided.
- For CSV, extra columns are preserved in the map.

## Docs
- Architecture diagram: `docs/architecture.txt`
- Submission PDF: `docs/Full Name - Software Engineer Intern Assignment.pdf`

## Prompts
- “Generate a Java 21 fan-out engine design using Strategy + Factory.”
- “Implement streaming ingestion for CSV/JSONL with backpressure and bounded queues.”
- “Provide a minimal Maven project with tests and README.”
