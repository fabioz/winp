import org.jvnet.process_factory.ProcessFactory;

/**
 * @author Kohsuke Kawaguchi
 */
public class Main2 {
    public static void main(String[] args) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(new String[]{"notepad"});
        Process p = pb.start();
        Thread.sleep(3000);
        ProcessFactory.createProcess(p).killRecursively();
    }
}
