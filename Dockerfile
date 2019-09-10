FROM openjdk:11-jre

VOLUME /etc/gateway
RUN mkdir /opt/gateway
ADD target/gateway-*.jar /opt/gateway/gateway.jar

ENTRYPOINT exec java -jar /opt/gateway/gateway.jar

EXPOSE 8080
