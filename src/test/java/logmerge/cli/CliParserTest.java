package logmerge.cli;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class CliParserTest {

    @Test
    public void test() throws IOException {
        Path pathLog1 = Paths.get(System.getProperty("user.dir"), "target", "log1");
        Path pathLog2 = Paths.get(System.getProperty("user.dir"), "target", "log2");
        if (!Files.exists(pathLog1)) {
            Files.createFile(pathLog1);
        }
        if (!Files.exists(pathLog2)) {
            Files.createFile(pathLog2);
        }
        CliParser cliParser = new CliParser();
        CliOptions cliOptions = cliParser.parse(new String[]{"-f", "1", "-d", " ", "-i", "target/log1,target/log2", "-o", "out.log", "-m", "-v"});
        assertThat(cliOptions.getDelimiter(), is(" "));
        assertThat(cliOptions.getFieldNumber().length, is(1));
        assertThat(cliOptions.getFieldNumber()[0], is(1));
        assertThat(cliOptions.getLogFiles().size(), is(2));
        assertThat(cliOptions.getLogFiles().get(0), is("target/log1"));
        assertThat(cliOptions.getLogFiles().get(1), is("target/log2"));
        assertThat(cliOptions.getOutputFile(), is("out.log"));
        assertThat(cliOptions.isMarker(), is(true));
        assertThat(cliOptions.isVerbose(), is(true));
    }
}