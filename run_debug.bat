@echo off
REM Run script com DEBUG habilitado para diagnosticar problemas de certificado

setlocal enabledelayedexpansion

set JAVA_HOME=C:\Program Files\Java\jdk-21
set PROJECT_DIR=%~dp0
set BUILD_DIR=%PROJECT_DIR%build
set LIB_DIR=%PROJECT_DIR%lib

echo ========================================
echo Salesforce Org Comparator - DEBUG MODE
echo ========================================
echo.
echo Habilitando debug para diagnosticar problemas...
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

echo Starting application with SSL debug...
echo.

REM Run with detailed SSL/TLS debug output
"%JAVA_HOME%\bin\java" ^
    -Djavax.net.debug=ssl:handshake ^
    -cp "%BUILD_DIR%;%LIB_DIR%\*" ^
    com.sfcomparator.ui.MainWindow

echo.
echo Application closed.
pause
