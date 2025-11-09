#!/bin/bash
#
# WDP Progress - Auto Release to Repository Server
# 
# This script automatically copies the built JAR file to the repository server
# after a successful Maven build. It's designed to be run as part of the build
# process or manually after creating a new release.
#
# Usage:
#   ./auto-release.sh [version]
#
# If version is not provided, it will be extracted from pom.xml

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TARGET_DIR="$PROJECT_DIR/target"
REPO_DIR="/WDP-Rework/WDP-Repo/releases"
REPO_JSON="/WDP-Rework/WDP-Repo/repository.json"

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}   WDP Progress - Auto Release Script${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo

# Extract version from pom.xml if not provided
if [ -z "$1" ]; then
    VERSION=$(grep -oP '<version>\K[^<]+' pom.xml | head -1 | sed 's/-SNAPSHOT//')
    echo -e "${YELLOW}ℹ ${NC}Version not provided, extracted from pom.xml: ${GREEN}$VERSION${NC}"
else
    VERSION="$1"
    echo -e "${YELLOW}ℹ ${NC}Using provided version: ${GREEN}$VERSION${NC}"
fi

# Find the JAR file
JAR_FILE=$(find "$TARGET_DIR" -name "WDP Progress*.jar" -type f | head -1)

if [ -z "$JAR_FILE" ]; then
    echo -e "${RED}✗${NC} Error: No JAR file found in $TARGET_DIR"
    echo -e "${YELLOW}ℹ ${NC}Please run 'mvn clean package' first"
    exit 1
fi

echo -e "${GREEN}✓${NC} Found JAR: $(basename "$JAR_FILE")"
echo

# Create repository directory if it doesn't exist
if [ ! -d "$REPO_DIR" ]; then
    echo -e "${YELLOW}ℹ ${NC}Creating repository directory: $REPO_DIR"
    mkdir -p "$REPO_DIR"
fi

# Define the release filename
RELEASE_NAME="WDPProgress-${VERSION}.jar"
RELEASE_PATH="$REPO_DIR/$RELEASE_NAME"

# Copy the JAR file
echo -e "${BLUE}→${NC} Copying JAR to repository..."
cp "$JAR_FILE" "$RELEASE_PATH"

if [ -f "$RELEASE_PATH" ]; then
    FILE_SIZE=$(du -h "$RELEASE_PATH" | cut -f1)
    echo -e "${GREEN}✓${NC} Release copied: $RELEASE_NAME (${FILE_SIZE})"
else
    echo -e "${RED}✗${NC} Error: Failed to copy release file"
    exit 1
fi

# Calculate SHA256 checksum
echo
echo -e "${BLUE}→${NC} Calculating SHA256 checksum..."
SHA256=$(sha256sum "$RELEASE_PATH" | cut -d' ' -f1)
echo -e "${GREEN}✓${NC} SHA256: $SHA256"

# Update repository.json metadata
if [ -f "$REPO_JSON" ]; then
    echo
    echo -e "${BLUE}→${NC} Updating repository metadata..."
    
    # Create backup
    cp "$REPO_JSON" "${REPO_JSON}.backup"
    
    # Update JSON (basic sed replacement - could use jq for more robust handling)
    sed -i "s|\"currentVersion\": \".*\"|\"currentVersion\": \"$VERSION\"|" "$REPO_JSON"
    sed -i "s|\"lastUpdated\": \".*\"|\"lastUpdated\": \"$(date -u +%Y-%m-%dT%H:%M:%SZ)\"|" "$REPO_JSON"
    sed -i "s|\"sha256\": \".*\"|\"sha256\": \"$SHA256\"|" "$REPO_JSON"
    
    echo -e "${GREEN}✓${NC} Metadata updated"
fi

# List all releases
echo
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}   Available Releases in Repository${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo
ls -lh "$REPO_DIR"/*.jar 2>/dev/null | awk '{print "  " $9 " (" $5 ")"}'

echo
echo -e "${GREEN}✓${NC} Release deployment complete!"
echo
echo -e "${YELLOW}📦${NC} Download URL: ${BLUE}https://repo.wdpserver.com/releases/$RELEASE_NAME${NC}"
echo -e "${YELLOW}🔗${NC} Repository: ${BLUE}https://repo.wdpserver.com${NC}"
echo
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
