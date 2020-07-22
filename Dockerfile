FROM gradle as compiler

USER root
WORKDIR /code
ENV GRADLE_USER_HOME=/Dependencies

ADD ./settings.gradle /code
ADD ./build.gradle /code
ADD ./src /code/src

RUN gradle build -x test --no-daemon && \
    mkdir -p /build && \
    cp /code/build/libs/kafka-to-es-*.jar /build/kafka-to-es.jar && \
    rm -rf /code

FROM registry.rd.ertelecom.ru/devops/docker/alpine-oraclejdk8:latest

# Installing dependencies:
#  - Debugging: busybox-extras curl
#  - Tomcat: tomcat-native (performance improvements with native libs)

RUN apk add --update --no-cache busybox-extras curl tomcat-native

# Environment variables:
#   JAVA_OPTS             - JVM options for the app
#   SPRING_PROFILE        - Spring default profile is "prod" which uses configuration from ENV variables
#   DD_AGENT_HOST         - Datadog agent host for APM traces
#   DD_TRACE_AGENT_PORT   - Datadog agent port for APM traces
#   DD_SERVICE_NAME       - Datadog service name
#   DD_JMXFETCH_ENABLED   - Datadog JMX trace flag

ENV JAVA_OPTS="-XX:+UseG1GC -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:MaxRAMFraction=2" \
    SPRING_PROFILE="prod" 

CMD java \
    ${JAVA_OPTS} \
    -Dspring.profiles.active=${SPRING_PROFILE} \
    -jar /app/kafka-to-es.jar

COPY --from=compiler /build/ /app/