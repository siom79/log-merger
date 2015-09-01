package logmerge.cli;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CliOptions {
	private String delimiter = ";";
	private String timestampFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
	private List<String> logFiles = new ArrayList<>();
	private int[] fieldNumber = new int[]{1};
	private String outputFile = null;
	private boolean marker = false;
	private boolean verbose;

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public String getTimestampFormat() {
		return timestampFormat;
	}

	public void setTimestampFormat(String timestampFormat) {
		this.timestampFormat = timestampFormat;
	}

	public List<String> getLogFiles() {
		return logFiles;
	}

	public int[] getFieldNumber() {
		return fieldNumber;
	}

	public void setFieldNumber(int[] fieldNumber) {
		this.fieldNumber = fieldNumber;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public void setMarker(boolean marker) {
		this.marker = marker;
	}

	public boolean isMarker() {
		return marker;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public boolean isVerbose() {
		return verbose;
	}

	@Override
	public String toString() {
		return "CliOptions{" +
				"delimiter='" + delimiter + '\'' +
				", timestampFormat='" + timestampFormat + '\'' +
				", logFiles=" + logFiles +
				", fieldNumber=" + Arrays.toString(fieldNumber) +
				", outputFile='" + outputFile + '\'' +
				", marker=" + marker +
				", verbose=" + verbose +
				'}';
	}
}
