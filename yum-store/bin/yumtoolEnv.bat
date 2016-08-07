@echo off


rem classpath
SET CLASS_PATH=%~dp0..\lib\*
rem logback
SET LOGBACK_CONFIGFILE=%~dp0../conf/logback.xml
rem mainclass
SET MAIN_CLASS="com.github.cat.yum.store.Launcher"

SET EXECJAVA="java"

if exist "%~dp0..\jre\bin\java.exe" SET EXECJAVA="%~dp0..\jre\bin\java.exe"
