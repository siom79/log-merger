# log-merger
Simple command line utility to merge different log files based on their timestamps preserving stacktraces.

## Usage

To merge two log files which contain the timestamp in the format `2015-08-29T15:49:46,919` in the first column
(columns are separated by space):
```
java -jar log-merger-0.0.2-jar-with-dependencies.jar -i in1.log,in2.log -o out.log -m -d " " -tf "yyyy-MM-dd'T'HH:mm:ss,SSS" -f 1
```
The available options are:
```
-d,--delimiter <del>            the delimiter
-f,--field <field(s)>           the field number(s) (comma separated) containing the timestamp
-i,--input <file(s)>            the log files (comma separated)
-m,--marker                     if a marker for each file should be inserted
-o,--output <file>              the output file
-tf,--timestamp-format <tf>     the timestamp format (e.g. yyyy-MM-dd'T'HH:mm:ss.SSSXXX)
-v,--verbose                    outputs additional logging information
```
When a file contains date and time in separate fields (e.g. 2015-08-29 14:15:30,472) you can provide the options `-d " " -tf "yyyy-MM-dd'T'HH:mm:ss,SSS" -f 1,2`
as the fields one and two are concatenated and parsed as one field according to the specified timestamp format.

The option `--marker` inserts a maker like `[0]` at the beginning of each line into the output to indicate from which file the current line stems. The
input files are numbered (starting with zero) such that the marker `[3]` tells you the lines stems from the fourth input file.

If the option `--input` is not provided, the output is written to stdout. Hence you can use this tool in a pipeline with
standard utilities like grep, cut, etc.

More information on how to specify the timestamp format can be found [here](http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html).
For httpd's access logs you can use for example: `'['dd/MMM/yyyy:HH:mm:ss` together with space as delimiter.

If a line does not contain the number of specified fields or does not contain a timestamp at the given fields, it is treated
like it would belong to the last line with proper timestamp. This way the stacktrace of an exception is merged together with the timestamp line.

Using the option `--verbose` will output any errors happening during timestamp conversion to stderr. As lines that do not contain
a valid timestamp will be appended to the last line with a valid timestamp, it can happen that the output is empty when specifying a
wrong timestamp format or the wrong field as there is no last line to append the invalid line to.

## Example:

file1.log:
```
2015-08-29 15:49:46,641 ERROR [org.jboss.msc.service.fail] (MSC service thread 1-4) MSC000001: Failed to start service jboss.undertow.listener.default: org.jboss.msc.service.StartException in service jboss.undertow.listener.default: Could not start http listener
	at org.wildfly.extension.undertow.ListenerService.start(ListenerService.java:150)
	at org.jboss.msc.service.ServiceControllerImpl$StartTask.startService(ServiceControllerImpl.java:1948)
	at org.jboss.msc.service.ServiceControllerImpl$StartTask.run(ServiceControllerImpl.java:1881)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
	at java.lang.Thread.run(Thread.java:745)
Caused by: java.net.BindException: Die Adresse wird bereits verwendet
	at sun.nio.ch.Net.bind0(Native Method)
	at sun.nio.ch.Net.bind(Net.java:436)
	at sun.nio.ch.Net.bind(Net.java:428)
	at sun.nio.ch.ServerSocketChannelImpl.bind(ServerSocketChannelImpl.java:214)
	at sun.nio.ch.ServerSocketAdaptor.bind(ServerSocketAdaptor.java:74)
	at sun.nio.ch.ServerSocketAdaptor.bind(ServerSocketAdaptor.java:67)
	at org.xnio.nio.NioXnioWorker.createTcpConnectionServer(NioXnioWorker.java:182)
	at org.xnio.XnioWorker.createStreamConnectionServer(XnioWorker.java:243)
	at org.wildfly.extension.undertow.HttpListenerService.startListening(HttpListenerService.java:115)
	at org.wildfly.extension.undertow.ListenerService.start(ListenerService.java:147)
	... 5 more
```
file2.log:
```
2015-08-29 15:49:46,033 INFO  [org.xnio] (MSC service thread 1-3) XNIO version 3.3.1.Final
```
output:
```
[1] 2015-08-29 15:49:46,033 INFO  [org.xnio] (MSC service thread 1-3) XNIO version 3.3.1.Final
[0] 2015-08-29 15:49:46,641 ERROR [org.jboss.msc.service.fail] (MSC service thread 1-4) MSC000001: Failed to start service jboss.undertow.listener.default: org.jboss.msc.service.StartException in service jboss.undertow.listener.default: Could not start http listener
[0] 	at org.wildfly.extension.undertow.ListenerService.start(ListenerService.java:150)
[0] 	at org.jboss.msc.service.ServiceControllerImpl$StartTask.startService(ServiceControllerImpl.java:1948)
[0] 	at org.jboss.msc.service.ServiceControllerImpl$StartTask.run(ServiceControllerImpl.java:1881)
[0] 	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
[0] 	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
[0] 	at java.lang.Thread.run(Thread.java:745)
[0] Caused by: java.net.BindException: Die Adresse wird bereits verwendet
[0] 	at sun.nio.ch.Net.bind0(Native Method)
[0] 	at sun.nio.ch.Net.bind(Net.java:436)
[0] 	at sun.nio.ch.Net.bind(Net.java:428)
[0] 	at sun.nio.ch.ServerSocketChannelImpl.bind(ServerSocketChannelImpl.java:214)
[0] 	at sun.nio.ch.ServerSocketAdaptor.bind(ServerSocketAdaptor.java:74)
[0] 	at sun.nio.ch.ServerSocketAdaptor.bind(ServerSocketAdaptor.java:67)
[0] 	at org.xnio.nio.NioXnioWorker.createTcpConnectionServer(NioXnioWorker.java:182)
[0] 	at org.xnio.XnioWorker.createStreamConnectionServer(XnioWorker.java:243)
[0] 	at org.wildfly.extension.undertow.HttpListenerService.startListening(HttpListenerService.java:115)
[0] 	at org.wildfly.extension.undertow.ListenerService.start(ListenerService.java:147)
[0] 	... 5 more
```

## Download

You can download a copy of `log-merger` from the [downloads](https://github.com/siom79/log-merger/releases) page.