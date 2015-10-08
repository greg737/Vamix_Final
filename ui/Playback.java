package ui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;

import model.RoundButton;
import net.miginfocom.swing.MigLayout;
import processes.RewindTask;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.DefaultFullScreenStrategy;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

/**
 * This class creates the layout of the playback panel
 * and the listeners for the components
 * @author Greggory
 *
 */
@SuppressWarnings("serial")
public class Playback extends PlaybackTemplate {
	private JLabel _status = new JLabel();
	private String _currentFileString = "";
	private boolean isFullScreen = false;
	private JPanel _labelPanel = new JPanel();

	/**
	 * This constructor is for the main playback tab
	 */
	public Playback(String file) {
		_currentFileString = "Now Playing: " + file;
		setLayout(new MigLayout("", "[grow]", "[520px,grow][20px][40px][20px]"));
		_seekPanel.setLayout(new MigLayout("", "[40px,grow 10][700px,grow 90][40px,grow 10]", "[20px]"));
		_playbackPanel.setLayout(new MigLayout("", "[40px][40px][10px][40px][40px][440px,grow][40px][160px]",
				"[40px]"));
		_playbackPanel.setBorder(new SoftBevelBorder(BevelBorder.RAISED, null, null, null, null));
		_labelPanel.setLayout(new MigLayout("", "[grow]", "[20px]"));
		_playerBG.setMinimumSize(new Dimension(950, 430));

		setUp();
	}

	/**
	 * Sets up all the panels and components of the player.
	 */
	protected void setUp() {
		add(_playerPanel, "cell 0 0,grow");
		add(_seekPanel, "cell 0 1,growx");
		add(_playbackPanel, "cell 0 2,growx");
		add(_labelPanel, "cell 0 3,growx");

		_playerBG.setBackground(Color.BLACK);
		_playerBG.setVisible(true);
		_mediaPlayerFactory = new MediaPlayerFactory();

		//BoilerPlate code for media player set up taken
		//from vlcj api
		_mediaPlayer = _mediaPlayerFactory.newEmbeddedMediaPlayer();
		_mediaPlayer.setVideoSurface(_mediaPlayerFactory.newVideoSurface(_playerBG));
		_playerPanel.setLayout(new MigLayout("", "[grow]", "[grow]"));
		_playerPanel.add(_playerBG, "cell 0 0, grow");

		_playbackPanel.add(_playPauseButton, "cell 0 0");
		_playbackPanel.add(_stop, "cell 1 0");
		_playbackPanel.add(_rewind, "cell 3 0");
		_playbackPanel.add(_fastForward, "cell 4 0");
		_playbackPanel.add(_mute, "cell 6 0");
		_playbackPanel.add(_volume, "cell 7 0");

		_status.setOpaque(true);
		_status.setBackground(Color.black);
		_status.setForeground(Color.white);
		_status.setText(_currentFileString);
		_labelPanel.add(_status, "grow");

		_seekPanel.add(_currentTime, "cell 0 0, alignx center");
		_seekPanel.add(_seekbar, "cell 1 0,grow");
		_seekPanel.add(_totalTime, "cell 2 0, alignx center");

		addListeners();
	}

	public void loadFile(String file) {
		_mediaPlayer.prepareMedia(file);
		startPlayer();
	}

	protected void addListeners() {
		super.addListeners();
		//This listener resets the player when the media is finished
		_mediaPlayer.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
			public void finished(MediaPlayer mediaPlayer) {
				_stop.doClick();
			}
		});

		//This listener increases the fastforward speed or decrease
		//the speed of rewind
		_fastForward.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (_isRewinding){
					rw.decreaseSpeed();
					if (rw.hasStop()){
						_isRewinding = false;
						_status.setText(_currentFileString);
					}
					else {
						_status.setText("Rewind x" + rw.getSpeed());
					}
				}
				else {
					changeButton(_playPauseButton, TEXT_PLAY);
					if (_mediaPlayer.getRate() == 1.0) {
						_mediaPlayer.setRate((float) 2.0);
						_status.setText("FastForward x2");
					} else if (_mediaPlayer.getRate() == 2.0) {
						_mediaPlayer.setRate((float) 3.0);
						_status.setText("FastForward x3");
					} else if (_mediaPlayer.getRate() == 3.0) {
						_mediaPlayer.setRate((float) 4.0);
						_status.setText("FastForward x4");
					}
				}
			}
		});

		//This listener decrease the fastforward speed or increase
		//the speed of rewind
		_rewind.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (_mediaPlayer.getRate() == 2.0) {
					_mediaPlayer.setRate((float) 1.0);
					_status.setText(_currentFileString);
				} else if (_mediaPlayer.getRate() == 3.0) {
					_mediaPlayer.setRate((float) 2.0);
					_status.setText("FastForward x2");
				} else if (_mediaPlayer.getRate() == 4.0) {
					_mediaPlayer.setRate((float) 3.0);
					_status.setText("FastForward x3");
				} else if (_isRewinding) {
					rw.increaseSpeed();
					_status.setText("Rewind x" + rw.getSpeed());
				} else {
					rw = new RewindTask(_mediaPlayer);
					rw.execute();
					_isRewinding = true;
					changeButton(_playPauseButton, TEXT_PLAY);
					_status.setText("Rewind x2");
				}
			}
		});

		//This listener plays and pause the player and switches 
		//the icons
		_playPauseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent action) {
				try {
					rw.cancel(true);
					_isRewinding = false;
					_status.setText(_currentFileString);
				} catch (NullPointerException ne) {
				}
				if (((RoundButton) action.getSource()).getDescription().equals(TEXT_PLAY)) {
					if (_mediaPlayer.getRate() > 1){
						_mediaPlayer.setRate((float) 1.0);
						_status.setText(_currentFileString);
					}
					else {
						_mediaPlayer.play();
					}
					changeButton(_playPauseButton, TEXT_PAUSE);
					_playPauseButton.validate();
				} else {
					_mediaPlayer.setRate((float) 1.0);
					_mediaPlayer.pause();
					changeButton(_playPauseButton, TEXT_PLAY);
					_playPauseButton.validate();
				}
			}
		});

		//This listener stops the player when the stop button is pressed
		_stop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					rw.cancel(true);
					_isRewinding = false;
				} catch (NullPointerException ne){
				}
				_mediaPlayer.setRate((float) 1.0);
				_status.setText(_currentFileString);
				stopPlayer();
				changeButton(_playPauseButton, TEXT_PLAY);
			}
		});

		/*
		 * Boilerplate fullscreen code adapted from
		 * http://stackoverflow.com/questions
		 * /21546540/vlcj-full-screen-video-player
		 */
		_playerBG.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && !isFullScreen) {
					isFullScreen = true;
					long currentTime = _mediaPlayer.getTime();
					_mediaPlayer.pause();
					final JFrame frame = new JFrame("VAMIX FUllscreen");
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory();
					Canvas c = new Canvas();
					c.setBackground(Color.black);
					JPanel p = new JPanel();
					p.setLayout(new BorderLayout());
					p.add(c, BorderLayout.CENTER);
					frame.add(p, BorderLayout.CENTER);
					final EmbeddedMediaPlayer mediaPlayer = mediaPlayerFactory
							.newEmbeddedMediaPlayer(new DefaultFullScreenStrategy(frame));
					mediaPlayer.setVideoSurface(mediaPlayerFactory.newVideoSurface(c));
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					frame.setVisible(true);
					frame.addKeyListener(new KeyAdapter() {
						@Override
						public void keyPressed(KeyEvent e) {
							if (e.getKeyCode() == KeyEvent.VK_ESCAPE|| e.getKeyCode() == KeyEvent.VK_Q) {
								_mediaPlayer.setTime(mediaPlayer.getTime());
								mediaPlayer.stop();
								frame.dispose();
								_mediaPlayer.play();
								isFullScreen = false;
							}
						}
					});

					c.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							if (e.getClickCount()==2) {
								_mediaPlayer.setTime(mediaPlayer.getTime());
								mediaPlayer.stop();
								frame.dispose();
								_mediaPlayer.play();
								isFullScreen = false;
							}
						}
					});
					mediaPlayer.setFullScreen(true);
					mediaPlayer.playMedia(Library._currentFileString);
					mediaPlayer.setTime(currentTime);
				}
			}
		});
	}
}