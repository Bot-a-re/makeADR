@echo off
REM ADR Generator 컴파일 스크립트

echo ============================================================
echo ADR Generator - 컴파일 시작
echo ============================================================

set JAVA_HOME=c:\jdk-25.0.2
set PATH=%JAVA_HOME%\bin;%PATH%

echo.
echo Java 버전 확인:
java -version

echo.
echo [1/2] 이전 빌드 정리...
if exist bin rmdir /s /q bin
mkdir bin

echo.
echo [2/2] 소스 코드 컴파일...
dir /s /b src\*.java > sources.txt
javac -d bin -cp "lib\*" -encoding UTF-8 @sources.txt

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================================
    echo ✅ 컴파일 성공!
    echo ============================================================
    del sources.txt
) else (
    echo.
    echo ============================================================
    echo ❌ 컴파일 실패!
    echo ============================================================
    del sources.txt
    exit /b 1
)

echo.
echo 컴파일된 클래스 파일:
dir /s /b bin\*.class

echo.
pause
