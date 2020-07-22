#!/bin/bash
VER=0.01
#gradle build
docker build --tag cherepakhin/kafka-to-es:$VER .
docker push cherepakhin/kafka-to-es:$VER


