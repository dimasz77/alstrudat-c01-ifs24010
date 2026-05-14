package del.ifs24010;

import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class App {
    public static void main(String[] args) throws IOException {
        FastScanner sc = new FastScanner(System.in);
        PrintWriter out = new PrintWriter(System.out);

        // Loop ini akan membaca SEMUA soal sampai file benar-benar habis
        while (true) {
            int n = sc.nextInt();
            if (n == -1) {
                break; // File selesai
            }
            
            int q = sc.nextInt();

            long[] weights = new long[n + 1];
            for (int i = 1; i <= n; i++) {
                weights[i] = sc.nextLong();
            }

            int[] parents = new int[n + 1];
            if (n > 1) {
                for (int i = 2; i <= n; i++) {
                    parents[i] = sc.nextInt();
                }
            }

            Program program = new Program();
            program.initializeLibrary(n, weights, parents);

            for (int i = 0; i < q; i++) {
                int type = sc.nextInt();
                if (type == 1) {
                    int x = sc.nextInt();
                    long v = sc.nextLong();
                    program.updateFragmentValue(x, v);
                } else {
                    out.println(program.calculateMaxHarmony());
                }
            }
        }
        
        out.flush();
        out.close();
    }

    static class FastScanner {
        private InputStream stream;
        private byte[] buf = new byte[1024 * 1024];
        private int head, tail;

        public FastScanner(InputStream stream) {
            this.stream = stream;
        }

        private int read() {
            if (head >= tail) {
                head = 0;
                try {
                    tail = stream.read(buf, 0, buf.length);
                } catch (IOException e) {
                    return -1;
                }
                if (tail <= 0) return -1;
            }
            return buf[head++];
        }

        public int nextInt() {
            int c = read();
            while (c <= 32) {
                if (c == -1) return -1;
                c = read();
            }
            int res = 0;
            boolean neg = false;
            if (c == '-') {
                neg = true;
                c = read();
            }
            while (c > 32) {
                res = res * 10 + c - '0';
                c = read();
            }
            return neg ? -res : res;
        }

        public long nextLong() {
            int c = read();
            while (c <= 32) {
                if (c == -1) return -1;
                c = read();
            }
            long res = 0;
            boolean neg = false;
            if (c == '-') {
                neg = true;
                c = read();
            }
            while (c > 32) {
                res = res * 10 + c - '0';
                c = read();
            }
            return neg ? -res : res;
        }
    }
}