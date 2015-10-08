package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import processes.DownloadTask;
import net.miginfocom.swing.MigLayout;

/**
 * This represents the download operation pane that the user uses to download a
 * particular file. It intermediates download progress to a JProgressBar with
 * the option of the user to pause/resume downloads as they please.
 * Taken from Assignment 3 (Added comments and refactored some parts)
 *  
 * @author Harry She
 * 
 * @param args
 */
@SuppressWarnings("serial")
public class Download extends JFrame {
	private DownloadTask dl = null;

	// Input library where to store downloads
	protected Download() {
		JLabel _label = new JLabel("Please enter URL of mp3 to download:");
		final JLabel _label2 = new JLabel(
				"The file must be open source to download!");
		final JLabel _downloading = new JLabel("Downloading");
		final JTextField _URLField = new JTextField("", 30);
		final JButton _download = new JButton("Download");
		final JButton _cancel = new JButton("Cancel download");
		final JButton _pause = new JButton("Pause");
		final JCheckBox _openSourceCheck = new JCheckBox("Is it open source?");
		final JProgressBar _progress = new JProgressBar(0, 100);
		final JLabel _outputLocation = new JLabel();

		_label2.setVisible(false);
		_downloading.setVisible(false);
		_download.setEnabled(false);
		_progress.setStringPainted(true);
		_cancel.setEnabled(false);
		_pause.setEnabled(false);

		/**
		 * Custom download listener created to monitor events coming from the
		 * download task to intermediate GUI updates.
		 */
		class DownloadListener implements PropertyChangeListener {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				if ("progress".equals(e.getPropertyName())) {
					_progress.setValue((Integer) e.getNewValue());
				} else if ("cancelled".equals(e.getPropertyName())) {
					_progress.setString((String) e.getNewValue());
					return;
				} else if ("_progressLabel".equals(e.getPropertyName())) {
					_progress.setString((String) e.getNewValue());
				} else if ("reenableDownload".equals(e.getPropertyName())) {
					_download.setEnabled(true);
					_downloading.setVisible(false);
					_cancel.setEnabled(false);
					_pause.setEnabled(false);
				} else if ("failure".equals(e.getPropertyName())) {
					_progress.setString("Download Failed!");
					JOptionPane.showMessageDialog(null, e.getNewValue(),
							"Error!", JOptionPane.WARNING_MESSAGE);
				}
			}
		}

		_cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dl.cancel(true);
				if (_pause.getText() == "Pause") {
					_pause.setText("Resume");
				}
			}
		});

		// Check if the file is open source
		_openSourceCheck.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (_openSourceCheck.isSelected()) {
					_label2.setVisible(false);
					_download.setEnabled(true);
				} else {
					_label2.setVisible(true);
					_download.setEnabled(false);
				}
			}
		});

		/**
		 * When the user clicks on download, this checks if the file exists or
		 * not and subsequently if the user wishes to overwrite the file or
		 * simply resume the download from where they left off.
		 * 
		 * @author harry
		 * 
		 */
		_download.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					String _url = _URLField.getText().trim();
					String _basename = _url.substring( _url.lastIndexOf(File.separator), _url.length());
					File _file = new File(Library.inputDir + _basename);
					if (_file.exists()) {
						Object[] options = { "Resume", "Overwrite", "Cancel" };
						int action = JOptionPane .showOptionDialog( null,
								"File: " + _file + " already exists, do you wish to overwrite?",
								"ERROR: File already exists:", JOptionPane.CANCEL_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
						if (action == JOptionPane.YES_OPTION) {
							// Resume
						} else if (action == JOptionPane.NO_OPTION) {
							// Overwrite
							_file.delete();
						} else {
							// Cancel
							return;
						}
					}

					// Only download if open source
					if (_openSourceCheck.isSelected()) {
						_outputLocation.setText(_url.substring(_url.lastIndexOf(File.separator) + 1)
								+ " will be downloaded to " + Library.inputDir);
						dl = new DownloadTask(_url);
						dl.addPropertyChangeListener(new DownloadListener());
						dl.execute();
						_downloading.setVisible(true);
						_downloading.setText("Downloading...");
						_download.setEnabled(false);
						_pause.setText("Pause");
						_cancel.setEnabled(true);
						_pause.setEnabled(true);
					}
				} catch (Exception exp) {
					JOptionPane.showMessageDialog(null, "Please enter a valid URL", "Error!",
							JOptionPane.WARNING_MESSAGE);
				}
			}
		});

		// Pause button to "pause" download, but really just cancels the process
		_pause.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (_pause.getText() == "Pause") {
					dl.cancel(true);
					_pause.setText("Resume");
				} else {
					_pause.setText("Pause");
					String _url = _URLField.getText();
					dl = new DownloadTask(_url);
					dl.addPropertyChangeListener(new DownloadListener());
					dl.execute();
					_downloading.setVisible(true);
					_downloading.setText("Downloading...");
				}
			}
		});

		setLayout(new MigLayout("center"));

		add(_label, "wrap, align center, gapbottom 30 ");
		add(_URLField, "wrap, align center, gapbottom 30");
		add(_openSourceCheck, "wrap, align center, gapbottom 10");
		add(_label2, "wrap, align center, gapbottom 10");
		add(_download, "height 100, width 500, wrap, align center");
		add(_downloading, "wrap, align center, gaptop 20");
		add(_outputLocation, "wrap, gapbottom 5, align center");
		add(_progress, "gapbottom 30, width 500, height 50, wrap, align center");
		add(_pause, "height 50, width 200, split 2, align center, gapright 40");
		add(_cancel, "height 50, width 200");
	}
}