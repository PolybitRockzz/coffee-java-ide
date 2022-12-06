package tech.polybit.coffeeide.components;

import java.io.File;

import javax.swing.JLabel;
import javax.swing.JTextPane;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

public class TabComponent {
	
	private File file;
	private JLabel label;
	private RSyntaxTextArea textPane;
	private String packages;
	
	public TabComponent(File file, JLabel label, RSyntaxTextArea textPane, String packages) {
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
	
	public boolean isJavaFile() {
		return packages != null;
	}
	
	public JLabel getLabel() {
		return label;
	}
	
	public RSyntaxTextArea getTextPane() {
		return textPane;
	}

}
