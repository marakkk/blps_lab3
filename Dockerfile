FROM maven:3.8.7-eclipse-temurin-17

WORKDIR /app
COPY . .


CMD ["sh", "-c", "mvn spring-boot:run -Dspring-boot.run.profiles=${SPRING_PROFILES_ACTIVE}"]