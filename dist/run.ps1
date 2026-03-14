$dir = Split-Path -Parent $MyInvocation.MyCommand.Path
& "$dir\runtime\bin\java.exe" --module-path "$dir\javafx" --add-modules javafx.controls,javafx.graphics,javafx.base -jar "$dir\eon-weather.jar"
