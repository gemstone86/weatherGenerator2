@echo off
SET DIR=%~dp0
"%DIR%runtime\bin\java.exe" --module-path "%DIR%javafx" --add-modules javafx.controls,javafx.graphics,javafx.base -jar "%DIR%eon-weather.jar"
