rmdir /S /Q classes & javac -cp lib\*;C:\TestJava\libs4log\* *.java -d classes &&^
xcopy /S /I classes\ lib\tmp &&^
xcopy /I lib\ lib\tmp &&^
cd lib\tmp &&^
jar --extract --file dateparser-1.0.10.jar &&^
jar --extract --file juniversalchardet-2.4.0.jar &&^
jar --extract --file lombok-1.18.22.jar &&^
jar --extract --file retree-1.0.4.jar &&^
jar --extract --file slf4j-api-1.7.36.jar &&^
del *.jar &&^
jar --create --file ..\..\NapominalkaFull.jar --main-class=napominalka.Napominalka . &&^
cd .. && rmdir /S /Q tmp\ 
cmd.exe /k