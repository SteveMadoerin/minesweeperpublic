#!/bin/bash

# List of module directories containing Dockerfiles
MODULES=("model" "persistence" "controller" "ui")

# Function to build Dockerfile in a directory
build_dockerfile() {
    local module=$1
    echo "Building Dockerfile in $module..."
    docker build -t "$module"-assembly-image:latest $module
}

# Loop through each module and build Dockerfile
for module in "${MODULES[@]}"; do
    build_dockerfile "$module" || exit 1
done

echo "All Dockerfiles built successfully!"