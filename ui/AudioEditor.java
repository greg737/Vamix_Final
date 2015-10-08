package ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import processes.AudioTask;
import processes.FileChecker;
import net.miginfocom.swing.MigLayout;

import javax.swing.JProgressBar;
import javax.swing.border.BevelBorder;

/**
 * This is the Audio Editor pane which is responsible for all audio manipulation
 * operations for this VAMIX application. This includes the ripping of audio
 * from a video, the replacing of an audio track on a video with another audio
 * track that the user selects, as well as an option to overlay another layer of
 * audio onto a video file. The pane also includes a small preview playback
 * instance of the current video file being edited as well as this file's
 * details.
 * Taken from Assignment 3 (Added comments and refactored some parts)
 * 
 * @author Harry She
 *
 */
@SuppressWarnings("serial")
public class AudioEditor extends JPanel {
	private String _inputFile;
	private JLabel _currentFileLabel;
	private String _mergeAudio;
	private String _replaceFile;
	private JTextArea _details;
	MiniPlayback _miniPlayback;

	private AudioTask replace;
	private AudioTask merge;
	private AudioTask strip;

	private String _outputLocationStrippedAudio;
	private String _outputLocationStrippedVideo;
	private String _outputLocationOverlayedVideo;
	private String _outputLocationReplacedAudioVideo;

	/**
	 * Create the Audio Editor panel.
	 */
	protected AudioEditor() {

		/*
		 * Setting up the AudioEditor panels with miglayout settings
		 */
		setLayout(new MigLayout("", "[425px,grow][425px,grow 30]", "[300px,grow][60px,grow][240px,grow]"));
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null), 
				"Audio file details:", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(panel, "cell 0 2,grow");
		panel.setLayout(new MigLayout("", "[grow]", "[grow]"));

		JPanel panel_1 = new JPanel();
		add(panel_1, "cell 1 0 1 3,grow");
		panel_1.setLayout(new MigLayout("", "[grow]", "[100px,grow][166px,grow][166px,grow][166px,grow]"));

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null), 
				"Strip audio", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_1.add(panel_2, "cell 0 1,grow");
		panel_2.setLayout(new MigLayout("", "[grow][grow][grow][grow]", "[grow][grow][grow]"));

		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null), 
				"Overlay layered audio", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_1.add(panel_3, "cell 0 2,grow");
		panel_3.setLayout(new MigLayout("", "[30px,grow][grow][grow][30px,grow]",
				"[grow][grow][grow][grow][grow]"));
		final JLabel overlayLabel = new JLabel("Audio to overlay:");
		overlayLabel.setMaximumSize(new Dimension(370, 30));
		panel_3.add(overlayLabel, "cell 0 0 4 1,alignx center");

		JPanel panel_4 = new JPanel();
		panel_4.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null),
				"Replace existing audio", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_1.add(panel_4, "cell 0 3,grow");
		panel_4.setLayout(new MigLayout("", "[grow][grow][grow][grow]",
				"[grow][grow][grow][grow][grow]"));

		final JLabel rep = new JLabel("Replacement audio:");
		rep.setMaximumSize(new Dimension(370, 30));
		panel_4.add(rep, "cell 0 0 4 1,alignx center");

		JPanel panel_5 = new JPanel();
		panel_5.setBorder(new TitledBorder(new EtchedBorder( EtchedBorder.LOWERED, null, null),
				"Current File being edited:", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(panel_5, "cell 0 1,growx");
		panel_5.setLayout(new MigLayout("", "[grow]", "[grow]"));
		_currentFileLabel = new JLabel("");
		_currentFileLabel.setMaximumSize(new Dimension(400, 30));
		panel_5.add(_currentFileLabel, "cell 0 0");

		JPanel panel_6 = new JPanel();
		panel_6.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null),
				"Change current video file", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_1.add(panel_6, "cell 0 0,grow");
		panel_6.setLayout(new MigLayout("", "[50px,grow][100px,grow][50px,grow]", "[100px]"));

		JPanel panel_7 = new JPanel();
		panel_7.setBorder(new TitledBorder(new EtchedBorder( EtchedBorder.LOWERED, null, null), "Preview",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(panel_7, "cell 0 0,grow");
		panel_7.setPreferredSize(new Dimension(425, 300));
		panel_7.setLayout(new MigLayout("", "[425px,grow]", "[300px,grow]"));
		_miniPlayback = new MiniPlayback();
		panel_7.add(_miniPlayback, "cell 0 0,alignx left,aligny top, grow");
		/*
		 * End of layout setup
		 */

		// Make the details Text area
		_details = new JTextArea();
		_details.setEditable(false);
		_details.setWrapStyleWord(true);
		JScrollPane _detailsScrollPane = new JScrollPane(_details);
		_detailsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		_detailsScrollPane.setPreferredSize(new Dimension(200, 300));
		panel.add(_detailsScrollPane, "cell 0 0,grow");

		/*
		 * Option for the user to change the current input video file
		 */
		JButton btnChange = new JButton("Change file");
		btnChange.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser _chooseInputFile = new JFileChooser(Library.inputDir);
				_chooseInputFile.setFileFilter(new FileNameExtensionFilter("Video files", Library._validVideoOnly));
				int returnValue = _chooseInputFile.showOpenDialog(null);

				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File inputFile = _chooseInputFile.getSelectedFile();
					FileChecker fc= new FileChecker(inputFile.toString());
					//If the file doesn't contain an audio stream, complain.
					if (!fc.checkAVFile("audio")) {
						JOptionPane .showMessageDialog( null, "Warning: This video file has no audio stream!",
								"Warning: No audio stream!", JOptionPane.WARNING_MESSAGE);
					} 
					_miniPlayback.stopPlayer();
					setInputFile(inputFile.toString());
				}

			}
		});
		panel_6.add(btnChange, "cell 1 0,alignx center,aligny center,grow");

		/*
		 * Strip Button and listener to create audiotask
		 */
		JLabel stripLabel = new JLabel("Strips audio to Output Library");
		panel_2.add(stripLabel, "cell 1 0 2 1,alignx center,aligny center");

		final JProgressBar stripProgressBar = new JProgressBar();
		panel_2.add(stripProgressBar, "cell 1 1 2 1,grow");

		JButton stripAudioButton = new JButton("Strip audio");
		panel_2.add(stripAudioButton, "cell 1 2,grow");

		JButton stripCancel = new JButton("Cancel");
		stripCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				strip.cancel(true);
				setCursor(Cursor.getDefaultCursor());
				stripProgressBar.setIndeterminate(false);
			}
		});

		panel_2.add(stripCancel, "cell 2 2,grow");
		stripAudioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String[] options = {"Get audio only", "Get video only", "Cancel"};
				String[] outputLocationStrip = {_outputLocationStrippedAudio , _outputLocationStrippedVideo};
				String cmd="";
				String outString="";

				int action = JOptionPane .showOptionDialog( null,
						"Would you like to save the stripped audio or the video that has will have its audio removed?",
						"Choose what to save:", JOptionPane.CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
						options, options[0]);
				if (action == JOptionPane.YES_OPTION) {
					cmd = "STRIP-audio";
					outString = outputLocationStrip[0];
				} else if (action == JOptionPane.NO_OPTION) {
					cmd = "STRIP-video";
					outString = outputLocationStrip[1];
				} else {
					//cancel
					return;
				}

				//Check if file exists first
				File _file = new File(outString);
				if (_file.exists()) {
					Object[] options2 = {"Overwrite", "Cancel" };
					int action2 = JOptionPane .showOptionDialog( null, "File: " + _file
							+ " already exists, do you wish to overwrite?", "ERROR: File already exists:",
							JOptionPane.CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options2, options2[1]);
					if (action2 == JOptionPane.YES_OPTION) {
						// Overwrite
						_file.delete();
					} else if (action2 == JOptionPane.NO_OPTION) {
						// Cancel
						return;
					}
				}

				strip = new AudioTask(_inputFile, outString, cmd);
				stripProgressBar.setIndeterminate(true);
				strip.addPropertyChangeListener(new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						if ("failure".equals(evt.getPropertyName())) {
							setCursor(Cursor.getDefaultCursor());
							stripProgressBar.setIndeterminate(false);
							JOptionPane.showMessageDialog(null, evt.getNewValue(), "Error!",
									JOptionPane.WARNING_MESSAGE);
						} else if ("success".equals(evt.getPropertyName())) {
							stripProgressBar.setIndeterminate(false);
							stripProgressBar.setValue(100);
							setCursor(Cursor.getDefaultCursor());
							JOptionPane .showMessageDialog( null, "Stripping of file from  "
									+ _inputFile + " to the output library was successful!", "Strip Successful",
									JOptionPane.INFORMATION_MESSAGE);
							stripProgressBar.setValue(0);
						}else if ("cancelled".equals(evt.getPropertyName())) {
							setCursor(Cursor.getDefaultCursor());
							stripProgressBar.setIndeterminate(false);
							JOptionPane .showMessageDialog( null, evt.getNewValue(),
									"Cancelled!", JOptionPane.WARNING_MESSAGE);
						}	
					}
				});
				strip.execute();
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			}
		});

		/*
		 * Browse Button and listener to import audio tracks to overlay.
		 */
		JButton bt = new JButton("Browse audio track to overlay");
		bt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser _chooseInputFile = new JFileChooser(Library.inputDir);
				_chooseInputFile.setFileFilter(new FileNameExtensionFilter(
						"Audio and Video", Library._validExtensions));
				int returnValue = _chooseInputFile.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File inputFile = _chooseInputFile.getSelectedFile();
					_mergeAudio = inputFile.getAbsolutePath();
					overlayLabel.setText("Audio to overlay: " + inputFile.getAbsolutePath());
				}
			}

		});
		panel_3.add(bt, "cell 1 1 2 1,alignx center,grow");

		final JProgressBar overlayProgressBar = new JProgressBar();
		panel_3.add(overlayProgressBar, "cell 1 2 2 1,grow");
		JButton btnMergeAudio = new JButton("Merge Audio Layers");
		panel_3.add(btnMergeAudio, "cell 1 4,alignx center,grow");

		JButton mergeCancel = new JButton("Cancel");
		panel_3.add(mergeCancel, "cell 2 4,grow");
		mergeCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				merge.cancel(true);
				setCursor(Cursor.getDefaultCursor());
				overlayProgressBar.setIndeterminate(false);
			}
		});

		/*
		 * Merge Button and listener to create an audiotask to merge the audio
		 * layers together.
		 */
		btnMergeAudio.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//Check if file exists first
				File _file = new File(_outputLocationOverlayedVideo);
				if (_file.exists()) {
					Object[] options = {"Overwrite", "Cancel" };
					int action = JOptionPane.showOptionDialog(null,
							"File: "+ _file + " already exists, do you wish to overwrite?",
							"ERROR: File already exists:", JOptionPane.CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
					if (action == JOptionPane.YES_OPTION) {
						// Overwrite
						_file.delete();
					} else if (action == JOptionPane.NO_OPTION) {
						// Cancel
						return;
					}
				}
				merge = new AudioTask(_inputFile, _outputLocationOverlayedVideo, _mergeAudio, "MERGE");
				overlayProgressBar.setIndeterminate(true);
				merge.addPropertyChangeListener(new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						if ("failure".equals(evt.getPropertyName())) {
							setCursor(Cursor.getDefaultCursor());
							overlayProgressBar.setIndeterminate(false);
							JOptionPane.showMessageDialog(null, evt.getNewValue(), "Error!",
									JOptionPane.WARNING_MESSAGE);
						} else if ("success".equals(evt.getPropertyName())) {
							setCursor(Cursor.getDefaultCursor());
							overlayProgressBar.setIndeterminate(false);
							overlayProgressBar.setValue(100);
							JOptionPane .showMessageDialog( null,
									"Merging of audio layers to the output library was successful!",
									"Merge Successful", JOptionPane.INFORMATION_MESSAGE);
							overlayProgressBar.setValue(0);
						} else if ("cancelled".equals(evt.getPropertyName())) {
							setCursor(Cursor.getDefaultCursor());
							overlayProgressBar.setIndeterminate(false);
							JOptionPane .showMessageDialog( null, evt.getNewValue(),
									"Cancelled!", JOptionPane.WARNING_MESSAGE);
						}	
					}
				});
				merge.execute();
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			}
		});

		/*
		 * Choose audio button to choose import an audio file that is to replace
		 * the current video's audio track
		 */
		JButton chooseAudio = new JButton("Choose audio file");
		panel_4.add(chooseAudio, "cell 1 1 2 1,alignx center,grow");
		chooseAudio.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser _chooseInputFile = new JFileChooser( Library.inputDir);
				_chooseInputFile.setFileFilter(new FileNameExtensionFilter(
						"Audio and Video", Library._validExtensions));
				int returnValue = _chooseInputFile.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File inputFile = _chooseInputFile.getSelectedFile();
					_replaceFile = (inputFile.getAbsolutePath());
					rep.setText("Replacement audio: " + inputFile.getAbsolutePath());
				}

			}
		});

		final JProgressBar replaceProgressBar = new JProgressBar();
		panel_4.add(replaceProgressBar, "cell 1 2 2 1,grow");
		JButton btnReplace = new JButton("Replace");
		panel_4.add(btnReplace, "cell 1 4,alignx center,grow");

		JButton replaceCancel = new JButton("Cancel");
		panel_4.add(replaceCancel, "cell 2 4,alignx center,grow");
		replaceCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				replace.cancel(true);
				setCursor(Cursor.getDefaultCursor());
				replaceProgressBar.setIndeterminate(false);
			}
		});
		/*
		 * Replace button to create audio task to replace existed audio track
		 * with another track all in the background
		 */
		btnReplace.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File _file = new File(_outputLocationReplacedAudioVideo);
				if (_file.exists()) {
					Object[] options = {"Overwrite", "Cancel" };
					int action = JOptionPane .showOptionDialog( null, "File: " + _file
							+ " already exists, do you wish to overwrite?", "ERROR: File already exists:",
							JOptionPane.CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
					if (action == JOptionPane.YES_OPTION) {
						// Overwrite
						_file.delete();
					} else if (action == JOptionPane.NO_OPTION) {
						// Cancel
						return;
					}
				}

				replace = new AudioTask(_inputFile, _outputLocationReplacedAudioVideo, _replaceFile, "REPLACE");
				replaceProgressBar.setIndeterminate(true);
				replace.addPropertyChangeListener(new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						if ("failure".equals(evt.getPropertyName())) {
							setCursor(Cursor.getDefaultCursor());
							replaceProgressBar.setIndeterminate(false);
							JOptionPane.showMessageDialog(null, evt.getNewValue(), "Error!",
									JOptionPane.WARNING_MESSAGE);
						} else if ("success".equals(evt.getPropertyName())) {
							setCursor(Cursor.getDefaultCursor());
							replaceProgressBar.setIndeterminate(false);
							replaceProgressBar.setValue(100);
							JOptionPane .showMessageDialog( null, "Replacement of audio from: "
									+ _replaceFile + " to the output library was successful!",
									"Replacement of audio Successful", JOptionPane.INFORMATION_MESSAGE);
							replaceProgressBar.setValue(0);
						}else if ("cancelled".equals(evt.getPropertyName())) {
							setCursor(Cursor.getDefaultCursor());
							replaceProgressBar.setIndeterminate(false);
							JOptionPane .showMessageDialog( null, evt.getNewValue(), "Cancelled!",
									JOptionPane.WARNING_MESSAGE);
						}	
					}
				});
				replace.execute();
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			}
		});

	}

	/**
	 * Mutator method for setting the input file of this audio editor (current
	 * file being edited) by the library class. It also assigns values to the
	 * appropriate name fields for the locations and basenames of the input file
	 * as well as the details of that file. It then also starts the small
	 * preview player with this input file.
	 * 
	 * @param _inputFile
	 */
	public void setInputFile(String _inputFile) {
		this._inputFile = _inputFile;
		String basename = _inputFile.substring(_inputFile.lastIndexOf(File.separator) + 1);
		_currentFileLabel.setText(basename);
		String filenameNoExtension = basename.substring(0,basename.lastIndexOf("."));
		String Extension = basename.substring(basename.lastIndexOf("."));
		_outputLocationStrippedAudio = Library.outputDir + File.separator
				+ filenameNoExtension + "[STRIPPED-VAMIX].mp3";
		_outputLocationStrippedVideo= Library.outputDir + File.separator
				+ filenameNoExtension + "[STRIPPED-VAMIX]" + Extension;
		_outputLocationOverlayedVideo = Library.outputDir + File.separator
				+ filenameNoExtension + "[MERGED_AUDIO-VAMIX]" + Extension;
		_outputLocationReplacedAudioVideo= Library.outputDir + File.separator
				+ filenameNoExtension + "[REPLACED_AUDIO-VAMIX]" + ".mp4";
		_details.setText(Library.getDetails(_inputFile).toString());
		_miniPlayback.loadFile(_inputFile);
	}
}