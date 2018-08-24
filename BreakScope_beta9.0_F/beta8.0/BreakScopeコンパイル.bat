rem StartJava
echo off
set JDKHOME=c:\Program Files (x86)\Java\jdk1.8.0_171
PATH %JDKHOME%\bin;%PATH%
javac -encoding UTF-8 -classpath .;source\Gimmick;source\Enemy;source\Entity BreakScope.java
java -classpath source\library\MpegAudioSPI1.9.5\mp3spi1.9.5.jar;source\library\MpegAudioSPI1.9.5\lib\tritonus_share.jar;source\library\MpegAudioSPI1.9.5\lib\jl1.0.1.jar;.;source\Gimmick;source\Enemy;source\Entity;source BreakScope
pause