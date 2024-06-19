FROM hseeberger/scala-sbt:17.0.2_1.6.2_3.1.1

WORKDIR /controller

COPY controller-assembly-0.1.0-SNAPSHOT.jar bin/

ADD . /controller

EXPOSE 9081

CMD java -jar bin/controller-assembly-0.1.0-SNAPSHOT.jar