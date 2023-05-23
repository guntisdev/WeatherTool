#!/bin/bash

cd web
npm run build
cd ..

if sbt test; then
  sbt assembly && fly deploy
else
  echo "Tests failed, skipping deploy!"
fi