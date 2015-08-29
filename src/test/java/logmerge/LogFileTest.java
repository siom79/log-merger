package logmerge;

import logmerge.cli.CliOptions;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class LogFileTest {

    @Test
    public void testWildflyServerLog() throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss,SSS");
        BufferedReader br = new BufferedReader(new FileReader(Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "wildfly_server.log").toFile()));
        CliOptions cliOptions = new CliOptions();
        cliOptions.setDelimiter(" ");
        cliOptions.setFieldNumber(new int[]{1, 2});
        LogFile logFile = new LogFile(br, sdf, cliOptions, 0);
        Date date = logFile.peekNextTimestamp();
        assertThat(date, is(createDate(29, 8, 2015, 14, 15, 30, 472)));
        List<String> nextLines = logFile.getNextLines();
        assertThat(nextLines.size(), is(1));
        date = logFile.peekNextTimestamp();
        assertThat(date, is(createDate(29, 8, 2015, 14, 15, 31, 44)));
        nextLines = logFile.getNextLines();
        assertThat(nextLines.size(), is(1));
        date = logFile.peekNextTimestamp();
        assertThat(date, is(createDate(29, 8, 2015, 14, 15, 31, 103)));
        nextLines = logFile.getNextLines();
        assertThat(nextLines.size(), is(1));
        date = logFile.peekNextTimestamp();
        assertThat(date, is(createDate(29, 8, 2015, 14, 15, 31, 104)));
        nextLines = logFile.getNextLines();
        assertThat(nextLines.size(), is(87));
        date = logFile.peekNextTimestamp();
        assertThat(date, is(createDate(29, 8, 2015, 14, 15, 31, 104)));
        nextLines = logFile.getNextLines();
        assertThat(nextLines.size(), is(1));
    }

    private Date createDate(int day, int month, int year, int hour, int minute, int second, int ms) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.set(Calendar.DAY_OF_MONTH, day);
        gc.set(Calendar.MONTH, month - 1);
        gc.set(Calendar.YEAR, year);
        gc.set(Calendar.HOUR_OF_DAY, hour);
        gc.set(Calendar.MINUTE, minute);
        gc.set(Calendar.SECOND, second);
        gc.set(Calendar.MILLISECOND, ms);
        return gc.getTime();
    }
}