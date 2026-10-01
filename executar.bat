@echo off
chcp 65001 >nul
if not exist out mkdir out
dir /s /b src\*.java > fontes.txt
javac -encoding UTF-8 -d out @fontes.txt
if errorlevel 1 (echo Erro ao compilar & pause & exit /b 1)
del fontes.txt
if not exist lib\*.jar (echo Coloque o driver mysql-connector-j.jar na pasta lib e execute de novo. & pause & exit /b 1)
java -cp "out;lib\*" br.com.helpdesk.Main
