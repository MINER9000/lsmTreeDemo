

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class MyScanner {
    private BufferedReader br;
    private StringTokenizer st;

    MyScanner() {
        this.br = new BufferedReader(new InputStreamReader(System.in));
    }

    MyScanner(String fileTitle) throws IOException {
        this.br = new BufferedReader(new FileReader(fileTitle));
    }

    public String nextLine() throws IOException {
        String s = br.readLine();
        return s == null ? "" : s;
    }

    String next() throws IOException {
        while (st == null || !st.hasMoreTokens()) {
            String s = br.readLine();
            if (s == null || s.equals("")) {
                return "";
            }
            st = new StringTokenizer(s);
        }
        return st.nextToken();
    }

    public Integer nextInt() throws IOException {
        return Integer.parseInt(this.next());
    }

    public Long nextLong() throws IOException {
        return Long.parseLong(this.next());
    }

    public Double nextDouble() throws IOException {
        return Double.parseDouble(this.next());
    }

    void close() throws IOException {
        this.br.close();
    }
}