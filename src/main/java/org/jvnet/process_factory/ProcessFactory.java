package org.jvnet.process_factory;

import org.jvnet.process_factory.OsCheck.OSType;
import org.jvnet.unixp.UnixProcess;
import org.jvnet.winp.WinProcess;

/**
 * Helper to create the proper process type depending on our system.
 */
public class ProcessFactory {

	public static AbstractProcess createProcess(long pid) {
		OSType operatingSystemType = OsCheck.getOperatingSystemType();
		if(operatingSystemType == OSType.Windows){
			return new WinProcess(pid);
		}
		return new UnixProcess(pid);
	}

	public static AbstractProcess createProcess(Process p) throws Exception {
		OSType operatingSystemType = OsCheck.getOperatingSystemType();
		if(operatingSystemType == OSType.Windows){
			return new WinProcess(p);
		}
		return new UnixProcess(p);
	}

}
