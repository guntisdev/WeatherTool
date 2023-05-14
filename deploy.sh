#!/bin/bash

cd web
npm run build
cd ..

sbt assembly && fly deploy