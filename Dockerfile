FROM openjdk:8

# Install Chromium v73
RUN apt-get update && apt-get install -y chromium

# Set up gradle environment variables
ENV GRADLE_VERSION=5.6.1
ENV GRADLE_HOME=/opt/gradle

# Download gradle and chromedriver binaries 
WORKDIR /tmp
RUN wget -nv https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip
RUN wget -nv https://chromedriver.storage.googleapis.com/73.0.3683.68/chromedriver_linux64.zip

# Unzip gradle binary
RUN mkdir ${GRADLE_HOME} 
RUN unzip -d ${GRADLE_HOME} /tmp/gradle-${GRADLE_VERSION}-bin.zip
ENV PATH=${PATH}:${GRADLE_HOME}/gradle-${GRADLE_VERSION}/bin

# Copy repository contents into application folder
WORKDIR /app
COPY src /app/src
ADD *.gradle /app/
ADD gradlew* /app/

# Replace chromedriver binary with downloaded version
RUN unzip -d src/main/resources /tmp/chromedriver_linux64.zip 
RUN mv src/main/resources/chromedriver src/main/resources/chromedriver_linux

# Nuke temporary folder
RUN rm -rf /tmp 

ENTRYPOINT ["gradle", "run"] 
