@echo off
setlocal
set "_VC_VARS_PATH=%ProgramFiles(x86)%\Microsoft Visual Studio\2022"
@REM set "_VC_VARS_PATH=%ProgramFiles(x86)%\Microsoft Visual Studio\18"
set "GRAALVM_HOME=C:\Program Files\java\jdk"


if not exist "%_VC_VARS_PATH%\BuildTools\VC\Auxiliary\Build\vcvarsall.bat" (
    >&2 echo Visual Studio Build Tools not found in "%_VC_VARS_PATH%"
    exit /b 1
)


cd /d "%~dp0"
2>nul rmdir /s /q target

echo ========================================
echo Tetris AOT Build
echo ========================================

set "JAVA_HOME=%GRAALVM_HOME%"
:: ========================================
:: Native Image Configuration
:: ========================================

if not exist "src\main\resources\META-INF\native-image" goto native-image

echo,
call "%_VC_VARS_PATH%\BuildTools\VC\Auxiliary\Build\vcvarsall.bat" x64

:: do not use native-image command
@REM native-image --no-fallback -H:+UnlockExperimentalVMOptions -H:-CheckToolchain --report-unsupported-elements-at-runtime -H:+ReportExceptionStackTraces -jar target\!JAR_FILE! -o target\tetris

:: Try mvn native:compile first
mvn clean native:compile -DskipTests
echo,


@REM if exist "target\tetris.exe" (
@REM     echo Changing subsystem to WINDOWS...
@REM     editbin /SUBSYSTEM:WINDOWS target\tetris.exe
@REM     if errorlevel 1 (
@REM         >&2 echo Warning: editbin failed, terminal window will be shown
@REM     )
@REM )

endlocal
exit /b 0

:native-image
    mvn clean package && for /f "usebackq delims=" %%a in (`
        dir /b "target\tetris-*.jar"
    `) do java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image -jar target\%%a --single
    goto :eof
