FROM eclipse-temurin:21-jre-jammy
WORKDIR /graphhopper

# Default JVM options
ENV JAVA_OPTS="-Xmx4g -Xms4g -server"

# We will copy the locally built JAR to the server as 'graphhopper.jar'
COPY graphhopper.jar .

# The config will be mounted via docker-compose, but we need the data directory
RUN mkdir -p /graphhopper/data

EXPOSE 8989 8990

# Start GraphHopper
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar graphhopper.jar server config.yml"]
