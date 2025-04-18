#!/bin/bash
set -e

echo "=== Certificate Service Heroku Deployment ==="

# Clean and build the project
echo "Building project..."
./gradlew clean bootJar

# Verify that the JAR was created
if [ ! -f build/libs/app.jar ]; then
  echo "ERROR: JAR file not found at build/libs/app.jar"
  exit 1
fi

echo "JAR file created successfully"

# Log into Heroku if needed
if ! heroku whoami &>/dev/null; then
  echo "Not logged into Heroku. Please log in:"
  heroku login
fi

# Create Heroku app if it doesn't exist
APP_NAME="certificate-service"
if ! heroku apps:info --app $APP_NAME &>/dev/null; then
  echo "Creating Heroku app '$APP_NAME'..."
  heroku create $APP_NAME
fi

# Set environment variables
echo "Setting environment variables..."
heroku config:set CERT_PWD=changeit --app $APP_NAME
heroku config:set JAVA_OPTS="-XX:+UseContainerSupport -Xms128m -Xmx512m" --app $APP_NAME

# Configure buildpacks
echo "Configuring buildpacks..."
heroku buildpacks:clear --app $APP_NAME
heroku buildpacks:add heroku/gradle --app $APP_NAME

# Deploy to Heroku
echo "Deploying to Heroku..."
git add .
git commit -m "Prepare for Heroku deployment" || echo "No changes to commit"
git push heroku main

echo "Deployment complete!"
echo "Your app should be running at: https://$APP_NAME.herokuapp.com/"
echo "Use the following command to view logs:"
echo "  heroku logs --tail --app $APP_NAME"