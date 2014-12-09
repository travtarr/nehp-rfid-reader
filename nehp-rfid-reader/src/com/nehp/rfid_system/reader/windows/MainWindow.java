package com.nehp.rfid_system.reader.windows;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import com.intermec.datacollection.rfid.BRIReader;
import com.intermec.datacollection.rfid.BasicReaderException;
import com.intermec.datacollection.rfid.RFIDButtonAdapter;
import com.intermec.datacollection.rfid.RFIDButtonEvent;
import com.intermec.datacollection.rfid.Tag;
import com.intermec.datacollection.rfid.TagEvent;
import com.intermec.datacollection.rfid.TagEventListener;
import com.intermec.datacollection.rfid.TagField;
import com.nehp.rfid_system.reader.helpers.Config;
import com.nehp.rfid_system.reader.server_connection.Server;

import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Group;
import org.slf4j.Logger;
import org.slf4j.impl.SimpleLoggerFactory;

public class MainWindow {

	private final SimpleLoggerFactory factory = new SimpleLoggerFactory();
	private final Logger LOG = factory.getLogger("MainWindow");

	protected Shell shell;
	protected Display display;
	protected Config config;
	protected Server server;

	private BRIReader m_Reader = null;
	private RFIDButtonAdapter m_RFIDTriggerSingleAdapter = null;
	private RFIDButtonAdapter m_RFIDTriggerMultiAdapter = null;
	private String m_sFieldSchema = "ANT";

	private ProgressBar progressBar;
	private ProgressBar progressBarInd;
	private Table table;
	private Text text_rfid;
	private Text text_itemid;
	private Text text_description;
	private Text text_currentStage;
	private Text text_currentRevision;
	private Text text_currentDate;
	private Text text_currentUser;
	private Button btnSingle;
	private Button btnMulti;

	private final String[] STAGES = { "MODELING", "KITTING", "MANUFACTURING",
			"QA/QC", "SHIPPED", "ARRIVAL", "INSTALLED", "STOPPED" };

	/**
	 * 
	 */
	public MainWindow(Display display, Config config) {
		this.display = display;
		this.config = config;
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	public void open() {
		shell = new Shell(display, SWT.NO_TRIM | SWT.ON_TOP);

		createContents();
		setupReader();
		shell.open();
		shell.layout();
		startServerConn();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 * 
	 */
	protected void createContents() {

		shell.setSize(480, 640);
		shell.setText("NEHP Tracker");

		/**
		 * Main Title of App
		 */
		shell.setLayout(new RowLayout(SWT.HORIZONTAL));
		Label lblAppTitle = new Label(shell, SWT.CENTER);
		lblAppTitle.setLayoutData(new RowData(405, 40));
		lblAppTitle.setForeground(SWTResourceManager
				.getColor(SWT.COLOR_LINK_FOREGROUND));
		lblAppTitle.setBackground(SWTResourceManager
				.getColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT));
		lblAppTitle.setAlignment(SWT.CENTER);
		lblAppTitle.setFont(SWTResourceManager
				.getFont("Segoe UI", 16, SWT.BOLD));
		lblAppTitle.setText("NEHP Tracker");

		/**
		 * Exit the application
		 */
		Button btnExit = new Button(shell, SWT.NONE);
		btnExit.setLayoutData(new RowData(65, 40));
		btnExit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean confirm = MessageDialog.openConfirm(shell,
						"Exit Confirmation", "Do you want to exit?");
				if (confirm) {
					if (m_Reader != null)
						m_Reader.dispose();
					shell.close();
					shell.dispose();
					System.exit(0);
				}
			}
		});

		btnExit.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
		btnExit.setText("Exit");

		/**
		 * Progress Bar
		 */
		Composite compProgress = new Composite(shell, SWT.NONE);
		compProgress.setLayoutData(new RowData(470, SWT.DEFAULT));
		progressBar = new ProgressBar(compProgress, SWT.NONE);
		progressBar.setSize(470, 20);
		progressBarInd = new ProgressBar(compProgress, SWT.INDETERMINATE);
		progressBarInd.setSize(470, 20);
		progressBarInd.setVisible(false);
		progressBarInd.setEnabled(false);

		/**
		 * Button Menu Bar
		 */
		Composite buttonBar = new Composite(shell, SWT.NONE);

		btnSingle = new Button(buttonBar, SWT.NONE);
		btnSingle.setBounds(0, 0, 235, 36);
		btnSingle.setText("Single");

		btnMulti = new Button(buttonBar, SWT.NONE);
		btnMulti.setBounds(235, 0, 235, 36);
		btnMulti.setText("Multiple");

		/**
		 * Shared Panel
		 */
		final Composite sharedPanel = new Composite(shell, SWT.NONE);
		sharedPanel.setLayoutData(new RowData(470, 532));
		final StackLayout stkLayout = new StackLayout();
		sharedPanel.setLayout(stkLayout);

		/**
		 * Multiple Panel
		 */
		final Composite compMultiple = new Composite(sharedPanel, SWT.NONE);
		compMultiple.setBackground(SWTResourceManager.getColor(204, 204, 255));
		compMultiple.pack();

		Label lblMultiTitle = new Label(compMultiple, SWT.NONE);
		lblMultiTitle.setBackground(SWTResourceManager.getColor(204, 204, 255));
		lblMultiTitle.setText("Scan and View Multiple Items");
		lblMultiTitle.setFont(SWTResourceManager.getFont("Segoe UI", 14,
				SWT.BOLD));
		lblMultiTitle.setBounds(10, 10, 340, 32);

		TableViewer tableViewer = new TableViewer(compMultiple, SWT.BORDER
				| SWT.FULL_SELECTION);
		table = tableViewer.getTable();
		table.setBounds(10, 48, 450, 380);
		table.setHeaderVisible(true);

		TableViewerColumn tableViewerColumn = new TableViewerColumn(
				tableViewer, SWT.NONE);
		TableColumn tblclmnRfidId = tableViewerColumn.getColumn();
		tblclmnRfidId.setResizable(false);
		tblclmnRfidId.setWidth(130);
		tblclmnRfidId.setText("RFID ID #");

		TableViewerColumn tableViewerColumn_2 = new TableViewerColumn(
				tableViewer, SWT.NONE);
		TableColumn tblclmnItem = tableViewerColumn_2.getColumn();
		tblclmnItem.setResizable(false);
		tblclmnItem.setWidth(200);
		tblclmnItem.setText("Item #");

		TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(
				tableViewer, SWT.NONE);
		TableColumn tblclmnCurrentStatus = tableViewerColumn_1.getColumn();
		tblclmnCurrentStatus.setAlignment(SWT.CENTER);
		tblclmnCurrentStatus.setResizable(false);
		tblclmnCurrentStatus.setWidth(106);
		tblclmnCurrentStatus.setText("Current Status");
		

		/**
		 * Single Panel
		 */
		final Composite compSingle = new Composite(sharedPanel, SWT.NONE);
		compSingle.setBackground(SWTResourceManager.getColor(204, 204, 255));
		compSingle.setLayout(new FormLayout());

		Label lblSingleTitle = new Label(compSingle, SWT.NONE);
		FormData fd_lblSingleTitle = new FormData();
		fd_lblSingleTitle.bottom = new FormAttachment(0, 42);
		fd_lblSingleTitle.right = new FormAttachment(0, 350);
		fd_lblSingleTitle.top = new FormAttachment(0, 10);
		fd_lblSingleTitle.left = new FormAttachment(0, 10);
		lblSingleTitle.setLayoutData(fd_lblSingleTitle);
		lblSingleTitle
				.setBackground(SWTResourceManager.getColor(204, 204, 255));
		lblSingleTitle.setText("Scan and View a Single Item");
		lblSingleTitle.setFont(SWTResourceManager.getFont("Segoe UI", 14,
				SWT.BOLD));
		FormData fd_compSingleData = new FormData();
		fd_compSingleData.bottom = new FormAttachment(0, 213);
		fd_compSingleData.right = new FormAttachment(0, 460);
		fd_compSingleData.top = new FormAttachment(0, 44);
		fd_compSingleData.left = new FormAttachment(0, 10);

		final Button btnStageComplete = new Button(compSingle, SWT.NONE);
		btnStageComplete.setForeground(SWTResourceManager.getColor(0, 0, 0));
		FormData fd_btnStageComplete = new FormData();
		fd_btnStageComplete.right = new FormAttachment(100, -28);
		btnStageComplete.setLayoutData(fd_btnStageComplete);
		btnStageComplete.setText("Stage Complete");

		final Button btnSendBack = new Button(compSingle, SWT.NONE);
		fd_btnStageComplete.top = new FormAttachment(btnSendBack, 0, SWT.TOP);
		btnSendBack.setForeground(SWTResourceManager.getColor(0, 0, 0));
		FormData fd_btnSendBack = new FormData();
		fd_btnSendBack.bottom = new FormAttachment(100, -22);
		fd_btnSendBack.left = new FormAttachment(0, 25);
		fd_btnSendBack.right = new FormAttachment(100, -349);
		btnSendBack.setLayoutData(fd_btnSendBack);
		btnSendBack.setText("Send Back");

		Composite compSingleData = new Composite(compSingle, SWT.NONE);
		FormData fd_compSingleData2 = new FormData();
		fd_compSingleData2.left = new FormAttachment(0, 10);
		fd_compSingleData2.top = new FormAttachment(0, 40);
		fd_compSingleData2.width = 450;
		compSingleData.setLayoutData(fd_compSingleData2);
		compSingleData
				.setBackground(SWTResourceManager.getColor(204, 204, 255));
		compSingleData.setLayout(new RowLayout(SWT.HORIZONTAL));

		Label lblRfid = new Label(compSingleData, SWT.NONE);
		lblRfid.setBackground(SWTResourceManager.getColor(204, 204, 255));
		lblRfid.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		lblRfid.setText("RFID #");

		text_rfid = new Text(compSingleData, SWT.BORDER);
		text_rfid.setLayoutData(new RowData(156, SWT.DEFAULT));
		text_rfid.setEditable(false);

		Label lblItemId = new Label(compSingleData, SWT.NONE);
		lblItemId.setBackground(SWTResourceManager.getColor(204, 204, 255));
		lblItemId.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD));
		lblItemId.setLayoutData(new RowData(SWT.DEFAULT, 20));
		lblItemId.setText(" Item ID #");

		text_itemid = new Text(compSingleData, SWT.BORDER);
		text_itemid.setLayoutData(new RowData(149, SWT.DEFAULT));
		text_itemid.setEditable(false);

		Label lblDescription = new Label(compSingleData, SWT.NONE);
		lblDescription
				.setBackground(SWTResourceManager.getColor(204, 204, 255));
		lblDescription.setFont(SWTResourceManager.getFont("Segoe UI", 10,
				SWT.BOLD));
		lblDescription.setText("Description ");

		text_description = new Text(compSingleData, SWT.BORDER | SWT.WRAP
				| SWT.MULTI);
		text_description.setLayoutData(new RowData(356, 40));
		text_description.setEditable(false);

		Label lblCurrentStage = new Label(compSingleData, SWT.NONE);
		lblCurrentStage.setBackground(SWTResourceManager
				.getColor(204, 204, 255));
		lblCurrentStage.setFont(SWTResourceManager.getFont("Segoe UI", 10,
				SWT.BOLD));
		lblCurrentStage.setText("Current Stage");

		text_currentStage = new Text(compSingleData, SWT.BORDER);
		text_currentStage.setEditable(false);
		text_currentStage.setLayoutData(new RowData(154, SWT.DEFAULT));

		Label lblCurrentRevision = new Label(compSingleData, SWT.NONE);
		lblCurrentRevision.setBackground(SWTResourceManager.getColor(204, 204,
				255));
		lblCurrentRevision.setFont(SWTResourceManager.getFont("Segoe UI", 10,
				SWT.BOLD));
		lblCurrentRevision.setText(" Current Revision");

		text_currentRevision = new Text(compSingleData, SWT.BORDER);
		text_currentRevision.setLayoutData(new RowData(64, SWT.DEFAULT));
		text_currentRevision.setEditable(false);

		Group grpLastStatusChange = new Group(compSingleData, SWT.NONE);
		grpLastStatusChange.setBackground(SWTResourceManager.getColor(204, 204,
				255));
		grpLastStatusChange.setText("Last Status Change");
		grpLastStatusChange.setLayoutData(new RowData(433, 34));

		Label lblCurrentDate = new Label(grpLastStatusChange, SWT.NONE);
		lblCurrentDate
				.setBackground(SWTResourceManager.getColor(204, 204, 255));
		lblCurrentDate.setFont(SWTResourceManager.getFont("Segoe UI", 10,
				SWT.BOLD));
		lblCurrentDate.setBounds(10, 24, 33, 20);
		lblCurrentDate.setText("Date");

		text_currentDate = new Text(grpLastStatusChange, SWT.BORDER);
		text_currentDate.setEditable(false);
		text_currentDate.setBounds(45, 23, 208, 21);

		Label lblCurrentUser = new Label(grpLastStatusChange, SWT.NONE);
		lblCurrentUser
				.setBackground(SWTResourceManager.getColor(204, 204, 255));
		lblCurrentUser.setFont(SWTResourceManager.getFont("Segoe UI", 10,
				SWT.BOLD));
		lblCurrentUser.setBounds(272, 24, 33, 20);
		lblCurrentUser.setText("User");

		text_currentUser = new Text(grpLastStatusChange, SWT.BORDER);
		text_currentUser.setEditable(false);
		text_currentUser.setBounds(311, 24, 118, 21);

		/**
		 * Confirm Sending Item Back
		 */
		final Composite compSingleConfirm = new Composite(compSingle,
				SWT.BORDER);
		FormData fd_compSingleConfirm = new FormData();
		fd_compSingleConfirm.bottom = new FormAttachment(compSingleData, 178,
				SWT.BOTTOM);
		fd_compSingleConfirm.right = new FormAttachment(0, 460);
		fd_compSingleConfirm.top = new FormAttachment(compSingleData, 31);
		fd_compSingleConfirm.left = new FormAttachment(0, 10);
		compSingleConfirm.setLayoutData(fd_compSingleConfirm);
		compSingleConfirm.setVisible(false);

		Label lblSendBackTo = new Label(compSingleConfirm, SWT.NONE);
		lblSendBackTo.setFont(SWTResourceManager.getFont("Segoe UI", 10,
				SWT.BOLD));
		lblSendBackTo.setBounds(10, 10, 253, 25);
		lblSendBackTo.setText("Send Back to Which Stage?");

		Button btnConfirmCancel = new Button(compSingleConfirm, SWT.NONE);
		btnConfirmCancel.setBounds(10, 108, 75, 25);
		btnConfirmCancel.setText("Cancel");

		Button btnConfirmConfirm = new Button(compSingleConfirm, SWT.NONE);
		btnConfirmConfirm.setBounds(361, 108, 75, 25);
		btnConfirmConfirm.setText("Confirm");

		final ToolBar toolBar = new ToolBar(compSingleConfirm, SWT.FLAT
				| SWT.RIGHT);
		toolBar.setBounds(20, 45, 164, 31);

		final ToolItem tltmSelectStage = new ToolItem(toolBar, SWT.DROP_DOWN);
		tltmSelectStage.setWidth(70);
		tltmSelectStage.setText("Select Stage");

		Button btnRefresh = new Button(compSingle, SWT.NONE);
		FormData fd_btnRefresh = new FormData();
		fd_btnRefresh.top = new FormAttachment(lblSingleTitle, 0, SWT.TOP);
		fd_btnRefresh.right = new FormAttachment(100, -10);
		btnRefresh.setLayoutData(fd_btnRefresh);
		btnRefresh.setText("Refresh");

		final Menu menu = new Menu(shell, SWT.POP_UP);

		// Sets the options of the drop-down selection for stages
		for (int i = 0; i < STAGES.length; i++) {
			MenuItem item = new MenuItem(menu, SWT.PUSH);
			item.setText(STAGES[i]);
			final String status = STAGES[i];
			item.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					tltmSelectStage.setText(status);
				}

			});
		}

		tltmSelectStage.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (event.detail == SWT.ARROW) {
					Rectangle rect = tltmSelectStage.getBounds();
					Point pt = new Point(rect.x, rect.y + rect.height);
					pt = toolBar.toDisplay(pt);
					menu.setLocation(pt.x, pt.y);
					menu.setVisible(true);
				}
			}
		});

		/**
		 * Switch between pages
		 */
		btnSingle.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				stkLayout.topControl = compSingle;
				setSingleRead();
				sharedPanel.layout();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		btnMulti.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				stkLayout.topControl = compMultiple;
				setMultiRead();
				sharedPanel.layout();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		/**
		 * Send Back
		 */
		btnSendBack.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				btnSendBack.setVisible(false);
				compSingleConfirm.setVisible(true);
				btnStageComplete.setEnabled(false);
				btnStageComplete.setVisible(false);
				LOG.info("isConnected: {}", server.isConnected());
			}
		});

		btnConfirmCancel.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				btnSendBack.setVisible(true);
				compSingleConfirm.setVisible(false);
				btnStageComplete.setEnabled(true);
				btnStageComplete.setVisible(true);
				LOG.info("Cancelling");
			}
		});

		btnConfirmConfirm.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				btnSendBack.setVisible(true);
				compSingleConfirm.setVisible(false);
				btnStageComplete.setEnabled(true);
				btnStageComplete.setVisible(true);
				LOG.info("Confirming stage: {}", tltmSelectStage.getText());
			}
		});

		btnStageComplete.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				// TODO: send data to server while updating progress bar
				// TODO: show confirmation of success or show error
				LOG.info("Stage completed");
			}
		});

	}

	/**
	 * Sets up the RFID reader.
	 */
	private void setupReader() {
		try {
			m_Reader = new BRIReader();
		} catch (BasicReaderException bre) {
			MessageDialog.openError(shell,
					"Exception occurs during reader connection",
					"Please verify the RFID reader is charged and turned on.");
			return;
		}
		
		// Sets up the RFID trigger button
		try {
			m_Reader.setRFIDButtonEnabled(BRIReader.RFIDButtonIDs.CENTER, true);
		} catch (BasicReaderException e) {
			e.printStackTrace();
		}
		
		// Adds a tag listener for continuous reading mode.
		m_Reader.addTagEventListener(new TagEventListener(){

			@Override
			public void tagRead(TagEvent tagEvent) {
				Tag tag = tagEvent.getTag();
				TableItem item = new TableItem(table, SWT.NONE);
				item.setText(0, tag.toString());
				int iNumTagFields = tag.tagFields.getItemCount();
				if (iNumTagFields > 0){
					TagField tagData = tag.tagFields.getField(0);
					item.setText( 1, String.valueOf(tagData.getDataLong()) );
				}
			}
			
		});
	}

	private RFIDButtonAdapter getTriggerSingleAdapter() {

		if (m_RFIDTriggerSingleAdapter != null) {
			m_RFIDTriggerSingleAdapter = new RFIDButtonAdapter() {
				public void buttonPressed(RFIDButtonEvent aButtonEvent) {
					if (aButtonEvent.getButtonID() == BRIReader.RFIDButtonIDs.CENTER) {
						// *****
						// * Issue a singleshot read when the center trigger is
						// pressed.
						// *****
						LOG.info("I-Center trigger read.\n");

						try {
							// Read all tags in the field.
							if (m_Reader.read() == true) {
								if (m_Reader.getTagCount() > 0)
									text_rfid.setText(m_Reader.tags[0].toString());
							} else {
								LOG.info("NO tags read\n");
							}
						} catch (Exception e) {
							LOG.error(e.getMessage());
						}
					}
				} 
			};
		}

		return m_RFIDTriggerSingleAdapter;
	}

	private RFIDButtonAdapter getTriggerMultiAdapter() {

		if (m_RFIDTriggerMultiAdapter != null) {
			m_RFIDTriggerMultiAdapter = new RFIDButtonAdapter() {
				@Override
				public void buttonPressed(RFIDButtonEvent aButtonEvent) {
					if (aButtonEvent.getButtonID() == BRIReader.RFIDButtonIDs.CENTER) {
						LOG.info("I-Center trigger read.\n");
						boolean success;
						try {
							success = m_Reader.startReadingTags(null,
									m_sFieldSchema,
									BRIReader.TagReportOptions.EVENT);
							if (success) {
								
								// reset table
								table.clearAll();
								
								// make sure user doesn't switch windows while reading
								btnSingle.setEnabled(false);
								
								// start progress bar
								progressBarInd.setVisible(true);
								progressBarInd.setEnabled(true);
							}
						} catch (Exception e) {
							LOG.error(e.getMessage());
						}
					}
				}

				@Override
				public void buttonReleased(RFIDButtonEvent aButtonEvent) {
					if (aButtonEvent.getButtonID() == BRIReader.RFIDButtonIDs.CENTER) {
						LOG.info("I-Center trigger release.\n");
						try {
							m_Reader.stopReadingTags();
							
							// re-enable button
							btnSingle.setEnabled(true);
							
							// stop progress bar
							progressBarInd.setVisible(false);
							progressBarInd.setEnabled(false);
							
							// update status of table items from server
							updateTable();
							
						} catch (BasicReaderException e) {
							LOG.error(e.getMessage());
						}
					}
				}
			};
		}

		return m_RFIDTriggerMultiAdapter;
	}

	
	/**
	 * Sets up the RFID Button listener to read a single tag.
	 */
	private void setSingleRead() {
		if (m_RFIDTriggerMultiAdapter != null)
			m_Reader.removeRFIDButtonListener(m_RFIDTriggerMultiAdapter);
		m_Reader.addRFIDButtonListener(getTriggerSingleAdapter());
	}
	

	/**
	 * Sets up the RFID Button listener to read tags until the button is
	 * released.
	 */
	private void setMultiRead() {
		if (m_RFIDTriggerSingleAdapter != null)
			m_Reader.removeRFIDButtonListener(m_RFIDTriggerSingleAdapter);
		m_Reader.addRFIDButtonListener(getTriggerMultiAdapter());
	}
	
	
	/**
	 * Updates the status of each item in the table with data from the server.
	 */
	private void updateTable() {
		// TODO: send tags to server and retrieve status of each
	}

	
	/**
	 * Sets up the connection to the server.
	 */
	protected void startServerConn() {
		server = new Server(config.getAddress(), config.getUser(),
				config.getPassword(), this);

		progressBar.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (!server.login(progressBar)) {
					MessageDialog
							.openInformation(
									shell,
									"Warning",
									"Connection failed, "
											+ "all transactions will be stored until connection is re-established. "
											+ "Certain data will be blank upon scanning items.");
				}
			}
		});
	}
}
