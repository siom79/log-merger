# log-merger
Simple command line utility to merge different log files based on their timestamps

## Usage

To merge two log files which contain the timestamp in the format `2015-08-29T15:49:46,919` in the first column
(columns are separated by space):
```
java -jar log-merger-0.0.1-jar-with-dependencies.jar -i in1.log,in2.log -o out.log -m -d " " -tf "yyyy-MM-dd'T'HH:mm:ss,SSS" -f 1
```
The available options are:
```
--delimiter <d>             the delimiter
--field <f>                 the field number(s) (comma separated) containing the timestamp
--input <i>                 the log files (comma separated)
--marker                    if a marker for each file should be inserted
--output <o>                the output file
--timestamp-format <tf>     the timestamp format (e.g. yyyy-MM-dd'T'HH:mm:ss.SSSXXX)
```
When a file contains date and time in separate fields (e.g. 2015-08-29 14:15:30,472) you can provide the options `-d " " -tf "yyyy-MM-dd'T'HH:mm:ss,SSS" -f 1,2`
as the fields one and two are concatenated and parsed as one field according to the specified timestamp format.

The option `--marker` inserts a maker like `[0]` at the beginning of each line into the output to indicate from which file the current line stems. The
input files are numbered (starting with zero) such that the marker `[3]` tells you the lines stems from the fourth input file.

If the option `--input` is not provided the output is written to stdout. Hence you can use this tool in a pipline with
standard utilities like grep, cut, etc.

More information on how to specify the timestamp format can be found [here](http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html).

## Download

You can download a copy of `log-merger` from the [downloads](https://github.com/siom79/log-merger/releases) page.