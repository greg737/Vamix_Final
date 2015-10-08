package processes;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.SwingWorker;

import ui.Library;

/**
 * This class represents the task of importing any files into the input library of the VAMIX application.
 * This is done in the background to prevent the GUI from freezing when copying large files.
 * Taken from Assignment 3 
 *
 * @author Harry She
 */
public class ImportTask extends SwingWorker<Void, Void> {

	private Path _input;
	private Path _importPath;

	public ImportTask(Path input, Path importPath) {
		_input = input;
		_importPath = importPath;
	}

	@Override
	protected Void doInBackground() throws Exception {
		try {
			Files.copy(_input, _importPath);
		} catch (IOException e) {
			firePropertyChange("failure", null, e.getMessage());
		}
		return null;
	}

	@Override
	protected void done() {
		Library.getInstance().refreshTree();
		firePropertyChange("success", null, "success");
	}

}
