FROM openjdk:17-bullseye

RUN apt-get update && apt-get install -y jq gpg

RUN apt-get clean \
 && rm -rf /var/lib/apt/lists/*
