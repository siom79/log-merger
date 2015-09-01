package logmerge;

import logmerge.cli.CliOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogFile implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(LogFile.class.getName());
    private final String fileName;
    private final BufferedReader bufferedReader;
    private final SimpleDateFormat simpleDateFormat;
    private final CliOptions cliOptions;
    private final int marker;
    private long currentLineNumber = 0;
    private String currentLine = null;
    private Date currentTimestamp = null;
    private boolean eof = false;

    public LogFile(String fileName, BufferedReader bufferedReader, SimpleDateFormat simpleDateFormat, CliOptions cliOptions, int marker) {
        this.fileName = fileName;
        this.bufferedReader = bufferedReader;
        this.simpleDateFormat = simpleDateFormat;
        this.cliOptions = cliOptions;
        this.marker = marker;
    }

    public Date peekNextTimestamp() throws IOException {
        if (!eof) {
            if (currentTimestamp == null) {
                currentTimestamp = getNextTimestampFromFile();
            }
            return currentTimestamp;
        }
        return null;
    }

    public Iterator<String> getNextLines() throws IOException {
        return new Iterator<String>() {
            private boolean hasNext = !eof;

            @Override
            public boolean hasNext() {
                return hasNext;
            }

            @Override
            public String next() {
                String lineToReturn = currentLine;
                try {
                    readNextLine();
                    if (currentLine != null) {
                        Date timestampFromLine = extractTimestampFromLine();
                        if (timestampFromLine != null) {
                            currentTimestamp = timestampFromLine;
                            hasNext = false;
                        } else {
                            hasNext = true;
                        }
                    } else {
                        hasNext = false;
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Failed to read from file '" + fileName + "': " + e.getMessage(), e);
                    currentLine = null;
                }
                return lineToReturn;
            }
        };
    }

    public Date getNextTimestampFromFile() throws IOException {
        readNextLine();
        while (currentLine != null) {
            Date timestampFromLine = extractTimestampFromLine();
            if (timestampFromLine != null) {
                return timestampFromLine;
            }
            readNextLine();
        }
        return null;
    }

    private Date extractTimestampFromLine() {
        Date returnValue = null;
        StringBuilder sb = new StringBuilder();
        String[] parts = currentLine.split(cliOptions.getDelimiter());
        for (int fieldNumber : cliOptions.getFieldNumber()) {
            if (fieldNumber <= parts.length) {
                String fieldValue = parts[fieldNumber - 1];
                sb.append(fieldValue);
            }
        }
        String value = sb.toString();
        try {
            returnValue = simpleDateFormat.parse(value);
        } catch (ParseException e) {
            String message = "Could not transform value '" + value + "' in line " + currentLineNumber + " to a timestamp";
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, message + ": " + e.getMessage());
            }
            if (cliOptions.isVerbose()) {
                System.err.println(message + ".");
            }
        }
        return returnValue;
    }

    @Override
    public void close() throws Exception {
        bufferedReader.close();
    }

    private void readNextLine() throws IOException {
        currentLine = bufferedReader.readLine();
        if (currentLine != null) {
            currentLineNumber++;
        } else {
            eof = true;
        }
    }

    public int getMarker() {
        return marker;
    }
}
