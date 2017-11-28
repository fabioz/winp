package org.jvnet.process_factory;

/**
 * Base class for processes on all platforms.
 * 
 * @author Fabio Zadrozny
 * @license MIT
 */
public abstract class AbstractProcess {

	protected final long pid;

	public AbstractProcess(long pid) {
		this.pid = pid;
	}

	/**
	 * Gets the process ID.
	 */
	public long getPid() {
		return pid;
	}

	public abstract void killRecursively() throws Exception;

	public abstract void kill() throws Exception;

}
