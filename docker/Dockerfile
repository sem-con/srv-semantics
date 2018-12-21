FROM java:8
MAINTAINER "Christoph Fabianek" christoph@ownyourdata.eu

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

COPY docker/ .

CMD java -jar *.jar

EXPOSE 2806
