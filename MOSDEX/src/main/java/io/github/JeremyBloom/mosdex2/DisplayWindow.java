/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.DefaultCaret;

/**
 * Displays output in a text window.
 * Replaces System.out to separate results output from console output, 
 * which can be very crowded and voluminous when using Spark.
 * Enables selecting and copying text to the system clipboard. You can highlight the text 
 * either by dragging the mouse or by using the Select All command on the Edit menu. 
 * Then the Copy command will copy the selection to the clipboard. 
 * Copy is available as an Edit menu item or as a popup.
 * 
 * @author Dr. Jeremy Bloom (jeremyblmca@gmail.com)
 *
 */
public class DisplayWindow {

	protected JTextArea text;
	protected JFrame frame;
	protected JTextArea lineNumbers;
	private boolean isOpen;
	
	/**
	 * Creates a window to display text.
	 * The window has a dropdown Edit menu that enables selecting all of the text 
	 * and copying the selection to the system clipboard. Text can also be selected 
	 * by dragging the mouse over it. The window also has a popup menu to copy the selected text.
	 * 
	 * @param title title to be shown at the top of the window
	 */
	public DisplayWindow(String title) {
		super();
		this.text= new JTextArea();
		text.setEditable(false);
		text.setLineWrap(false);
		text.setMargin(new Insets(10, 10, 10, 10));
		text.setFont(new Font("Consolas", Font.PLAIN, 12));
		((DefaultCaret)text.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);	//automatically scrolls to the end of the text
		
		this.frame= new JFrame(title);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		        if (JOptionPane.showConfirmDialog(frame, 
		            "Are you sure you want to close this window?", "Close Window?", 
		            JOptionPane.YES_NO_OPTION,
		            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
		        	{
			            DisplayWindow.this.isOpen= false;
			        	frame.dispose();
			        }
		    }
		});
		
//		Put the text area into a scroll pane
		JScrollPane scroll= new JScrollPane(text, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setPreferredSize(new Dimension(600, 450));
		
		frame.getContentPane().add(scroll);
		
//		Create line numbers
		this.lineNumbers= new JTextArea();
		lineNumbers.setEnabled(false);
		lineNumbers.setLineWrap(false);
		lineNumbers.setMargin(new Insets(10, 10, 10, 10));
		lineNumbers.setFont(new Font("Consolas", Font.PLAIN, 12));
		lineNumbers.setForeground(Color.LIGHT_GRAY);
		JViewport linesView= new JViewport();
		linesView.setView(lineNumbers);
		scroll.setRowHeader(linesView);
		
//		Create the File menu
		JMenu fileMenu= new JMenu("File");
		JMenuItem printCmd= new JMenuItem("Print");
		printCmd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					text.print();
				} catch (PrinterException e1) {
					System.err.println("Unable to print");
					e1.printStackTrace();
				}
			}
		});
		fileMenu.add(printCmd);
		
//		Create the Edit menu
		JMenu editMenu= new JMenu("Edit");
		
		JMenuItem selectAllCmd= new JMenuItem("Select All");
		selectAllCmd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				text.selectAll();
			}
		});
		editMenu.add(selectAllCmd);
		
		editMenu.add(makeCopyCmd());
		
		JMenuBar menuBar= new JMenuBar();
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		frame.setJMenuBar(menuBar);
		
//		Make Copy popup menu
		JPopupMenu popup= new JPopupMenu();
		popup.add(makeCopyCmd());
		text.setComponentPopupMenu(popup);
		text.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.isPopupTrigger())
					text.getComponentPopupMenu().show(text, e.getX(), e.getY());
			}
		});	
		
	}/*DisplayWindow constructor*/
	
	/**Creates a display window with a blank title.*/
	public DisplayWindow() {
		this("");
	}
	
	/**
	 * Sets a title for the display if one has not already been set.
	 * 
	 * @param title
	 * @throws UnsupportedOperationException if the display already has a title.
	 */
	public void setTitle(String title) {
		if(frame.getTitle().length()>0)
			throw new UnsupportedOperationException("Title already set");
		frame.setTitle(title);
	}
	
	/**
	 * @return true if the display window is open, false otherwise
	 */
	public boolean isOpen() {
		return isOpen;
	}

	/**
	 * Creates a menu item for the copy command.
	 * Enables putting a copy command both on the menu bar and in a popup menu.
	 * 
	 * @return a menu item
	 */
	protected JMenuItem makeCopyCmd() {
		JMenuItem copyCmd= new JMenuItem("Copy");
		copyCmd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				text.copy();
			}
		});
		return copyCmd;
	}
	
	/**
	 * Appends text to the end of the document and updates the line numbers.
	 * 
	 * @param s text to be appended
	 */
	public void print(String s) {
        this.text.append(s);
		while(this.lineNumbers.getLineCount() < this.text.getLineCount()) {
			this.lineNumbers.append(Integer.toString(this.lineNumbers.getLineCount()) + System.lineSeparator());
		}		  	
	}
	
	/**
	 * Appends the string representation of an object to the end of the document and updates the line numbers.
	 * @param s text to be appended
	 */
	public void print(Object s) {
		print(String.valueOf(s));
	}
	
	/**
	 * Appends text to the end of the document and terminates the current line by writing the line separator string.
	 * 
	 * @param s text to be appended
	 */
	public void println(String s) {
		print(s + System.lineSeparator());		
	}
	
	/**
	 * Appends the string representation of an object to the end of the document and terminates the current line by writing the line separator string.
	 * 
	 * @param s text to be appended
	 */
	public void println(Object s) {
		println(s.toString());
	}
	
	/**Appends an empty string to the end of the document and terminates the current line by writing the line separator string.*/
	public void println() {
		println("");
	}
		
	/**
	 * Gets the number of the last line written.
	 * @return the number of the last line written
	 */
	public int getLineNumber() {
		return lineNumbers.getLineCount();
	}
	
	/**
	 * Shows the display window.
	 * Calling show before any calls to print will cause the display to update as new lines 
	 * are appended.
	 */
	public void show() {
		frame.setSize(600, 450);
		frame.pack();
		frame.setVisible(true);
		this.isOpen= true;
	}
	
	/**
	 * Creates an extended PrintStream instance that directs its output to this display window.
	 * Enables using all of the PrintStream print(...) and println(...) methods 
	 * for different types of arguments.
	 * Note that using the PrintStream is possibly inefficient due to the need to check updates to the line numbers 
	 * after writing each character.
	 * 
	 * @return an ExtendedPrintStream instance
	 */
	public ExtendedPrintStream printStream() {
		OutputStream out= new OutputStream(){
			@Override
			public void write(int b) throws IOException {
				DisplayWindow.this.print(String.valueOf((char)b));
			}
		};
		return new ExtendedPrintStream(out);
	}
	
	/**
	 * Adds a method to print an entire text file to the stream.
	 * 
	 * @author bloomj
	 *
	 */
	public static class ExtendedPrintStream extends PrintStream {

		public ExtendedPrintStream(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
			super(file, csn);
		}

		public ExtendedPrintStream(File file) throws FileNotFoundException {
			super(file);
		}

		public ExtendedPrintStream(OutputStream out, boolean autoFlush, String encoding)
				throws UnsupportedEncodingException {
			super(out, autoFlush, encoding);
		}

		public ExtendedPrintStream(OutputStream out, boolean autoFlush) {
			super(out, autoFlush);
		}

		public ExtendedPrintStream(OutputStream out) {
			super(out);
		}

		public ExtendedPrintStream(String fileName, String csn)
				throws FileNotFoundException, UnsupportedEncodingException {
			super(fileName, csn);
		}

		public ExtendedPrintStream(String fileName) throws FileNotFoundException {
			super(fileName);
		}
		
		/**
		 * Displays a file of text.
		 * 
		 * @param text the text source to be displayed
		 */
		public void printFile(Reader text) {		
			try {
				BufferedReader br = new BufferedReader(text); 
				String line = null;
				while ((line = br.readLine()) != null) {
					println(line);
				}
				flush();
				br.close();
			} catch (IOException e) {
				println("cannot display " + text);
				e.printStackTrace();
				throw new IOError(e);
			}
			return;
		}
	
		/**
		 * Displays a file of text.
		 * 
		 * @param text the text source to be displayed
		 */
		public void printFile(File text) {	
			try {
				printFile(new FileReader(text));
			} catch (FileNotFoundException e) {
				println("file " + text + " not found");
				e.printStackTrace();
				throw new IOError(e);
			}
			return;
		}
		
		/**
		 * Displays a file of text.
		 * 
		 * @param text the text source to be displayed
		 */
		public void printFile(String text) {	
			printFile(new StringReader(text));			
		}

		/**
		 * Displays a file of text.
		 * 
		 * @param text the text source to be displayed
		 */
		public void printFile(InputStream text) {	
			printFile(new InputStreamReader(text));			
		}

	}/*class DisplayWindow.ExtendedPrintStream*/
	

}/*class DisplayWindow*/
