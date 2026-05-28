@echo off
REM Run script for Salesforce Org Comparator

set PROJECT_DIR=%~dp0
set BUILD_DIR=%PROJECT_DIR%build
set LIB_DIR=%PROJECT_DIR%lib

REM Locate java.exe: use default JAVA_HOME if available, otherwise rely on PATH
set JAVA_HOME_DEFAULT=C:\Program Files\Java\jdk-21
if exist "%JAVA_HOME_DEFAULT%\bin\java.exe" (
    set JAVA_CMD="%JAVA_HOME_DEFAULT%\bin\java"
) else if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set JAVA_CMD="%JAVA_HOME%\bin\java"
    ) else (
        set JAVA_CMD=java
    )
) else (
    set JAVA_CMD=java
)

echo ========================================
echo Salesforce Org Comparator
echo ========================================
echo.

REM Check if build directory exists
if not exist "%BUILD_DIR%" (
    echo Build directory not found. Running build script first...
    call "%PROJECT_DIR%build.bat"
    if !ERRORLEVEL! NEQ 0 (
        echo Build failed!
        pause
        exit /b 1
    )
)

echo Starting application...
echo.

%JAVA_CMD% -cp "%BUILD_DIR%;%LIB_DIR%\*" com.sfcomparator.ui.MainWindow

echo.
echo Application closed.
pause
