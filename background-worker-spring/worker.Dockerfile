

# --- STAGE 1: Build Phase ---
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# --- STAGE 2: Runtime Phase ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

# Workers don't listen on external ports because they consume from ActiveMQ,
# but we document it if you ever add health-check actuators.
EXPOSE 8082

CMD ["java", "-jar", "app.jar"]