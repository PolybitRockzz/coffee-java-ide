package tech.firehouse.javaide;

import java.awt.Color;
import java.awt.Image;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.ImageIcon;

public class Info {
	
	public static String getName() {
		return "Coffee IDE";
	}
	
	public static String getProject() {
		return "The Firehouse Project";
	}
	
	public static String getUser() {
		return System.getProperty("user.name");
	}
	
	public static String getVersion() {
		return "0.1.0";
	}
	
	public static String getFullVersion() {
		return "v0.1.0 ALPHA";
	}
	
	public static String getTime(String str) {
		SimpleDateFormat format = new SimpleDateFormat(str);
		Date date = new Date();
		return format.format(date);
	}
	
	public static String getRandomWishMessage() {
		String[] messages = {
				"Pick up from where you left off!",
				"Don't forget to end with a semicolon!",
				"I heard that r/learnjava is a good subreddit!",
				"Tea or Coffee?",
				"Compiler's never wrong, brotha!",
				"NullPointerException",
				"ثق في الرب ، وقم بتشغيل الكود",
				"Made for Java, by Java! Huh!",
				"It's surprising you haven't switched to Python yet...",
				"You know you could've chosen a better IDE, right?",
				"Ever tried Linux?",
				"We are open sourced btw, check out our GitHub page!",
				"public static void main (String[] args)",
				"Shhh... we're secretly mining crypto...",
				"Default packages are discouraged",
				"Take a break, you don't deserve it, but still...",
				"sus"
		};
		return messages[(int) (Math.random() * messages.length)];
	}
	
	public static ImageIcon getImage(String path, int w, int h) {
		ImageIcon imageIcon = new ImageIcon(path);
		Image image = imageIcon.getImage();
		Image newimg = image.getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH);
		imageIcon = new ImageIcon(newimg);
		return imageIcon;
	}
	
	public static Color getThemeColor(String theme, int shade) {
		Color[] darkTheme = {
				new Color(25, 25, 25), // Dark +
				new Color(35, 35, 35), // Dark
				new Color(45, 45, 45), // Dark :: Hover
				new Color(255, 255, 255), // Font Priority
				new Color(235, 235, 235), // Font
				new Color(135, 135, 135), // Font Minor
				new Color(232, 86, 86), // Font Keyword
				new Color(5, 117, 173), // Font Number
				new Color(207, 127, 8), // Font Special Symbols
				new Color(27, 198, 128), // Font Strings
		};
		if (theme.equals("dark"))
			return darkTheme[shade];
		return null;
	}

}
