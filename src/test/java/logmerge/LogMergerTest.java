package logmerge;

import logmerge.cli.CliOptions;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class LogMergerTest {

    @Test
    public void test() throws IOException {
        Path outputPath = Paths.get(System.getProperty("user.dir"), "target", "wildfly_server12.log");
        Path inputPath1 = Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "wildfly_server1.log");
        Path inputPath2 = Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "wildfly_server2.log");
        CliOptions cliOptions = new CliOptions();
        cliOptions.setFieldNumber(new int[]{1, 2});
        cliOptions.setDelimiter(" ");
        cliOptions.setOutputFile(outputPath.toString());
        cliOptions.setTimestampFormat("yyyy-MM-ddHH:mm:ss,SSS");
        cliOptions.getLogFiles().add(inputPath1.toString());
        cliOptions.getLogFiles().add(inputPath2.toString());
        cliOptions.setMarker(true);
        LogMerger logMerger = new LogMerger(cliOptions);
        logMerger.merge();
        assertThat(Files.exists(outputPath), is(true));
        List<String> outputLines = Files.readAllLines(outputPath);
        assertThat(outputLines.size(), is(337));
        for (String inputLine : Files.readAllLines(inputPath1)) {
            assertThat(containsLine(outputLines, inputLine), is(true));
        }
        for (String inputLine : Files.readAllLines(inputPath2)) {
            assertThat(containsLine(outputLines, inputLine), is(true));
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
        createBigFile(inputPath1, inputPath2, timestampFormat);
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
    }

    private void createBigFile(Path inputPath1, Path inputPath2, String timestampFormat) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat(timestampFormat);
        try (FileWriter fw1 = new FileWriter(inputPath1.toFile());
             FileWriter fw2 = new FileWriter(inputPath2.toFile())) {
            for (int i = 0; i < 1000000; i++) {
                fw1.write(sdf.format(new Date()) + " " + i + "\n");
                fw1.flush();
                fw2.write(sdf.format(new Date()) + " " + i + "\n");
                fw2.flush();
            }
        }
    }
}