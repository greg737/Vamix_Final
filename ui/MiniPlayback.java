package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;

import model.RoundButton;
import net.miginfocom.swing.MigLayout;
import processes.RewindTask;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;

/**
 * This class creates the layout of the playback panel
 * and the listeners for the components
 * @author Greggory Tan
 *
 */
@SuppressWarnings("serial")
public class MiniPlayback extends PlaybackTemplate{
	protected Boolean _isFirstTime;
	
	/**
	 * This constructor is for the mini version of playback
	 */
	public MiniPlayback() {
		setLayout(new MigLayout("", "[grow]", "[220px,grow][20px][40px][20px]"));
		_seekPanel.setLayout(new MigLayout("",
				"[20px,grow 10][350px,grow 90][20px,grow 10]", "[10px]"));
		_playbackPanel.setBorder(new SoftBevelBorder(BevelBorder.RAISED, null,
				null, null, null));
		_playbackPanel.setLayout(new MigLayout("",
				"[20px][20px][5px][20px][20px][220px,grow][20px][80px]",
				"[20px]"));
		_playerBG.setMinimumSize(new Dimension(360, 220));

		setUp();
	}

	/**
	 * Sets up all the panels and components of the player. 
	 * If isPlayback argument is true then the status bar would be 
	 * added.
	 */
	protected void setUp() {
		add(_playerPanel, "cell 0 0,grow");
		add(_seekPanel, "cell 0 1,growx");
		add(_playbackPanel, "cell 0 2,growx");

		_playerBG.setBackground(Color.BLACK);
		_playerBG.setVisible(true);

		//BoilerPlate code for media player set up taken
		//from vlcj api
		_mediaPlayer = _mediaPlayerFactory.newEmbeddedMediaPlayer();
		_mediaPlayer.setVideoSurface(_mediaPlayerFactory.newVideoSurface(_playerBG));
		_playerPanel.setLayout(new MigLayout("", "[grow]", "[grow]"));
		_playerPanel.add(_playerBG, "cell 0 0, grow");

		changeButton(_playPauseButton, TEXT_PLAY);
		_playPauseButton.setEnabled(false);
		_stop.setEnabled(false);
		_rewind.setEnabled(false);
		_fastForward.setEnabled(false);
		_mute.setEnabled(false);
		_playbackPanel.add(_playPauseButton, "cell 0 0");
		_playbackPanel.add(_stop, "cell 1 0");
		_playbackPanel.add(_rewind, "cell 3 0");
		_playbackPanel.add(_fastForward, "cell 4 0");
		_playbackPanel.add(_mute, "cell 6 0");
		_playbackPanel.add(_volume, "cell 7 0");

		_seekPanel.add(_currentTime, "cell 0 0, alignx center");
		_seekPanel.add(_seekbar, "cell 1 0,grow");
		_seekPanel.add(_totalTime, "cell 2 0, alignx center");

		addListeners();
	}

	public void loadFile(String file) {
		_playPauseButton.setEnabled(true);
		_stop.setEnabled(true);
		_rewind.setEnabled(true);
		_fastForward.setEnabled(true);
		_mute.setEnabled(true);
		_isFirstTime = true;
		_mediaPlayer.prepareMedia(file);
	}
	
	/**
	 * Returns the current time including milliseconds from the player
	 * @return (int) current time
	 */
	public String getTime(){
		long current = _mediaPlayer.getTime();
		String time = time(current);
		if (time.length() < 8){
			return "00:" + time + ":" + new DecimalFormat("000").format(current%1000);
		} else {
			return time + ":" + new DecimalFormat("000").format(current%1000);
		}
	}

	/**
	 * This method adds the listeners for all the components
	 */
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
					}
					else {
					}
				}
				else {
					changeButton(_playPauseButton, TEXT_PLAY);
					if (_mediaPlayer.getRate() == 1.0) {
						_mediaPlayer.setRate((float) 2.0);
					} else if (_mediaPlayer.getRate() == 2.0) {
						_mediaPlayer.setRate((float) 3.0);
					} else if (_mediaPlayer.getRate() == 3.0) {
						_mediaPlayer.setRate((float) 4.0);
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
				} else if (_mediaPlayer.getRate() == 3.0) {
					_mediaPlayer.setRate((float) 2.0);
				} else if (_mediaPlayer.getRate() == 4.0) {
					_mediaPlayer.setRate((float) 3.0);
				} else if (_isRewinding) {
					rw.increaseSpeed();
				} else {
					rw = new RewindTask(_mediaPlayer);
					rw.execute();
					_isRewinding = true;
					changeButton(_playPauseButton, TEXT_PLAY);
				}
			}
		});

		//This listener plays and pause the player and switches 
		//the icons
		_playPauseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent action) {
				if (_isFirstTime){
					_isFirstTime = false;
					changeButton(_playPauseButton, TEXT_PAUSE);
					_mediaPlayer.play();
					startPlayer();
				}
				else {
				try {
					rw.cancel(true);
					_isRewinding = false;
				} catch (NullPointerException ne) {
				}
				if (((RoundButton) action.getSource()).getDescription().equals(TEXT_PLAY)) {
					if (_mediaPlayer.getRate() > 1){
						_mediaPlayer.setRate((float) 1.0);
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
				stopPlayer();
				changeButton(_playPauseButton, TEXT_PLAY);
			}
		});
	}
}
