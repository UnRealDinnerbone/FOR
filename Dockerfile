FROM gradle:7.0.0-jdk16 as builder

WORKDIR /build

COPY server/build.gradle /build
COPY server/src /build/src

RUN gradle shadowJar

FROM openjdk:16
COPY --from=builder /build/build/libs/forserver-1.0.0-all.jar forserver-1.0.0-all.jar

CMD ["java", "-jar", "forserver-1.0.0-all.jar"]