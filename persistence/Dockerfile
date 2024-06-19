FROM hseeberger/scala-sbt:17.0.2_1.6.2_3.1.1

WORKDIR /persistence

COPY persistence-assembly-0.1.0-SNAPSHOT.jar bin/

ADD . /persistence

EXPOSE 9083

CMD java -jar bin/persistence-assembly-0.1.0-SNAPSHOT.jar