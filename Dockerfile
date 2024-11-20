FROM --platform=$TARGETPLATFORM eclipse-temurin:20-jre-alpine

EXPOSE 8080:8080

RUN mkdir /app
COPY server/build/libs/*.jar /app/mani-backend.jar

ENTRYPOINT ["java","-Xms512M","-Xmx2G","-jar","/app/mani-backend.jar"]