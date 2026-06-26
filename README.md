# KnowledgeOS

A minimalist, highly interactive technology-only learning platform — your personalized learning operating system. It features interactive learning slides, gamified active recall quiz quests, spaced repetition flashcard review, and Feynman Sandbox writing critiques with AI-generated feedback.

The application compiles as a unified Single-JAR package, meaning the Spring Boot backend automatically bundles and serves the React frontend UI as static resources from the same origin on port `8080`.

---

## Tech Stack

* **Frontend**: React 19, TypeScript, Vite, Tailwind CSS, ShadCN UI, TanStack Query, React Router, Zustand, Recharts
* **Backend**: Java 21, Spring Boot 3.5, Spring Data JPA, H2 Database (PostgreSQL compatibility mode), Flyway Migrations, OpenAPI
* **AI**: OpenAI-compatible API Integration (defaults to high-performance NVIDIA API)
* **Storage**: Persistent local file-based H2 storage (`db/knowledgeos`)

---

## Environment Variables

### System Configuration

| Environment Variable | Description | Default / Fallback |
| :--- | :--- | :--- |
| `OPENAI_API_KEY` | Your AI provider API secret key | *(Required for AI features)* |
| `OPENAI_BASE_URL` | Base URL of the OpenAI-compatible endpoint | `https://integrate.api.nvidia.com/v1` |
| `OPENAI_MODEL` | AI model name | `z-ai/glm-5.1` |
| `OPENAI_ENABLED` | Enable or disable AI-driven tutor features | `true` |
| `OPENAI_TIMEOUT_SECONDS` | Network request timeout in seconds | `60` |
| `JDBC_DATABASE_URL` | JDBC database connection string | `jdbc:h2:file:./db/knowledgeos;MODE=PostgreSQL...` |
| `ALLOWED_ORIGINS` | Permitted CORS origins (comma-separated patterns) | `http://localhost:5173,http://localhost:3000` |

---

## Quick Start (Local Development)

### 1. Setup Environment
Set your OpenAI/NVIDIA API Key:
```bash
# Windows (PowerShell)
$env:OPENAI_API_KEY="your-nvapi-key"

# Linux / macOS
export OPENAI_API_KEY="your-nvapi-key"
```

### 2. Start the Backend
Runs Flyway migrations and starts the Spring Boot server on port `8080` using a local persistent file database.
```bash
cd backend
mvn spring-boot:run
```
* **API Documentation & Swagger UI**: http://localhost:8080/swagger-ui.html

### 3. Start the Frontend
Installs dependencies and runs the Vite development server in watch mode with API proxying.
```bash
cd frontend
npm install
npm run dev
```
* **Local Development App URL**: http://localhost:5173

---

## Production Build & Deployment

Because the UI is packaged inside the Spring Boot JAR, you only need to host the backend application.

### Local Packaging (Single JAR)
To bundle the frontend inside the backend JAR manually:
1. Build the frontend:
   ```bash
   cd frontend
   npm install
   npm run build
   ```
   *(This outputs compiled HTML/JS/CSS assets directly into `backend/src/main/resources/static`)*
2. Package the Spring Boot JAR:
   ```bash
   cd ../backend
   mvn clean package -DskipTests
   ```
3. Run the single JAR:
   ```bash
   java -jar target/knowledgeos-backend-1.0.0-SNAPSHOT.jar
   ```
Your app will be served entirely at http://localhost:8080.

### Render Deployment (Unified Docker Host)

Deploy your application as a single Web Service using the included multi-stage root `Dockerfile`.

1. Create a new **Web Service** on Render connected to your repository.
2. Configure the following service settings:
   * **Name**: `knowledgeos`
   * **Language/Environment**: Select **Docker** (Render will automatically detect the root `Dockerfile`)
   * **Root Directory**: `.` *(Use the repository root)*
3. Click **Advanced** and configure:
   * **Environment Variables**:
     * `OPENAI_API_KEY`: `nvapi-SMN0uTxbmS_...` *(Your OpenAI API Key)*
     * `JDBC_DATABASE_URL`: `jdbc:h2:file:/var/data/knowledgeos;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE`
4. Scroll down to **Disks** and click **Add Disk**:
   * **Name**: `knowledgeos-db`
   * **Mount Path**: `/var/data`
   * **Size**: `1 GB`
5. Click **Create Web Service**.

Your entire application (both React UI and Spring API) will be served from a single Render URL!
