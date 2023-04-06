FROM openjdk:11-jre-slim-buster
ADD target/as400-extractor-2.1.1-jar-with-dependencies.jar ./
RUN apt update -o Acquire::Check-Valid-Until=false -o Acquire::Check-Date=false

ENTRYPOINT ["java","-jar","as400-extractor-2.1.1-jar-with-dependencies.jar"]
