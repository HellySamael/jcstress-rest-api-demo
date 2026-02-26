@echo off
echo Starting Java client ConcurrentVoteClient...

REM Construct the classpath for Windows (using ; as separator and \ for paths)
SET "CLASSPATH=build\classes\java\main"

REM Add all JARs from build\libs
FOR %%i IN ("build\libs\*.jar") DO CALL SET "CLASSPATH=%%CLASSPATH%%;%%i"

REM Add all JARs from app\build\libs
FOR %%i IN ("app\build\libs\*.jar") DO CALL SET "CLASSPATH=%%CLASSPATH%%;%%i"

REM Execute the Java client
java -cp "%CLASSPATH%" com.example.demo.client.ConcurrentVoteClient %*

echo Java client finished.
