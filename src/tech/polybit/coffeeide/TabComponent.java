package tech.polybit.coffeeide;

import java.io.File;

import javax.swing.JLabel;
import javax.swing.JTextPane;

public class TabComponent {
	
	private File file;
	private JLabel label;
	private JTextPane textPane;
	private String packages;
	
	public TabComponent(File file, JLabel label, JTextPane textPane, String packages) {
		this.file = file;
		this.label = label;
		this.textPane = textPane;
		this.packages = packages;
	}
	
	public File getFile() {
		return file;
	}
	
	public String getFileName() {
		return file.getName();
	}
	
	public String getFilePackage() {
		return packages;
	}
	
	public JLabel getLabel() {
		return label;
	}
	
	public JTextPane getTextPane() {
		return textPane;
	}

}
