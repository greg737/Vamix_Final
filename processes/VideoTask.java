package processes;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import ui.Library;

/**
 * This class does the bash commands for video editing in the background. This
 * includes the creation of temporary video files containing the title or
 * credits and then the concatenation of the input video with this generated
 * title video.
 * Edited version from Assignment 3 
 * 
 * @author Greggory Tan
 *
 */
public class VideoTask extends SwingWorker<Void, Void> {
	final public static String tempDir = System.getProperty("user.home")
			+ File.separator + "vamix" + File.separator + "temp";

	private String _inputFile;
	private String _tempLocation;
	private String _fadeFile;
	private String _editedLocation;
	public String _finalOutput = "";
	private String _txt;
	private Font _font;
	private String _color;
	private Dimension _videoSize;
	private JTextArea _textArea;
	private int _size;
	private int _duration;
	private Boolean _isTitle;
	private Boolean _isPreview;
	private Boolean errorState;
	private Boolean _isFade = false;

	public VideoTask(String inputFile, JTextArea textArea, int min, int sec,
			Boolean isTitle, Boolean isPreview) {
		_inputFile = inputFile;
		_duration = sec + min * 60;
		_isTitle = isTitle;
		_isPreview = isPreview;
		_txt = textArea.getText();
		_font = textArea.getFont();
		_size = textArea.getFont().getSize();
		_color = String.format("%02x%02x%02x%02x", textArea.getForeground().getRed(),
				textArea.getForeground().getGreen(), textArea.getForeground().getBlue(),
				textArea.getForeground().getAlpha());
		getDimensions();
		_textArea = textArea;
		setUpFileLocation();
		new File(tempDir).mkdirs();
	}

	/**
	 * This method is to be called during the construction of the class. It
	 * provides the locations for the temporary files which are saved the a temp
	 * folder. The final output will be saved in the output library folder
	 */
	private void setUpFileLocation() {
		String basename = _inputFile.substring(_inputFile.lastIndexOf(File.separator) + 1);
		String filenameNoExtension = basename.substring(0, basename.lastIndexOf("."));
		String videoExtension = basename.substring(basename.lastIndexOf("."), basename.length());
		_tempLocation = tempDir + File.separator + filenameNoExtension + "[Temp]" + videoExtension;
		_fadeFile = tempDir + File.separator + filenameNoExtension + "[Fade]" + videoExtension;
		_editedLocation = Library.outputDir + File.separator 
				+ filenameNoExtension + "[VAMIX-TEXTEDITED].mpg";
	}

	@Override
	protected Void doInBackground() throws Exception {
		ProcessBuilder builder = null;
		errorState = false;

		FontFinder finder = new FontFinder();
		finder.setUp();
		String fontLocation = finder.getFontDirectory(_font.getName());

		File temp = new File(_tempLocation);
		if (temp.exists()) {
			temp.delete();
		}
		//If the input is has multiple lines
		if (_txt.contains("\n")){
			String[] text = _txt.split("\n");
			int textMid = (getAscent() + getDescent())/2;
			//Calculates the position where the first line would be printed at
			int currentPosition = getCentredYAxis(_videoSize.getHeight()) - textMid*(text.length-1);
			String escapedTxt = text[0].replaceAll("(?=[]\\[+&|!(){}^\"~*?:\\\\-])", "\\\\");
			StringBuffer input = new StringBuffer();
			input.append("[in] drawtext=fontfile='" + fontLocation + "'" + ":text="
					+ escapedTxt + ":fontsize=" + _size + ":fontcolor=" + _color + ":x=" 
					+ getCentredXAxis(text[0]) + ":y=" + currentPosition);
			for (int i=1; i<text.length; i++){
				escapedTxt = text[i].replaceAll("(?=[]\\[+&|!(){}^\"~*?:\\\\-])", "\\\\");
				currentPosition = currentPosition + textMid*2;
				input.append(" ,drawtext=fontfile='" + fontLocation + "'" + ":text=" 
						+ escapedTxt + ":fontsize=" + _size + ":fontcolor=" + _color 
						+ ":x=" + getCentredXAxis(text[i]) + ":y=" + currentPosition);
			}
			builder = new ProcessBuilder("/bin/bash", "-c", "avconv -filter_complex \"color=0x000000ff:"
					+ (int) _videoSize.getWidth() + "x" + (int) _videoSize.getHeight() + " [in]; "
					+ input.toString() + "\" -t " + _duration + " " + _tempLocation);
		}
		else {		
			/* 
			 * Escapes special characters
			 * Code taken from 
			 * (//http://stackoverflow.com/questions/13696461/replace-special-character-with-an-
			 * escape-preceded-special-character-in-java)
			 */
			String escapedTxt = _txt.replaceAll("(?=[]\\[+&|!(){}^\"~*?:\\\\-])", "\\\\");
			builder = new ProcessBuilder("/bin/bash", "-c", "avconv -filter_complex \"color=0x000000ff:"
					+ (int) _videoSize.getWidth() + "x" + (int) _videoSize.getHeight() + " [in]; "
					+ "[in] drawtext=fontfile='" + fontLocation + "'" + ":text=" + escapedTxt + ":fontsize="
					+ _size + ":fontcolor=" + _color + ":x=" + getCentredXAxis(_txt)
					+ ":y=" + getCentredYAxis(_videoSize.getHeight()) + "\" -t " + _duration + " "
					+ _tempLocation);
		}
		bashProcess(builder);

		if (_isFade){
			File fade = new File(_fadeFile);
			if (fade.exists()) {
				fade.delete();
			}
			/*
			 * Code taken from (http://superuser.com/questions/84631/how-do-i-get-the-number-of-
			 * frames-in-a-video-on-the-linux-command-line)
			 */
			ProcessBuilder processBuilder0 = new ProcessBuilder("/bin/bash", "-c", "avconv -i " + _tempLocation 
					+ " -vcodec copy " + "-f rawvideo -y /dev/null 2>&1 | tr ^M '\n' | awk '/^frame=/ "
					+ "{print $2}'|tail -n 1");
			processBuilder0.redirectErrorStream(true);
			Process process0 = processBuilder0.start();
			InputStream stdout = process0.getInputStream();
			BufferedReader stdoutBuffered = new BufferedReader(new InputStreamReader(stdout));

			String line = stdoutBuffered.readLine();
			int totalFrame = 0;
			if (line != null){
				totalFrame = Integer.parseInt(line);
			}
			ProcessBuilder processBuilder1 = new ProcessBuilder("/bin/bash", "-c",
					"avconv -i " + _tempLocation + " -vf fade=in:0:" + (int)totalFrame/3 + ",fade=out:"
							+ (int)totalFrame/3*2 + ":100 "	+ _fadeFile);
			bashProcess(processBuilder1);
		}

		// Saving Task
		if (!_isPreview) {
			String file;
			if (_isFade){
				file = _fadeFile;
			}
			else {
				file = _tempLocation;
			}
			ProcessBuilder pb1 = new ProcessBuilder("/bin/bash", "-c","avconv -i " + file + " -y " + tempDir
					+ File.separator + "text.mpg");
			pb1.redirectErrorStream(true);
			Process p1 = pb1.start();
			p1.waitFor();
			mpgCreate();
			return null;
		}
		return null;
	}

	/**
	 * Helper method that converts the input file to an .mpg so that it can be
	 * concatenated with the text video
	 * 
	 * @throws InterruptedException
	 */
	private void mpgCreate() throws InterruptedException {
		ProcessBuilder pb2 = new ProcessBuilder("/bin/bash", "-c", "avconv -i "
				+ _inputFile + " -y " + tempDir + File.separator + "input.mpg");
		pb2.redirectErrorStream(true);
		Process p2 = null;
		try {
			p2 = pb2.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		p2.waitFor();
		catMPG();
	}

	/**
	 * This helper method concatenates the text video to the input video
	 */
	private void catMPG() {
		if (_isTitle) {
			ProcessBuilder pb3 = new ProcessBuilder("/bin/bash", "-c", "cat "+ tempDir + File.separator 
					+ "text.mpg" + " " + tempDir + File.separator + "input.mpg" + " > " + _editedLocation);
			pb3.redirectErrorStream(true);
			Process p3 = null;
			try {
				p3 = pb3.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				p3.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		} else {
			ProcessBuilder pb3 = new ProcessBuilder("/bin/bash", "-c","avconv -i concat:" + tempDir 
					+ File.separator + "input.mpg" + "\\|" + tempDir + File.separator
					+ "text.mpg" + " -c copy -y " + _editedLocation);
			pb3.redirectErrorStream(true);
			bashProcess(pb3);

		}
	}

	@Override
	protected void done() {
		try {
			this.get();
			if (_isPreview) {
				if (_isFade){
					_finalOutput = _fadeFile;
				}
				else {
					_finalOutput = _tempLocation;
				}
			}
			if (errorState == false) {
				Library.getInstance().refreshTree();
				firePropertyChange("success", null, "success");
			}
		} catch (CancellationException e) {
			firePropertyChange("cancelled", null, "The text editing task was stopped!");
			File toDelete = new File(_editedLocation);
			toDelete.delete();
			Library.getInstance().refreshTree();
			return;

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

	}

	/**
	 * From AudioTask (line 123)
	 * @see AudioTask.bashProcess(ProcessBuilder)
	 * 
	 * @param builder
	 */
	private void bashProcess(ProcessBuilder builder) {
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
		String last = null;
		try {
			while ((line = stdoutBuffered.readLine()) != null) {
				last = line;
				if (isCancelled()) {
					process.destroy();
					return;
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			if (process.waitFor() != 0) {
				firePropertyChange("failure", null, last);
				errorState = true;
			}
			else {
				process.destroy();
			}
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * This method returns the directory of the final output file
	 * 
	 * @return Location of final file
	 */
	public String getOutputFileName() {
		return _editedLocation;
	}

	/**
	 * This method calculate the y-axis value required to center 
	 * the text
	 * @return ascent
	 */
	private int getCentredYAxis(double yPosition) {
		FontMetrics metrics = _textArea.getFontMetrics(_font);

		int yMidPoint = (int) (yPosition / 2);
		int ascent = metrics.getAscent();
		int descent = metrics.getDescent();

		if (ascent > descent) {
			return yMidPoint - (ascent - descent) / 2;
		} else {
			return yMidPoint + (descent - ascent) / 2;
		}
	}

	/**
	 * This method returns the ascent of the font
	 * @return ascent
	 */
	private int getAscent(){
		FontMetrics metrics = _textArea.getFontMetrics(_font);
		return metrics.getAscent();
	}

	/**
	 * This method returns the descent of the font
	 * @return descent
	 */
	private int getDescent(){
		FontMetrics metrics = _textArea.getFontMetrics(_font);
		return metrics.getDescent();
	}

	/**
	 * This method calculate the x-axis value required to center 
	 * the text
	 * @return ascent
	 */
	private int getCentredXAxis(String txt){
		FontMetrics metrics = _textArea.getFontMetrics(_font);
		int width = metrics.stringWidth(txt);

		return ((int) _videoSize.getWidth()) / 2 - (width / 2);
	}

	/**
	 * This method checks whether the text width is larger than the width of the
	 * video
	 * 
	 * @return isTextTooLong
	 */
	public Boolean isTextTooLong() {
		FontMetrics metrics = _textArea.getFontMetrics(_font);

		if (_txt.contains("\n")) {
			String[] lines = _txt.split("\n");
			for (String i : lines){
				if (metrics.stringWidth(i) > _videoSize.width){
					return true;
				}
			}
			return false;
		} else {
			if ( metrics.stringWidth(_txt) > _videoSize.width){
				return true;
			}
			return false;
		}

	}

	/**
	 * This method checks whether the total height of all the lines exceed the dimension of the video
	 * @return tooManyLines
	 */
	public Boolean tooManyLines(){
		String[] temp = _txt.split("\n");
		if (temp.length*(getDescent() + getAscent()) > _videoSize.getHeight()){
			System.out.println(temp.length*(getDescent() + getAscent()) + " > " + _videoSize.getHeight());
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * This method uses avprobe to obtain the dimensions of the video
	 * 
	 * @return Dimension of the video file
	 */
	private Dimension getDimensions() {
		ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c",
				"avprobe " + _inputFile + " 2>&1 | grep -i video");
		builder.redirectErrorStream(true);

		Pattern progressValue = Pattern.compile("(\\d*)x(\\d*)");
		StringBuffer output = new StringBuffer();
		try {
			Process process = builder.start();
			InputStream stdout = process.getInputStream();
			BufferedReader stdoutBuffered = new BufferedReader(
					new InputStreamReader(stdout));
			String line;
			while ((line = stdoutBuffered.readLine()) != null) { 
				output.append(line);
				output.append(" ");
			}
			Matcher matcher = progressValue.matcher(output);
			matcher.find();
			_videoSize = new Dimension(Integer.parseInt(matcher.group(1)),
					Integer.parseInt(matcher.group(2)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setFade(){
		_isFade = true;
	}
}