@echo off
set MAVEN_VERSION=3.9.9
set WRAPPER_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%
set MAVEN_HOME=%WRAPPER_HOME%\apache-maven-%MAVEN_VERSION%
if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
  if not exist "%WRAPPER_HOME%" mkdir "%WRAPPER_HOME%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$u='https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip'; $z='%WRAPPER_HOME%\maven.zip'; Invoke-WebRequest -Uri $u -OutFile $z; Expand-Archive -Path $z -DestinationPath '%WRAPPER_HOME%' -Force; Remove-Item $z"
)
call "%MAVEN_HOME%\bin\mvn.cmd" %*
