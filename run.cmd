rem set JAVA_HOME=C:\Program Files\Java\jdk1.6.0_45

"%JAVA_HOME%\bin\java" -Dtangosol.coherence.cacheconfig=src/main/resources/%1 -Dtangosol.coherence.distributed.localstorage=%2 -jar target/CoherenceApp-1.0-SNAPSHOT.jar


