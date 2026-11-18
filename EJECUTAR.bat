@echo off
setlocal
cd /d "%~dp0"
where javac >nul 2>nul
if errorlevel 1 (
  echo No se encontro javac. Instale un JDK 8 o posterior y configure PATH.
  pause
  exit /b 1
)
where java >nul 2>nul
if errorlevel 1 (
  echo No se encontro java. Configure el JDK en PATH.
  pause
  exit /b 1
)
if not exist clases mkdir clases
javac -encoding UTF-8 -d clases AnalisisNumericoMiniDonas.java
if errorlevel 1 (
  echo No se pudo compilar el programa.
  pause
  exit /b 1
)
java -cp clases AnalisisNumericoMiniDonas
pause
