rem StartJava
echo off
set JDKHOME=c:\Program Files (x86)\Java\jdk1.8.0_171
PATH %JDKHOME%\bin;%PATH%
javac -encoding UTF-8 -source 1.7 -target 1.7 -classpath .;source\Enemy;source\Gimmick;source\Entity ConfigLoader.java
java -classpath .;source\Enemy;source\Gimmick;source\Entity ConfigLoader
pause