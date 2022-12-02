package tech.polybit.coffeeide;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.json.simple.JSONObject;

public class Settings extends JDialog {

	private static final long serialVersionUID = -1260305299291649742L;
	private final JPanel contentPanel = new JPanel();

	public Settings(ProjectSelection frame) {
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		setResizable(false);
		setTitle("Settings");
		setModal(true);
		setLocationRelativeTo(null);
		contentPanel.setLayout(null);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPanel.setBackground(Info.getThemeColor(0));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		
		JLabel themeLabel = new JLabel("Theme");
		themeLabel.setBounds(20, 30, 100, 25);
		themeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		themeLabel.setForeground(Info.getThemeColor(4));
		contentPanel.add(themeLabel);
		
		String[] themes = {Info.theme.equals("dark") ? "dark" : "light", Info.theme.equals("dark") ? "light" : "dark"};
		
		JComboBox<String> themeComboBox = new JComboBox<String>(themes);
		themeComboBox.setBounds(getWidth() - 150, 30, 100, 25);
		themeComboBox.setSelectedIndex(0);
		themeComboBox.setMaximumSize(themeComboBox.getPreferredSize());
		themeComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		themeComboBox.setForeground(Info.getThemeColor(4));
		themeComboBox.setBackground(Info.getThemeColor(0));
		contentPanel.add(themeComboBox);
		
		JLabel tabSizeLabel = new JLabel("Tab Size");
		tabSizeLabel.setBounds(20, 70, 100, 25);
		tabSizeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		tabSizeLabel.setForeground(Info.getThemeColor(4));
		contentPanel.add(tabSizeLabel);
		
		Integer[] tabSizes = {2, 4, 6, 8, 10, 12, 14, 16};
		
		JComboBox<Integer> tabSizeComboBox = new JComboBox<Integer>(tabSizes);
		tabSizeComboBox.setBounds(getWidth() - 150, 70, 100, 25);
		tabSizeComboBox.setSelectedIndex((Info.tabSize/2) - 1);
		tabSizeComboBox.setMaximumSize(tabSizeComboBox.getPreferredSize());
		tabSizeComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		tabSizeComboBox.setForeground(Info.getThemeColor(4));
		tabSizeComboBox.setBackground(Info.getThemeColor(0));
		contentPanel.add(tabSizeComboBox);
		
		JLabel autoCompileLabel = new JLabel("Auto Compile on Save");
		autoCompileLabel.setBounds(20, 110, 150, 25);
		autoCompileLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		autoCompileLabel.setForeground(Info.getThemeColor(4));
		contentPanel.add(autoCompileLabel);
		
		JCheckBox autoCompileCheckbox = new JCheckBox();
		autoCompileCheckbox.setSelected(Info.autoCompile);
		autoCompileCheckbox.setBounds(getWidth() - 150, 110, 100, 25);
		autoCompileCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		autoCompileCheckbox.setForeground(Info.getThemeColor(4));
		autoCompileCheckbox.setHorizontalAlignment(SwingConstants.CENTER);
		contentPanel.add(autoCompileCheckbox);
		
		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent e) {
				Info.theme = themes[themeComboBox.getSelectedIndex()];
				Info.tabSize = tabSizes[tabSizeComboBox.getSelectedIndex()];
				Info.autoCompile = autoCompileCheckbox.isSelected();
				
				File dir = new File(System.getenv("APPDATA") + "\\" + Info.getName());
				if (!dir.exists()) dir.mkdirs();
				
				//Saves settings
				File settings = new File(dir.getAbsolutePath() + "\\settings.json");
				JSONObject settingsObj = new JSONObject();
				settingsObj.put("theme", Info.theme);
				settingsObj.put("tab-size", (long) Info.tabSize);
				settingsObj.put("auto-compile", Info.autoCompile);
				try (FileWriter file = new FileWriter(settings)) {
					file.write(settingsObj.toString());
					file.flush();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, e1.getMessage(), "An Exception Occured!", JOptionPane.ERROR_MESSAGE);
				}
				
				Info.setUILookAndFeel();
				SwingUtilities.updateComponentTreeUI(frame);
				SwingUtilities.updateComponentTreeUI(Settings.this);
				
				JOptionPane.showMessageDialog(null, "Your settings have been saved successfully!\nRefresh the IDE for all changes to be visible...", "Settings Configuration Success",JOptionPane.INFORMATION_MESSAGE);
			}
			
		});
		saveButton.setBounds(getWidth() - 150, getHeight() - 75, 100, 25);
		saveButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		saveButton.setForeground(Info.getThemeColor(4));
		saveButton.setBackground(Info.getThemeColor(0));
		contentPanel.add(saveButton);
		
		setVisible(true);
	}
	
	public void check() {
		return;
	}

}
