FROM eclipse-temurin:17.0.6_10-jdk-focal

RUN apt-get update && apt-get install -y gpg

RUN apt-get clean \
 && rm -rf /var/lib/apt/lists/*