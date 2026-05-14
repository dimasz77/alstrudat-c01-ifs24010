package del.ifs24010;

import java.io.InputStream;
import java.io.IOException;

public class App {
    public static void main(String[] args) throws IOException {
        FastScanner sc = new FastScanner(System.in);
        StringBuilder out = new StringBuilder();

        // Membaca N (jika file kosong, n = -1)
        int n = sc.nextInt();
        if (n == -1) {
            return;
        }
        
        int q = sc.nextInt();

        // Membaca Nilai Keaslian Awal
        long[] weights = new long[n + 1];
        for (int i = 1; i <= n; i++) {
            weights[i] = sc.nextLong();
        }

        // Membaca Sumber Fragmen (Parent)
        int[] parents = new int[n + 1];
        if (n > 1) {
            for (int i = 2; i <= n; i++) {
                parents[i] = sc.nextInt();
            }
        }

        // Inisialisasi Program
        Program program = new Program();
        program.initializeLibrary(n, weights, parents);

        // Memproses Kueri
        for (int i = 0; i < q; i++) {
            int type = sc.nextInt();
            if (type == 1) {
                int x = sc.nextInt();
                long v = sc.nextLong();
                program.updateFragmentValue(x, v);
            } else {
                // Simpan hasil ke memori sementara (super cepat)
                out.append(program.calculateMaxHarmony()).append("\n");
            }
        }
        
        // Cetak semua hasil sekaligus ke terminal tanpa cacat format
        System.out.print(out);
    }

    // Custom Scanner Super Cepat (Anti Time-Limit)
    static class FastScanner {
        private InputStream stream;
        private byte[] buf = new byte[1024 * 1024]; // Buffer 1MB
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