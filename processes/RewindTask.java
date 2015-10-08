package processes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.SwingWorker;
import javax.swing.Timer;

import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

/**
 * The class that represents the worker that performs the rewinding in the playback pane.
 * 
 * @author Greggory Tan
 *
 */
public class RewindTask extends SwingWorker<Void,Integer> {

	private EmbeddedMediaPlayer _mediaPlayer;
	private int _speed = 0;
	private Timer _updater = new Timer(10, new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			_mediaPlayer.skip(-10*_speed);
		}
	});

	/**
	 * This constructor takes the mediaPlayer component that is rewinding
	 * @param mediaPlayer - vlcj mediaPlayer
	 */
	public RewindTask(EmbeddedMediaPlayer mediaPlayer) {
		_mediaPlayer=mediaPlayer;
	}

	@Override
	protected Void doInBackground() throws Exception {
		increaseSpeed();
		_mediaPlayer.pause();
		_updater.start();

		while(true){
			if (isCancelled()){
				_updater.stop();
				_mediaPlayer.play();
				break;
			}
		}
		return null;
	}

	/**
	 * Increases the rewind speed
	 */
	public void increaseSpeed(){
		if (_speed == 0){
			_speed = 1;
		}
		else if (_speed == 1){
			_speed = 2;
		}
		else if (_speed == 2){
			_speed = 3;
		}
		else if (_speed == 3){
			_speed = 4;
		}
	}

	/**
	 * Decreases the rewind speed
	 */
	public void decreaseSpeed(){
		if (_speed == 4){
			_speed = 3;
		}
		else if (_speed == 3){
			_speed = 2;
		}
		else if (_speed == 2){
			_speed = 1;
		}
		else if (_speed == 1){
			_speed = 0;
			cancel(true);
		}
	}

	/**
	 * Checks whether the rewind task has been stopped
	 * @return true if speed equals 0 else false
	 */
	public Boolean hasStop(){
		if (_speed == 0){
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Returns the speed of the rewind task
	 * @return (int)current speed of rewind 
	 */
	public int getSpeed(){
		return Math.abs(_speed);
	}
}
