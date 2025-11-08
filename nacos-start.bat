@echo off
echo ====== Starting Nacos ======
start "Nacos" cmd /k "cd /d D:\Environments\nacos-server-3.0.2\bin && startup.cmd -m standalone"

echo ====== Starting MinIO ======
set MINIO_ROOT_USER=root
set MINIO_ROOT_PASSWORD=123456789
start "MinIO" cmd /k "cd /d D:\Environments\Minio\bin && minio.exe server D:\Environments\Minio\data"

echo ====== All services launched! ======
pause
