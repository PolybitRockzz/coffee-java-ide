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
				String trimmedText = str;
				
				int totalCount;
				trimmedText = text.substring(
					totalCount = (text.substring(0, offset).lastIndexOf("\n") == -1 ? 0 : text.substring(0, offset).lastIndexOf("\n") + 1),
					text.substring(offset + str.length()).indexOf("\n") == -1 ? text.length() : offset + str.length() + text.substring(offset + str.length()).indexOf("\n")
				);
				
				setCharacterAttributes(totalCount, trimmedText.length(), attributeNormal, false);

				int wordL = 0;
				int wordR = 0;

				while (wordR <= trimmedText.length() - 1) {
					if (String.valueOf(text.charAt(wordR)).matches("\\W")) {
						if (text.substring(wordL, wordR).matches("(\\W)*(" + Arrays.stream(keywords).collect(Collectors.joining("|")) + ")"))
							setCharacterAttributes(wordL, wordR - wordL, attributeKeyword, false);
						else if (text.substring(wordL, wordR).matches("(\\W)*(\\d)"))
							setCharacterAttributes(wordL, wordR - wordL, attributeNumbers, false);
						else
							setCharacterAttributes(wordL, wordR - wordL, attributeNormal, false);
						wordL = wordR;
					}
					wordR++;
				}
				
				// String highlighting
				String string = "";
				Scanner sc = new Scanner(trimmedText);
				int stringCount = 0;
				while (sc.hasNextLine()) {
					string = sc.nextLine();
					boolean inString = false, inChar = false; int backslashCount = 0;
					for (int i = 0; i < string.length(); i++) {
						if (string.charAt(i) == '"' && backslashCount % 2 == 0 && !inChar) {
							inString = !inString; backslashCount = 0;
							if (!inString)
								setCharacterAttributes(i + stringCount + totalCount, 1, attributeStrings, false);
						} else if (string.charAt(i) == '\'' && backslashCount % 2 == 0 && !inString) {
							inChar = !inChar; backslashCount = 0;
							if (!inChar)
								setCharacterAttributes(i + stringCount + totalCount, 1, attributeStrings, false);
						} else if (string.charAt(i) == '\\') {
							backslashCount++;
						} else {
							backslashCount = 0;
						}
						if (inString) {
							setCharacterAttributes(i + stringCount + totalCount, 1, attributeStrings, false);
						} if (inChar) {
							setCharacterAttributes(i + stringCount + totalCount, 1, attributeStrings, false);
						}
					}
					stringCount += string.length() + 1;
				}
				sc.close();

				// Auto comment highlighting
				int start2 = trimmedText.indexOf("//", totalCount);
				if (start2 != -1) {
					setCharacterAttributes(totalCount + start2, trimmedText.length(), attributeComments, false);
				}

				// Auto comment highlighting
				int start3 = -1, end3 = -1; int fx = totalCount;
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
				int curlyBlockCount = (int) text.substring(0, offset + 1).chars().filter(ch -> ch == '{').count();
				curlyBlockCount -= (int) text.substring(0, offset + 1).chars().filter(ch -> ch == '}').count();
				if (text.charAt(offset) == '\n') {
					try {
						Robot robot = new Robot();
						for (int i = 0; i < curlyBlockCount; i++) {
							robot.keyPress(KeyEvent.VK_TAB);
							robot.keyRelease(KeyEvent.VK_TAB);
						}
					} catch (Exception e) {}
				}
				
				//Auto closing
				for (String i : autoCloseTrigger) {
					if (str.equals(String.valueOf(i.charAt(0)))) {
						super.insertString(offset + 1, String.valueOf(i.charAt(1)), a);
						try {
							Robot robot = new Robot();
							robot.keyRelease(KeyEvent.VK_SHIFT);
							robot.keyPress(KeyEvent.VK_LEFT);
							robot.keyRelease(KeyEvent.VK_LEFT);
						} catch (Exception e) {}
					}
				}
				
			}

			public void remove (int offs, int len) throws BadLocationException {
				super.remove(offs, len);
				super.insertString(offs, "", attributeNormal);
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
