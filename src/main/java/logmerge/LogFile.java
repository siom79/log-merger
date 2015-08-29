package logmerge;

import logmerge.cli.CliOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogFile implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(LogFile.class.getName());
    private final BufferedReader bufferedReader;
    private final SimpleDateFormat simpleDateFormat;
    private final CliOptions cliOptions;
    private final int marker;
    private long currentLineNumber = 0;
    private String currentLine = null;
    private Date currentTimestamp = null;
    private boolean eof = false;

    public LogFile(BufferedReader bufferedReader, SimpleDateFormat simpleDateFormat, CliOptions cliOptions, int marker) {
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

    public List<String> getNextLines() throws IOException {
        List<String> lines = new ArrayList<>();
        if (currentLine != null) {
            lines.add(currentLine);
            readNextLine();
            while (currentLine != null) {
                Date timestampFromLine = extractTimestampFromLine();
                if (timestampFromLine != null) {
                    currentTimestamp = timestampFromLine;
                    break;
                } else {
                    lines.add(currentLine);
                }
                readNextLine();
            }
        }
        return lines;
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
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Could not transform value '" + value + "' in line " + currentLineNumber + " to a timestamp: " + e.getMessage());
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
