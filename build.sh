#!/bin/bash
# Salesforce Org Comparator - Build Script for Linux/Mac

JAVA_HOME=$(which java)
PROJECT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
SRC_DIR="$PROJECT_DIR/src/main/java"
BUILD_DIR="$PROJECT_DIR/build"
LIB_DIR="$PROJECT_DIR/lib"
DIST_DIR="$PROJECT_DIR/dist"

echo "========================================"
echo "Salesforce Org Comparator - Build Script"
echo "========================================"
echo ""

# Create directories
mkdir -p "$BUILD_DIR"
mkdir -p "$DIST_DIR"

echo "[1/3] Compiling Java sources..."
CLASSPATH="$LIB_DIR/*"
javac -cp "$CLASSPATH" -d "$BUILD_DIR" \
    "$SRC_DIR"/com/sfcomparator/model/*.java \
    "$SRC_DIR"/com/sfcomparator/api/*.java \
    "$SRC_DIR"/com/sfcomparator/comparator/*.java \
    "$SRC_DIR"/com/sfcomparator/ui/*.java

if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi

echo "[2/3] Creating JAR manifest..."
mkdir -p "$BUILD_DIR/META-INF"
cat > "$BUILD_DIR/META-INF/MANIFEST.MF" << EOF
Manifest-Version: 1.0
Main-Class: com.sfcomparator.ui.MainWindow
Class-Path: lib/json-20231013.jar
EOF

echo "[3/3] Packaging JAR file..."
cd "$BUILD_DIR"
jar cfm "$DIST_DIR/SalesforceComparator.jar" META-INF/MANIFEST.MF com/
cd "$PROJECT_DIR"

echo ""
echo "========================================"
echo "Build completed successfully!"
echo "JAR file: $DIST_DIR/SalesforceComparator.jar"
echo ""
echo "To run the application:"
echo "  java -cp 'build:lib/*' com.sfcomparator.ui.MainWindow"
echo "  or"
echo "  java -jar dist/SalesforceComparator.jar"
echo "========================================"
