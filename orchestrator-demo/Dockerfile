FROM eclipse-temurin:21-jre
VOLUME /tmp
ARG JAR_FILE=boot/target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
