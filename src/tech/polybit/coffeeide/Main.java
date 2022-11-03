package tech.polybit.coffeeide;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;
import java.util.Scanner;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

public class Main {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		
		try {
			
			SplashScreen.display();
			
			File dir = new File(System.getenv("APPDATA") + "\\" + Info.getName());
			
			JSONParser parser = new JSONParser();
			
			File settings = new File(dir.getAbsolutePath() + "\\settings.json");
			if (!settings.exists()) {
				settings.createNewFile();
				JSONObject settingsObj = new JSONObject();
				settingsObj.put("theme", Info.theme);
				settingsObj.put("tab-size", (long) Info.tabSize);
				try (FileWriter file = new FileWriter(settings)) {
					file.write(settingsObj.toString());
					file.flush();
				}
			}
			parser = new JSONParser();
			try (FileReader reader = new FileReader(settings)) {
				Object obj = parser.parse(reader);
				JSONObject settingsList = (JSONObject) obj;
				Info.theme = (String) settingsList.get("theme");
				Info.tabSize = (int) ((long) settingsList.get("tab-size"));
			}

			try {
				if (Info.theme.equals("dark"))
					UIManager.setLookAndFeel(new FlatDarkLaf());
				else if (Info.theme.equals("light"))
					UIManager.setLookAndFeel(new FlatLightLaf());

				//Frame
				UIManager.put("RootPane.background", Info.getThemeColor(1));
				UIManager.put("TitlePane.centerTitle", true);

				//Button
				UIManager.put("Button.background", Info.getThemeColor(0));
				UIManager.put("Button.hoverBackground", Info.getThemeColor(1));

				//Text Field
				UIManager.put("TextField.background", Info.getThemeColor(0));
				UIManager.put("TextField.focusedBackground", Info.getThemeColor(1));

				//Scroll Bar
				UIManager.put("ScrollBar.track", Info.getThemeColor(1));
				UIManager.put("ScrollBar.hoverTrackColor", Info.getThemeColor(2));
				UIManager.put("ScrollBar.pressedTrackColor", Info.getThemeColor(2));
				UIManager.put("ScrollBar.thumb", Info.getThemeColor(1));
				UIManager.put("ScrollBar.hoverThumbColor", Info.getThemeColor(2));
				UIManager.put("ScrollBar.pressedThumbColor", Info.getThemeColor(2));
			} catch (UnsupportedLookAndFeelException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(), "An Exception Occured!", JOptionPane.ERROR_MESSAGE);
			}
			JFrame.setDefaultLookAndFeelDecorated(true);
			JDialog.setDefaultLookAndFeelDecorated(true);

			doBasicChecks();
			checkLatestVersion();

			ProjectSelection projSelection = new ProjectSelection();
			projSelection.check();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "An Exception Occured!", JOptionPane.ERROR_MESSAGE);
			//e.printStackTrace();
		}
	}

	private static void doBasicChecks() throws Exception {
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		if (width < 1366 || height < 768) {
			JOptionPane.showMessageDialog(null,"Your screen resolution is too low for this application to run. \nYour monitor needs to have a minimum resolution of 1366x768 (in pixels). \nSorry for that :(","Couldn't launch " + Info.getName() + " [" + Info.getVersion() + "]",JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
	
	private static void checkLatestVersion() {
		try {
			URL url = new URL("https://pastebin.com/raw/wHNdqbLr");
			Scanner sc = new Scanner(url.openStream());
			String x = sc.nextLine();
			String rawData = (String) ((JSONObject) new JSONParser().parse(x)).get("version");
			String whatsNew = (String) ((JSONObject) new JSONParser().parse(x)).get("whats-new");
			if (!rawData.equals(Info.getVersion()))
				JOptionPane.showMessageDialog(null, "<html><b>A new update is available for " + Info.getName() + "!</b></html>\nCurrent Version: v" + Info.getVersion() + "\nLatest Version: v" + rawData + "\n<html><b>What's New?</b></html>\n" + whatsNew.replaceAll("\\n", "\n"), "Update Now!", JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception e) {}
	}

}
