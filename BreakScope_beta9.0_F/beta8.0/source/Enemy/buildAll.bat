ECHO OFF

REM jdkéQè∆êÊ
SET JDKHOME=c:\Program Files (x86)\Java\jdk1.8.0_171
PATH %JDKHOME%\bin;%PATH%

FOR %%i IN (*.java) DO (
    ECHO buildingStart: %%i
    javac -encoding UTF-8 -classpath .;..;..\..;..\Gimmick;..\Entity %%i
)

pause