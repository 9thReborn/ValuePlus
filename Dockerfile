FROM eclipse-temurin:23-jdk-alpine-3.21
WORKDIR /app
COPY target/value-plus-backend-0.0.1.jar /app/value-plus-backend-0.0.1.jar


EXPOSE 3030

ENV SPRING_PROFILES_ACTIVE=DEV
CMD ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-jar","value-plus-backend-0.0.1.jar"]