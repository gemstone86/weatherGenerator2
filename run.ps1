$dir = Split-Path -Parent $MyInvocation.MyCommand.Path
java -jar "$dir\target\eon-weather-generator-1.0-SNAPSHOT.jar"
