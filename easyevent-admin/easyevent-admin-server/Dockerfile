FROM openjdk:8-jdk-alpine
VOLUME /tmp
ARG JAR_FILE=target/easyevent-admin-server-1.5.0-beta.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8088
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
