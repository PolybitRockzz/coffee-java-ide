package tech.firehouse.javaide;

import java.awt.Cursor;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.formdev.flatlaf.FlatClientProperties;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ProjectSelection extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2296115734782161248L;

	private String theme;

	private JPanel contentPane;

	private JPanel titlePanel;
	private JPanel projectsPanel;
	private JTextField searchField;
	private JScrollPane projectScrollPane;

	/**
	 * Launch the application.
	 */
	//	public static void main(String[] args) {
	//		EventQueue.invokeLater(new Runnable() {
	//			public void run() {
	//				try {
	//					ProjectSelection frame = new ProjectSelection();
	//					frame.setVisible(true);
	//				} catch (Exception e) {
	//					e.printStackTrace();
	//				}
	//			}
	//		});
	//	}

	/**
	 * Create the frame.
	 */
	public ProjectSelection(String theme) throws Exception {
		this.theme = theme;

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				System.exit(0);
			}
		});
		setTitle(Info.getName() + " - Your Local Projects");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 750, 600);
		setMinimumSize(new Dimension(750, 600));
		setResizable(false);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(null);
		contentPane.setBackground(Info.getThemeColor(theme, 0));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		titlePanel = new JPanel();
		titlePanel.setBounds(0, 0, getWidth(), 100);
		titlePanel.setBackground(Info.getThemeColor(theme, 1));
		contentPane.add(titlePanel);
		titlePanel.setLayout(null);

		String wish = Integer.parseInt(Info.getTime("HH")) < 12 ? "Good Morning" : (Integer.parseInt(Info.getTime("HH")) > 17 ? "Good Evening" : "Good Afternoon");
		wish += ", " + Info.getUser() + " ";
		JLabel userWishLabel = new JLabel(wish);
		userWishLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 20));
		userWishLabel.setHorizontalAlignment(SwingConstants.CENTER);
		userWishLabel.setForeground(Info.getThemeColor(theme, 3));
		userWishLabel.setToolTipText(Info.getTime("HH:mm"));
		userWishLabel.setBounds(getWidth()/2 - 205, 15, 410, 50);
		titlePanel.add(userWishLabel);

		JLabel subTitleLabel = new JLabel(Info.getRandomWishMessage());
		subTitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		subTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		subTitleLabel.setForeground(Info.getThemeColor(theme, 4));
		subTitleLabel.setBounds(getWidth()/2 - 350, 55, 700, 20);
		titlePanel.add(subTitleLabel);

		JLabel projectLabel = new JLabel(Info.getProject().toUpperCase());
		projectLabel.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		projectLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		projectLabel.setHorizontalAlignment(SwingConstants.LEFT);
		projectLabel.setForeground(Info.getThemeColor(theme, 5));
		projectLabel.setBounds(5, 80, 150, 15);
		titlePanel.add(projectLabel);

		JLabel versionLabel = new JLabel(Info.getFullVersion());
		versionLabel.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		versionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		versionLabel.setForeground(Info.getThemeColor(theme, 5));
		versionLabel.setBounds(getWidth() - 95, 80, 75, 15);
		titlePanel.add(versionLabel);

		JButton newProjectButton = new JButton("New Project");
		newProjectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int option = fileChooser.showOpenDialog(null);
				if (option == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					try {
						searchField.setText("");
						addProject(file.getAbsolutePath(), "default");
						contentPane.remove(projectScrollPane);
						setupProjects(getProjectsJSONData(searchField.getText()));
						contentPane.add(projectScrollPane);
						refresh();
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, e1.getMessage(), "Couldn't create new local project!", JOptionPane.ERROR_MESSAGE);
					}
				} else {
					
				}
			}
		});
		newProjectButton.setBounds(50, 110, 115, 30);
		newProjectButton.setBackground(Info.getThemeColor(theme, 0));
		newProjectButton.setForeground(Info.getThemeColor(theme, 4));
		newProjectButton.setIcon(Info.getImage(".\\assets\\plus.png", 15, 15));
		newProjectButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		contentPane.add(newProjectButton);

		searchField = new JTextField();
		searchField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				searchField.setBackground(Info.getThemeColor(theme, 1));
			}
			@Override
			public void focusLost(FocusEvent e) {
				searchField.setBackground(Info.getThemeColor(theme, 0));
			}
		});
		searchField.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				//
			}

			@Override
			public void keyPressed(KeyEvent e) {
				//
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					try {
						contentPane.remove(projectScrollPane);
						setupProjects(getProjectsJSONData(searchField.getText()));
						contentPane.add(projectScrollPane);
						refresh();
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, e1.getMessage(), "Couldn't filter project files!", JOptionPane.ERROR_MESSAGE);
					}
				}
			}

		});
		searchField.setBackground(Info.getThemeColor(theme, 0));
		searchField.setBorder(new JButton().getBorder());
		searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		searchField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, Info.getImage(".\\assets\\search.png", 15, 15));
		searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search in Local Projects [Hit Enter to Search]");
		searchField.setBounds(175, 110, 500, 30);
		contentPane.add(searchField);
		searchField.setColumns(10);

		setupProjects(getProjectsJSONData(""));
		contentPane.add(projectScrollPane);

		setVisible(true);
	}

	private void addFile(String[] filepath, String[] java, String lastModified, int index) {
		JLabel titleLabel = new JLabel(filepath[filepath.length - 1]);

		JPanel panel = new JPanel();
		panel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				panel.setBackground(Info.getThemeColor(theme, 2));
				titleLabel.setForeground(Info.getThemeColor(theme, 3));
				panel.repaint();
			}
			@Override
			public void mouseExited(MouseEvent e) {
				panel.setBackground(Info.getThemeColor(theme, 1));
				titleLabel.setForeground(Info.getThemeColor(theme, 4));
				panel.repaint();
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				setVisible(false);
				ProjectEditor editor = new ProjectEditor(filepath, theme);
				editor.check();
			}
		});
		panel.setBounds(10, 10 + (index*100), projectsPanel.getWidth() - 30, 90);
		panel.setLayout(null);
		panel.setBorder(null);
		panel.setBackground(Info.getThemeColor(theme, 1));
		panel.setToolTipText(Arrays.stream(filepath).collect(Collectors.joining("\\")));
		panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		projectsPanel.add(panel);

		titleLabel.setBounds(15, 10, panel.getWidth() - 20, 30);
		titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 25));
		titleLabel.setForeground(Info.getThemeColor(theme, 4));
		panel.add(titleLabel);

		JLabel javaLabel = new JLabel("Java Runtime: " + java[java.length - 1]);
		javaLabel.setBounds(15, 45, panel.getWidth() - 20, 15);
		javaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		javaLabel.setForeground(Info.getThemeColor(theme, 4));
		panel.add(javaLabel);

		JLabel lastModifiedLabel = new JLabel("Last Modified: " + lastModified);
		lastModifiedLabel.setBounds(15, 65, panel.getWidth() - 20, 15);
		lastModifiedLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		lastModifiedLabel.setForeground(Info.getThemeColor(theme, 4));
		panel.add(lastModifiedLabel);
	}

	private void setupProjects(ArrayList<String[]> data) {
		projectsPanel = new JPanel();
		projectsPanel.setBounds(50, 150, getWidth() - 125, getHeight() - 150);
		projectsPanel.setBackground(Info.getThemeColor(theme, 0));
		projectsPanel.setPreferredSize(new Dimension(getWidth() - 125, 10 + (5*100)));
		projectsPanel.setLayout(null);

		for (int i = 0; i < data.size(); i++) {
			addFile(data.get(i)[0].split("\\\\"), data.get(i)[1].split("\\\\"), data.get(i)[2], i);
		}

		projectsPanel.setPreferredSize(new Dimension(getWidth() - 125, 10 + (data.size()*100)));

		projectScrollPane = new JScrollPane(projectsPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		projectScrollPane.setBounds(50, 150, getWidth() - 125, getHeight() - 200);
		projectScrollPane.setBorder(null);

		refresh();
	}
	
	@SuppressWarnings("unchecked")
	private void addProject(String directory, String java) throws Exception {
		for (String[] delta : getProjectsJSONData("")) {
			if (delta[0].equals(directory)) {
				JOptionPane.showMessageDialog(null, directory + " already exists as a project!", "Couldn't create new local project!", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		try {
			File projects = new File(System.getenv("APPDATA") + "\\" + Info.getName() + "\\projects.json");
			JSONParser parser = new JSONParser();
			try (FileReader reader = new FileReader(projects)) {
				Object obj = parser.parse(reader);
				JSONArray projectList = (JSONArray) obj;
				JSONArray updatedList = projectList;
				JSONObject newProject = new JSONObject();
				newProject.put("project-dir", directory);
				newProject.put("java-dir", "default");
				newProject.put("last-modified", Info.getTime("dd-MM-yyyy HH:mm"));
				updatedList.add(newProject);
				try (FileWriter file = new FileWriter(projects)) {
					file.write(updatedList.toString());
					file.flush();
				}
			}
			File newProjectDirectory = new File(directory);
			if (!newProjectDirectory.exists()) newProjectDirectory.mkdir();
			File newProjectDirectorySrc = new File(directory + "\\src");
			if (!newProjectDirectorySrc.exists()) newProjectDirectorySrc.mkdir();
			File newProjectDirectoryBin = new File(directory + "\\bin");
			if (!newProjectDirectoryBin.exists()) newProjectDirectoryBin.mkdir();
			File newProjectDirectoryLib = new File(directory + "\\lib");
			if (!newProjectDirectoryLib.exists()) newProjectDirectoryLib.mkdir();
			File newProjectDirectoryAssets = new File(directory + "\\assets");
			if (!newProjectDirectoryAssets.exists()) newProjectDirectoryAssets.mkdir();
			File newProjectDirectoryHelloWorld = new File(directory + "\\src\\HelloWorld.java");
			if (!newProjectDirectoryHelloWorld.exists()) newProjectDirectoryHelloWorld.createNewFile();
			try (FileWriter fw = new FileWriter(newProjectDirectoryHelloWorld)) {
				fw.write("class HelloWorld {\n\t\n\tpublic static void main (String[] args) {\n\t\tSystem.out.println(\"Hello, world!\");\n\t}\n\t\n}");
				fw.close();
			}
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage(), "Couldn't update project info!", JOptionPane.ERROR_MESSAGE);
		}
	}

	@SuppressWarnings("unchecked")
	private ArrayList<String[]> getProjectsJSONData(String filter) throws Exception {
		ArrayList<String[]> projectsList = new ArrayList<String[]>();

		File dir = new File(System.getenv("APPDATA") + "\\" + Info.getName());
		if (!dir.exists()) { dir.mkdir(); }
		File projects = new File(dir.getAbsolutePath() + "\\projects.json");
		if (!projects.exists()) {
			projects.createNewFile();
			JSONArray projectList = new JSONArray();
			try (FileWriter file = new FileWriter(projects)) {
				file.write(projectList.toString());
				file.flush();
			}
		}
		JSONParser parser = new JSONParser();
		try (FileReader reader = new FileReader(projects)) {
			Object obj = parser.parse(reader);
			JSONArray projectList = (JSONArray) obj;
			projectList.forEach( emp -> {
				String[] tempData = parseProject((JSONObject) emp, filter);
				if (tempData != null) { projectsList.add(tempData); System.out.println(tempData[0]); }
			} );
		}

		return projectsList;
	}

	private static String[] parseProject(JSONObject project, String filter) {
		String[] data = new String[3];
		data[0] = (String) project.get("project-dir");
		if (!data[0].toLowerCase().split("\\\\")[data[0].split("\\\\").length - 1].startsWith(filter.toLowerCase())) return null;
		data[1] = (String) project.get("java-dir");
		data[2] = (String) project.get("last-modified");
		return data;
	}

	private void refresh() {
		revalidate();
		repaint();
	}

	void check() {
		return;
	}
}
