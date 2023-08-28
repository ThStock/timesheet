@echo off
:restart
java -jar .\target\timesheet.jar
if %errorlevel% neq 0 goto restart
