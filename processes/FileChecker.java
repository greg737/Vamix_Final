package processes;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This class can check information about a given file useful for the
 * application, either that a file exists or if it is a valid audio/video file
 * or not.
 * Taken from Assignment 3 (Added comments and refactored some parts)
 * 
 * @author Harry She
 * 
 */
public class FileChecker  {
	String _file;

	public FileChecker(String file) {
		_file = file;
	}
	
	/**
	 * Method to check that a file exists.
	 */
	public boolean checkExists() {
		File _fullyQualiFile = new File(_file);
		if (_fullyQualiFile.exists()) {
			return true;
		} else {
			return false;
		}
	}
	/**
	 * Method to check that a file is truly an audio file by calling file on it
	 * and checking whether it contains the word "audio" in it.
	 */
	public boolean checkAVFile(String type) {
		ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c",
				"avprobe " + _file + " 2>&1  | grep -wi '"+type+"'");
		Process process = null;
		builder.redirectErrorStream(true);
		try {
			process = builder.start();

		} catch (IOException e1) {
			e1.printStackTrace();
		}
		InputStream stdout = process.getInputStream();
		BufferedReader stdoutBuffered = new BufferedReader(new InputStreamReader(stdout));
		try {
			if (stdoutBuffered.readLine() != null) {
				return true;
			} else {
				return false;
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return false;

	}

}
