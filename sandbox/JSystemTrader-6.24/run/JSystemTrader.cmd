set appHome=..

set cp=%appHome%
set cp=%cp%;%appHome%/bin
set cp=%cp%;%appHome%/classes
set cp=%cp%;%appHome%/lib/liquidlnf.jar
set cp=%cp%;%appHome%/lib/API-9.4.jar
set cp=%cp%;%appHome%/lib/mail.jar
set cp=%cp%;%appHome%/lib/jcommon-1.0.9.jar
set cp=%cp%;%appHome%/lib/jfreechart-1.0.6.jar
set cp=%cp%;%appHome%/lib/OTFeed.jar
set cp=%cp%;%appHome%/lib/jcalendar-1.3.2.jar
set cp=%cp%;%appHome%/lib/joda-time-1.5.jar
set cp=%cp%;%appHome%/lib/commons-math-1.2.jar

set javaHome=C:/WINDOWS/system32/
set javaOptions=-Xmx512M -XX:+UseParallelGC -XX:+AggressiveHeap
set mainClass=com.jsystemtrader.platform.startup.JSystemTrader

%javaHome%java.exe -cp "%cp%" %javaOptions% %mainClass% "%appHome%"

pause

