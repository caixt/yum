@echo off
if "%OS%" == "Windows_NT" setlocal
call "%~dp0yumtoolEnv.bat"

cd %~dp0..

"%EXECJAVA%" "-Dlogback.configurationFile=%LOGBACK_CONFIGFILE%" -cp "%CLASS_PATH%" "%MAIN_CLASS%" %*

