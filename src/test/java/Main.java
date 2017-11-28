import org.jvnet.process_factory.AbstractProcess;
import org.jvnet.process_factory.ProcessFactory;
import org.jvnet.winp.WinProcess;
import org.jvnet.winp.Priority;

/**
 * Test program.
 * @author Kohsuke Kawaguchi
 */
public class Main {
    public static void main(String[] args) throws Exception {
        AbstractProcess p = ProcessFactory.createProcess(Integer.parseInt(args[0]));
        p.killRecursively();
        // p.setPriority(Priority.BELOW_NORMAL);
    }
}
