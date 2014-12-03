package com.nehp.rfid_system.reader.windows;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import com.intermec.datacollection.rfid.BRIReader;
import com.intermec.datacollection.rfid.RFIDButtonAdapter;
import com.nehp.rfid_system.reader.Launcher;
import com.nehp.rfid_system.reader.helpers.SimpleSignal;
import com.nehp.rfid_system.reader.server_connection.Server;

public class MainWindow {

	protected Shell shell;
	private Server server;
	
	private BRIReader          m_Reader = null;
	private RFIDButtonAdapter  m_RFIDCenterBtnAdapter = null;
	private String             m_sFieldSchema = "ANT";
	private boolean            m_fContinuousReadInProgress = false;
	
	Display display = null;
	
	/**
	 * 
	 */
	public MainWindow(Display display, Server server){
		this.display = display;
		this.server = server;
	}
	
	/**
	 * Open the window.
	 * @wbp.parser.entryPoint
	 */
	public void open() {
		createContents();
		shell.open();
		shell.layout();
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
		shell = new Shell(display, SWT.NO_TRIM | SWT.ON_TOP);
		shell.setSize(480, 640);
		shell.setText("NEHP Tracker");
		
		/**
		 * Main Title of App
		 */
		Label lblAppTitle = new Label(shell, SWT.NONE);
		lblAppTitle.setForeground(SWTResourceManager.getColor(SWT.COLOR_LINK_FOREGROUND));
		lblAppTitle.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT));
		lblAppTitle.setAlignment(SWT.CENTER);
		lblAppTitle.setFont(SWTResourceManager.getFont("Segoe UI", 16, SWT.NORMAL));
		lblAppTitle.setBounds(0, 0, 403, 38);
		lblAppTitle.setText("NEHP Tracker");
		
		/**
		 * Exit the application
		 */
		Button btnExit = new Button(shell, SWT.NONE);
		btnExit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean confirm = MessageDialog.openConfirm(shell, "Exit", "Do you want to exit?");
				if(confirm){
					// TODO: need to close connections to server and RFID reader
					shell.close();
					shell.dispose();
				}
			}
		});
		btnExit.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
		btnExit.setBounds(402, 0, 78, 38);
		btnExit.setText("Exit");

	}
}
