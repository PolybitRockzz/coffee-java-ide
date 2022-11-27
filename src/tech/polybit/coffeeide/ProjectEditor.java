package tech.polybit.coffeeide;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JSplitPane;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import tech.polybit.coffeeide.components.Contact;
import tech.polybit.coffeeide.components.MyTreeCellRenderer;

import javax.swing.JOptionPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JButton;

public class ProjectEditor extends JFrame {

	private static final long serialVersionUID = 3364502009512038342L;
	private JPanel contentPane;
	private JPanel editorPanel;
	private JTabbedPane tabbedPane;
	private JLabel noOpenTabsLabel;
	private JScrollPane directoryTreeScrollPane;
	private JTree fileTree;

	private JButton saveButton;
	private JButton compileButton;

	private String[] filepath;
	private String filepathStr;

	private String[] ignore = {"bin", ".class", "run.bat", ".settings"};

	private ArrayList<TabComponent> tabs;

	// Constructor
	public ProjectEditor(String[] filepath) {
		this.filepath = filepath;
		this.filepathStr = Arrays.stream(filepath).collect(Collectors.joining("\\"));
		display();
	}

	// Main Display Function
	public void display() {

		// Initializing ArrayLists
		tabs = new ArrayList<TabComponent>();

		addWindowListener(new WindowAdapter() {

			@SuppressWarnings("unchecked")
			@Override
			public void windowClosing(WindowEvent e) {

				// Check for unsaved changes
				for (TabComponent obj1 : tabs) {
					if (obj1.getLabel().getText().startsWith("*")) {
						int c1 = JOptionPane.showConfirmDialog(null,
								"Looks like you have unsaved changes. Do you want to save them before quitting?",
								"You didn't save all files!", JOptionPane.WARNING_MESSAGE);
						if (c1 == JOptionPane.YES_OPTION) {
							for (TabComponent obj2 : tabs) {
								if (obj2.getLabel().getText().startsWith("*")) {
									try {
										saveFile(tabs.get(tabs.indexOf(obj2)));
									} catch (Exception e1) {
										JOptionPane.showMessageDialog(null, e1.getMessage(), "Unable to Save File!", JOptionPane.ERROR_MESSAGE);
									}
								}
							}
						}
						break;
					}
				}

				// Save progress
				try {
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
						JSONArray updatedList = new JSONArray();
						for (Object obj1 : projectList) {
							JSONObject jsonObj = (JSONObject) obj1;
							if (jsonObj.get("project-dir").equals(filepathStr)) {
								jsonObj.replace("last-modified", Info.getTime("dd-MM-yyyy HH:mm"));
							}
							updatedList.add(jsonObj);
						}
						try (FileWriter file = new FileWriter(projects)) {
							file.write(updatedList.toString());
							file.flush();
						}
					}
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(null, e1.getMessage(), "Couldn't update project info!", JOptionPane.ERROR_MESSAGE);
				}

				setVisible(false);
				System.exit(0);
			}
		});
		setTitle(Info.getName() + " - " + filepathStr);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 1000, 600);
		setMinimumSize(new Dimension(900, 650));
		setExtendedState(JFrame.MAXIMIZED_BOTH);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setBackground(Info.getThemeColor(0));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JSplitPane workspaceSplitPane = new JSplitPane();
		workspaceSplitPane.setDividerLocation(300);
		workspaceSplitPane.setBackground(Info.getThemeColor(0));
		contentPane.add(workspaceSplitPane, BorderLayout.CENTER);

		directoryTreeScrollPane = new JScrollPane();
		directoryTreeScrollPane.setMinimumSize(new Dimension(200, 600));
		directoryTreeScrollPane.setBackground(Info.getThemeColor(1));
		directoryTreeScrollPane.setBorder(null);
		workspaceSplitPane.setLeftComponent(directoryTreeScrollPane);

		setupFileTree();

		JPanel packageExplorerHeaderPanel = new JPanel();
		packageExplorerHeaderPanel.setBackground(Info.getThemeColor(0));
		directoryTreeScrollPane.setColumnHeaderView(packageExplorerHeaderPanel);

		JLabel packageExplorerLabel = new JLabel("Package Explorer");
		packageExplorerLabel.setHorizontalAlignment(SwingConstants.CENTER);
		packageExplorerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		packageExplorerLabel.setForeground(Info.getThemeColor(4));
		packageExplorerLabel.setPreferredSize(new Dimension((int) packageExplorerLabel.getPreferredSize().getWidth(), 25));
		packageExplorerHeaderPanel.add(packageExplorerLabel);

		editorPanel = new JPanel();
		workspaceSplitPane.setRightComponent(editorPanel);
		editorPanel.setLayout(new BorderLayout(0, 0));
		editorPanel.setMinimumSize(new Dimension(800, 600));
		editorPanel.setBackground(Info.getThemeColor(1));

		JPanel editorQuickAccessPanel = new JPanel();
		editorQuickAccessPanel.setBackground(Info.getThemeColor(0));
		editorQuickAccessPanel.setLayout(new BorderLayout(0, 0));
		editorPanel.add(editorQuickAccessPanel, BorderLayout.NORTH);

		JLabel emptyQuickAccessLabel = new JLabel(" ");
		emptyQuickAccessLabel.setHorizontalAlignment(SwingConstants.CENTER);
		emptyQuickAccessLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		emptyQuickAccessLabel.setForeground(Info.getThemeColor(4));
		editorQuickAccessPanel.add(emptyQuickAccessLabel);

		JPanel leftPanel = new JPanel();
		leftPanel.setBackground(Info.getThemeColor(0));
		editorQuickAccessPanel.add(leftPanel, BorderLayout.WEST);

		JPanel rightPanel = new JPanel();
		rightPanel.setBackground(Info.getThemeColor(0));
		editorQuickAccessPanel.add(rightPanel, BorderLayout.EAST);

		JButton refreshButton = new JButton();
		refreshButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setupFileTree();
				refresh();
				System.gc();
			}

		});
		refreshButton.setFocusable(false);
		refreshButton.setBackground(Info.getThemeColor(0));
		refreshButton.setForeground(Info.getThemeColor(4));
		refreshButton.setIcon(Info.getImage(".\\assets\\images\\refresh.png", 15, 15));
		refreshButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		leftPanel.add(refreshButton);

		JButton newClassButton = new JButton("New Class");
		newClassButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				createNewClass();
			}

		});
		newClassButton.setFocusable(false);
		newClassButton.setBackground(Info.getThemeColor(0));
		newClassButton.setForeground(Info.getThemeColor(4));
		newClassButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		leftPanel.add(newClassButton);

		saveButton = new JButton("Save File");
		saveButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (tabbedPane.getTabCount() == 0) return;
				try {
					saveFile(tabs.get(tabbedPane.getSelectedIndex()));
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(null, e1.getMessage(), "Unable to Save File!", JOptionPane.ERROR_MESSAGE);
				}
			}

		});
		saveButton.setFocusable(false);
		saveButton.setBackground(Info.getThemeColor(0));
		saveButton.setForeground(Info.getThemeColor(4));
		saveButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		leftPanel.add(saveButton);

		JButton deleteButton = new JButton("Delete File");
		deleteButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (tabbedPane.getTabCount() == 0) return;
				try {
					deleteFile(tabs.get(tabbedPane.getSelectedIndex()));
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(null, e1.getMessage(), "Unable to Delete File!", JOptionPane.ERROR_MESSAGE);
				}
			}

		});
		deleteButton.setFocusable(false);
		deleteButton.setBackground(Info.getThemeColor(0));
		deleteButton.setForeground(Info.getThemeColor(4));
		deleteButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		leftPanel.add(deleteButton);

		compileButton = new JButton("Compile");
		compileButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (tabbedPane.getTabCount() == 0) return;
				try {
					compileFile(tabs.get(tabbedPane.getSelectedIndex()));
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(null, e1.getMessage(), "Unable to Compile File!", JOptionPane.ERROR_MESSAGE);
				}
			}

		});
		compileButton.setFocusable(false);
		compileButton.setBackground(Info.getThemeColor(0));
		compileButton.setForeground(Info.getThemeColor(4));
		compileButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		if (Info.autoCompile) {
			compileButton.setEnabled(false);
			compileButton.setText("Auto Compile is Enabled");
		}
		rightPanel.add(compileButton);

		JButton runButton = new JButton("Run");
		runButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (tabbedPane.getTabCount() == 0) return;
				try {
					runFile(tabs.get(tabbedPane.getSelectedIndex()));
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(null, e1.getMessage(), "Unable to Run File!", JOptionPane.ERROR_MESSAGE);
				}
			}

		});
		runButton.setFocusable(false);
		runButton.setBackground(Info.getThemeColor(0));
		runButton.setForeground(Info.getThemeColor(4));
		runButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		rightPanel.add(runButton);

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBackground(Info.getThemeColor(1));

		noOpenTabsLabel = new JLabel("No tabs are open. Double click a file from the Project Explorer to get started!");
		noOpenTabsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		noOpenTabsLabel.setForeground(Info.getThemeColor(5));
		noOpenTabsLabel.setHorizontalAlignment(SwingConstants.CENTER);

		checkIfNoOpenTabs();
		setVisible(true);
	}

	// Document Listener
	private DocumentListener getDocumentListener() {
		return new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateLog(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateLog(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
			}

			private void updateLog(DocumentEvent e) {
				try {
					if (!tabs.get(tabbedPane.getSelectedIndex()).getLabel().getText().startsWith("*"))
						tabs.get(tabbedPane.getSelectedIndex()).getLabel().setText("*" + tabs.get(tabbedPane.getSelectedIndex()).getLabel().getText());
				} catch (Exception e1) {
					//
				}
			}

		};
	}

	// Setting up the Project Explorer
	private void setupFileTree() {
		DefaultMutableTreeNode dirTree = new DefaultMutableTreeNode(filepath[filepath.length - 1]);
		File currentDir = new File(filepathStr);
		File[] folders = currentDir.listFiles();
		for (File i : folders) {
			if (Arrays.stream(ignore).anyMatch(i.getName()::endsWith))
				continue;
			dirTree.add(getFiles(i.getAbsolutePath()));
		}

		fileTree = new JTree(dirTree);
		fileTree.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int selRow = fileTree.getRowForLocation(e.getX(), e.getY());
				TreePath selPath = fileTree.getPathForLocation(e.getX(), e.getY());
				if (selRow != -1) {
					if (e.getClickCount() == 2)
						try {
							openFile(selPath.getPath());
						} catch (Exception e1) {
							JOptionPane.showMessageDialog(null, e1.getMessage(), "Couldn't Open File!", JOptionPane.ERROR_MESSAGE);
						}
				}
			}
		});
		fileTree.setCellRenderer(new MyTreeCellRenderer());
		fileTree.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		fileTree.setForeground(Info.getThemeColor(4));
		fileTree.setBackground(Info.getThemeColor(1));
		directoryTreeScrollPane.setViewportView(fileTree);

		refresh();
	}

	// Creating a new class
	private void createNewClass() {
		String className = JOptionPane.showInputDialog(null, "Enter the class name (recommended using Camel Casing)", "Input Class Name", JOptionPane.PLAIN_MESSAGE);
		if (className == null) return;
		for (int i = 0; i < className.length(); i++) {
			char ch = className.charAt(i);
			int chx = (int) ch;
			if ((chx >= 48 && chx <= 57) || (chx >= 65 && chx <= 90) || (chx >= 97 && chx <= 122)) {
				continue;
			} else if (i == 0 && (chx >= 48 && chx <= 57)) {
				JOptionPane.showMessageDialog(null, "The first character in the class name cannot be a numeric digit", "Couldn't parse Class Name!", JOptionPane.ERROR_MESSAGE);
				return;
			} else {
				JOptionPane.showMessageDialog(null, "Make sure the characters only include (A-Z), (a-z) or (0-9)", "Couldn't parse Class Name!", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		File file = new File(filepathStr + "\\src\\" + className + ".java");
		if (!file.exists())
			try {
				file.createNewFile();
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage(), "Couldn't create new Class!", JOptionPane.ERROR_MESSAGE);
			}
		try {
			FileWriter fw = new FileWriter(file);
			fw.write("class " + className + " {\n\t\n\tpublic static void main (String[] args) {\n\t\tSystem.out.println(\"Hello, world!\");\n\t}\n\t\n}");
			fw.close();
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage(), "Couldn't create Class!", JOptionPane.ERROR_MESSAGE);
		}

		setupFileTree();
	}

	// Save a File
	private void saveFile(TabComponent tab) throws Exception {
		FileWriter fw = new FileWriter(tab.getFile());
		fw.write(tab.getTextPane().getText());
		fw.close();
		tab.getLabel().setText(tab.getFileName());
		if (Info.autoCompile) {
			compileFile(tab);
		}
	}

	// Delete a File
	private void deleteFile(TabComponent tab) {
		int choice = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete " + tab.getFileName() + "?", "Confirmation for Deletion of File!", JOptionPane.WARNING_MESSAGE);
		if (choice == JOptionPane.YES_OPTION) {
			File file = tab.getFile();
			file.delete();
			tabs.remove(tabbedPane.getSelectedIndex());
			tabbedPane.remove(tabbedPane.getSelectedIndex());
			checkIfNoOpenTabs();
			setupFileTree();
		}
	}

	// Compile File
	private void compileFile(TabComponent tab) throws Exception {
		if (tab.getFilePackage() == null) return;
		File file = new File(filepathStr + "\\run.bat");
		if (!file.exists()) file.createNewFile();
		FileWriter writer = new FileWriter(file);
		String command = "";
		command += "javac -d bin -cp lib\\* src\\" + tab.getFilePackage() + tab.getFileName() + "\npause\nexit";
		writer.write(command);
		writer.close();
		ProcessBuilder builder = new ProcessBuilder(new String[] {"cmd", "/c", "start", "run.bat"});
		builder.directory(new File(filepathStr));
		Process p = builder.start();
		p.info();
	}

	// Run File
	private void runFile(TabComponent tab) throws Exception {
		if (tab.getFilePackage() == null) return;
		File file = new File(filepathStr + "\\run.bat");
		if (!file.exists()) file.createNewFile();
		FileWriter writer = new FileWriter(file);
		String command = "";
		command += "java -cp \"" + filepathStr + "\\lib\\*;bin\"; " +
				tabs.get(tabbedPane.getSelectedIndex()).getFilePackage().replaceAll("\\\\", ".") +
				tabs.get(tabbedPane.getSelectedIndex()).getLabel().getText().replace(".java", "");
		command += "\npause\nexit";
		writer.write(command);
		writer.close();
		ProcessBuilder builder = new ProcessBuilder(new String[] {"cmd", "/c", "start", "run.bat"});
		builder.directory(new File(filepathStr));
		Process p = builder.start();
		p.info();
	}

	// Returns the file structure
	private DefaultMutableTreeNode getFiles(String str) {
		File file = new File(str);
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new Contact(file.getName(), file.isFile()));
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File i : files) {
				DefaultMutableTreeNode tempNode = getFiles(i.getAbsolutePath());
				node.add(tempNode);
			}
		}
		return node;
	}

	// Checks if the file is available
	private void openFile(Object[] fileArr) throws Exception {
		String[] strFile = new String[fileArr.length - 1];
		for (int i = 0; i < strFile.length; i++) strFile[i] = fileArr[i + 1].toString();
		String filedir = Arrays.stream(strFile).collect(Collectors.joining("\\"));
		File file = new File(filepathStr + "\\" + filedir);
		if (!file.isDirectory()) {
			createNewTab(file, filedir);
		}
	}

	// Creates a new tab for a file
	private void createNewTab(File file, String filedir) throws Exception {
		for (TabComponent tab : tabs) {
			if (file.getCanonicalPath().contains(tab.getFilePackage() + tab.getFileName()))
				return;
		}
		
		String text = "";
		FileReader reader = new FileReader(file);
		int i;
		while ((i = reader.read()) != -1)
			text += (char) i;
		reader.close();

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		tabbedPane.addTab(file.getName(), null, scrollPane, file.getAbsolutePath());
		scrollPane.setBorder(null);
		tabbedPane.setSelectedComponent(scrollPane);

		JLabel tabLabel = new JLabel(file.getName());
		tabLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		tabLabel.setForeground(Info.getThemeColor(4));
		tabLabel.setPreferredSize(new Dimension((int) tabLabel.getPreferredSize().getWidth() + 10, 25));

		JPanel tabComponent = new JPanel(new BorderLayout());
		tabComponent.setOpaque(false);
		tabComponent.setBackground(new Color(0,0,0,0));

		tabComponent.add(tabLabel, BorderLayout.WEST);

		JTextPane editorPane;
		if (file.getName().endsWith(".java")) {
			DefaultStyledDocument javadoc = Documents.getJavaStyledDoc(tabs, tabbedPane);
			javadoc.addDocumentListener(getDocumentListener());
			editorPane = new JTextPane(javadoc);
		} else {
			DefaultStyledDocument doc = Documents.getDefaultStyledDoc(tabs, tabbedPane);
			doc.addDocumentListener(getDocumentListener());
			editorPane = new JTextPane(doc);
		}
		editorPane.setText(text);
		editorPane.setBackground(Info.getThemeColor(1));
		editorPane.setForeground(Info.getThemeColor(4));
		editorPane.setBorder(null);
		editorPane.setFont(new Font("Consolas", Font.PLAIN, 14));
		editorPane.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {

			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.isControlDown()) {
					if (e.getKeyCode() == KeyEvent.VK_S)
						saveButton.doClick();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {

			}

		});

		TextLineNumber tln = new TextLineNumber(editorPane);
		tln.setBackground(Info.getThemeColor(1));
		tln.setUpdateFont(true);
		tln.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		tln.setCurrentLineForeground(Info.getThemeColor(7));
		tln.setBorder(new MatteBorder(0, 0, 0, 1, Info.getThemeColor(5)));
		scrollPane.setRowHeaderView(tln);

		setTabs(editorPane, Info.tabSize);

		JButton closeButton = new JButton();
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JButton button = (JButton)e.getSource();
				for(int i = 0; i < tabbedPane.getTabCount(); i++) {
					if(button.getParent() == tabbedPane.getTabComponentAt(i)) {
						if (tabs.get(i).getLabel().getText().startsWith("*")) {
							int choice = JOptionPane.showConfirmDialog(null, "Want to save " + tabs.get(i).getFileName() + " before closing?", "File isn't saved", JOptionPane.WARNING_MESSAGE);
							if (choice == JOptionPane.YES_OPTION) {
								saveButton.doClick();
							}
						}
						tabbedPane.remove(i);
						tabs.remove(i);
						checkIfNoOpenTabs();
						break;
					}
				}
			}
		});
		closeButton.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				closeButton.setIcon(Info.getImage(".\\assets\\images\\cross-hover.png", 10, 10));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				closeButton.setIcon(Info.getImage(".\\assets\\images\\cross.png", 10, 10));
			}
			
		});
		closeButton.setHorizontalAlignment(SwingConstants.CENTER);
		closeButton.setIcon(Info.getImage(".\\assets\\images\\cross.png", 10, 10));
		closeButton.setMinimumSize(new Dimension(15, 15));
		closeButton.setFocusable(false);
		closeButton.setOpaque(false);
		closeButton.setContentAreaFilled(false);
		closeButton.setBorderPainted(false);
		tabComponent.add(closeButton, BorderLayout.CENTER);
		
		tabbedPane.add(scrollPane);
		tabbedPane.setTabComponentAt(tabs.size(), tabComponent);
		tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);

		scrollPane.setViewportView(editorPane);
		if (file.getName().endsWith(".java"))
			tabs.add(new TabComponent(file, tabLabel, editorPane, filedir.substring(4, filedir.length() - file.getName().length())));
		else
			tabs.add(new TabComponent(file, tabLabel, editorPane, null));
		checkIfNoOpenTabs();
	}

	// Checks if no tabs are open
	private void checkIfNoOpenTabs() {
		if(tabs.size() == 0) {
			editorPanel.remove(tabbedPane);
			editorPanel.add(noOpenTabsLabel, BorderLayout.CENTER);
		} else {
			editorPanel.remove(noOpenTabsLabel);
			editorPanel.add(tabbedPane, BorderLayout.CENTER);
		}
		refresh();
	}

	// Sets the indentation count in the text editor
	private static void setTabs(final JTextPane textPane, int charactersPerTab) {
		FontMetrics fm = textPane.getFontMetrics( textPane.getFont() );
		int charWidth = fm.charWidth( ' ' );
		int tabWidth = charWidth * charactersPerTab;

		TabStop[] tabs = new TabStop[5];

		for (int j = 0; j < tabs.length; j++)
		{
			int tab = j + 1;
			tabs[j] = new TabStop( tab * tabWidth );
		}

		TabSet tabSet = new TabSet(tabs);
		SimpleAttributeSet attributes = new SimpleAttributeSet();
		StyleConstants.setTabSet(attributes, tabSet);
		int length = textPane.getDocument().getLength();
		textPane.getStyledDocument().setParagraphAttributes(0, length, attributes, false);
	}

	private void refresh() {
		revalidate();
		repaint();
	}

	void check() {
		return;
	}

}
