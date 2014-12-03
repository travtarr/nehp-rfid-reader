package com.nehp.rfid_system.reader.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

import com.nehp.rfid_system.reader.helpers.SimpleSignal;
import org.eclipse.swt.graphics.Point;
import org.eclipse.wb.swt.SWTResourceManager;

public class Loading {

	protected Shell shell;
	private Display display = null;

	private int splashPos = 0;
	private final int SPLASH_MAX = 100;

	public Loading(Display display) {
		this.display = display;
	}

	/**
	 * Open the window.
	 */
	public void open() {
		createContents();
	}

	/**
	 * Create contents of the window.
	 * 
	 * @wbp.parser.entryPoint
	 */
	protected void createContents() {

		shell = new Shell(SWT.ON_TOP | SWT.NO_TRIM);
		shell.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		shell.setSize(480, 640);
		shell.setText("NEHP Tracker");

		Label label = new Label(shell, SWT.NONE);
		label.setBounds(63, 28, 43, 15);
		label.setText("Loading");
		final ProgressBar bar = new ProgressBar(shell, SWT.NONE);
		bar.setBounds(-5, 0, 170, 17);
		bar.setMaximum(SPLASH_MAX);
		shell.pack();

		Rectangle splashRect = shell.getBounds();
		Rectangle displayRect = display.getBounds();
		int x = (displayRect.width - splashRect.width) / 2;
		int y = (displayRect.height - splashRect.height) / 2;
		shell.setLocation(x, y);
		shell.open();

		display.asyncExec(new Runnable() {
			public void run() {

				for (splashPos = 0; splashPos < SPLASH_MAX; splashPos++) {
					try {

						Thread.sleep(100);
					} catch (InterruptedException e) {

						e.printStackTrace();
					}
					bar.setSelection(splashPos);
				}
				shell.close();
				
			}
		});

		while (splashPos != SPLASH_MAX) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

}
