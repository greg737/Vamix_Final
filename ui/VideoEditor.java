package ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;
import processes.FontFinder;
import processes.VideoTask;
import javax.swing.border.BevelBorder;
import javax.swing.JCheckBox;

/**
 * Class that deals with the text editing of videos. Ie. The creation of title
 * and credits.
 * 
 * This includes the ability to preview edited text and resume working from
 * where you were on this file last time.
 * 
 * @author Harry She and Greggory Tan
 *
 */
@SuppressWarnings("serial")
public class VideoEditor extends JPanel {
	private JCheckBox _checkBox;
	private String _inputFile;
	protected MiniPlayback _originalPlayback;
	protected MiniPlayback _editedPlayback;
	private JButton _colourButton;
	private JButton _saveButton;
	JButton _previewButton;
	private JComboBox<String> _comboBox;
	private JSpinner _fontSpinner;
	private JSpinner _minSpinner;
	private JSpinner _secSpinner;
	private File _cfgFile;
	private ButtonGroup _group;
	final JTextArea _textArea = new JTextArea("");
	private FontFinder _fonts = new FontFinder();

	JProgressBar _progressBar;
	protected VideoTask videoTask;

	/**
	 * Create the panel.
	 * 
	 */
	protected VideoEditor() {
		setLayout(new MigLayout("", "[400px,grow][400px,grow]","[400px,grow][250px,grow]"));

		// Original video is displayed in this panel
		JPanel player = new JPanel(new MigLayout(""));
		player.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Original Video",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		_originalPlayback = new MiniPlayback();
		player.add(_originalPlayback);
		add(player, "cell 0 0,grow");

		// Edited version is shown here
		JPanel preview = new JPanel(new MigLayout(""));
		preview.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Text Preview",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		_editedPlayback = new MiniPlayback();
		preview.add(_editedPlayback);
		add(preview, "cell 1 0,grow");

		JPanel textPanel = new JPanel();
		textPanel.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null), 
				"Text Editing", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(textPanel, "cell 0 1 2 1,grow");
		textPanel.setLayout(new MigLayout("", "[200px,grow][200px,grow][200px,grow][200px,grow]", "[40px,grow][50px,grow][50px,grow][50px,grow][50px,grow]"));
		
		JLabel lblAddText = new JLabel("Add Text:");
		textPanel.add(lblAddText, "cell 0 0");

		JScrollPane scrollPane = new JScrollPane();
		textPanel.add(scrollPane, "cell 0 1 1 4,grow");
		_textArea.setLineWrap(true);
		_textArea.setWrapStyleWord(true);
		_textArea.setBackground(Color.black);
		scrollPane.setViewportView(_textArea);

		JLabel fontSizeLabel = new JLabel("Font Size");
		fontSizeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		textPanel.add(fontSizeLabel, "flowx,cell 1 0,alignx center");

		SpinnerModel fontSpinnerModel = new SpinnerNumberModel(25, 20, 50, 1);
		_fontSpinner = new JSpinner(fontSpinnerModel);
		((JSpinner.DefaultEditor) _fontSpinner.getEditor()).getTextField().setEditable(false);
		textPanel.add(_fontSpinner, "cell 1 0,alignx center");

		JLabel durationLabel = new JLabel("Duration (MM:SS):");
		durationLabel.setHorizontalAlignment(SwingConstants.CENTER);
		textPanel.add(durationLabel, "cell 2 0,alignx center");

		JLabel typeLabel = new JLabel("Type:");
		textPanel.add(typeLabel, "cell 3 0,alignx center");

		_colourButton = new JButton("Choose Text Colour");
		textPanel.add(_colourButton, "cell 1 2 1 2,alignx center");

		_checkBox = new JCheckBox("Fade Effect");
		textPanel.add(_checkBox, "cell 3 2 1 2,alignx center,aligny center");

		_previewButton = new JButton("Preview");
		textPanel.add(_previewButton, "flowx,cell 1 4,alignx center");

		_progressBar = new JProgressBar();
		textPanel.add(_progressBar, "cell 2 4,alignx center,grow");

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					videoTask.cancel(true);
				} catch (NullPointerException ne) {
				}
			}
		});
		textPanel.add(cancelButton, "cell 3 4,alignx center,growy");

		JLabel fontLabel = new JLabel("Font:");
		textPanel.add(fontLabel, "flowx,cell 1 1,alignx center");
		_fonts.setUp();
		List<String> list = new ArrayList<String>();
		for (Font font : _fonts.getFontList()) {
			list.add(font.getName());
		}
		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>(list.toArray(new String[0]));
		_comboBox = new JComboBox<String>(model);
		textPanel.add(_comboBox, "cell 1 1,alignx center");

		SpinnerModel minSpinnerModel = new SpinnerNumberModel(0, 0, 59, 1);
		_minSpinner = new JSpinner(minSpinnerModel);
		((JSpinner.DefaultEditor) _minSpinner.getEditor()).getTextField().setEditable(false);
		textPanel.add(_minSpinner, "cell 2 1,alignx center");

		SpinnerModel secSpinnerModel = new SpinnerNumberModel(0, 0, 59, 1);
		_secSpinner = new JSpinner(secSpinnerModel);
		((JSpinner.DefaultEditor) _secSpinner.getEditor()).getTextField().setEditable(false);
		textPanel.add(_secSpinner, "cell 2 1,alignx center");

		JRadioButton selectTitle = new JRadioButton("Title");
		textPanel.add(selectTitle, "flowx,cell 3 1,alignx center");
		JRadioButton selectCredits = new JRadioButton("Credits");
		textPanel.add(selectCredits, "cell 3 1,alignx center");
		_group = new ButtonGroup();
		_group.add(selectTitle);
		_group.add(selectCredits);
		_group.setSelected(selectTitle.getModel(), true);

		_saveButton = new JButton("Save");
		textPanel.add(_saveButton, "cell 1 4,alignx center");

		setUpListener();
	}

	/**
	 * This method sets up the listeners for the components
	 */
	public void setUpListener() {
		_saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Boolean isTitle = true;
				for (Enumeration<AbstractButton> buttons = _group.getElements(); buttons.hasMoreElements();) {
					AbstractButton button = buttons.nextElement();
					if (button.isSelected() && !button.getText().equals("Title")) {
						isTitle = false;
						break;
					}
				}
				videoTask = new VideoTask(_inputFile, _textArea, ((Integer) _minSpinner.getValue()),
						((Integer) _secSpinner.getValue()), isTitle, false);
				if (validInput(videoTask)){
					File newFile = new File(videoTask.getOutputFileName());
					if (newFile.exists()) {
						Object[] options = { "Overwrite", "Cancel" };
						int action = JOptionPane.showOptionDialog(null, "File: "+ newFile + 
								" already exists, do you wish to overwrite?",
								"ERROR: File already exists:",JOptionPane.CANCEL_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
						if (action == JOptionPane.YES_OPTION) {
							// Overwrite
							newFile.delete();
						} else if (action == JOptionPane.NO_OPTION) {
							// Cancel
							return;
						}
					}
					videoTask.execute();
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					_progressBar.setIndeterminate(true);
					videoTask.addPropertyChangeListener(new PropertyChangeListener() {
						public void propertyChange(PropertyChangeEvent evt) {
							if ("failure".equals(evt.getPropertyName())) {
								setCursor(Cursor.getDefaultCursor());
								_progressBar.setIndeterminate(false);
								JOptionPane.showMessageDialog(null,evt.getNewValue(),"Error!", JOptionPane.WARNING_MESSAGE);
							} else if ("success".equals(evt.getPropertyName())) {
								_progressBar.setIndeterminate(false);
								_progressBar.setValue(100);
								setCursor(Cursor.getDefaultCursor());
								JOptionPane.showMessageDialog(null,"Text editing of file from  "
										+ _inputFile + " to the output library was successful!",
										"Text editing Successful", JOptionPane.INFORMATION_MESSAGE);
								_progressBar.setValue(0);
							} else if ("cancelled".equals(evt.getPropertyName())) {
								setCursor(Cursor.getDefaultCursor());
								_progressBar.setIndeterminate(false);
								JOptionPane.showMessageDialog(null,evt.getNewValue(),"Cancelled!", JOptionPane.WARNING_MESSAGE);
							}
						}
					});
				}
			}
		});

		_previewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Boolean isTitle = true;
				for (Enumeration<AbstractButton> buttons = _group.getElements(); buttons.hasMoreElements();) {
					AbstractButton button = buttons.nextElement();
					if (button.isSelected() && !button.getText().equals("Title")) {
						isTitle = false;
						break;
					}
				}

				videoTask = new VideoTask(_inputFile, _textArea,
						((Integer) _minSpinner.getValue()),
						((Integer) _secSpinner.getValue()), isTitle, true);

				if (validInput(videoTask)){
					if (_checkBox.isSelected()){
						videoTask.setFade();
					}
					videoTask.execute();
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					videoTask.addPropertyChangeListener(new PropertyChangeListener() {
						@Override
						public void propertyChange(PropertyChangeEvent evt) {
							if ("failure".equals(evt.getPropertyName())) {
								setCursor(Cursor.getDefaultCursor());
							} else if ("success".equals(evt.getPropertyName())) {
								setCursor(Cursor.getDefaultCursor());
								startPreview(videoTask._finalOutput);
							}
						}
					});
				}

			}
		});


		_fontSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				float newSize = ((Integer) ((JSpinner) e.getSource()).getValue()).floatValue();
				Font newFont = _textArea.getFont().deriveFont(newSize);
				_textArea.setFont(newFont);
				writeSetting(null, newFont.getSize(), null, null, null);
			}
		});

		_minSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int newMin = ((Integer) ((JSpinner) e.getSource()).getValue());
				writeSetting(null, null, null, newMin, null);
			}
		});

		_secSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int newSec = ((Integer) ((JSpinner) e.getSource()).getValue());
				writeSetting(null, null, null, null, newSec);
			}
		});

		_colourButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Color newCol = JColorChooser.showDialog(null, "Choose font colour", _textArea.getForeground());
				if (newCol != null){
					_textArea.setForeground(newCol);
					writeSetting(null, null, String.format("%02x",newCol.getAlpha())+
							SubtitleEditor.convertToHexRGB(newCol), null, null);
				}
			}
		});

		_comboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					Font newFont = _fonts.searchFont(e.getItem().toString());
					float newSize = _textArea.getFont().getSize();
					_textArea.setFont(newFont.deriveFont(newSize));
					writeSetting(newFont.getName(), null, null, null, null);
				}
			}
		});

		_textArea.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				writeText(_textArea.getText());
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				writeText(_textArea.getText());
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}
		});
	}

	/**
	 * This method checks if the duration and size of the 
	 * text is valid
	 * @param task - VideoTask object
	 * @return true if task is valid
	 */
	private Boolean validInput(VideoTask task){
		if (((Integer) _minSpinner.getValue()) == 0&& ((Integer) _secSpinner.getValue()) == 0) {
			JOptionPane.showMessageDialog(null,"Please enter a valid duration.");
			return false;
		} else if (task.isTextTooLong()) {
			JOptionPane.showMessageDialog(null, "Line too long.");
			return false;
		}
		else if (task.tooManyLines()){
			JOptionPane.showMessageDialog(null, "Too many lines.");
			return false;
		} 
		else {
			return true;
		}

	}

	/**
	 * This method is to be called when there is change to the settings like
	 * font, font size, color and duration. If there is no change for that
	 * field, then a null should be placed for that argument. This method also
	 * keeps the old text if there is any
	 * @param font - name of font used
	 * @param size - size of font
	 * @param color - RGBA value of color used
	 * @param min - minute duration 
	 * @param sec - second duration
	 */
	private void writeSetting(String font, Integer size, String color, Integer min, Integer sec) {
		String[] oldSetting = readSetting(true);
		String[] newSetting = new String[5];
		StringBuffer firstLine = new StringBuffer();
		// Font Name
		if (font != null && !font.equals("")) {
			newSetting[0] = font;
		} else {
			newSetting[0] = oldSetting[0];
		}
		// Font Size
		if (size != null) {
			newSetting[1] = size + "";
		} else {
			newSetting[1] = oldSetting[1];
		}
		// Font Color (In Hexadecimal)
		if (color != null && !color.equals("")) {
			newSetting[2] = color;
		} else {
			newSetting[2] = oldSetting[2];
		}
		// Duration minutes
		if (min != null) {
			newSetting[3] = min + "";
		} else {
			newSetting[3] = oldSetting[3];
		}
		// Duration seconds
		if (sec != null) {
			newSetting[4] = sec + "";
		} else {
			newSetting[4] = oldSetting[4];
		}

		for (String i : newSetting) {
			firstLine.append(i);
			firstLine.append("@");
		}
		try {
			String oldText = readText().toString();
			PrintWriter out = new PrintWriter(_cfgFile);
			out.println(firstLine.substring(0, firstLine.length() - 1));
			out.println(oldText);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method is to be called when there is change to the text by the user.
	 * This method maintains the old settings.
	 * 
	 * @param text - text that is to be added to the video
	 */
	private void writeText(String text) {
		String[] setting = readSetting(false);

		try {
			PrintWriter out = new PrintWriter(_cfgFile);
			out.println(setting[0]);
			out.println(text);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method reads the first line which contains the fonts, size, color,
	 * duration and type. If the Boolean argument is true then the first line
	 * would be split. Else the whole first line would be saved in the first
	 * array.
	 * 
	 * @param needSplit - true if need to be split
	 * @return The settings in the cfg file
	 */
	private String[] readSetting(Boolean needSplit) {
		String[] settings = new String[5];
		try {
			BufferedReader br = new BufferedReader(new FileReader(_cfgFile));
			String firstLine = br.readLine();
			if (firstLine != null) {
				if (needSplit) {
					settings = firstLine.split("@");
				} else {
					settings[0] = firstLine;
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return settings;
	}

	/**
	 * This method reads the text saved in the .cfg file
	 * 
	 * @return Text in the cfg file
	 */
	private String readText() {
		StringBuffer text = new StringBuffer();

		try {
			BufferedReader br = new BufferedReader(new FileReader(_cfgFile));
			String currentLine = br.readLine(); // To skip first line
			while ((currentLine = br.readLine()) != null) {
				text.append(currentLine);
				text.append("\n");
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text.toString().trim();
	}

	/**
	 * This method sets up the JTextArea, JSpinners and JComboBox.
	 */
	public void setUpComponents() {
		String basename = _inputFile.substring(_inputFile.lastIndexOf(File.separator) + 1);
		String filenameNoExtension = basename.substring(0,basename.lastIndexOf("."));
		String output = Library.outputDir + File.separator+ filenameNoExtension + ".cfg";
		_cfgFile = new File(output);
		if (!_cfgFile.exists()) {
			try {
				PrintWriter out = new PrintWriter(_cfgFile);
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			writeSetting("Garuda", 25, "ffffffff", 0, 0);
		}

		// Loads font, size and color from file
		String[] settings = readSetting(true);
		// Checks if there is 5 arguments. If it doesn't it would reset the .cfg file
		if (settings.length < 5) {
			writeSetting("Garuda", 25, "ffffffff", 0, 0);
			settings = readSetting(true);
		}

		// Input values into _textArea
		_textArea.setFont(new Font(settings[0], Font.PLAIN, Integer.parseInt(settings[1]))); 
		// fonts
		if (settings[2].length() == 6) {
			_textArea.setForeground(Color.decode(settings[2]));
		} else {
			int r = Integer.parseInt(settings[2].substring(0, 2), 16);
			int g = Integer.parseInt(settings[2].substring(2, 4), 16);
			int b = Integer.parseInt(settings[2].substring(4, 6), 16);
			int a = Integer.parseInt(settings[2].substring(6, 8), 16);
			Color newColor = new Color(r, g, b, a);
			_textArea.setForeground(newColor);
		}
		// Set up fonts spinner
		_comboBox.setSelectedIndex(((DefaultComboBoxModel<String>) _comboBox
				.getModel()).getIndexOf(_textArea.getFont().getName()));
		// Set up font size spinner
		_fontSpinner.setValue(_textArea.getFont().getSize());

		// Set up min spinner
		_minSpinner.setValue(Integer.parseInt(settings[3]));
		// Set up sec spinner
		_secSpinner.setValue(Integer.parseInt(settings[4]));

		// Set up text
		String text = readText();
		if (!text.isEmpty()) {
			_textArea.setText(text.toString());
		}
	}

	/**
	 * This method sets the input file for the original
	 * playback component.
	 * 
	 * @param inputFile
	 */
	public void setInputFile(String inputFile) {
		_inputFile = inputFile;
		setUpComponents();
		_originalPlayback.loadFile(inputFile);
	}
	
	/**
	 * This method is to be called when the preview video created
	 * 
	 * @param previewFile
	 */
	public void startPreview(String previewFile) {
		_editedPlayback.loadFile(previewFile);
		_editedPlayback._playPauseButton.doClick();
	}

	/**
	 * This method stops all the players in VideoEditor
	 */
	public void stopAllPlayers() {
		_originalPlayback.stopPlayer();
		_editedPlayback.stopPlayer();
	}
}