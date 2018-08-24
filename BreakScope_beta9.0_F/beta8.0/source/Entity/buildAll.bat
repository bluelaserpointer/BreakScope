ECHO OFF

REM jdkéQè∆êÊ
SET JDKHOME=c:\Program Files\Java\jdk1.7.0_09
PATH %JDKHOME%\bin;%PATH%

FOR %%i IN (*.java) DO (
    ECHO buildingStart: %%i
    javac -encoding UTF-8 -source 1.7 -target 1.7 -classpath .;..\Gimmick;..\Enemy;..;..\.. %%i
)
pause