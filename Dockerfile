# 1️⃣ Build Stage
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw .
COPY pom.xml .

# ✅ Give mvnw execute permission
RUN chmod +x mvnw

# ✅ Download dependencies first to speed up rebuilds
RUN ./mvnw dependency:go-offline

COPY src ./src

# ✅ Build JAR (skipping tests for faster build)
RUN ./mvnw clean package -DskipTests

# 2️⃣ Run Stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# ✅ Copy the built JAR into the container
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
