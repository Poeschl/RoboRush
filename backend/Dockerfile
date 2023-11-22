FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app
ENTRYPOINT ["java", "-jar", "/app/pathseeker-backend.jar"]
EXPOSE 8080

ADD build/libs/backend-*.jar /app/pathseeker-backend.jar
