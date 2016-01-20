set JAVA_HOME=C:\Program Files\Java\jdk1.7.0_51

"%JAVA_HOME%\bin\java" ^
  -Dtangosol.coherence.cacheconfig=src/main/resources/%1 ^
  -Dtangosol.coherence.distributed.localstorage=%2 ^
  -jar target/CoherenceApp-1.0-SNAPSHOT.war



