# Instructions for the Gateway

This is a Maven project so use Maven for all compiling, testing and installing purposes.

## How to build the Docker Image

Run `mvn clean install` to build and push the Docker Image to DockerHub. 
If you only want to build and push the Docker Image without tests etc just use the 
Dockerfile plugin by itself in Maven

## Update the server with the new image

First connect with VPN to the egov-egovlab.dsv.su.se testbed proxy, the connect with SSH
to the 192.168.163.5 restapi server.

Run `docker stack deploy -c gateway-docker-compose-yml` to pull the new image to the server
and restart it.