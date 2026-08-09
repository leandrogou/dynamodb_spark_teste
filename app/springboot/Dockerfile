#FROM openjdk:21-jdk
FROM maven:4.0.0-rc-5-amazoncorretto-21

WORKDIR /app

# Copia o JAR gerado pelo Maven/Gradle
CMD "ls -l"
#COPY target/springboot-app.jar app.jar

# Executa a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
