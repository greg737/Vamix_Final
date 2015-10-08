package ui;

import java.awt.Canvas;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import processes.RewindTask;
import model.RoundButton;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

/**
 * This class creates the layout of the playback panel
 * and the listeners for the components
 * @author Greggory Tan
 *
 */
@SuppressWarnings("serial")
public abstract class PlaybackTemplate extends JPanel {
	protected final String TEXT_PLAY = "Play";
	protected final String TEXT_PAUSE = "Pause";
	protected final String TEXT_UNMUTE = "Unmute";
	protected final String TEXT_MUTE = "Mute";

	protected LoadIcons loader = new LoadIcons();
	protected RoundButton _playPauseButton = new RoundButton(
			loader.createImageIcon("pause.png"), loader.createImageIcon("pause_p.png"),
			loader.createImageIcon("pause_r.png"), TEXT_PAUSE);
	protected RoundButton _mute = new RoundButton(loader.createImageIcon("unmute.png"),
			loader.createImageIcon("unmute_p.png"), loader.createImageIcon("unmute_r.png"), TEXT_UNMUTE);
	protected RoundButton _stop = new RoundButton(loader.createImageIcon("stop.png"),
			loader.createImageIcon("stop_p.png"), loader.createImageIcon("stop_r.png"), "Stop");
	protected RoundButton _fastForward = new RoundButton( loader.createImageIcon("fastforward.png"),
			loader.createImageIcon("fastforward_p.png"),
			loader.createImageIcon("fastforward_r.png"), "FastForward");
	protected RoundButton _rewind = new RoundButton(
			loader.createImageIcon("rewind.png"), loader.createImageIcon("rewind_p.png"),
			loader.createImageIcon("rewind_r.png"), "Rewind");

	protected final JSlider _seekbar = new JSlider(0, 0, 0);
	protected final JSlider _volume = new JSlider(0, 100, 50);
	protected JLabel _currentTime = new JLabel("--:--:--");
	protected JLabel _totalTime = new JLabel("--:--:--");

	protected Canvas _playerBG = new Canvas();
	public EmbeddedMediaPlayer _mediaPlayer;
	protected MediaPlayerFactory _mediaPlayerFactory = new MediaPlayerFactory();
	
	protected JPanel _playerPanel = new JPanel();
	protected JPanel _seekPanel = new JPanel();
	protected JPanel _playbackPanel = new JPanel();
	protected RewindTask rw;
	protected Boolean _isRewinding = false;
	
	/**
	 * Sets up all the panels and components of the player. 
	 * If isPlayback argument is true then the status bar would be 
	 * added.
	 */
	protected void setUp() {
	}

	/**
	 * This method loads up the media file for the player
	 * If it is for a playback panel, the video will start automatically.
	 * Else the video will only start when the play button is pressed.
	 * 
	 * @param file - location of media file
	 */
	public void loadFile(String file) {
	}

	/**
	 * This method stops the player
	 */
	public void stopPlayer() {
		_mediaPlayer.stop();
	}
	
	/**
	 * This method starts the player and loads the duration of the media file.
	 * A timer is set up to update the current time label after a certain time
	 */
	public void startPlayer(){
		_mediaPlayer.play();
		if(_mediaPlayer.isMute()){
			_mediaPlayer.mute(false);
		}
		long length = 0;
		_mediaPlayer.parseMedia();
		length = _mediaPlayer.getLength();
		while (length == 0){
			length = _mediaPlayer.getLength();
		}

		_mediaPlayer.setVolume(50);
		_totalTime.setText(time(length));
		_seekbar.setMaximum((int) length);

		//Code taken for Nasser's Lecture
		Timer updater = new Timer(250, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				long current = _mediaPlayer.getTime();
				_currentTime.setText(time(current));
				_seekbar.setValue((int) current);
			}
		});
		updater.start();
	}

	/**
	 * This method adds the common listeners for all the components which 
	 * are shared between the two types of playbacks.
	 * Must be called once.
	 */
	protected void addListeners() {
		//This listener resets the player when the media is finished
		_mediaPlayer.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
			public void finished(MediaPlayer mediaPlayer) {
				_stop.doClick();
			}
		});

		//This listener mutes the player
		_mute.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (_mediaPlayer.isMute()) {
					_mediaPlayer.mute(false);
					changeButton(_mute, TEXT_UNMUTE);
				} else {
					_mediaPlayer.mute(true);
					changeButton(_mute, TEXT_MUTE);
				}
			}
		});

		//This listener enables the drag function for the slider
		_volume.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (_mediaPlayer.isPlaying() && source.getValueIsAdjusting()) {
					_mediaPlayer.setVolume(source.getValue());
				}
			}
		});

		//This listener enables the click function for the slider
		_volume.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				Point p = e.getPoint();
				JSlider source = (JSlider) e.getSource();
				double percent = p.x / ((double) source.getWidth());
				int range = source.getMaximum() - source.getMinimum();
				double newVal = range * percent;
				int result = (int) (source.getMinimum() + newVal);
				source.setValue(result);
			}
		});

		//This listener enables the drag function for the slider
		_seekbar.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (source.getValueIsAdjusting()) {
					_mediaPlayer.setTime((long) source.getValue());
				}
			}
		});

		//This listener enables the click function for the slider
		_seekbar.addMouseListener(new MouseAdapter() {
			// Edited version of the code on
			// http://stackoverflow.com/questions/7095428/jslider-clicking-makes-the-dot-go-towards-that-direction
			@Override
			public void mousePressed(MouseEvent e) {
				Point p = e.getPoint();
				double percent = p.x / ((double) _seekbar.getWidth());
				int range = _seekbar.getMaximum() - _seekbar.getMinimum();
				double newVal = range * percent;
				int result = (int) (_seekbar.getMinimum() + newVal);
				_seekbar.setValue(result);
			}
		});
	}

	/**
	 * This method switches the icons & the description of the RoundButton
	 * based on the description parameter
	 * 
	 * @param button - RoundButton object
	 * @param type - Description of the button
	 */
	protected void changeButton(RoundButton button, String type) {
		if (type.equals("")) {
			// Null checking
		} else if (type.equals(TEXT_PLAY)) {
			button.setIcons(loader.createImageIcon("play.png"),
					loader.createImageIcon("play_p.png"),
					loader.createImageIcon("play_r.png"), TEXT_PLAY);
		} else if (type.equals(TEXT_PAUSE)) {
			button.setIcons(loader.createImageIcon("pause.png"),
					loader.createImageIcon("pause_p.png"),
					loader.createImageIcon("pause_r.png"), TEXT_PAUSE);
		} else if (type.equals(TEXT_MUTE)) {
			button.setIcons(loader.createImageIcon("mute.png"),
					loader.createImageIcon("mute_p.png"),
					loader.createImageIcon("mute_r.png"), TEXT_MUTE);
		} else if (type.equals(TEXT_UNMUTE)) {
			button.setIcons(loader.createImageIcon("unmute.png"),
					loader.createImageIcon("unmute_p.png"),
					loader.createImageIcon("unmute_r.png"), TEXT_UNMUTE);
		}
	}

	/**
	 * This method takes the length in millisecond and converts it into the
	 * format "HOUR:MINUTER:SECOND"
	 * 
	 * @param millisec 
	 * @return string following format "HOUR:MINUTER:SECOND"
	 */
	protected static String time(long millisec) {
		String time = "";
		int duration = (int) (millisec / 1000.00);
		int sec = (duration % 3600) % 60;
		int min = (duration % 3600) / 60;
		int hour = duration / 3600;
		DecimalFormat formatter = new DecimalFormat("00");
		if (duration < 3600) {
			time = formatter.format(min) + ":" + formatter.format(sec);
		} else {
			time = formatter.format(hour) + ":" + formatter.format(min) + ":"
					+ formatter.format(sec);
		}
		return time;
	}
}
