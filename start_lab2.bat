@echo off
cd /d "%~dp0"

echo ==========================================
echo PORNIRE LAB 2 DEBUG MODE
echo ==========================================
echo.

REM ---- BUILD MAVEN ----
echo [1/4] Maven build...
mvn clean package dependency:copy-dependencies
echo.
pause

REM ---- TEST REDIS ----
echo [2/4] Verific Redis...
redis-cli ping
IF %ERRORLEVEL% NEQ 0 (
    echo Redis nu este disponibil!
    echo Daca ai Docker, ruleaza: docker run -d --name redis -p 6379:6379 redis
    echo Continui fara cache...
    pause
) ELSE (
    echo Redis raspunde OK.
)
echo.
pause

REM ---- PORNESC DW1 ----
echo [3/4] Pornire DWServer 8081...
start "DW1" cmd /k java -cp "target/classes;target/dependency/*" labs.partea1.DWServer 8081

REM ---- PORNESC DW2 ----
echo Pornire DWServer 8082...
start "DW2" cmd /k java -cp "target/classes;target/dependency/*" labs.partea1.DWServer 8082

REM ---- PORNESC PROXY ----
echo [4/4] Pornire ProxyServer 8080...
start "Proxy" cmd /k java -cp "target/classes;target/dependency/*" labs.partea2.ProxyServer

echo.
echo ==========================================
echo Totul a fost lansat IN FERSTRE SEPARATE.
echo Verifica fiecare fereastra pentru erori.
echo ==========================================
pause
