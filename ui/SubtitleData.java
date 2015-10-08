package ui;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.table.AbstractTableModel;

/**
 * This class represents the subtitle list where all the subtitle entries are stored.
 * This class handles the reading, checking, sorting and writing of all the subtitle 
 * entries.
 * @author Greggory Tan
 *
 */
@SuppressWarnings("serial")
public class SubtitleData extends AbstractTableModel{
	private List<Subtitle> subtitles = new ArrayList<Subtitle>(); 
	private String[] columnName = new String[] {"No. ", "Start", "End", "Text"};

	/**
	 * This constructor takes the whole text in a srt file and create subtitle classes for
	 * every entry. The regex is a modified version from (http://stackoverflow.com/questions/5062914/java-api-for-srt-subtitles)
	 * @param txt - text from srt file
	 */
	public SubtitleData(String txt) { //Maybe needed as StringBuffer
		if (!txt.equals("")){
			String nl = "\\\n";
			String sp = "[ \\t]*";
			Pattern subtitlePattern = Pattern.compile("(?s)(\\d+)" + sp + nl + 
					"(\\d{1,2}):(\\d\\d):(\\d\\d),(\\d\\d\\d)" + sp + "-->"+ sp + 
					"(\\d\\d):(\\d\\d):(\\d\\d),(\\d\\d\\d)" + sp + nl + "(.*?)" + nl + nl);
			Matcher matcher = subtitlePattern.matcher(txt);
			while (matcher.find()) {
				String start = matcher.group(2) + ":" + matcher.group(3) + ":" + matcher.group(4) + ":" + matcher.group(5);
				String end = matcher.group(6) + ":" + matcher.group(7) + ":" + matcher.group(8) + ":" + matcher.group(9);
				Subtitle subtitle = new Subtitle(start, end , matcher.group(10));
				subtitles.add(subtitle); 
			}
			sortData();
		}
	}

	/**
	 * This method is called when adding or editing subtitles into the list.
	 * Any overlapping duration will be checked. 
	 * @param sub - Subtitle object being added
	 * @param oldPostion -position of the edited entry, -1 if it is a new entry.
	 * @return true if the adding was successful.
	 */
	public Boolean addSubtitle(Subtitle sub, int oldPostion){
		Boolean delete = false;
		for (int i = 0; i < subtitles.size(); i++){
			if (i == oldPostion){
				delete = true;
			}
			else {
				Subtitle temp = subtitles.get(i);
				if (Subtitle.convertStringToTime(sub.start.split(":")) == Subtitle.convertStringToTime(temp.start.split(":"))){
					return false;
				}
				else if (Subtitle.convertStringToTime(sub.start.split(":")) > Subtitle.convertStringToTime(temp.start.split(":"))){
					if (Subtitle.convertStringToTime(sub.start.split(":")) < Subtitle.convertStringToTime(temp.end.split(":"))){
						return false;
					}
				}
				else if (Subtitle.convertStringToTime(sub.start.split(":")) < Subtitle.convertStringToTime(temp.start.split(":"))){
					if (Subtitle.convertStringToTime(sub.end.split(":")) > Subtitle.convertStringToTime(temp.start.split(":"))){
						return false;
					}
				}
				else if (Subtitle.convertStringToTime(sub.end.split(":")) == Subtitle.convertStringToTime(temp.end.split(":"))){
					return false;
				}
			}
		}
		if (delete){
			subtitles.remove(oldPostion);
		}
		subtitles.add(sub);
		sortData();
		return true; //If empty then valid
	}

	/**
	 * This method sorts the subtitle entries in the list
	 */
	private void sortData(){
		Collections.sort(subtitles, new Comparator<Subtitle>(){
			@Override
			public int compare(Subtitle o1, Subtitle o2) {
				return o1.compareTo(o2);
			}
		});
	}

	/**
	 * This method deletes the subtitle entry in the list at
	 * the specified position.
	 * @param position - position of the entry in the list
	 */
	public void deleteSubtitle(int position){
		subtitles.remove(position);
		sortData();
	}

	/**
	 * This method writes the srt file that will contain 
	 * all the entries
	 * @param location - absolute location of the srt file
	 * @return true if save was successful
	 */
	public Boolean saveData(String location){
		try {
			PrintWriter out = new PrintWriter(location);
			for (int i = 0; i < subtitles.size(); i++){
				Subtitle temp = subtitles.get(i);
				out.println(i+1);
				out.println(insertComma(temp.start) + " --> " + insertComma(temp.end));
				String output = temp.text;
				if (temp.isBold){
					output = "<b>" + output + "</b>";
				}
				if (temp.isItalic){
					output = "<i>" + output + "</i>";
				}
				if (!temp.color.equals("000000")){
					output = "<font color=\"#" + temp.color + "\">" + output + "</font>";
				}
				out.println(output);
				out.println("");
			}
			out.println("");
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public int getColumnCount() {
		return 4;
	}

	@Override
	public int getRowCount() {
		return subtitles.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Subtitle sub = subtitles.get(rowIndex);
		if (columnIndex == 0){
			return rowIndex+1;
		}
		else if(columnIndex == 1){
			return insertComma(sub.start);
		}
		else if (columnIndex == 2){
			return insertComma(sub.end);
		}
		else {
			return sub.text;
		}
	}

	/**
	 * Returns the column names of the subtitle data
	 */
	public String getColumnName(int col) {
		return columnName[col];
	}
	
	/**
	 * This method replaces the last ":" with 
	 * a "'"
	 * @param time - String of the time including milliseconds
	 * @return 
	 */
	public static String insertComma(String time){
		int i = time.lastIndexOf(":");
		return new StringBuilder(time).replace(i, i+1,",").toString();
	}

	/**
	 * Returns the subtitle object at the position in the list
	 * @param position - position in the list
	 * @return subtitle object
	 */
	public Subtitle getSubtitle(int position){
		return subtitles.get(position);
	}
}