package logmerge.cli;

import logmerge.LogMergeException;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CliParser {
	private static final Logger LOGGER = Logger.getLogger(CliParser.class.getName());

	public CliOptions parse(String[] args) {
		Options options = new Options();
		options.addOption(Option.builder().argName("d").hasArg().desc("the delimiter").type(String.class).longOpt("delimiter").build());
		options.addOption(Option.builder().argName("tf").hasArg().desc("the timestamp format (e.g. yyyy-MM-dd'T'HH:mm:ss.SSSXXX)").type(String.class).longOpt("timestamp-format").build());
		options.addOption(Option.builder().argName("i").hasArg().desc("the log files (comma separated)").type(String.class).longOpt("input").required().build());
		options.addOption(Option.builder().argName("o").hasArg().desc("the output file").type(String.class).longOpt("output").build());
		options.addOption(Option.builder().argName("f").hasArg().desc("the field number(s) (comma separated) containing the timestamp").type(String.class).longOpt("field").build());
		options.addOption(Option.builder().argName("m").desc("if a marker for each file should be inserted").type(Boolean.class).longOpt("marker").build());
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine cli = parser.parse(options, args);
			CliOptions cliOptions = new CliOptions();
			if (cli.hasOption("d")) {
				String optionValue = cli.getOptionValue("d");
				cliOptions.setDelimiter(optionValue);
			}
			if (cli.hasOption("tf")) {
				String optionValue = cli.getOptionValue("tf");
				try {
					new SimpleDateFormat(optionValue);
				} catch (IllegalArgumentException e) {
					throw new LogMergeException(LogMergeException.Reason.CliParser, "The provided timestamp format is not valid: " + e.getMessage(), e);
				}
				cliOptions.setTimestampFormat(optionValue);
			}
			if (cli.hasOption("i")) {
				String optionValue = cli.getOptionValue("i");
				String[] files = optionValue.split(",");
				for (String fileName : files) {
                    File file = new File(fileName);
                    if (!file.exists()) {
                        throw new LogMergeException(LogMergeException.Reason.CliParser, "The input file '" + fileName + "' does not exist.");
                    }
                    if (!file.canRead()) {
                        throw new LogMergeException(LogMergeException.Reason.CliParser, "Cannot read input file '" + fileName + "'.");
                    }
					cliOptions.getLogFiles().add(fileName);
				}
			}
			if (cli.hasOption("f")) {
				String optionValue = cli.getOptionValue("f");
                String[] parts = optionValue.split(",");
                int[] fields = new int[parts.length];
                int fieldsIndex = 0;
                for (String part : parts) {
                    try {
                        int fieldNumber = Integer.parseInt(part);
                        fields[fieldsIndex++] = fieldNumber;
                    } catch (NumberFormatException e) {
                        throw new LogMergeException(LogMergeException.Reason.CliParser, "Please provide an integer for option 'f': " + e.getMessage());
                    }
                }
                cliOptions.setFieldNumber(fields);
			}
            if (cli.hasOption("o")) {
                String optionValue = cli.getOptionValue("o");
                File file = new File(optionValue);
                if (file.exists()) {
                    throw new LogMergeException(LogMergeException.Reason.CliParser, "Output file '" + file + "' already exists.");
                }
                cliOptions.setOutputFile(optionValue);
            }
            if (cli.hasOption("m")) {
                cliOptions.setMarker(true);
            }
			if (LOGGER.isLoggable(Level.FINE)) {
				LOGGER.log(Level.FINE, cliOptions.toString());
			}
			return cliOptions;
		} catch (ParseException e) {
			throw new LogMergeException(LogMergeException.Reason.CliParser, e.getMessage() + "\n" + createHelp(options), e);
		}
	}

	private String createHelp(Options options) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.printHelp(printWriter, 100, "java -jar log-merger.jar", "log-merger", options, 3, 5, "");
		return stringWriter.toString();
	}
}