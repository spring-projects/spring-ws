FROM eclipse-temurin:17-jdk-focal

RUN apt-get update && apt-get install -y gpg

RUN apt-get clean \
 && rm -rf /var/lib/apt/lists/*