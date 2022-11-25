package tech.polybit.coffeeide;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;

import javax.swing.JTabbedPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class Documents {
	
	// Styling Attributes
	private static final StyleContext styleContext = StyleContext.getDefaultStyleContext();
	private static final AttributeSet attributeKeyword = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Info.getThemeColor(6));
	private static final AttributeSet attributeSpecialSymbols = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Info.getThemeColor(8));
	private static final AttributeSet attributeNumbers = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Info.getThemeColor(7));
	private static final AttributeSet attributeComments = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Info.getThemeColor(5));
	private static final AttributeSet attributeStrings = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Info.getThemeColor(9));
	private static final AttributeSet attributeNormal = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Info.getThemeColor(4));
	
	private static final String[] keywords = {
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
	private static final String[] symbols = {
			"+", "-", "*", "/", "%", "=", "?", ":", ";",
			"{", "}", "."
	};

	private static final String[] autoCloseTrigger = {
			"()", "\"\"", "\'\'", "{}", "[]"
	};
	
	public static DefaultStyledDocument getJavaStyledDoc(ArrayList<TabComponent> tabs, JTabbedPane tabbedPane) {
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
					int curlyBlockCount = (int) text.substring(0, tabs.get(tabbedPane.getSelectedIndex()).getTextPane().getCaretPosition()).chars().filter(ch -> ch == '{').count();
					curlyBlockCount -= (int) text.substring(0, tabs.get(tabbedPane.getSelectedIndex()).getTextPane().getCaretPosition()).chars().filter(ch -> ch == '}').count();
					if (tabs.get(tabbedPane.getSelectedIndex()).getTextPane().getCaretPosition() != 0 && text.charAt(tabs.get(tabbedPane.getSelectedIndex()).getTextPane().getCaretPosition() - 1) == '\n') {
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
						if ((text.charAt(tabs.get(tabbedPane.getSelectedIndex()).getTextPane().getCaretPosition() - 1)) == i.charAt(0)) {
							int x = tabs.get(tabbedPane.getSelectedIndex()).getTextPane().getCaretPosition();
							tabs.get(tabbedPane.getSelectedIndex()).getTextPane().setText(
									text.substring(0, tabs.get(tabbedPane.getSelectedIndex()).getTextPane().getCaretPosition())
									+ i.charAt(1)
									+ text.substring(tabs.get(tabbedPane.getSelectedIndex()).getTextPane().getCaretPosition()));
							tabs.get(tabbedPane.getSelectedIndex()).getTextPane().setCaretPosition(x);
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
	
	public static DefaultStyledDocument getDefaultStyledDoc(ArrayList<TabComponent> tabs, JTabbedPane tabbedPane) {
		return new DefaultStyledDocument() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 5172740319640565443L;

			public void insertString (int offset, String str, AttributeSet a) throws BadLocationException {
				super.insertString(offset, str, a);

				String text = getText(0, getLength());
				
				setCharacterAttributes(0, text.length(), attributeNormal, false);
			}

			public void remove (int offs, int len) throws BadLocationException {
				super.remove(offs, len);

				String text = getText(0, getLength());
				
				setCharacterAttributes(0, text.length(), attributeNormal, false);
			}
		};
	}

}
