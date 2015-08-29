package logmerge;

import logmerge.cli.CliOptions;
import logmerge.cli.CliParser;

import java.util.logging.Level;
import java.util.logging.Logger;

public class App {
	private static final Logger LOGGER = Logger.getLogger(App.class.getName());

	public static void main(String[] args) {
		int returnCode = 0;
		try {
			App app = new App();
			app.run(args);
		} catch (Exception e) {
			if (e instanceof LogMergeException) {
				LogMergeException logMergeException = (LogMergeException)e;
				LogMergeException.Reason reason = logMergeException.getReason();
				if (reason == LogMergeException.Reason.CliParser) {
					System.out.println(e.getMessage());
					returnCode = -1;
				} else {
					LOGGER.log(Level.SEVERE, "Execution failed: " + e.getMessage(), e);
				}
			} else {
				LOGGER.log(Level.SEVERE, "Execution failed: " + e.getMessage(), e);
			}
		}
		System.exit(returnCode);
	}

	private void run(String[] args) {
		CliParser cliParser = new CliParser();
		CliOptions cliOptions = cliParser.parse(args);
		LogMerger logMerger = new LogMerger(cliOptions);
		logMerger.merge();
	}
}
