# ---------- Build Stage ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Gradle 캐시 최적화
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --version

# 의존성만 먼저 다운로드(빌드 캐시)
COPY src/main/resources/application.yml ./src/main/resources/
RUN ./gradlew -x test dependencies || true

# 실제 소스 빌드
COPY src ./src
RUN ./gradlew clean bootJar -x test

# ---------- Runtime Stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# 타임존/로케일(옵션)
ENV TZ=Asia/Seoul

# JAR 복사
COPY --from=build /workspace/build/libs/*SNAPSHOT*.jar /app/app.jar

# (선택) 런타임 키/설정 마운트 경로
#  - JWT 키를 이미지에 넣지 말고, 배포 시 마운트/시크릿로 주입 권장
VOLUME ["/app/keys"]

# 헬스엔드포인트 노출
EXPOSE 8080

# 프로파일/메모리 옵션은 환경변수로
ENV JAVA_OPTS="-XX:+UseZGC -XX:+AlwaysActAsServerClassMachine"
ENV SPRING_PROFILES_ACTIVE="docker"

HEALTHCHECK --interval=10s --timeout=3s --retries=10 \
  CMD curl -fsS http://localhost:8080/actuator/health | grep UP || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
