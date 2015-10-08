package ui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * This class is used to load icons for buttons.
 * The icons used in library are from (www.flaticon.com).
 * The icons used in the player are from the Windows Media Player and 
 * the roundbutton code.
 * (Class made to reduce repeated code)
 * @author Greggory Tan
 *
 */
public class LoadIcons {
	/**
	 * Creates image icons for the button icons
	 * 
	 * @param name - name of the icon in the icons folder
	 * @return ImageIcon object
	 */
	protected ImageIcon createImageIcon(String name) {
		BufferedImage image;
		try {
			image = ImageIO.read(getClass().getResourceAsStream(File.separator + "icons" + File.separator + name));
			return new ImageIcon(image);
		} catch (IOException e) {
			System.err.println("Couldn't find file: " + name);
			e.printStackTrace();
		}
		return null;
	}
}
