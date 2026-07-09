ARG MAVEN_IMAGE=maven:3.9.9-eclipse-temurin-17
ARG JRE_IMAGE=eclipse-temurin:17-jre-alpine

FROM ${MAVEN_IMAGE} AS build

WORKDIR /workspace

COPY docker-settings.xml /root/.m2/settings.xml
COPY pom.xml .
RUN mvn -B -ntp -s /root/.m2/settings.xml dependency:go-offline

COPY src ./src
RUN mvn -B -ntp -s /root/.m2/settings.xml clean package -DskipTests

FROM ${JRE_IMAGE}

ENV TZ=Asia/Shanghai
ENV JAVA_OPTS=""

WORKDIR /app

RUN addgroup -S livehouse \
    && adduser -S -G livehouse livehouse \
    && mkdir -p /data/livehouse/uploads \
    && chown -R livehouse:livehouse /app /data/livehouse

COPY --from=build --chown=livehouse:livehouse /workspace/target/livehouse-api-*.jar /app/app.jar

EXPOSE 8080

USER livehouse

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar"]
