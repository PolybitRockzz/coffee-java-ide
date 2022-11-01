package tech.polybit.coffeeide;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JSplitPane;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Robot;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.swing.JOptionPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

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

	/**
	 * 
	 */
	private static final long serialVersionUID = 3364502009512038342L;
	private JPanel contentPane;
	private JPanel editorPanel;
	private JTabbedPane tabbedPane;
	private JLabel noOpenTabsLabel;
	private JScrollPane directoryTreeScrollPane;
	private JTree fileTree;

	private JButton saveButton;

	private String[] filepath;
	private final StyleContext styleContext;
	private final AttributeSet attributeKeyword, attributeSpecialSymbols, attributeNumbers, attributeComments, attributeStrings, attributeNormal;

	private String[] ignore = {"bin", ".class", "run.bat", ".settings"};
	private String[] keywords = {
			"abstract", "assert", "boolean", "break", "byte", "case",
			"catch", "char", "class", "continue", "default", "do",
			"double", "else", "enum", "extends", "final", "finally",
			"float", "for", "if", "implements", "import", "instanceof",
			"int", "interface", "long", "native", "new", "null", "package",
			"private", "protected", "public", "return", "short", "static",
			"strictfp", "super", "switch", "synchronized", "this", "throw",
			"throws", "transient", "try", "void", "volatile", "while",
			"const", "goto"
	};
	private String[] symbols = {
			"+", "-", "*", "/", "%", "=", "?", ":", ";",
			"{", "}", "."
	};

	private String[] autoCloseTrigger = {
			"()", "\"\"", "\'\'", "{}", "[]"
	};

	private ArrayList<String> openTabs;
	private ArrayList<JLabel> nameLists;
	private ArrayList<JTextPane> documents;

	// Constructor
	public ProjectEditor(String[] filepath) {
		this.filepath = filepath;
		
		styleContext = StyleContext.getDefaultStyleContext();
		attributeKeyword = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Info.getThemeColor(6));
		attributeSpecialSymbols = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Info.getThemeColor(8));
		attributeNumbers = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Info.getThemeColor(7));
		attributeComments = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Info.getThemeColor(5));
		attributeStrings = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Info.getThemeColor(9));
		attributeNormal = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Info.getThemeColor(4));
		display();
	}

	// Main Display Function
	public void display() {

		// Initializing ArrayLists
		openTabs = new ArrayList<String>();
		nameLists = new ArrayList<JLabel>();
		documents = new ArrayList<JTextPane>();

		addWindowListener(new WindowAdapter() {

			@SuppressWarnings("unchecked")
			@Override
			public void windowClosing(WindowEvent e) {

				// Check for unsaved changes
				for (Object obj1 : nameLists.toArray()) {
					JLabel label = (JLabel) obj1;
					if (label.getText().startsWith("*")) {
						int c1 = JOptionPane.showConfirmDialog(null, "Looks like you have unsaved changes. Do you want to save them before quitting?", "You didn't save all files!", JOptionPane.WARNING_MESSAGE);
						if (c1 == JOptionPane.YES_OPTION) {
							for (Object obj2 : nameLists.toArray()) {
								JLabel label1 = (JLabel) obj2;
								if (label1.getText().startsWith("*")) {
									try {
										saveFile(openTabs.get(nameLists.indexOf(obj2)));
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
							if (jsonObj.get("project-dir").equals(Arrays.stream(filepath).collect(Collectors.joining("\\")))) {
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
		setTitle(Info.getName() + " - " + Arrays.stream(filepath).collect(Collectors.joining("\\")));
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
		refreshButton.setIcon(Info.getImage(".\\assets\\refresh.png", 15, 15));
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
					saveFile(openTabs.get(tabbedPane.getSelectedIndex()));
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
					deleteFile(openTabs.get(tabbedPane.getSelectedIndex()));
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

		JButton compileButton = new JButton("Compile");
		compileButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (tabbedPane.getTabCount() == 0) return;
				try {
					compileFile(openTabs.get(tabbedPane.getSelectedIndex()));
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(null, e1.getMessage(), "Unable to Compile File!", JOptionPane.ERROR_MESSAGE);
				}
			}

		});
		compileButton.setFocusable(false);
		compileButton.setBackground(Info.getThemeColor(0));
		compileButton.setForeground(Info.getThemeColor(4));
		compileButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		rightPanel.add(compileButton);

		JButton runButton = new JButton("Run");
		runButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (tabbedPane.getTabCount() == 0) return;
				try {
					runFile(openTabs.get(tabbedPane.getSelectedIndex()));
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

	//Initialize Document (for Styling)
	private DefaultStyledDocument initializeDoc() {
		return new DefaultStyledDocument() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 5172740319640565443L;

			public void insertString (int offset, String str, AttributeSet a) throws BadLocationException {
				super.insertString(offset, str, a);

				String text = getText(0, getLength());
				
				setCharacterAttributes(0, text.length(), attributeNormal, false);

				int wordL = 0;
				int wordR = 0;

				while (wordR <= text.length() - 1) {
					if (String.valueOf(text.charAt(wordR)).matches("\\W")) {
						if (text.substring(wordL, wordR).matches("(\\W)*(" + Arrays.stream(keywords).collect(Collectors.joining("|")) + ")"))
							setCharacterAttributes(wordL, wordR - wordL, attributeKeyword, false);
						else if (text.substring(wordL, wordR).matches("(\\W)*(\\d)"))
							setCharacterAttributes(wordL, wordR - wordL, attributeNumbers, false);
						else
							setCharacterAttributes(wordL, wordR - wordL, attributeNormal, false);
						wordL = wordR + 1; wordR++;
					}
					wordR++;
				}

				for (int i = 0; i < getLength(); i++) {
					if (Arrays.asList(symbols).contains(Character.toString(text.charAt(i))))
						setCharacterAttributes(i, 1, attributeSpecialSymbols, false);
				}

				// Auto comment highlighting
				int start2 = -1, end2 = -1, fx = 0;
				while (true) {
					start2 = text.indexOf("//", fx);
					end2 = text.indexOf("\n", start2 + 2);
					if (start2 == -1) break;
					if (start2 != -1 && end2 != -1) {
						setCharacterAttributes(start2, end2 - start2, attributeComments, false);
						fx = end2 + 1;
						start2 = -1;
						end2 = -1;
					}
				}

				// Auto comment highlighting
				int start3 = -1, end3 = -1; fx = 0;
				while (true) {
					start3 = text.indexOf("/*", fx);
					end3 = text.indexOf("*/", start3 + 2);
					if (start3 == -1 || end3 == -1) break;
					if (start3 != -1 && end3 != -1) {
						setCharacterAttributes(start3, end3 - start3 + 2, attributeComments, false);
						fx = end3 + 2;
						start3 = -1;
						end3 = -1;
					}
				}

				//Auto Indentation
				try {
					int curlyBlockCount = (int) text.substring(0, documents.get(tabbedPane.getSelectedIndex()).getCaretPosition()).chars().filter(ch -> ch == '{').count();
					curlyBlockCount -= (int) text.substring(0, documents.get(tabbedPane.getSelectedIndex()).getCaretPosition()).chars().filter(ch -> ch == '}').count();
					if (documents.get(tabbedPane.getSelectedIndex()).getCaretPosition() != 0 && text.charAt(documents.get(tabbedPane.getSelectedIndex()).getCaretPosition() - 1) == '\n') {
						try {
							Robot robot = new Robot();
							for (int i = 0; i < curlyBlockCount; i++) {
								robot.keyPress(KeyEvent.VK_TAB);
								robot.keyRelease(KeyEvent.VK_TAB);
							}
						} catch (Exception e) {
							//
						}
					}
				} catch (Exception e) {
					//
				}

				// Auto closing trigger
				try {
					for (String i : autoCloseTrigger) {
						if ((text.charAt(documents.get(tabbedPane.getSelectedIndex()).getCaretPosition() - 1)) == i.charAt(0)) {
							int x = documents.get(tabbedPane.getSelectedIndex()).getCaretPosition();
							documents.get(tabbedPane.getSelectedIndex()).setText(text.substring(0, documents.get(tabbedPane.getSelectedIndex()).getCaretPosition()) + i.charAt(1) + text.substring(documents.get(tabbedPane.getSelectedIndex()).getCaretPosition()));
							documents.get(tabbedPane.getSelectedIndex()).setCaretPosition(x);
						}
					}
				} catch (Exception e) {
					//
				}
				
				// String highlighting
				Scanner sc = new Scanner(text);
				int totalCount = 0;
				while (sc.hasNextLine()) {
					String string = sc.nextLine();
					boolean inString = false; int backslashCount = 0;
					for (int i = 0; i < string.length(); i++) {
						if (string.charAt(i) == '"' && backslashCount % 2 == 0) {
							inString = !inString; backslashCount = 0;
							if (!inString)
								setCharacterAttributes(i + totalCount, 1, attributeStrings, false);
						} else if (string.charAt(i) == '\\') {
							backslashCount++;
						} else {
							backslashCount = 0;
						}
						if (inString) {
							setCharacterAttributes(i + totalCount, 1, attributeStrings, false);
						}
					}
					totalCount += string.length() + 1;
				}
				sc.close();
			}

			public void remove (int offs, int len) throws BadLocationException {
				super.remove(offs, len);

				String text = getText(0, getLength());
				
				setCharacterAttributes(0, text.length(), attributeNormal, false);

				int wordL = 0;
				int wordR = 0;

				while (wordR <= text.length() - 1) {
					if (String.valueOf(text.charAt(wordR)).matches("\\W")) {
						if (text.substring(wordL, wordR).matches("(\\W)*(" + Arrays.stream(keywords).collect(Collectors.joining("|")) + ")"))
							setCharacterAttributes(wordL, wordR - wordL, attributeKeyword, false);
						else if (text.substring(wordL, wordR).matches("(\\W)*(\\d)"))
							setCharacterAttributes(wordL, wordR - wordL, attributeNumbers, false);
						else
							setCharacterAttributes(wordL, wordR - wordL, attributeNormal, false);
						wordL = wordR + 1; wordR++;
					}
					wordR++;
				}

				for (int i1 = 0; i1 < getLength(); i1++) {
					if (Arrays.asList(symbols).contains(Character.toString(text.charAt(i1))))
						setCharacterAttributes(i1, 1, attributeSpecialSymbols, false);
				}

				int start2 = -1, end2 = -1, fx = 0;
				while (true) {
					start2 = text.indexOf("//", fx);
					end2 = text.indexOf("\n", start2 + 2);
					if (start2 == -1) break;
					if (start2 != -1 && end2 != -1) {
						setCharacterAttributes(start2, end2 - start2, attributeComments, false);
						fx = end2 + 1;
						start2 = -1;
						end2 = -1;
					}
				}

				int start3 = -1, end3 = -1; fx = 0;
				while (true) {
					start3 = text.indexOf("/*", fx);
					end3 = text.indexOf("*/", start3 + 2);
					if (start3 == -1 || end3 == -1) break;
					if (start3 != -1 && end3 != -1) {
						setCharacterAttributes(start3, end3 - start3 + 2, attributeComments, false);
						fx = end3 + 2;
						start3 = -1;
						end3 = -1;
					}
				}
				
				// String highlighting
				Scanner sc = new Scanner(text);
				int totalCount = 0;
				while (sc.hasNextLine()) {
					String string = sc.nextLine();
					boolean inString = false; int backslashCount = 0;
					for (int i = 0; i < string.length(); i++) {
						if (string.charAt(i) == '"' && backslashCount % 2 == 0) {
							inString = !inString; backslashCount = 0;
							if (!inString)
								setCharacterAttributes(i + totalCount, 1, attributeStrings, false);
						} else if (string.charAt(i) == '\\') {
							backslashCount++;
						} else {
							backslashCount = 0;
						}
						if (inString) {
							setCharacterAttributes(i + totalCount, 1, attributeStrings, false);
						}
					}
					totalCount += string.length() + 1;
				}
				sc.close();
			}
		};
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
					if (!nameLists.get(tabbedPane.getSelectedIndex()).getText().startsWith("*"))
						nameLists.get(tabbedPane.getSelectedIndex()).setText("*" + nameLists.get(tabbedPane.getSelectedIndex()).getText());
				} catch (Exception e1) {
					//
				}
			}

		};
	}

	// Setting up the Project Explorer
	private void setupFileTree() {
		DefaultMutableTreeNode dirTree = new DefaultMutableTreeNode(filepath[filepath.length - 1]);
		File currentDir = new File(Arrays.stream(filepath).collect(Collectors.joining("\\")));
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
							JOptionPane.showMessageDialog(null, e1.getStackTrace(), "Couldn't Open File!", JOptionPane.ERROR_MESSAGE);
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
		File file = new File(Arrays.stream(filepath).collect(Collectors.joining("\\")) + "\\src\\" + className + ".java");
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
	private void saveFile(String str) throws Exception {
		String txt = documents.get(tabbedPane.getSelectedIndex()).getText(0, documents.get(tabbedPane.getSelectedIndex()).getText().length());
		File file = new File(Arrays.stream(filepath).collect(Collectors.joining("\\")) + "\\" + str);
		FileWriter fw = new FileWriter(file);
		fw.write(txt);
		fw.close();
		String labelTxt = nameLists.get(tabbedPane.getSelectedIndex()).getText();
		if (labelTxt.startsWith("*")) nameLists.get(tabbedPane.getSelectedIndex()).setText(labelTxt.substring(1, labelTxt.length()));
	}

	// Delete a File
	private void deleteFile(String str) {
		int choice = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete " + str + "?", "Confirmation for Deletion of File!", JOptionPane.WARNING_MESSAGE);
		if (choice == JOptionPane.YES_OPTION) {
			File file = new File(Arrays.stream(filepath).collect(Collectors.joining("\\")) + "\\" + str);
			file.delete();
			tabbedPane.remove(tabbedPane.getSelectedIndex());
			openTabs.remove(tabbedPane.getSelectedIndex() + 1);
			nameLists.remove(tabbedPane.getSelectedIndex() + 1);
			documents.remove(tabbedPane.getSelectedIndex() + 1);
			checkIfNoOpenTabs();
			setupFileTree();
		}
	}

	// Compile File
	private void compileFile(String str) throws Exception {
		String path = Arrays.stream(filepath).collect(Collectors.joining("\\"));
		File file = new File(path + "\\run.bat");
		if (!file.exists()) file.createNewFile();
		FileWriter writer = new FileWriter(file);
		String command = "";
		command += "javac -d bin -cp lib\\* " + str + "\npause\nexit";
		writer.write(command);
		writer.close();
		ProcessBuilder builder = new ProcessBuilder(new String[] {"cmd", "/c", "start", "run.bat"});
		builder.directory(new File(path));
		Process p = builder.start();
		p.info();
	}

	// Run File
	private void runFile(String str) throws Exception {
		String path = Arrays.stream(filepath).collect(Collectors.joining("\\"));
		File file = new File(path + "\\run.bat");
		if (!file.exists()) file.createNewFile();
		FileWriter writer = new FileWriter(file);
		String command = "";
		command += "cd bin\ncls\n";
		command += "java -cp \"" + path + "\\lib\\*\"; " + nameLists.get(tabbedPane.getSelectedIndex()).getText().replace(".java", "");
		command += "\npause\nexit";
		writer.write(command);
		writer.close();
		ProcessBuilder builder = new ProcessBuilder(new String[] {"cmd", "/c", "start", "run.bat"});
		builder.directory(new File(path));
		Process p = builder.start();
		p.info();
	}

	// Returns the file structure
	private DefaultMutableTreeNode getFiles(String str) {
		File file = new File(str);
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new Contact(file.getName(), file.isFile()));
		//DefaultMutableTreeNode node = new DefaultMutableTreeNode(file.getName());
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
		System.out.println(filedir);
		File file = new File(Arrays.stream(filepath).collect(Collectors.joining("\\")) + "\\" + filedir);
		if (!file.isDirectory()) {
			if (!openTabs.contains(filedir))
				createNewTab(Arrays.stream(filepath).collect(Collectors.joining("\\")) + "\\" + filedir, filedir);
			else {
				int xi = openTabs.indexOf(filedir);
				tabbedPane.setSelectedIndex(xi);
			}
		}
	}

	// Creates a new tab for a file
	private void createNewTab(String filepath, String filedir) throws Exception {
		String text = ""; File file;
		FileReader reader = new FileReader(file = new File(filepath));
		int i;
		while ((i = reader.read()) != -1)
			text += (char) i;
		reader.close();

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		tabbedPane.addTab(file.getName(), null, scrollPane, filepath);
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

		DefaultStyledDocument doc = initializeDoc();
		doc.addDocumentListener(getDocumentListener());

		JTextPane editorPane = new JTextPane(doc);
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
					if(SwingUtilities.isDescendingFrom(button, tabbedPane.getTabComponentAt(i))) {
						tabbedPane.remove(i);
						openTabs.remove(i);
						nameLists.remove(i);
						documents.remove(i);
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
				closeButton.setIcon(Info.getImage(".\\assets\\cross-hover.png", 10, 10));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				closeButton.setIcon(Info.getImage(".\\assets\\cross.png", 10, 10));
			}
			
		});
		closeButton.setHorizontalAlignment(SwingConstants.CENTER);
		closeButton.setIcon(Info.getImage(".\\assets\\cross.png", 10, 10));
		closeButton.setMinimumSize(new Dimension(15, 15));
		closeButton.setFocusable(false);
		closeButton.setOpaque(false);
		closeButton.setContentAreaFilled(false);
		closeButton.setBorderPainted(false);
		tabComponent.add(closeButton, BorderLayout.CENTER);

		tabbedPane.setTabComponentAt(openTabs.size(), tabComponent);
		tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);

		scrollPane.setViewportView(editorPane);
		openTabs.add(filedir);
		nameLists.add(tabLabel);
		documents.add(editorPane);
		checkIfNoOpenTabs();
	}

	// Checks if no tabs are open
	private void checkIfNoOpenTabs() {
		if(openTabs.size() == 0) {
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
		//int charWidth = fm.charWidth( 'w' );
		int charWidth = fm.charWidth( ' ' );
		int tabWidth = charWidth * charactersPerTab;
		//int tabWidth = 100;

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
