@echo off
REM ADR Generator 실행 스크립트

set JAVA_HOME=c:\jdk-25.0.2
set PATH=%JAVA_HOME%\bin;%PATH%

if "%1"=="" (
    echo 사용법: run.bat ^<zip-file^> [output-directory]
    echo.
    echo 예제:
    echo   run.bat project-source.zip
    echo   run.bat project-source.zip ./output
    exit /b 1
)

java -cp "bin;lib\*" com.adr.Main %*
