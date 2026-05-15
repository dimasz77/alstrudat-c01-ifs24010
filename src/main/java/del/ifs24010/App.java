package del.ifs24010;

import java.io.*;
import java.util.StringTokenizer;

public class App {
    public static void main(String[] args) throws IOException {
        FastReader sc = new FastReader(System.in);
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)));

        while (true) {
            String s = sc.next();
            if (s == null || s.equals("-1")) break;
            
            int n = Integer.parseInt(s);
            int q = sc.nextInt();

            long[] weights = new long[n + 1];
            for (int i = 1; i <= n; i++) weights[i] = sc.nextLong();

            int[] parents = new int[n + 1];
            for (int i = 2; i <= n; i++) parents[i] = sc.nextInt();

            Program program = new Program();
            program.initializeLibrary(n, weights, parents);

            for (int i = 0; i < q; i++) {
                int type = sc.nextInt();
                if (type == 1) {
                    program.updateFragmentValue(sc.nextInt(), sc.nextLong());
                } else {
                    out.println(program.calculateMaxHarmony());
                }
            }
        }
        out.flush();
        out.close();
    }

    static class FastReader {
        BufferedReader br;
        StringTokenizer st;

        // Constructor ini yang sebelumnya hilang di kode Anda
        public FastReader(InputStream is) {
            br = new BufferedReader(new InputStreamReader(is));
        }

        String next() {
            while (st == null || !st.hasMoreElements()) {
                String line;
                try {
                    line = br.readLine();
                } catch (IOException e) {
                    return null;
                }
                if (line == null) return null;
                st = new StringTokenizer(line);
            }
            return st.nextToken();
        }

        int nextInt() {
            return Integer.parseInt(next());
        }

        long nextLong() {
            return Long.parseLong(next());
        }
    }
}