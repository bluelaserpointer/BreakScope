ECHO OFF

REM jdkéQè∆êÊ
SET JDKHOME=c:\Program Files\Java\jdk1.7.0_09
PATH %JDKHOME%\bin;%PATH%
javac -encoding UTF-8 -classpath library\MpegAudioSPI1.9.5\mp3spi1.9.5.jar;library\MpegAudioSPI1.9.5\lib\tritonus_share.jar;.;..;Gimmick;Entity;Enemy BreakScope_Editor.java

REM é¿çs
java -classpath .;..;Gimmick;Entity;Enemy BreakScope_Editor
