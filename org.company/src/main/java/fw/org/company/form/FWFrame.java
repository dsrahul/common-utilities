package fw.org.company.form;


import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.table.TableModel;
import javax.swing.text.MaskFormatter;

import net.sourceforge.jdatepicker.JDateComponentFactory;
import net.sourceforge.jdatepicker.JDatePicker;

import org.apache.commons.collections.SetUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.johnlewis.jjs.jjs2core.common.util.JJSDateAndTimeUtil;
import com.johnlewis.jjs.jjs2core.common.vo.dateandtime.JJSDate;

import fw.org.company.component.InfiniteProgressPanel;
import fw.org.company.model.FWResponseTableModel;
import fw.org.company.model.FWResponseTableModelFactory;

@Component
public class FWFrame extends JFrame {

	private static final Log log = LogFactory.getLog(FWFrame.class);
	private static final BigDecimal PROFITABILITY_1400 = new BigDecimal("1400");
	private static final long serialVersionUID = 1L;

	@Autowired
	private @Qualifier("tablefactory")
	FWResponseTableModelFactory factory;
	private JTextField commaSeperatedPostcodes;
	private JComboBox editableCB;

	@Autowired
	@Qualifier("fwTable")
	private JTable table;
	private JLabel msgline;
	private JTextArea messageArea;
	private JTextField profitabilityTextField;
	private JTextField measure1TextField = new JTextField();
	private JTextField measure2TextField = new JTextField();
	private JTextField measure3TextField = new JTextField();

	final InfiniteProgressPanel glasspane = new InfiniteProgressPanel("Please wait....");
	private JDatePicker createJDatePicker;
	private ActionListener queryFWActionListener = new ActionListener() {

		public void actionPerformed(ActionEvent e) {
			FWFrame.this.commaSeperatedPostcodes.setBackground(Color.WHITE);
			FWFrame.this.measure1TextField.setBackground(Color.WHITE);
			FWFrame.this.measure2TextField.setBackground(Color.WHITE);
			FWFrame.this.measure3TextField.setBackground(Color.WHITE);
			FWFrame.this.displayQueryResults();
		}
	};
	private Thread waitThread;

	public FWFrame() {
	}

	@PostConstruct
	protected void init() {
		setTitle("QueryFrame");

		addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		this.commaSeperatedPostcodes = new JTextField();
		this.commaSeperatedPostcodes.setMaximumSize(new Dimension(600, 25));
		this.commaSeperatedPostcodes.setPreferredSize(new Dimension(600, 25));
		this.msgline = new JLabel();
		this.createJDatePicker = JDateComponentFactory.createJDatePicker();
		((java.awt.Component) this.createJDatePicker).setMaximumSize(new Dimension(125, 30));
		((java.awt.Component) this.createJDatePicker).setPreferredSize(new Dimension(125, 30));

		Container contentPane = getContentPane();
		String[] items = { "DEL", "HIT", "INS" };
		this.editableCB = new JComboBox(items);
		this.editableCB.setEditable(true);
		this.editableCB.setMaximumSize(new Dimension(100, 25));
		this.editableCB.setPreferredSize(new Dimension(100, 25));

		Box box = Box.createVerticalBox();
		JPanel panelTop = new JPanel(new FlowLayout(0));
		panelTop.add(this.commaSeperatedPostcodes);
		panelTop.add((JComponent) this.createJDatePicker);
		panelTop.add(new JLabel(" For Requirement : "));
		panelTop.add(this.editableCB);
		panelTop.add(new JLabel(" with Profitability >="));
		MaskFormatter maskFormatter = null;
		try {
			maskFormatter = new MaskFormatter("####.####");
			maskFormatter.setPlaceholderCharacter('0');
		}
		catch (ParseException e1) {
			e1.printStackTrace();
		}
		this.profitabilityTextField = new JFormattedTextField(maskFormatter);
		this.profitabilityTextField.setText(PROFITABILITY_1400.toString());
		this.profitabilityTextField.setColumns(10);
		this.profitabilityTextField.setMaximumSize(new Dimension(10, 25));
		this.profitabilityTextField.setPreferredSize(new Dimension(10, 25));

		panelTop.add(this.profitabilityTextField);

		JButton queryJButton = new JButton("Query");
		queryJButton.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "ENTER");
		queryJButton.getActionMap().put("ENTER", new AbstractAction() {

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				FWFrame.this.displayQueryResults();
			}
		});
		queryJButton.addActionListener(this.queryFWActionListener);
		panelTop.add(queryJButton);

		FlowLayout inputBoxBottom = new FlowLayout(0);
		JPanel panelBottom = new JPanel(inputBoxBottom);

		panelBottom.add(new JLabel(" Measures ====> "));
		panelBottom.add(new JLabel(" Measure 1 : "));
		Dimension maximumSize = new Dimension(50, 25);
		this.measure1TextField.setMaximumSize(maximumSize);
		this.measure1TextField.setPreferredSize(maximumSize);
		this.measure1TextField.addActionListener(this.queryFWActionListener);
		panelBottom.add(this.measure1TextField);
		panelBottom.add(new JLabel(" Measure 2 : "));
		this.measure2TextField.setMaximumSize(maximumSize);
		this.measure2TextField.setPreferredSize(maximumSize);
		this.measure2TextField.addActionListener(this.queryFWActionListener);
		panelBottom.add(this.measure2TextField);
		panelBottom.add(new JLabel(" Measure 3 : "));
		this.measure3TextField.setMaximumSize(maximumSize);
		this.measure3TextField.setPreferredSize(maximumSize);
		this.measure3TextField.addActionListener(this.queryFWActionListener);
		panelBottom.add(this.measure3TextField);

		Box inputBox = Box.createVerticalBox();
		inputBox.add(panelTop, "North");
		inputBox.add(panelBottom, "South");

		box.add(inputBox, "North");
		box.add(new JScrollPane(this.table), "Center");
		box.add(this.msgline, "South");

		contentPane.add(box, "North");
		this.messageArea = new JTextArea(7, 110);
		this.messageArea.setWrapStyleWord(true);
		this.messageArea.setLineWrap(true);
		this.messageArea.setEditable(false);
		JScrollPane messageScrollPane = new JScrollPane(this.messageArea, 20, 31);
		contentPane.add(messageScrollPane, "South");

		this.commaSeperatedPostcodes.addActionListener(this.queryFWActionListener);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(screenSize.width, screenSize.height - 100);

		setInformation();
		setGlassPane(this.glasspane);
		setVisible(true);
	}

	private void setInformation() {
		this.msgline.setText("<html>1: Enter comma seperated postcodes in the text box above e.g.  CB61AB, WD245BB <br> 2: Pick or Key in a Requirement and Hit Query </html>");
	}

	public FWFrame(FWResponseTableModelFactory f) {
		this.factory = f;
		init();
	}

	public void displayQueryResults() {
		String commaSeperatedPostcodesText = this.commaSeperatedPostcodes.getText();
		Date suppliedDate = ((GregorianCalendar) this.createJDatePicker.getModel().getValue()).getTime();
		JJSDate startDate = null;
		if (suppliedDate != null) {
			startDate = JJSDateAndTimeUtil.createJJSDateFromJavaUtilDate(suppliedDate);
		}
		else {
			Date currentDate = Calendar.getInstance().getTime();
			startDate = JJSDateAndTimeUtil.createJJSDateFromJavaUtilDate(currentDate);
		}

		String selectedItem = (String) this.editableCB.getSelectedItem();
		String profit = PROFITABILITY_1400.toString();
		if (this.profitabilityTextField.getText() != null) {
			profit = this.profitabilityTextField.getText();
		}
		BigDecimal profitability = new BigDecimal(profit);

		String sMeasure1 = this.measure1TextField.getText();
		String sMeasure2 = this.measure2TextField.getText();
		String sMeasure3 = this.measure3TextField.getText();
		if (StringUtils.isEmpty(commaSeperatedPostcodesText)) {
			this.commaSeperatedPostcodes.setBackground(Color.RED);
			return;
		}
		if ((StringUtils.isEmpty(sMeasure1)) || (!StringUtils.isNumeric(sMeasure1))) {
			this.measure1TextField.setBackground(Color.RED);
			return;
		}
		if ((StringUtils.isEmpty(sMeasure2)) || (!StringUtils.isNumeric(sMeasure2))) {
			this.measure2TextField.setBackground(Color.RED);
			return;
		}
		if ((StringUtils.isEmpty(sMeasure3)) || (!StringUtils.isNumeric(sMeasure3))) {
			this.measure3TextField.setBackground(Color.RED);
			return;
		}
		Integer measure1 = Integer.valueOf(Integer.parseInt(sMeasure1));
		Integer measure2 = Integer.valueOf(Integer.parseInt(sMeasure2));
		Integer measure3 = Integer.valueOf(Integer.parseInt(sMeasure3));
		this.table.setModel(FWResponseTableModel.EMPTY_MODEL);

		this.msgline.setText("Querying Fleetwise Please wait...");

		final String[] aOfPostcodes = StringUtils.split(StringUtils.upperCase(commaSeperatedPostcodesText), ",");
		List<String> asList = Arrays.asList(aOfPostcodes);
		final Set<String> sOfPostcodes = new HashSet<String>(new ArrayList<String>(asList));
		final int sizeOfItems = sOfPostcodes.size();
		final FWQueryDTO queryDTO = new FWQueryDTO(sOfPostcodes, startDate, selectedItem, profitability, measure1, measure2, measure3);
		Thread thread = new Thread(new Runnable() {

			public void run() {
				Set<String> sOfFailedPostcodes = SetUtils.EMPTY_SET;
				try {
					TableModel resultSetTableModel = FWFrame.this.factory.getResultSetTableModel(queryDTO);

					FWFrame.this.table.setModel(resultSetTableModel);
					sOfFailedPostcodes = ((FWResponseTableModel) resultSetTableModel).getSOfPostcodes();
					if (sOfFailedPostcodes.isEmpty()) {
						FWFrame.this.messageArea.setText(" No of rows fetched :[ " + resultSetTableModel.getRowCount() + " ]".concat("\n"));
					}
					else
						FWFrame.this.messageArea.setText(" ************ Not all advise calls were successfull. *******************\n Could not query \n" + sOfFailedPostcodes);

				}
				catch (Exception ex) {
					FWFrame.this.table.setModel(FWResponseTableModel.EMPTY_MODEL);
					FWFrame.this.messageArea.setText(ex.getClass().getName() + ": " + ex.getMessage());
					ex.printStackTrace();
				}
				FWFrame.this.setInformation();
				FWFrame.this.glasspane.stop();
				FWFrame.this.glasspane.setVisible(false);
				FWFrame.this.waitThread.stop();
				if (!sOfFailedPostcodes.isEmpty()) {
					JOptionPane.showMessageDialog(FWFrame.this, new String[] { "Not all advise calls were successfull.\nA total of " + aOfPostcodes.length + " were querried and " + sOfFailedPostcodes.size() + " failed" });
				}
				sOfFailedPostcodes.clear();
			}
		});
		thread.start();
		this.glasspane.start();
		this.waitThread = new Thread(new Runnable() {

			public void run() {
				while (true) {
					int sizeOfItemsPendingProcessing = sOfPostcodes.size();
					int a = sizeOfItemsPendingProcessing * 100 / sizeOfItems;
					StringBuffer message = new StringBuffer(sizeOfItemsPendingProcessing + " out of " + sizeOfItems + " postcode[s] left to query.");
					if (a < 20) {
						message.append(" Nearly there.");
					}
					else if (a <= 50) {
						message.append(" Half way there. Please wait....");
					}
					else {
						message.append(" This may take some time. Please wait.....");
					}

					FWFrame.this.glasspane.setText(message.toString());
				}
			}
		});
		this.waitThread.start();
		this.glasspane.setVisible(true);
	}

	public static void main(String[] args) throws Exception {
		FWResponseTableModelFactory factory = new FWResponseTableModelFactory();

		new FWFrame(factory);
	}

	public class FWQueryDTO {

		private Set<String> sOfpostcodes;
		private JJSDate startDate;
		private String requirement;
		private BigDecimal profitability;
		private Integer measure1;
		private Integer measure2;
		private Integer measure3;

		public FWQueryDTO(Set<String> aSOfPostcodes, JJSDate aStartDate, String aRequirement, BigDecimal aProfitability, Integer aMeasure1, Integer aMeasure2, Integer aMeasure3) {
			this.sOfpostcodes = aSOfPostcodes;
			this.startDate = aStartDate;
			this.requirement = aRequirement;
			this.profitability = aProfitability;
			this.measure1 = aMeasure1;
			this.measure2 = aMeasure2;
			this.measure3 = aMeasure3;
		}

		public Set<String> getPostcodes() {
			return this.sOfpostcodes;
		}

		public String getRequirement() {
			return this.requirement;
		}

		public BigDecimal getProfitability() {
			return this.profitability;
		}

		public JJSDate getStartDate() {
			return this.startDate;
		}

		public Integer getMeasure1() {
			return this.measure1;
		}

		public Integer getMeasure2() {
			return this.measure2;
		}

		public Integer getMeasure3() {
			return this.measure3;
		}
	}
}