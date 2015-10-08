package ui;

/**
 * This class represents a subtitle entry in the srt file
 * @author Greggory Tan
 *
 */
public class Subtitle implements Comparable<Subtitle>{
	protected String start;
	protected String end;
	protected String text;
	protected String color;
	protected Boolean isBold;
	protected Boolean isItalic;
	
	/**
	 * Constructor of the subtitle class. 
	 * Checks if the text have any html tags for bold, italics
	 * or font color
	 * @param start time of the subtitle entry
	 * @param end time of the subtitle entry
	 * @param text to be displayed
	 */
	public Subtitle(String start, String end, String text){
		this.start = start;
		this.end = end;
		String tagless = text;
		//Checks if there is the bold tag
		if (text.contains("<b>") && text.contains("</b>")){
			isBold = true;
			tagless = tagless.replace("<b>", "");
			tagless = tagless.replace("</b>", "");
		}
		else {
			isBold = false;
		}
		//Checks if there is the italic tag
		if (text.contains("<i>") && text.contains("</i>")){
			isItalic = true;
			tagless = tagless.replace("<i>", "");
			tagless = tagless.replace("</i>", "");
		} 
		else {
			isItalic = false;
		}
		//Checks if there is the font color tag
		if (text.contains("<font color=") && text.contains("</font>")){
			int beginIndex = text.indexOf("<font color=")+14;
			color = text.substring(beginIndex, beginIndex+6);
			tagless = tagless.replace("<font color=\"#" + color + "\">", "");
			tagless = tagless.replace("</font>", "");
		}
		else {
			color = "000000";
		}
		this.text = tagless;
	}

	/**
	 * This method sets the hexadecimal rgb color value for the subtitle 
	 * entry
	 * @param color
	 */
	public void setColor(String color){
		this.color = color;
	}

	/**
	 * Checks if the subtitle entry has a valid start and end time 
	 * and also if it has any text
	 * @return true is it is valid subtitle entry
	 */
	protected Boolean validSubtitle(){
		int startTime = convertStringToTime(start.split(":"));
		int endTime = convertStringToTime(end.split(":"));
		if (startTime < endTime){
			if (!text.equals("")){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int compareTo(Subtitle o) {
		/* Converts the start time of both entries to seconds and 
		 * then compares them. The subtitle is "smaller" if 
		 * it has a smaller start time.
		 */
		int seconds1 = convertStringToTime(start.split(":"));
		int seconds2 = convertStringToTime(o.start.split(":"));
		if (seconds1 > seconds2){
			return 1;
		}
		else if (seconds1 < seconds2){
			return -1;
		}
		else {
			return 0;
		}
	}
	
	/**
	 * Static method that takes a string array, a of width 4 where
	 * a[0] = hours , a[1] = minutes, a[2] = seconds and a[3] = milliseconds.
	 * The string array is then converted to (int)milliseconds.
	 * @param (String[3])time
	 * @return (int)time in milliseconds
	 */
	public static int convertStringToTime(String[] time){
		return (Integer.parseInt(time[0])*3600 + Integer.parseInt(time[1])*60 + Integer.parseInt(time[2]))*1000 + Integer.parseInt(time[3]);
	}
}