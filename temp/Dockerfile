FROM openjdk:17
WORKDIR /app
COPY target/value-plus-backend-0.0.1.jar /app/value-plus-backend-0.0.1.jar
ENV SPRING_PROFILES_ACTIVE={CI_WILL_REPLACE_PROFILE}
EXPOSE 3030
CMD ["java", "-Dspring.profiles.active={CI_WILL_REPLACE_PROFILE}", "-jar","value-plus-backend-0.0.1.jar"]
