rem StartJava
echo off
set JDKHOME=c:\Program Files\Java\jdk1.7.0_09
PATH %JDKHOME%\bin;%PATH%
javac -encoding UTF-8 -source 1.7 -target 1.7 modVersionUpdater.java
java modVersionUpdater
pause