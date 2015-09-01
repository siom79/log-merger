package logmerge;

import logmerge.cli.CliOptions;
import org.junit.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class LogMergerTest {

    @Test
    public void test() throws IOException {
        String timestampFormat = "yyyy-MM-ddHH:mm:ss,SSS";
        Path outputPath = Paths.get(System.getProperty("user.dir"), "target", "wildfly_server12.log");
        Path inputPath1 = Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "wildfly_server1.log");
        Path inputPath2 = Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "wildfly_server2.log");
        CliOptions cliOptions = new CliOptions();
        cliOptions.setFieldNumber(new int[]{1, 2});
        cliOptions.setDelimiter(" ");
        cliOptions.setOutputFile(outputPath.toString());
        cliOptions.setTimestampFormat(timestampFormat);
        cliOptions.getLogFiles().add(inputPath1.toString());
        cliOptions.getLogFiles().add(inputPath2.toString());
        cliOptions.setMarker(true);
        LogMerger logMerger = new LogMerger(cliOptions);
        logMerger.merge();
        assertThat(Files.exists(outputPath), is(true));
        List<String> outputLines = Files.readAllLines(outputPath, Charset.forName("UTF-8"));
        assertThat(outputLines.size(), is(337));
        for (String inputLine : Files.readAllLines(inputPath1, Charset.forName("UTF-8"))) {
            assertThat(containsLine(outputLines, inputLine), is(true));
        }
        for (String inputLine : Files.readAllLines(inputPath2, Charset.forName("UTF-8"))) {
            assertThat(containsLine(outputLines, inputLine), is(true));
        }
        assertThatAllTimestampsAreAscending(timestampFormat, outputLines);
    }

    private void assertThatAllTimestampsAreAscending(String timestampFormat, List<String> outputLines) {
        Date lastDate = null;
        SimpleDateFormat sdf = new SimpleDateFormat(timestampFormat);
        for (String line : outputLines) {
            String[] split = line.split(" ");
            if (split.length >= 2) {
                String ts = split[0] + split[1];
                try {
                    Date currentDate = sdf.parse(ts);
                    if (lastDate != null) {
                        assertTrue(currentDate.compareTo(lastDate) >= 0);
                    }
                    lastDate = currentDate;
                } catch (Exception ignore) {}
            }
        }
    }

    private boolean containsLine(List<String> lines, String lineToContain) {
        boolean contained = false;
        for (String line : lines) {
            if (line.contains(lineToContain)) {
                contained = true;
                break;
            }
        }
        return contained;
    }

    @Test
    public void testBigFiles() throws IOException {
        String timestampFormat = "yyyy-MM-ddHH:mm:ss,SSS";
        Path outputPath = Paths.get(System.getProperty("user.dir"), "target", "bigfile_output.log");
        Path inputPath1 = Paths.get(System.getProperty("user.dir"), "target", "bigfile_input1.log");
        Path inputPath2 = Paths.get(System.getProperty("user.dir"), "target", "bigfile_input2.log");
        int numberOfLines = 100000;
        createBigFile(inputPath1, inputPath2, timestampFormat, numberOfLines);
        CliOptions cliOptions = new CliOptions();
        cliOptions.setFieldNumber(new int[]{1});
        cliOptions.setDelimiter(" ");
        cliOptions.setOutputFile(outputPath.toString());
        cliOptions.setTimestampFormat(timestampFormat);
        cliOptions.getLogFiles().add(inputPath1.toString());
        cliOptions.getLogFiles().add(inputPath2.toString());
        cliOptions.setMarker(true);
        LogMerger logMerger = new LogMerger(cliOptions);
        logMerger.merge();
        assertThat(Files.exists(outputPath), is(true));
        assertThatTimestampsAreAscending(timestampFormat, outputPath, numberOfLines * 2);
    }

    private void assertThatTimestampsAreAscending(String timestampFormat, Path outputPath, int expectedLineCount) throws IOException {
        Date lastDate = null;
        int lineCount = 0;
        SimpleDateFormat sdf = new SimpleDateFormat(timestampFormat);
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(outputPath.toString()), "UTF-8"))) {
            String line = fileReader.readLine();
            while (line != null) {
                lineCount++;
                String[] split = line.split(" ");
                if (split.length >= 2) {
                    String ts = split[0] + split[1];
                    try {
                        Date currentDate = sdf.parse(ts);
                        if (lastDate != null) {
                            assertTrue(currentDate.compareTo(lastDate) >= 0);
                        }
                        lastDate = currentDate;
                    } catch (Exception ignore) {}
                }
                line = fileReader.readLine();
            }
        }
        assertThat(lineCount, is(expectedLineCount));
    }

    private void createBigFile(Path inputPath1, Path inputPath2, String timestampFormat, int numberOfLines) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat(timestampFormat);
        try (FileWriter fw1 = new FileWriter(inputPath1.toFile());
             FileWriter fw2 = new FileWriter(inputPath2.toFile())) {
            for (int i = 0; i < numberOfLines; i++) {
                fw1.write(sdf.format(new Date()) + " " + i + "\n");
                fw1.flush();
                fw2.write(sdf.format(new Date()) + " " + i + "\n");
                fw2.flush();
            }
        }
    }

    @Test
    public void testBigFilesWithWrongField() throws IOException {
        String timestampFormat = "yyyy-MM-ddHH:mm:ss,SSS";
        Path outputPath = Paths.get(System.getProperty("user.dir"), "target", "bigfile_output.log");
        Path inputPath1 = Paths.get(System.getProperty("user.dir"), "target", "bigfile_input1.log");
        Path inputPath2 = Paths.get(System.getProperty("user.dir"), "target", "bigfile_input2.log");
        createBigFile(inputPath1, inputPath2, timestampFormat, 100000);
        CliOptions cliOptions = new CliOptions();
        cliOptions.setFieldNumber(new int[]{2});
        cliOptions.setDelimiter(" ");
        cliOptions.setOutputFile(outputPath.toString());
        cliOptions.setTimestampFormat(timestampFormat);
        cliOptions.getLogFiles().add(inputPath1.toString());
        cliOptions.getLogFiles().add(inputPath2.toString());
        cliOptions.setMarker(true);
        LogMerger logMerger = new LogMerger(cliOptions);
        logMerger.merge();
        assertThat(Files.exists(outputPath), is(true));
        assertThatTimestampsAreAscending(timestampFormat, outputPath, 0);
    }

    @Test
    public void testMergeOneExceptionWithOneLine() throws IOException {
        String timestampFormat = "yyyy-MM-ddHH:mm:ss,SSS";
        Path outputPath = Paths.get(System.getProperty("user.dir"), "target", "testMergeOneExceptionWithNoException.log");
        Path inputPath1 = Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "oneException.log");
        Path inputPath2 = Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "oneLine.log");
        CliOptions cliOptions = new CliOptions();
        cliOptions.setFieldNumber(new int[]{1, 2});
        cliOptions.setDelimiter(" ");
        cliOptions.setOutputFile(outputPath.toString());
        cliOptions.setTimestampFormat(timestampFormat);
        cliOptions.getLogFiles().add(inputPath1.toString());
        cliOptions.getLogFiles().add(inputPath2.toString());
        cliOptions.setMarker(true);
        LogMerger logMerger = new LogMerger(cliOptions);
        logMerger.merge();
        assertThat(Files.exists(outputPath), is(true));
        List<String> outputLines = Files.readAllLines(outputPath, Charset.forName("UTF-8"));
        assertThat(outputLines.size(), is(20));
        for (String inputLine : Files.readAllLines(inputPath1, Charset.forName("UTF-8"))) {
            assertThat(containsLine(outputLines, inputLine), is(true));
        }
        for (String inputLine : Files.readAllLines(inputPath2, Charset.forName("UTF-8"))) {
            assertThat(containsLine(outputLines, inputLine), is(true));
        }
        assertThatAllTimestampsAreAscending(timestampFormat, outputLines);
    }

    @Test
    public void testMergeApacheLogs() throws IOException {
        String timestampFormat = "'['dd/MMM/yyyy:HH:mm:ss";
        Path outputPath = Paths.get(System.getProperty("user.dir"), "target", "apache_access12.log");
        Path inputPath1 = Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "apache_access1.log");
        Path inputPath2 = Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "apache_access2.log");
        CliOptions cliOptions = new CliOptions();
        cliOptions.setDelimiter(" ");
        cliOptions.setFieldNumber(new int[]{4});
        cliOptions.setOutputFile(outputPath.toString());
        cliOptions.setTimestampFormat(timestampFormat);
        cliOptions.getLogFiles().add(inputPath1.toString());
        cliOptions.getLogFiles().add(inputPath2.toString());
        cliOptions.setMarker(true);
        LogMerger logMerger = new LogMerger(cliOptions);
        logMerger.merge();
        assertThat(Files.exists(outputPath), is(true));
        List<String> outputLines = Files.readAllLines(outputPath, Charset.forName("UTF-8"));
        assertThat(outputLines.size(), is(4));
        for (String inputLine : Files.readAllLines(inputPath1, Charset.forName("UTF-8"))) {
            assertThat(containsLine(outputLines, inputLine), is(true));
        }
        for (String inputLine : Files.readAllLines(inputPath2, Charset.forName("UTF-8"))) {
            assertThat(containsLine(outputLines, inputLine), is(true));
        }
        assertThatAllTimestampsAreAscending(timestampFormat, outputLines);
    }
}