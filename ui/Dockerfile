FROM hseeberger/scala-sbt:17.0.2_1.6.2_3.1.1

WORKDIR /app

ADD . /app

ENV DISPLAY=host.docker.internal:0.0

RUN apt-get update && \
    apt-get install -y \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libgl1-mesa-glx libgtk-3-0 openjfx libgl1-mesa-dri libgl1-mesa-dev libcanberra-gtk-module libcanberra-gtk3-module default-jdk

EXPOSE 9088
EXPOSE 9087

CMD sbt run