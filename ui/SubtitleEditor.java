package ui;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

import javax.swing.JTextArea;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JColorChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JCheckBox;

import java.awt.Canvas;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;

import javax.swing.SpinnerNumberModel;

/**
 * This class handles the UI components of the Subtitle Editor Panel.
 * The Subtitle Editor Panel lets the user add and edit subtitles.
 * @author Greggory Tan
 *
 */
@SuppressWarnings("serial")
public class SubtitleEditor extends JPanel {
	MiniPlayback _mediaPlayer;
	private JTable table_1;
	private JSpinner startHourSpinner;
	private JSpinner startMinSpinner;
	private JSpinner startSecSpinner;
	private JSpinner endHourSpinner;
	private JSpinner endMinSpinner;
	private JSpinner endSecSpinner;
	private JCheckBox boldCheckBox;
	private JCheckBox italicCheckBox;
	private Canvas canvas;
	private JScrollPane scrollPane_1;
	private JTextArea textArea;
	private JButton colourButton;
	private JButton btnAddSubtitle;
	private JButton btnEditSubtitle;
	private JButton btnDeleteSubtitle;
	private JButton btnSaveSubtitles;
	private JButton getEnd;
	private JButton getStart;
	private Boolean isEditing = false;
	private int selectedSub;
	private SubtitleData data;
	private File srt;
	private JSpinner startMilliSpinner;
	private JSpinner endMilliSpinner;

	/**
	 * UI components of the subtitle panel
	 */
	public SubtitleEditor(String mediaFile) {
		setLayout(new MigLayout("", "[550px,grow][300px,grow]", "[400px,grow][250px,grow]"));

		JPanel playerPanel = new JPanel(new MigLayout(""));
		playerPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null),
				"Video", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		_mediaPlayer = new MiniPlayback();
		playerPanel.add(_mediaPlayer);
		add(playerPanel, "cell 0 0,grow");

		JPanel settingsPanel = new JPanel();
		settingsPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null),
				"Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(settingsPanel, "cell 1 0,grow");
		settingsPanel.setLayout(new MigLayout("", "[50px,grow][50px,grow][50px,grow][50px,grow][50px,grow][50px,grow]", "[50px,grow][50px,grow][50px,grow][50px,grow][50px,grow][50px,grow][50px,grow][50px,grow]"));

		JLabel startLabel = new JLabel("Start:");
		settingsPanel.add(startLabel, "cell 0 0,alignx right,aligny center");
		
				startHourSpinner = new JSpinner();
				startHourSpinner.setModel(new SpinnerNumberModel(0, 0, 59, 1));
				settingsPanel.add(startHourSpinner, "cell 1 0,alignx center,aligny center");
		startMinSpinner = new JSpinner();
		startMinSpinner.setModel(new SpinnerNumberModel(0, 0, 59, 1));
		settingsPanel.add(startMinSpinner, "cell 2 0,alignx center,aligny center");
		startSecSpinner = new JSpinner();
		startSecSpinner.setModel(new SpinnerNumberModel(0, 0, 59, 1));
		settingsPanel.add(startSecSpinner, "cell 3 0,alignx center,aligny center");
		
		startMilliSpinner = new JSpinner();
		startMilliSpinner.setModel(new SpinnerNumberModel(0, 0, 999, 1));
		settingsPanel.add(startMilliSpinner, "cell 4 0,alignx center,aligny center");
		getStart = new JButton("Get Start Time");
		settingsPanel.add(getStart, "cell 1 1 4 1,alignx center,aligny center");
		JLabel endLabel = new JLabel("End:");
		settingsPanel.add(endLabel, "cell 0 2,alignx right,aligny center");
		
				endHourSpinner = new JSpinner();
				endHourSpinner.setModel(new SpinnerNumberModel(0, 0, 59, 1));
				settingsPanel.add(endHourSpinner, "cell 1 2,alignx center,aligny center");
		endMinSpinner = new JSpinner();
		endMinSpinner.setModel(new SpinnerNumberModel(0, 0, 59, 1));
		settingsPanel.add(endMinSpinner, "cell 2 2,alignx center,aligny center");
		endSecSpinner = new JSpinner();
		endSecSpinner.setModel(new SpinnerNumberModel(0, 0, 59, 1));
		settingsPanel.add(endSecSpinner, "cell 3 2,alignx center,aligny center");
		
		endMilliSpinner = new JSpinner();
		endMilliSpinner.setModel(new SpinnerNumberModel(0, 0, 999, 1));
		settingsPanel.add(endMilliSpinner, "cell 4 2,alignx center,aligny center");
		getEnd = new JButton("Get End Time");
		settingsPanel.add(getEnd, "cell 1 3 4 1,alignx center,aligny center");

		JLabel boldLabel = new JLabel("Bold");
		settingsPanel.add(boldLabel, "cell 1 4,alignx right,aligny center");
		boldCheckBox = new JCheckBox("");
		settingsPanel.add(boldCheckBox, "cell 2 4,alignx center,aligny center");

		JLabel italicsLabel = new JLabel("Italics");
		settingsPanel.add(italicsLabel, "cell 3 4,alignx right,aligny center");
		italicCheckBox = new JCheckBox("");
		settingsPanel.add(italicCheckBox, "cell 4 4,alignx center,aligny center");

		JLabel colourLabel = new JLabel("Font Colour:");
		settingsPanel.add(colourLabel, "cell 0 5 2 1,alignx center,aligny center");

		JPanel canvasPanel = new JPanel();
		settingsPanel.add(canvasPanel, "cell 2 5,grow");
		canvasPanel.setLayout(new MigLayout("", "[50px,grow]", "[50px,grow]"));

		canvas = new Canvas();
		canvas.setBackground(Color.BLACK);
		canvas.setPreferredSize(new Dimension(50,50));
		canvasPanel.add(canvas, "cell 0 0,alignx center,aligny center");

		colourButton = new JButton("Choose Colour");
		settingsPanel.add(colourButton, "cell 3 5 3 1,alignx center,aligny center");

		btnAddSubtitle = new JButton("Add Subtitle");
		settingsPanel.add(btnAddSubtitle, "cell 1 6 2 1,alignx center,aligny center");
		btnEditSubtitle = new JButton("Edit Subtitle");
		settingsPanel.add(btnEditSubtitle, "cell 3 6 2 1,alignx center,aligny center");
		btnDeleteSubtitle = new JButton("Delete Subtitle");
		settingsPanel.add(btnDeleteSubtitle, "cell 1 7 2 1,alignx center,aligny center");
		btnSaveSubtitles = new JButton("Save Subtitles");
		settingsPanel.add(btnSaveSubtitles, "cell 3 7 2 1,alignx center,aligny center");

		JPanel table = new JPanel();
		table.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Table",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(table, "cell 0 1,grow");
		table.setLayout(new MigLayout("", "[px,grow,fill]", "[grow,fill]"));
		table_1 = new JTable();

		scrollPane_1 = new JScrollPane();
		scrollPane_1.setViewportView(table_1);
		table.add(scrollPane_1, "cell 0 0,alignx left,aligny top");

		JPanel textPanel = new JPanel();
		textPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Subtitle Text",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(textPanel, "cell 1 1,grow");
		textPanel.setLayout(new MigLayout("", "[grow]", "[grow]"));

		textArea = new JTextArea();
		JScrollPane scrollPane = new JScrollPane();
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		scrollPane.setViewportView(textArea);
		textPanel.add(scrollPane, "cell 0 0,grow");
		addListeners();
		loadFiles(mediaFile);
	}

	/**
	 * This method load the srt file of the video file. If it doesn't exist, a new srt file would be
	 * made.
	 * @param mediaFile - location of the video file
	 */
	private void loadFiles(String mediaFile){
		_mediaPlayer.loadFile(mediaFile);
		String filenameNoExtension = mediaFile.substring(0, mediaFile.lastIndexOf("."));
		srt = new File(filenameNoExtension + ".srt");

		if (srt.exists()){
			StringBuffer text = new StringBuffer();
			try {
				BufferedReader br = new BufferedReader(new FileReader(srt));
				String currentLine;
				while ((currentLine = br.readLine()) != null) {
					text.append(currentLine);
					text.append("\n");
				}
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			data = new SubtitleData(text.toString());
		}
		else {
			try {
				PrintWriter out = new PrintWriter(srt);
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			data = new SubtitleData("");
		}
		//Sets the width for each column
		table_1.setModel(data);
		table_1.getColumnModel().getColumn(0).setResizable(false);
		table_1.getColumnModel().getColumn(0).setMaxWidth(55);
		table_1.getColumnModel().getColumn(0).setMinWidth(55);
		table_1.getColumnModel().getColumn(1).setResizable(false);
		table_1.getColumnModel().getColumn(1).setMaxWidth(100);
		table_1.getColumnModel().getColumn(1).setMinWidth(100);
		table_1.getColumnModel().getColumn(2).setResizable(false);
		table_1.getColumnModel().getColumn(2).setMaxWidth(100);
		table_1.getColumnModel().getColumn(2).setMinWidth(100);
		table_1.getColumnModel().getColumn(3).setPreferredWidth(200);
		table_1.getColumnModel().getColumn(3).setMinWidth(150);
	}

	/**
	 * Adds the Listeners for the components
	 */
	private void addListeners(){
		colourButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Color newCol = JColorChooser.showDialog(null, "Choose font colour",
						canvas.getForeground());
				canvas.setBackground(newCol);
				if (table_1.getSelectedRow() != -1){
					data.getSubtitle(table_1.getSelectedRow()).setColor(convertToHexRGB(newCol));
				}
			}
		});

		btnEditSubtitle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (table_1.getSelectedRow() != -1){
					if (isEditing){
						isEditing = false;
						btnEditSubtitle.setText("Edit Subtitle");
						btnAddSubtitle.setText("Add Subtitle");
					}
					else {
						isEditing = true;
						btnAddSubtitle.setText("Save Edit");
						btnEditSubtitle.setText("Cancel Edit");
						selectedSub = table_1.getSelectedRow();
						Subtitle sub = data.getSubtitle(selectedSub);
						String[] start = sub.start.split(":");
						startHourSpinner.setValue(Integer.parseInt(start[0]));
						startMinSpinner.setValue(Integer.parseInt(start[1]));
						startSecSpinner.setValue(Integer.parseInt(start[2]));
						startMilliSpinner.setValue(Integer.parseInt(start[3]));
						String[] end = sub.end.split(":");
						endHourSpinner.setValue(Integer.parseInt(end[0]));
						endMinSpinner.setValue(Integer.parseInt(end[1]));
						endSecSpinner.setValue(Integer.parseInt(end[2]));
						endMilliSpinner.setValue(Integer.parseInt(end[3]));

						if (sub.isBold){
							boldCheckBox.setSelected(true);
						}
						if (sub.isItalic){
							italicCheckBox.setSelected(true);
						}

						int r = Integer.parseInt(sub.color.substring(0, 2), 16);
						int g = Integer.parseInt(sub.color.substring(2, 4), 16);
						int b = Integer.parseInt(sub.color.substring(4, 6), 16);
						Color newColor = new Color(r, g, b);
						canvas.setBackground(newColor);
						textArea.setText(sub.text);
					}
				}
			}
		});

		btnAddSubtitle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String start = String.format("%02d:%02d:%02d:%03d", startHourSpinner.getValue(),
						startMinSpinner.getValue(), startSecSpinner.getValue(), startMilliSpinner.getValue());
				String end = String.format("%02d:%02d:%02d:%03d", endHourSpinner.getValue(),
						endMinSpinner.getValue(), endSecSpinner.getValue(), endMilliSpinner.getValue());
				Subtitle temp = new Subtitle(start, end, textArea.getText());
				if (boldCheckBox.isSelected()){
					temp.isBold = true;
				}
				if (italicCheckBox.isSelected()){
					temp.isItalic = true;
				}
				temp.setColor(convertToHexRGB(canvas.getBackground()));

				if (temp.validSubtitle()){
					if (isEditing){
						if (data.addSubtitle(temp, selectedSub)){
							data.fireTableDataChanged();
							isEditing = false;
							btnAddSubtitle.setText("Add Subtitle");
							btnEditSubtitle.setText("Edit Subtitle");
						}
						else {
							JOptionPane.showMessageDialog(null,"Start or End time overlaps with other entries.");
						}
					}
					else {
						if (data.addSubtitle(temp, -1)){
							data.fireTableDataChanged();
						}
						else {
							JOptionPane.showMessageDialog(null,"Start or End time overlaps with other entries.");
						}
					}
				}
				else {
					JOptionPane.showMessageDialog(null,"Invalid start or end time or empty text.");
				}
			}
		});

		getEnd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] end = _mediaPlayer.getTime().split(":");
				endHourSpinner.setValue(Integer.parseInt(end[0]));
				endMinSpinner.setValue(Integer.parseInt(end[1]));
				endSecSpinner.setValue(Integer.parseInt(end[2]));
				endMilliSpinner.setValue(Integer.parseInt(end[3]));
			}
		});

		getStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] start = _mediaPlayer.getTime().split(":");
				startHourSpinner.setValue(Integer.parseInt(start[0]));
				startMinSpinner.setValue(Integer.parseInt(start[1]));
				startSecSpinner.setValue(Integer.parseInt(start[2]));
				startMilliSpinner.setValue(Integer.parseInt(start[3]));
			}
		});

		btnDeleteSubtitle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (table_1.getSelectedRow() != -1){
					data.deleteSubtitle(table_1.getSelectedRow());
					data.fireTableDataChanged();
				}
			}
		});

		btnSaveSubtitles.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (data.saveData(srt.getAbsolutePath())){
					JOptionPane.showMessageDialog(null,"Subtitles have been saved. \nStop and play the player again to view changes");
				}
				else {
					JOptionPane.showMessageDialog(null,"Error. Failed to save subtitles");
				}
			}
		});
	}

	/**
	 * Returns string representation of the RGB values of the color
	 * @param color - Color object
	 * @return string containing color rgb value
	 */
	public static String convertToHexRGB(Color color){
		return String.format("%02x%02x%02x",
				color.getRed(), color.getGreen(), color.getBlue());
	}
}