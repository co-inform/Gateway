# Instructions for the Gateway

This is a Maven project so use Maven for all compiling, testing and installing purposes.

## How to build the Docker Image

Set the active profile to docker. Deploy with the docker compose file.

Run `mvn clean install` to build and push the Docker Image to DockerHub. 
If you only want to build and push the Docker Image without tests etc just use the 
Dockerfile plugin by itself in Maven, ex `mvn dockerfile:build`

