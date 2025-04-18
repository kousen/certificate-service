#!/bin/bash

# Clean and build the project
./gradlew clean bootJar

# Copy main JAR file to the location Heroku expects
cp build/libs/app.jar app.jar

# Create a keystore for Heroku if it doesn't exist
if [ ! -f .cert_keystore.p12 ]; then
  echo "Creating a keystore for Heroku deployment"
  ./gradlew build -x test
fi

# Deploy to Heroku
git add .
git commit -m "Prepare for Heroku deployment"
git push heroku master