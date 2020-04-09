FROM openjdk:11-jre

VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} gateway.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Dio.netty.tryReflectionSetAccessible=true","-jar","/gateway.jar"]

EXPOSE 8080
