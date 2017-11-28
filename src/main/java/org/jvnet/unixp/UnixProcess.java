package org.jvnet.unixp;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.StringTokenizer;

import org.jvnet.process_factory.AbstractProcess;

/**
 * Basic unix process which relies on kill and pgrep to kill a process tree.
 *
 * @author Fabio Zadrozny
 * @license MIT
 */
public class UnixProcess extends AbstractProcess {

	public UnixProcess(long pid) {
		super(pid);
	}

	public UnixProcess(Process p) throws Exception {
		super(getPid(p));
	}

	private static long getPid(Process process) throws Exception {
		try {
			// On java 9, we have a Process.pid() method
			// Use through reflection because we have to support previous versions
			// of java too.
			try {
				Method m = Process.class.getDeclaredMethod("pid");
				m.setAccessible(true);
				Object pid = m.invoke(process);
				if(pid instanceof Integer) {
					return ((Integer) pid).intValue();
				}
				if(pid instanceof Long) {
					return ((Long) pid).longValue();
				}
			} catch (Throwable e) {
				// just ignore (not java 9).
			}

			Field f = process.getClass().getDeclaredField("pid");
			f.setAccessible(true);
			return f.getInt(process);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void killRecursively() throws IOException {
		killRecursively(pid);
	}

	@Override
	public void kill() throws IOException {
		runAndGetOutput(new String[] { "kill", "-KILL",
				Long.toString(pid) });
	}

	/**
	 * Adds to the final list all the pids of the process tree. It's ordered so
	 * that the parent is the first item and children are always after the
	 * parent.
	 *
	 * @throws IOException
	 */
	private static void killRecursively(long pid, LinkedHashSet<Long> listed)
			throws IOException {
		listed.add(pid);

		// When listing, before getting the children, ask to stop forking
		runAndGetOutput(new String[] { "kill", "-stop", Long.toString(pid) });

		// Now, get the children
		Output outputPGrep = runAndGetOutput("pgrep", "-P",
				Long.toString(pid));

		if (outputPGrep.stderr != null && outputPGrep.stderr.length() > 0) {
			throw new RuntimeException(outputPGrep.stderr);
		}

		// When the children are gotten actually go on and forcefully kill the
		// parent
		runAndGetOutput("kill", "-KILL", Long.toString(pid));

		String ids = outputPGrep.stdout;
		StringTokenizer strTok = new StringTokenizer(ids);
		while (strTok.hasMoreTokens()) {
			String nextToken = strTok.nextToken();
			long found = Long.parseLong(nextToken);
			if (!listed.contains(found)) {
				killRecursively(found, listed);
			}
		}
	}

	/**
	 * This is the public API to kill ids recursively. Note that it'll initially
	 * just do a Ctrl+C
	 *
	 * @param pid
	 * @return
	 * @throws IOException
	 */
	private static LinkedHashSet<Long> killRecursively(long pid)
			throws IOException {
		final LinkedHashSet<Long> listed = new LinkedHashSet<Long>();
		killRecursively(pid, listed);
		return listed;
	}

	private static Output runAndGetOutput(String... cmdarray)
			throws IOException {
		Process createProcess = Runtime.getRuntime().exec(cmdarray, null, null);
		return getProcessOutput(createProcess);
	}

	private static class Output {
		public final String stdout;
		public final String stderr;

		public Output(String stdout, String stderr) {
			this.stdout = stdout;
			this.stderr = stderr;
		}
	}

	public static Output getProcessOutput(Process process) throws IOException {
		try {
			// i.e.: no writing to it anymore...
			process.getOutputStream().close();
		} catch (IOException e2) {
		}

		InputStreamReader inputStream = new InputStreamReader(
				new BufferedInputStream(process.getInputStream()));
		InputStreamReader errorStream = new InputStreamReader(
				new BufferedInputStream(process.getErrorStream()));

		try {
			// Wait for it to finish
			process.waitFor();
		} catch (InterruptedException e1) {
		}

		try {
			// Wait a bit for the output to be available (just in case).
			Object sync = new Object();
			synchronized (sync) {
				sync.wait(10);
			}
		} catch (InterruptedException e) {
		}

		return new Output(readInputStream(inputStream),
				readInputStream(errorStream));
	}

	private static String readInputStream(InputStreamReader in)
			throws IOException {
		int c;
		StringBuffer contents = new StringBuffer();
		char[] buf = new char[80];
		while ((c = in.read(buf)) != -1) {
			contents.append(buf, 0, c);
		}
		return contents.toString();
	}
}
