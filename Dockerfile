# Stage 1: Build the Frontend assets
FROM node:20-alpine AS frontend-build
WORKDIR /frontend
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

# Stage 2: Build the Backend Spring Boot application
FROM maven:3.9.6-eclipse-temurin-21 AS backend-build
WORKDIR /app
COPY backend/pom.xml .
COPY backend/src ./src
# Copy compiled static assets from Stage 1 into Spring Boot resources
COPY --from=frontend-build /backend/src/main/resources/static ./src/main/resources/static
RUN mvn clean package -DskipTests

# Stage 3: Run stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend-build /app/target/knowledgeos-backend-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
