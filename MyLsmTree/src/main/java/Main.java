

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.TreeMap;

public class Main {
    private TreeMap<Integer, String> tree;
    MyFileManager mfm;
    private MyScanner in;
    private PrintWriter out;

    private final String dataFileName = "data.in";
    private final int MAX_TREE_SIZE = 10;

    public static void main(String[] args) throws IOException {
        Locale.setDefault(Locale.US);
        new Main().run();
    }

    private void run() throws IOException {
        in = new MyScanner();
        out = new PrintWriter(System.out);
        tree = new TreeMap<>();
        mfm = new MyFileManager(dataFileName);
        inputHandle();
        mfm.close(dataFileName);
        in.close();
        out.close();
    }

    private void inputHandle() throws IOException {
        out.println("Welcome to Lsm Tree demo!\n" +
                "To start using it type following commands:\n" +
                "- add <key> <value>\n" +
                "- find <key>\n"        +
                "- delete <key>\n"      +
                "- exit\n"              +
                "Warning! Key must be Integer!");
        out.flush();
        String command;
        input:
        while (true) {
            command = in.next();
            switch (command) {
                case "add": {
                    int key = in.nextInt();
                    String value = in.next();
                    tree.put(key, value);
                    out.println("Added key " + key + " with value: " + value);

                    if (tree.size() >= MAX_TREE_SIZE) {
                        mfm.addNewFile(tree);
                        tree = new TreeMap<>();
                    }
                    break;
                }
                case "find": {
                    int key = in.nextInt();
                    if (!tree.containsKey(key)) {
                        String val = mfm.findValue(key);
                        out.println(val == null ? "No such key" : "Key founded with value: " + val);
                    } else {
                        out.println("Key founded with value: " + tree.get(key));
                    }
                    break;
                }
                case "delete": {
                    int key = in.nextInt();
                    if (!tree.containsKey(key)) {
                        boolean deleted = mfm.removeKey(key);
                        out.println(deleted ? "Key removed successfully" : "No suck key");
                    } else {
                        tree.remove(key);
                        out.println("Key removed successfully!");
                    }
                    break;
                }
                case "exit": {
                    if (tree.size() > 0) mfm.addNewFile(tree);
                    break input;
                }
                default: {
                    out.println("Wrong command. Use commands:" +
                            "\"add <key> <value>\", \"find <key>\", \"delete <key>\"");
                }
            }
            out.flush();
        }
    }
}