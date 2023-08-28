@echo off
setlocal
cd %~dp0
start /b wscript.exe "run.vbs" "run.bat"
endlocal
