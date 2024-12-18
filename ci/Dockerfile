FROM eclipse-temurin:17.0.13_10-jdk-jammmy

RUN apt-get update && apt-get install -y gpg

RUN apt-get clean \
 && rm -rf /var/lib/apt/lists/*