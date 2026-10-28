@echo off
echo ========================================================
echo Compiling and Starting Smart Learning Companion (Java)
echo ========================================================
if not exist "bin" mkdir bin
javac -encoding UTF-8 -cp "backend\lib\*" -d bin backend\src\com\smartlearning\models\*.java backend\src\com\smartlearning\util\*.java backend\src\com\smartlearning\auth\*.java backend\src\com\smartlearning\database\*.java backend\src\com\smartlearning\handlers\*.java backend\src\com\smartlearning\server\*.java backend\src\com\smartlearning\*.java
if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)
echo Server starting on http://localhost:8080...
java -cp "bin;backend\lib\*" com.smartlearning.Main 8080
pause
