package tech.polybit.coffeeide;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;
import java.util.Scanner;

import javax.swing.JOptionPane;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import tech.polybit.coffeeide.components.Info;
import tech.polybit.coffeeide.menu.ProjectSelection;

public class Main {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		
		try {
			
			SplashScreen.display();
			
			File dir = new File(System.getenv("APPDATA") + "\\" + Info.getName());
			if (!dir.exists()) dir.mkdirs();
			
			JSONParser parser = new JSONParser();
			
			File settings = new File(dir.getAbsolutePath() + "\\settings.json");
			if (!settings.exists()) {
				settings.createNewFile();
				JSONObject settingsObj = new JSONObject();
				settingsObj.put("theme", Info.theme);
				settingsObj.put("tab-size", (long) Info.tabSize);
				settingsObj.put("auto-compile", Info.autoCompile);
				settingsObj.put("java-template", Info.javaTemplate);
				try (FileWriter file = new FileWriter(settings)) {
					file.write(settingsObj.toString());
					file.flush();
				}
			}
			parser = new JSONParser();
			try (FileReader reader = new FileReader(settings)) {
				Object obj = parser.parse(reader);
				JSONObject settingsList = (JSONObject) obj;
				try {
					Info.theme = (String) settingsList.get("theme");
					Info.tabSize = (int) ((long) settingsList.get("tab-size"));
					Info.autoCompile = (boolean) settingsList.get("auto-compile");
					Info.javaTemplate = (String) settingsList.get("java-template");
				} catch (Exception e) {
					JSONObject settingsObj = new JSONObject();
					settingsObj.put("theme", Info.theme);
					settingsObj.put("tab-size", (long) Info.tabSize);
					settingsObj.put("auto-compile", Info.autoCompile);
					settingsObj.put("java-template", Info.javaTemplate);
					try (FileWriter file = new FileWriter(settings)) {
						file.write(settingsObj.toString());
						file.flush();
					}
				}
			}

			Info.setUILookAndFeel();

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
		if (width < 1280 || height < 768) {
			JOptionPane.showMessageDialog(null,"Your screen resolution is too low for this application to run. \nYour monitor needs to have a minimum resolution of 1280x1024 or 1366x768 (in pixels). \nSorry for that :(","Couldn't launch " + Info.getName() + " [" + Info.getVersion() + "]",JOptionPane.ERROR_MESSAGE);
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
