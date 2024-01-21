#!/bin/bash

# build web files
cd web
npm run build
cd ..

# Check and set secrets
if [ -s .env-fly ]; then
  while read line; do
    echo "Setting secrets: $line"
    fly secrets set "$line"
  done < .env-fly
else
  echo ".env-fly file is missing or empty, skipping setting secrets"
  exit 1
fi

# Test, build and deply
if sbt test; then
  sbt assembly && fly deploy
else
  echo "Tests failed, skipping deploy!"
  exit 1
fi