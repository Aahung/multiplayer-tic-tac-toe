# Multiplayer Tic Tac Toe

A web app using WebSocket to enable multiplayer tic tac toe. 

## Requirement

- Java JRE/JDK 7 (not 8)
- Tomcat 7 (not 8)

## Deploy on Linux

As simple as ABC :-0
```bash
./deploy
```

## Deploy on Windows

I am too lazy to write a batch script, please do the following:

-  copy all the files from /public and /servlet to C:\xampp\Tomcat\webapps\ROOT\

-  run the following script

```
javac -cp json-simple-1.1.1.jar;C:\xampp\Tomcat\lib\catalina.jar;C:\xampp\Tomcat\lib\tomcat-coyote.jar;C:\xampp\Tomcat\lib\servlet-api.jar *.java
```

-  create a folder WEB-INF/classes
- create a folder WEB-INF/classes/ee4216
- create a folder WEB-INF/lib
- move all *.class to WEB-INF/classes/ee4216
- move all *.jar to WEB-INF/lib
