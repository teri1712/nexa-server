# Nexa - AI-Powered Document Management & RAG System

Finding critical information within a company shouldn't be a manual ordeal. Too often, employees find themselves hunting
through mountains of documents, manually scanning each section just to find the one piece of information that matches
the specific problem they face. It's a process that is repetitive, time-consuming, and drains productivity.

**Nexa** is a document management system built to eliminate that friction. Backed by the **Microsoft GraphRAG search
engine**, Nexa allows you to seamlessly ingest, index, and intelligently query your organization's unstructured data.
Instead of manual searching, Nexa uses advanced knowledge graphs and Retrieval-Augmented Generation (RAG) to transform
your static documents into a dynamic, instantly searchable knowledge base.

## 🚀 Tech Stack

### Backend

- **Java 21**: Leveraging modern features like **Virtual Threads** for high-concurrency I/O.
- **Spring Boot 3.5**: The core framework for building robust, production-ready services.
- **Spring AI**: Integration with OpenAI and Ollama for LLM and embedding capabilities.
- **Spring Modulith**: Ensures a clean, modular architecture with domain-driven design.
- **Microsoft GraphRAG**: Powering advanced knowledge graph-based retrieval.
- **Flyway**: Database schema versioning and migrations.
- **MapStruct & Lombok**: Reducing boilerplate for DTO mapping and domain objects.

### Data & Search

- **PostgreSQL**: Primary relational database for structured data.
- **Elasticsearch**: Powering high-performance vector search and document indexing.
- **Redis**: High-speed caching and session management.
- **MinIO (S3)**: Object storage for document and file persistence.

### AI & Specialized Sidecars

- **Python Sidecars**: Dedicated services for **GraphRAG** and **FAQ Clustering**.
- **OpenAI & Ollama**: Support for both cloud-based and local LLM models.

### Observability

- **Prometheus & Grafana**: Real-time metrics and dashboards.
- **Loki & Tempo**: Integrated logging and distributed tracing.

---

## 🎥 Demo

Check out the MVP in action:
[Watch the Demo Video](https://drive.google.com/file/d/1GClymwnrGJapg9QCKHJf0S7Ol8sf3gnR/view?usp=sharing)

---

## 🛠️ Local Setup

### Prerequisites

- **Java 21** or higher.
- **Docker & Docker Compose**.
- **Maven**.

### Steps

#### 1. Backend Setup

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd nexa
   ```

2. **Start Infrastructure**:
   Launch the database, search engine, storage, and observability stack. **Note:** Before running, please modify the
   `GRAPHRAG_API_BASE` in the `compose.yaml` file to point to your specific GraphRAG service endpoint.
   ```bash
   docker compose up -d
   ```

3. **Configure Environment**:
   If using OpenAI, set your API key in `src/main/resources/application-openai.yaml` or as an environment variable:
   ```bash
   export ULTIMATE_API_KEY=your_api_key_here
   ```

4. **Run the Application**:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev,openai"
   ```
   The API will be available at `http://localhost:8080`, and the Swagger UI at `http://localhost:8080/swagger-ui.html`.

5. **Run FAQ Clustering (Manually)**:
   Since the FAQ clustering service is configured as a one-shot task, you can execute it manually via Docker Compose to
   cluster the queries in the database:
   ```bash
   docker compose run --rm faq
   ```
   You can also pass custom arguments such as target date or cluster count:
   ```bash
   docker compose run --rm faq --date 2026-06-18 --num-clusters 3
   ```

#### 2. Frontend Setup

For instructions on how to set up and run the user interface, please refer to
the [Nexa Client Repository](https://github.com/teri1712/nexa-client).

---

## 📊 Performance Report: Virtual Threads

One of Nexa's core strengths is its efficient use of **Java 21 Virtual Threads** for document ingestion tasks. These
tasks are heavily I/O-bound (reading PDFs, chunking, and calling embedding APIs).

### Ingestion Efficiency Comparison

Based on internal performance benchmarks (referencing `@performance/virtual-thread/**`), we observed a dramatic
reduction in active thread time for ingesting specific sample documents when switching from Platform Threads to Virtual
Threads.

| Sample Document                        | Platform Thread Active Time | Virtual Thread Active Time | Improvement       |
|:---------------------------------------|:----------------------------|:---------------------------|:------------------|
| **Java Core Concepts (100 MCQs).pdf**  | ~114.4 seconds              | ~10.7 seconds              | **~10.6x faster** |
| **SQL Query Optimization (MySQL).pdf** | ~109.4 seconds              | ~12.5 seconds              | **~8.7x faster**  |

> **Note**: Benchmark samples can be found in `src/test/resources/samples/`. Virtual Threads can be toggled in
`application.yaml` via `spring.threads.virtual.enabled`.
