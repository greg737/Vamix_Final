package ui;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.apache.commons.io.FileUtils;

import processes.VideoTask;
import net.miginfocom.swing.MigLayout;

/**
 * This the main class of the VAMIX Application. It is responsible for setting
 * the look and feel of the GUI, creating and showing the GUI and also is
 * responsible for the creation of new tabs.
 * Taken from Assignment 3 (Added comments and refactored some parts)
 * 
 * @author Harry She
 * 
 */
/* BoilerPlate code of JFileChooser, ProcessBuilder, JColorChooser from Java API and example
 * tutorial code
 */
@SuppressWarnings("serial")
public class Main extends JFrame {
	public static JTabbedPane _tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	public static JMenuItem importFile = new JMenuItem();
	public static JMenuItem editAudio;
	public static JMenuItem editVideo;
	public static JMenuItem subtitle;
	public static JMenuItem download;
	public static JMenuItem play;
	public static Main window;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					// Set look and feel to Nimbus
					for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
						if ("Nimbus".equals(info.getName())) {
							UIManager.setLookAndFeel(info.getClassName());
							break;
						}
					}
				} catch (Exception e) {
					try {
						UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
					} catch (Exception ex) {
					}
				}
				try {
					window = new Main();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Main() {
		pack();
		setTitle("VAMIX");
		setVisible(true);
		initialize();

	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		// Set up frame
		setBounds(100, 100, 1000, 700);
		setMinimumSize(new Dimension(1000, 700));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//Listener to delete all the files in the temp folder
		addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e){
				File temp = new File(VideoTask.tempDir);
				if (temp.exists()){
					for (File f : temp.listFiles()){
						f.delete();
					}
				}
				e.getWindow().dispose();
			}
		});
		setLocationRelativeTo(null);
		getContentPane().setLayout(new MigLayout("", "[grow]", "[grow]"));

		// Set up main library tab
		getContentPane().add(_tabbedPane, "cell 0 0,aligny top,grow");

		// Menu Bar setup
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		JMenu menuFile = new JMenu("File");
		menuBar.add(menuFile);
		JMenu edit = new JMenu("Edit");
		menuBar.add(edit);
		JMenu menuPlay = new JMenu("Play");
		menuBar.add(menuPlay);

		importFile = new JMenuItem("Import File", KeyEvent.VK_I);
		importFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		menuFile.add(importFile);

		download = new JMenuItem("Download", KeyEvent.VK_D);
		download.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
		menuFile.add(download);

		JSeparator separator_1 = new JSeparator();
		menuFile.add(separator_1);

		JMenuItem quit = new JMenuItem("Quit", KeyEvent.VK_Q);
		quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		menuFile.add(quit);
		quit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
				System.exit(0);
			}
		});

		editAudio = new JMenuItem("Edit Audio", KeyEvent.VK_E);
		editAudio.setEnabled(false);
		editAudio.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
		edit.add(editAudio);

		editVideo = new JMenuItem("Edit Video", KeyEvent.VK_V);
		editVideo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
		editVideo.setEnabled(false);
		edit.add(editVideo);
		
		subtitle = new JMenuItem("Add Subtitles", KeyEvent.VK_S);
		subtitle.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		subtitle.setEnabled(false);
		edit.add(subtitle);

		play = new JMenuItem("Play", KeyEvent.VK_P);
		play.setEnabled(false);
		play.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
		menuPlay.add(play);

		JMenu menuHelp = new JMenu("Help");
		menuBar.add(menuHelp);

		JMenuItem manual = new JMenuItem("Help", KeyEvent.VK_H);
		manual.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK));
		manual.addActionListener(new ActionListener() {
			// Read VAMIX PDF
			@Override
			public void actionPerformed(ActionEvent e) {
				if (Desktop.isDesktopSupported()) {
					try {
						File file = new File(System.getProperty("user.home") + File.separator + "vamix" 
								+ File.separator + "VAMIX_User_Manual.pdf");
						Desktop.getDesktop().open(file);
					} catch (IOException ex) {
					}
				}
			}
		});
		menuHelp.add(manual);

		// Set up library file system.
		boolean displayHelp = librarySetUp();
		createNewTab("Library", Library.getInstance(), 0);
		if (displayHelp) {
			JOptionPane.showMessageDialog(null, "We have detected this is your first time running VAMIX. "
					+ "VAMIX requires avconv in the libav library to run.\nPlease read the User help manual "
					+ "by pressing Ctrl-H to learn how to use VAMIX.","Please read manual"
					, JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/**
	 * Method to create tabs for specific panes, such as Audio Editor, Playback,
	 * Video Editor. Creates a cross-off option for every tab except the main
	 * library tab which shouldn't ever be closed in the application.
	 * 
	 * @param tabName
	 * @param pane
	 * @param num
	 */
	public static void createNewTab(String tabName, final JPanel pane, int num) {
		int position = _tabbedPane.indexOfTab(tabName);
		// Only create new tab if there isn't the same tab already open
		if (position != -1 && !(pane instanceof Playback) && !(pane instanceof Library)) {
			JPanel panel = (JPanel)_tabbedPane.getTabComponentAt(position);
			for (Component i: panel.getComponents()){
				try{
					((JButton)i).doClick();
				}
				catch(Exception e){
				}
			}
			num =_tabbedPane.getTabCount();
		}
		JLabel title = new JLabel(tabName);
		JPanel tab = new JPanel();
		tab.setOpaque(false);
		tab.add(title);
		_tabbedPane.addTab(tabName, pane);
		_tabbedPane.setTabComponentAt(num, tab);
		// If not Library create X button
		if (!(pane instanceof Library)) {
			JButton x = new JButton("X");
			tab.add(x, "align left");
			x.setContentAreaFilled(true);
			x.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (pane instanceof Playback) {
						// Put stop media method here
						((Playback) pane).stopPlayer();
					} else if (pane instanceof AudioEditor) {
						((AudioEditor) pane)._miniPlayback.stopPlayer();
					} else if (pane instanceof VideoEditor) {
						((VideoEditor) pane).stopAllPlayers();
					}
					_tabbedPane.remove(pane);
				}
			});
		}
	}

	/**
	 * Sets up the library directories if they don't exist on the user's
	 * computer.
	 * 
	 * @return
	 */
	public boolean librarySetUp() {
		File _input = new File(Library.inputDir);
		File _output = new File(Library.outputDir);
		boolean firstTime = false;

		if (!_input.exists()) {
			_input.mkdirs();
			firstTime = true;
		}
		if (!_output.exists()) {
			_output.mkdirs();
			firstTime = true;
		}
		
		URL inputUrl = getClass().getResource( File.separator + "manual" 
				+ File.separator + "VAMIX_User_Manual.pdf");
		File dest = new File(System.getProperty("user.home") + File.separator
				+ "vamix" + File.separator + "VAMIX_User_Manual.pdf");
		try {
			FileUtils.copyURLToFile(inputUrl, dest);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return firstTime;
	}
}