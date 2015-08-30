package logmerge;

import logmerge.cli.CliOptions;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class LogFileTest {

    @Test
    public void testWildflyServerLog() throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss,SSS");
        Path path = Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "wildfly_server.log");
        BufferedReader br = new BufferedReader(new FileReader(path.toFile()));
        CliOptions cliOptions = new CliOptions();
        cliOptions.setDelimiter(" ");
        cliOptions.setFieldNumber(new int[]{1, 2});
        LogFile logFile = new LogFile(path.toString(), br, sdf, cliOptions, 0);
        Date date = logFile.peekNextTimestamp();
        assertThat(date, is(createDate(29, 8, 2015, 14, 15, 30, 472)));
        Iterator<String> iterator = logFile.getNextLines();
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), not(nullValue()));
        date = logFile.peekNextTimestamp();
        assertThat(date, is(createDate(29, 8, 2015, 14, 15, 31, 44)));
        iterator = logFile.getNextLines();
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), not(nullValue()));
        date = logFile.peekNextTimestamp();
        assertThat(date, is(createDate(29, 8, 2015, 14, 15, 31, 103)));
        iterator = logFile.getNextLines();
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), not(nullValue()));
        date = logFile.peekNextTimestamp();
        assertThat(date, is(createDate(29, 8, 2015, 14, 15, 31, 104)));
        iterator = logFile.getNextLines();
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), not(nullValue()));
        date = logFile.peekNextTimestamp();
        assertThat(date, is(createDate(29, 8, 2015, 14, 15, 31, 104)));
        iterator = logFile.getNextLines();
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), not(nullValue()));
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