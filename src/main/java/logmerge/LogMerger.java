package logmerge;

import logmerge.cli.CliOptions;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogMerger {
	private static final Logger LOGGER = Logger.getLogger(LogMerger.class.getName());
	private final CliOptions cliOptions;

	public LogMerger(CliOptions cliOptions) {
		this.cliOptions = cliOptions;
	}


	public void merge() {
		List<LogFile> logFiles = null;
        OutputStream outputStream = null;
		try {
            outputStream = createOutputStream(cliOptions);
            logFiles = createLogFiles(cliOptions);
            LogFile nextLogFile = getNextLogFile(logFiles);
            while (nextLogFile != null) {
                List<String> nextLines = nextLogFile.getNextLines();
                if (nextLines.size() > 0) {
                    String markerString = "[" + nextLogFile.getMarker() + "]" + cliOptions.getDelimiter();
                    for (String line : nextLines) {
                        if (cliOptions.isMarker()) {
                            outputStream.write(markerString.getBytes("UTF-8"));
                        }
                        outputStream.write(line.getBytes("UTF-8"));
                        outputStream.write('\n');
                    }
                    outputStream.flush();
                }
                nextLogFile = getNextLogFile(logFiles);
            }
		} catch (IOException e) {
            throw new LogMergeException(LogMergeException.Reason.IOException, "Merging files failed due to I/O error: " + e.getMessage(), e);
        } finally {
			if (logFiles != null) {
				for (LogFile logFile : logFiles) {
					try {
                        logFile.close();
					} catch (Exception ignored) {}
				}
			}
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ignored) {}
            }
		}
	}

    private OutputStream createOutputStream(CliOptions cliOptions) throws FileNotFoundException {
        OutputStream returnValue;
        String outputFile = cliOptions.getOutputFile();
        if (outputFile == null) {
            returnValue = System.out;
        } else {
            returnValue = new FileOutputStream(new File(outputFile));
        }
        return returnValue;
    }

    private LogFile getNextLogFile(List<LogFile> logFiles) throws IOException {
        Date currentDate = null;
        LogFile currentLogFile = null;
        for (LogFile logFile : logFiles) {
            Date date = logFile.peekNextTimestamp();
            if (date != null) {
                if (currentDate == null) {
                    currentDate = date;
                    currentLogFile = logFile;
                } else {
                    if (date.before(currentDate)) {
                        currentDate = date;
                        currentLogFile = logFile;
                    }
                }
            }
        }
        return currentLogFile;
    }

    private List<LogFile> createLogFiles(CliOptions cliOptions) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(cliOptions.getTimestampFormat());
		List<LogFile> logFiles = new ArrayList<>(cliOptions.getLogFiles().size());
        int marker = 0;
		for (String fileName : cliOptions.getLogFiles()) {
			try {
				BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
                LogFile logFile = new LogFile(fileReader, simpleDateFormat, cliOptions, marker++);
				logFiles.add(logFile);
			} catch (FileNotFoundException e) {
				throw new LogMergeException(LogMergeException.Reason.FileNotFound, "File not found: " + e.getMessage());
			} catch (UnsupportedEncodingException e) {
                throw new LogMergeException(LogMergeException.Reason.UnsupportedEncoding, "Unsupported encoding: " + e.getMessage());
            }
        }
		return logFiles;
	}
}
