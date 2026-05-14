package del.ifs24010;

import java.util.*;
import java.io.*;

public class App {
    public static void main(String[] args) throws IOException {
        FastReader sc = new FastReader(System.in);
        PrintWriter out = new PrintWriter(System.out);

        String sN = sc.next();
        if (sN == null) return;
        int n = Integer.parseInt(sN);
        int q = Integer.parseInt(sc.next());

        long[] weights = new long[n + 1];
        for (int i = 1; i <= n; i++) {
            weights[i] = Long.parseLong(sc.next());
        }

        int[] parents = new int[n + 1];
        if (n > 1) {
            for (int i = 2; i <= n; i++) {
                parents[i] = Integer.parseInt(sc.next());
            }
        }

        Program program = new Program();
        program.initializeLibrary(n, weights, parents);

        for (int i = 0; i < q; i++) {
            int type = Integer.parseInt(sc.next());
            if (type == 1) {
                int x = Integer.parseInt(sc.next());
                long v = Long.parseLong(sc.next());
                program.updateFragmentValue(x, v);
            } else {
                out.println(program.calculateMaxHarmony());
            }
        }
        out.flush();
        out.close();
    }

    static class FastReader {
        BufferedReader br;
        StringTokenizer st;

        public FastReader(InputStream is) {
            br = new BufferedReader(new InputStreamReader(is));
        }

        String next() {
            while (st == null || !st.hasMoreElements()) {
                try {
                    String line = br.readLine();
                    if (line == null) return null;
                    st = new StringTokenizer(line);
                } catch (IOException e) {
                    return null;
                }
    }
            return st.nextToken();
        }
    }
}