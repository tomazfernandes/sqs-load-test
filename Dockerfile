FROM openjdk:17.0.2-slim-buster@sha256:1201f5d5750aac6efa9e97c50bbedb6490a617b408b7ade93358d6388e261bcd
RUN mkdir /app
COPY ./build/libs/sqs-load-test-*.jar /app/sqs-load-test.jar
WORKDIR /app
CMD "java" "-jar" "sqs-load-test.jar"
EXPOSE 8080/tcp