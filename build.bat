@echo off
REM Salesforce Org Comparator - Build Script
REM This script compiles and packages the application

setlocal enabledelayedexpansion

set JAVA_HOME=C:\Program Files\Java\jdk-21
set PROJECT_DIR=%~dp0
set SRC_DIR=%PROJECT_DIR%src\main\java
set BUILD_DIR=%PROJECT_DIR%build
set LIB_DIR=%PROJECT_DIR%lib
set DIST_DIR=%PROJECT_DIR%dist

echo ========================================
echo Salesforce Org Comparator - Build Script
echo ========================================
echo.

REM Create directories
if not exist "%BUILD_DIR%" mkdir "%BUILD_DIR%"
if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"

echo [1/3] Compiling Java sources...
set CLASSPATH="%LIB_DIR%\*"

cd /d "%SRC_DIR%"
"%JAVA_HOME%\bin\javac" -cp "%CLASSPATH%" -d "%BUILD_DIR%" com\sfcomparator\model\*.java com\sfcomparator\api\*.java com\sfcomparator\cli\*.java com\sfcomparator\comparator\*.java com\sfcomparator\util\*.java com\sfcomparator\ui\*.java

if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    cd /d "%PROJECT_DIR%"
    exit /b 1
)

cd /d "%PROJECT_DIR%"

echo [2/3] Creating JAR manifest...
(
    echo Manifest-Version: 1.0
    echo Main-Class: com.sfcomparator.ui.MainWindow
    echo Class-Path: ../lib/json-20231013.jar
) > "%BUILD_DIR%\MANIFEST.MF"

echo [3/3] Packaging JAR file...
cd /d "%BUILD_DIR%"
"%JAVA_HOME%\bin\jar" cfm "%DIST_DIR%\SalesforceComparator.jar" MANIFEST.MF com\

cd /d "%PROJECT_DIR%"

echo.
echo ========================================
echo Build completed successfully!
echo JAR file: %DIST_DIR%\SalesforceComparator.jar
echo.
echo To run the application:
echo   java -cp "build;lib\*" com.sfcomparator.ui.MainWindow
echo   or
echo   java -jar dist\SalesforceComparator.jar
echo ========================================
