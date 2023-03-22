FROM ubuntu:22.04

RUN apt-get update \
    && apt-get install -y wget build-essential \
    && apt-get clean && apt-get autoclean && apt-get autoremove \
    && rm -rf /var/lib/apt/lists/*

RUN cd /tmp \
    && wget https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.9.1%2B1/OpenJDK11U-jdk_x64_linux_hotspot_11.0.9.1_1.tar.gz -O java.tar.gz \
    && mkdir $HOME/java \
    && tar -xf java.tar.gz -C $HOME/java \
    && wget http://mirror.easyname.ch/apache/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz -O maven.tar.gz \
    && mkdir $HOME/maven \
    && tar -xf maven.tar.gz -C $HOME/maven

ENV PATH=/root/java/jdk-11.0.9.1+1/bin:/root/maven/apache-maven-3.6.3/bin:$PATH

COPY ./java/src /srv/app/java/src
COPY ./java/pom.xml /srv/app/java/
RUN cd /srv/app/java \
    && mvn package \
    && cd -

ENV TERM=xterm
COPY src/entrypoint.sh /srv/app/src/
RUN chmod +x /srv/app/src/entrypoint.sh

ENTRYPOINT [ "/srv/app/src/entrypoint.sh" ]
