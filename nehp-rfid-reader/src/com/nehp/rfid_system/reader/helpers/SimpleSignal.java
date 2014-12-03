package com.nehp.rfid_system.reader.helpers;

public class SimpleSignal {
	protected boolean doneLoading = false;

	public synchronized boolean doneLoading() {
		return this.doneLoading;
	}

	public synchronized void setDoneLoading(boolean done) {
		this.doneLoading = done;
	}

}
