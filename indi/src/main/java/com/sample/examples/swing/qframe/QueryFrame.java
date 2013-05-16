package com.sample.examples.swing.qframe;


import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.annotation.PostConstruct;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;
import javax.swing.text.AbstractDocument;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sample.examples.swing.qframe.glasspane.InfiniteProgressPanel;

/**
 * This class creates a Swing GUI that allows the user to enter a SQL query. It
 * then obtains a ResultSetTableModel for the query and uses it to display the
 * results of the query in a scrolling JTable component.
 **/
@Component
public class QueryFrame extends JFrame {

	private static final int MAX_AVAILABLE_WIDTH = 1265;
	private static final long serialVersionUID = 1L;
	@Autowired
	private ResultSetTableModelFactory factory; // A factory to obtain our table data

	// private JTextArea queryTextArea;
	private JTextPane queryTextPane;
	private JTable queryResulttable; // The table for displaying data
	private JLabel msgLabel; // For displaying messages
	private JTextArea messageArea;
	@Autowired
	private SecurePrompt securePrompt;
	final InfiniteProgressPanel glasspane = new InfiniteProgressPanel("Please wait....");
	private AbstractDocument doc;
	private static boolean INITFEEL = false;;
	// Specify the look and feel to use by defining the LOOKANDFEEL constant
	// Valid values are: null (use the default), "Metal", "System", "Motif",
	// and "GTK"
	final static String LOOKANDFEEL = "System";
	// If you choose the Metal L&F, you can also choose a theme.
	// Specify the theme to use by defining the THEME constant
	// Valid values are: "DefaultMetal", "Ocean", and "Test"
	final static String THEME = "Ocean";

	public QueryFrame() {
		super();
		initLookAndFeel();
	}

	@PostConstruct
	protected void init() {
		securePrompt.setVisible(true);
		this.setTitle("QueryFrame"); // Set window title
		// Arrange to quit the program when the user closes the window
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		// Create the Swing components we'll be using
		queryResulttable = new JTable(); // Displays the table
		queryResulttable.setColumnSelectionAllowed(true);
		msgLabel = new JLabel("   "); // Displays messages

		// Place the components within this window
		Container contentPane = getContentPane();

		// textArea.setFont(new Font("Serif", Font.ITALIC, 16));
		/*
		 * queryTextArea = new JTextArea(15, 100);
		 * queryTextArea.setLineWrap(true);
		 * queryTextArea.setWrapStyleWord(true); JScrollPane areaScrollPane =
		 * new JScrollPane(queryTextArea);
		 */
		queryTextPane = new JTextPane();
		queryTextPane.setCaretPosition(0);
		queryTextPane.setMargin(new Insets(5, 5, 5, 5));
		StyledDocument styledDoc = queryTextPane.getStyledDocument();
		queryTextPane.addKeyListener(new KeyListener() {
			private String prev;
			public void keyTyped(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {}
			public void keyPressed(KeyEvent e) {
				String keyedText = String.valueOf(e.getKeyChar());
				if (StringUtils.equals(keyedText, "-") && StringUtils.equals(prev, keyedText)) {
					System.out.println("hurrah");
				}
				prev = keyedText;
			}
		});
		queryTextPane.setEditorKit(new StyledEditorKit());
		queryTextPane.setFont(new Font("COURIER NEW", Font.PLAIN, 12));
		
	    JMenu fontMenu = new JMenu("Font Size");
	    for (int i = 48; i >= 8; i -= 10) {
	      JMenuItem menuItem = new JMenuItem("" + i);
	      // add an action
	      menuItem.addActionListener(new StyledEditorKit.FontSizeAction("myaction-" + i, i));
	      fontMenu.add(menuItem);
	    }
	    JMenuBar menuBar = new JMenuBar();
	    menuBar.add(fontMenu);
	    setJMenuBar(menuBar);
		
		JScrollPane areaScrollPane = new JScrollPane(queryTextPane);
		areaScrollPane.setPreferredSize(new Dimension(800, 275));
		areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		// box.add(query, BorderLayout.NORTH);
		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		topPanel.add(areaScrollPane);

		JButton queryButton = new JButton("Query");
		queryButton.addActionListener(new ActionListener() {

			// This method is invoked when the user hits ENTER in the field
			public void actionPerformed(ActionEvent e) {
				// Get the user's query and pass to displayQueryResults()
				// displayQueryResults(queryTextArea.getText());
				displayQueryResults(queryTextPane.getText());
			}
		});
		topPanel.add(queryButton);

		JScrollPane tableScrollPane = new JScrollPane(queryResulttable);
		tableScrollPane.setPreferredSize(new Dimension(MAX_AVAILABLE_WIDTH, 450));

		JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		centerPanel.add(tableScrollPane);

		JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		bottomPanel.setPreferredSize(new Dimension(MAX_AVAILABLE_WIDTH, 20));
		bottomPanel.add(msgLabel);

		Box box = Box.createVerticalBox();
		box.add(topPanel, BorderLayout.NORTH);
		box.add(centerPanel, BorderLayout.CENTER);
		box.add(bottomPanel, BorderLayout.SOUTH);

		messageArea = new JTextArea(5, 50);
		messageArea.setWrapStyleWord(true);
		messageArea.setLineWrap(true);
		messageArea.setEditable(false);
		JScrollPane messageScrollPane = new JScrollPane(messageArea, 
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		messageScrollPane.setPreferredSize(new Dimension(MAX_AVAILABLE_WIDTH, 120));
		JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		messagePanel.add(messageScrollPane);

		contentPane.add(box, BorderLayout.PAGE_START);
		contentPane.add(messagePanel);

		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(screenSize.width, screenSize.height - 100);
		setGlassPane(glasspane);
		this.setVisible(true);
	}

	/**
	 * This constructor method creates a simple GUI and hooks up an event
	 * listener that updates the table when the user enters a new query.
	 **/
	public QueryFrame(ResultSetTableModelFactory f) {
		// Remember the factory object that was passed to us
		this.factory = f;
		init();
	}

	/**
	 * This method uses the supplied SQL query string, and the
	 * ResultSetTableModelFactory object to create a TableModel that holds the
	 * results of the database query. It passes that TableModel to the JTable
	 * component for display.
	 **/
	public void displayQueryResults(final String q) {
		// It may take a while to get the results, so give the user some
		// immediate feedback that their query was accepted.
		msgLabel.setText("Contacting database...");
		queryResulttable.setModel(ResultSetTableModel.EMPTY_MODEL);
		// In order to allow the feedback message to be displayed, we don't
		// run the query directly, but instead place it on the event queue
		// to be run after all pending events and redisplays are done.
		EventQueue.invokeLater(new Runnable() {

			public void run() {
				try {
					ResultSetTableModel resultSetTableModel = factory
							.getResultSetTableModel(q);
					// This is the crux of it all. Use the factory object
					// to obtain a TableModel object for the query results
					// and display that model in the JTable component.
					queryResulttable.setModel(resultSetTableModel);
					// We're done, so clear the feedback message
					msgLabel.setText(StringUtils.EMPTY);
					messageArea.setText(" No of rows fetched :[ "
							+ resultSetTableModel.getRowCount()
							+ " ]".concat("\n").concat(
									resultSetTableModel.getConnectionDetails()));
				} catch (Exception ex) {
					// If something goes wrong, clear the message line
					queryResulttable.setModel(ResultSetTableModel.EMPTY_MODEL);
					msgLabel.setText(StringUtils.EMPTY);
					messageArea.setText(ex.getClass().getName() + ": "
							+ ex.getMessage());
					// Then display the error in a dialog box
					// JOptionPane.showMessageDialog(QueryFrame.this, new
					// String[] { // Display a 2-line message
					// ex.getClass().getName() + ": ", ex.getMessage() });
					// ex.printStackTrace();
				}
				glasspane.stop();
				glasspane.setVisible(false);
			}
		});
		glasspane.start();
		glasspane.setVisible(true);
	}

	private static void initLookAndFeel() {
		
		if (!INITFEEL) return;
		
		String lookAndFeel = null;

		if (LOOKANDFEEL != null) {
			if (LOOKANDFEEL.equals("Metal")) {
				lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
				// an alternative way to set the Metal L&F is to replace the
				// previous line with:
				// lookAndFeel = "javax.swing.plaf.metal.MetalLookAndFeel";

			}

			else if (LOOKANDFEEL.equals("System")) {
				lookAndFeel = UIManager.getSystemLookAndFeelClassName();
			}

			else if (LOOKANDFEEL.equals("Motif")) {
				lookAndFeel = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
			}

			else if (LOOKANDFEEL.equals("GTK")) {
				lookAndFeel = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
			}

			else {
				System.err
						.println("Unexpected value of LOOKANDFEEL specified: "
								+ LOOKANDFEEL);
				lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
			}

			try {

				UIManager.setLookAndFeel(lookAndFeel);

				// If L&F = "Metal", set the theme

				if (LOOKANDFEEL.equals("Metal")) {
					if (THEME.equals("DefaultMetal"))
						MetalLookAndFeel
								.setCurrentTheme(new DefaultMetalTheme());
					else if (THEME.equals("Ocean"))
						MetalLookAndFeel.setCurrentTheme(new OceanTheme());
					

					UIManager.setLookAndFeel(new MetalLookAndFeel());
				}

			}

			catch (ClassNotFoundException e) {
				System.err
						.println("Couldn't find class for specified look and feel:"
								+ lookAndFeel);
				System.err
						.println("Did you include the L&F library in the class path?");
				System.err.println("Using the default look and feel.");
			}

			catch (UnsupportedLookAndFeelException e) {
				System.err.println("Can't use the specified look and feel ("
						+ lookAndFeel + ") on this platform.");
				System.err.println("Using the default look and feel.");
			}

			catch (Exception e) {
				System.err.println("Couldn't get specified look and feel ("
						+ lookAndFeel + "), for some reason.");
				System.err.println("Using the default look and feel.");
				e.printStackTrace();
			}
		}
	}

	/**
	 * This simple main method tests the class. It expects four command-line
	 * arguments: the driver classname, the database URL, the username, and the
	 * password
	 **/
	public static void main(String args[]) throws Exception {
		// Set the look and feel.
		initLookAndFeel();

		// Make sure we have nice window decorations.
		JFrame.setDefaultLookAndFeelDecorated(true);
		// Create the factory object that holds the database connection using
		// the data specified on the command line
		ResultSetTableModelFactory factory = new ResultSetTableModelFactory(
				"com.ibm.db2.jcc.DB2Driver",
				"jdbc:db2://lmc1dbt.mc1.johnlewis.co.uk:11200/LMC1DBT",
				"TPU005", "Zenpa678");
		// Create a QueryFrame component that uses the factory object.
		QueryFrame queryFrame = new QueryFrame(factory);

		// Display the window.
		queryFrame.pack();
		queryFrame.setVisible(true);
	}
	
}
