@echo off
echo ====== Starting Nacos ======
start "Nacos" cmd /k "cd /d D:\Environments\nacos-server-3.0.2\bin && startup.cmd -m standalone"
