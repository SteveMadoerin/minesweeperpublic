FROM hseeberger/scala-sbt:17.0.2_1.6.2_3.1.1

WORKDIR /model

COPY model-assembly-0.1.0-SNAPSHOT.jar bin/

EXPOSE 9082

CMD java -jar bin/model-assembly-0.1.0-SNAPSHOT.jar

# sbt assembly
# docker build -t model-assembly-image .
# then docker-compose up -d