package processes;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingWorker;

import ui.Library;

/**
 * This task represents the download operation run by the Downloader in response
 * to a user executing a download. This uses a call to wget to download and uses
 * the Java pattern method to obtain percentage progress from wget output to ask
 * the EDT to intermediate to a progress bar in the DownloadPane.
 * Edited code from Assignment 3 (Added comments and refactored some parts)
 * 
 * @author Harry She & Greggory Tan
 * 
 */
public class DownloadTask extends SwingWorker<Void, String> {
	private String _URL;

	private boolean errorState = false;

	private String inputDir = System.getProperty("user.home") + File.separator
			+ "vamix" + File.separator + "InputLibrary" + File.separator;

	public DownloadTask(String URL) {
		_URL = URL.replaceAll("(?=[]\\[+&|!(){}^\"~*?:\\\\-])", "\\\\");;
	}

	/**
	 * Uses wget with progress dots and continue options to download the input
	 * URL. Uses Java's pattern class to find the percentages such as "25%" and
	 * send these to the EDT to update the GUI.
	 */
	@Override
	protected Void doInBackground() throws Exception {
		String _basename = _URL.substring(_URL.lastIndexOf(File.separator), _URL.length());
		inputDir += _basename;
		errorState = false;
		ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c",
				"wget -A --progress=dot --continue " + _URL + " -O " + inputDir + " 2>&1");
		Process process = null;
		builder.redirectErrorStream(true);
		try {
			process = builder.start();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		InputStream stdout = process.getInputStream();
		BufferedReader stdoutBuffered = new BufferedReader(new InputStreamReader(stdout));
		String line = null;
		Pattern p = Pattern.compile("\\d+%");
		try {
			while ((line = stdoutBuffered.readLine()) != null) {
				if (isCancelled()) {
					process.destroy();
					return null;
				}
				Matcher m = p.matcher(line);
				while (m.find()) {
					String s = m.group();
					s = s.substring(0, s.length() - 1);
					publish(s);
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			switch (process.waitFor())  {
				case 1:
				firePropertyChange("failure", null, "Generic error code");
				errorState = true;
				return null;
				case 2: 
					firePropertyChange("failure", null, "Parse error");
					errorState = true;
					break;
				case 3:
					firePropertyChange("failure", null, "File I/O error");
					errorState = true;
					break;
				case 4:
					firePropertyChange("failure", null, "Network failure");
					errorState = true;
					break;				case 5:
					firePropertyChange("failure", null, "SSL verification failure");
					errorState = true;
					break;				case 6:
					firePropertyChange("failure", null, "Username/password authentication failure");
					errorState = true;
					break;				case 7:
					firePropertyChange("failure", null, "Protocol errors");
					errorState = true;
					break;				case 8:
					firePropertyChange("failure", null, "Server issued an error response");
					errorState = true;
					break;			
					} return null;
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		return null;
	}

	/**
	 * Send appropriate messages to the EDT to update when completed
	 * successfully or ungracefully.
	 */
	@Override
	protected void done() {
		try {
			if (errorState == false) {
				this.get();
				firePropertyChange("_progressLabel", null, "Download Complete!");
			}
		} catch (CancellationException e) {
			firePropertyChange("cancelled", null, "Download Stopped!");
			return;
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
		}
		Library.getInstance().refreshTree();
		firePropertyChange("reenableDownload", null, "reenable");
		return;
	}

	/**
	 * Update progress
	 */
	@Override
	protected void process(List<String> chunks) {
		for (String s : chunks) {
			setProgress(Integer.parseInt(s));
			firePropertyChange("_progressLabel", null, s + "%");
		}
	}
}
