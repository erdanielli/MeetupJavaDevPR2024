# https://docs.spring.io/spring-boot/reference/packaging/container-images/dockerfiles.html
FROM bellsoft/liberica-openjre-alpine-musl:23 AS builder
WORKDIR /build
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

FROM bellsoft/liberica-openjre-alpine-musl:23
WORKDIR /application
COPY --from=builder /build/extracted/dependencies/ ./
COPY --from=builder /build/extracted/spring-boot-loader/ ./
COPY --from=builder /build/extracted/snapshot-dependencies/ ./
COPY --from=builder /build/extracted/application/ ./
ENV SPRING_PROFILES_ACTIVE=server

ENTRYPOINT ["java", "-Duser.country=BR", "-Duser.language=pt", "-Duser.timezone=America/Sao_Paulo", \
"-jar", "application.jar"]

