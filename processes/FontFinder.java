package processes;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This class provides all the fonts in the font package file.
 * {@link #setUp()} method needs to be called before use.
 * 
 * @author Greggory Tan
 *
 */
public class FontFinder {

	private HashMap<String, String> _fontDirectory = new HashMap<String, String>();
	private List<Font> _fontList = new ArrayList<Font>();

	/**
	 * Extracts the default fonts if the folder doesn't exist
	 * Zip extraction code taken from
	 * http://www.codejava.net/java-se/file-io/programmatically-extract-a-zip-file-using-java
	 * Loads all the Fonts into an ArrayList and the String containing the 
	 * Font directory into a HashMap
	 */
	public void setUp() {
		File folder = new File(System.getProperty("user.home")+ File.separator + "vamix"
				+ File.separator + "fonts" + File.separator);
		//If the fonts folder doesn't exist, the fonts would be extracted 
		if (!folder.exists()){
			ZipInputStream zipIn = new ZipInputStream(getClass().getResourceAsStream(
					File.separator + "fonts.zip"));
			ZipEntry entry;
			try {
				entry = zipIn.getNextEntry();
				// iterates over entries in the zip file
				while (entry != null) {
					String filePath = System.getProperty("user.home")
							+ File.separator + "vamix" + File.separator + entry.getName();
					if (!entry.isDirectory()) {
						// if the entry is a file, extracts it
						extractFile(zipIn, filePath);
					} else {
						// if the entry is a directory, make the directory
						File dir = new File(filePath);
						dir.mkdir();
					}
					zipIn.closeEntry();
					entry = zipIn.getNextEntry();
				}
				zipIn.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		//Loads all the fonts in the folder
		File[] listofFiles = folder.listFiles();
		for (int i = 0; i < listofFiles.length; i++) {
			if (listofFiles[i].isFile()) {
				try {
					Font newFont = Font.createFont(Font.TRUETYPE_FONT, listofFiles[i]);
					_fontList.add(Font.createFont(Font.TRUETYPE_FONT, listofFiles[i]));
					_fontDirectory.put(newFont.getName(), listofFiles[i].toString());
				} catch (FontFormatException | IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
	/**
	 * Taken from
	 * http://www.codejava.net/java-se/file-io/programmatically-extract-a-zip-file-using-java
	 * Extracts a zip entry (file entry)
	 * @param zipIn - ZipInputStream of the zip file
	 * @param filePath - path of the output
	 * @throws IOException
	 */
	private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
		byte[] bytesIn = new byte[4096];
		int read = 0;
		while ((read = zipIn.read(bytesIn)) != -1) {
			bos.write(bytesIn, 0, read);
		}
		bos.close();
	}

	/**
	 * Returns the List containing all the font objects
	 * @return List containing the font objects
	 */
	public List<Font> getFontList() {
		return _fontList;
	}

	/**
	 * Returns the required font based on the name 
	 * @param fontName
	 * @return desired Font object
	 */
	public Font searchFont(String fontName){
		Font wantedFont = null;
		for (Font font :_fontList){
			if (fontName.equals(font.getName())){
				wantedFont = font.deriveFont(12);
			}
		}
		return wantedFont;
	}

	/**
	 * Returns the corresponding font directory
	 * @param fontName
	 * @return directory of the font
	 */
	public String getFontDirectory(String fontName){
		return _fontDirectory.get(fontName);
	}
}
