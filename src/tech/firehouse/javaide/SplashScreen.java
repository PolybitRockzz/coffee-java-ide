package tech.firehouse.javaide;

import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;

public class SplashScreen {
	
	static void display() {
		JWindow window = new JWindow();
		window.setBounds(0, 0, 500, 300);
		window.setLocationRelativeTo(null);
		window.setLayout(null);
		window.setAlwaysOnTop(true);
		
		window.getContentPane().setBackground(Info.getThemeColor("dark", 0));
		
		JLabel mainLabel = new JLabel(Info.getName());
		mainLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 40));
		mainLabel.setHorizontalAlignment(SwingConstants.CENTER);
		mainLabel.setForeground(Info.getThemeColor("dark", 3));
		mainLabel.setHorizontalAlignment(SwingConstants.CENTER);
		mainLabel.setBounds((window.getWidth()/2) - 200, 115, 400, 50);
		window.getContentPane().add(mainLabel, SwingConstants.CENTER);
		
		JLabel versionLabel = new JLabel(Info.getFullVersion());
		versionLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
		versionLabel.setHorizontalAlignment(SwingConstants.CENTER);
		versionLabel.setForeground(Info.getThemeColor("dark", 4));
		versionLabel.setHorizontalAlignment(SwingConstants.CENTER);
		versionLabel.setBounds((window.getWidth()/2) - 200, 165, 400, 20);
		
		window.setVisible(true);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		window.getContentPane().add(versionLabel, SwingConstants.CENTER);
		window.revalidate();
		window.repaint();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		window.setVisible(false);
	}

}
