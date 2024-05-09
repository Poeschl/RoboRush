FROM  docker.io/eclipse-temurin:21-jdk-alpine as detect-deps

COPY build/libs/backend-*.jar /app/app.jar

RUN mkdir /app/unpacked && \
    cd /app/unpacked && \
    unzip ../app.jar && \
    cd .. && \
    $JAVA_HOME/bin/jdeps \
    --ignore-missing-deps \
    --print-module-deps \
    -q \
    --recursive \
    --multi-release 17 \
    --class-path="./unpacked/BOOT-INF/lib/*" \
    --module-path="./unpacked/BOOT-INF/lib/*" \
    ./app.jar > /deps.info && \
    cat /deps.info

FROM  docker.io/eclipse-temurin:21-jdk-alpine as slim-jre

RUN apk add --no-cache binutils
COPY --from=detect-deps /deps.info /deps.info
RUN $JAVA_HOME/bin/jlink \
    --verbose \
    --add-modules $(cat /deps.info) \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=2 \
    --output /slim-jre

FROM docker.io/alpine:3.19 as application
ENV JAVA_HOME=/jre
ENV PATH="${JAVA_HOME}/bin:${PATH}"

COPY --from=slim-jre /slim-jre $JAVA_HOME

# Add dedicated user for non-root run
ARG APPLICATION_USER=java
RUN adduser --no-create-home -u 1000 -D $APPLICATION_USER && \
    mkdir /app && chown -R $APPLICATION_USER /app
USER 1000
WORKDIR /app

ENTRYPOINT ["java", "-jar", "/app/roborush-backend.jar"]
EXPOSE 8080

COPY --chown=1000:1000 build/libs/backend-*.jar /app/roborush-backend.jar
