package del.ifs24010;

import java.util.*;

public class Program {
    // Gunakan konstanta INF yang besar untuk menangani nilai negatif
    private static final long INF = 1_000_000_000_000_000L; 
    
    private int n;
    private long[] weights;
    // Deklarasi semua array yang diperlukan sebagai field class
    private int[] parent, heavy, head, pos, tail, size, nodeAtPos, visitOrder;
    private int curPos;
    private int[] hEdge, nEdge, tEdge;

    // Array untuk Segment Tree
    private long[] tSum, tPref, tSuff, tAns;
    private MaxHeap[] lightF, lightG;

    public void initializeLibrary(int n, long[] w, int[] p) {
        this.n = n;
        this.weights = w;
        this.curPos = 0;
        
        // Inisialisasi array adjacency list
        this.hEdge = new int[n + 1];
        this.nEdge = new int[n + 1];
        this.tEdge = new int[n + 1];
        Arrays.fill(hEdge, -1);
        
        this.parent = new int[n + 1];
        for (int i = 2; i <= n; i++) {
            this.parent[i] = p[i];
            tEdge[i] = i;
            nEdge[i] = hEdge[p[i]];
            hEdge[p[i]] = i;
        }

        // Inisialisasi array struktur pohon
        this.heavy = new int[n + 1];
        this.head = new int[n + 1];
        this.pos = new int[n + 1];
        this.tail = new int[n + 1];
        this.size = new int[n + 1]; // FIX: Sekarang size sudah dikenali
        this.nodeAtPos = new int[n + 1];
        this.lightF = new MaxHeap[n + 1];
        this.lightG = new MaxHeap[n + 1];
        
        for (int i = 1; i <= n; i++) {
            lightF[i] = new MaxHeap();
            lightG[i] = new MaxHeap();
        }

        // 1. Bangun struktur pohon secara iteratif (Mencegah StackOverflow)
        buildStructureIteratively();
        
        // 2. Alokasi memori Segment Tree
        tSum = new long[4 * n + 1];
        tPref = new long[4 * n + 1];
        tSuff = new long[4 * n + 1];
        tAns = new long[4 * n + 1];

        // 3. Inisialisasi DP Light Children dari bawah ke atas
        for (int i = n; i >= 1; i--) {
            int u = visitOrder[i];
            if (u != 1) {
                int pr = parent[u];
                if (u != heavy[pr]) {
                    lightF[pr].add(getDP1(u));
                    lightG[pr].add(getDP2(u));
                }
            }
        }
        
        buildST(1, 1, n);
    }

    private void buildStructureIteratively() {
        visitOrder = new int[n + 1];
        int[] stack = new int[n + 1];
        int top = 0, vIdx = 0;
        stack[++top] = 1;
        
        // DFS Pertama untuk visit order
        while (top > 0) {
            int u = stack[top--];
            visitOrder[++vIdx] = u;
            for (int e = hEdge[u]; e != -1; e = nEdge[e]) {
                stack[++top] = tEdge[e];
            }
        }
        
        // Hitung size dan heavy child
        for (int i = n; i >= 1; i--) {
            int u = visitOrder[i];
            size[u] = 1;
            heavy[u] = -1;
            int maxChildSize = -1;
            for (int e = hEdge[u]; e != -1; e = nEdge[e]) {
                int v = tEdge[e];
                size[u] += size[v];
                if (size[v] > maxChildSize) {
                    maxChildSize = size[v];
                    heavy[u] = v;
                }
            }
        }
        
        // Decompose HLD (Iteratif)
        int hTop = 0;
        int[] hStack = new int[n + 1];
        int[] headStack = new int[n + 1];
        hStack[++hTop] = 1; 
        headStack[hTop] = 1;
        
        while (hTop > 0) {
            int u = hStack[hTop];
            int h = headStack[hTop--];
            head[u] = h;
            pos[u] = ++curPos;
            nodeAtPos[curPos] = u;
            tail[h] = u; // Menyimpan ujung dari heavy chain
            
            for (int e = hEdge[u]; e != -1; e = nEdge[e]) {
                int v = tEdge[e];
                if (v != heavy[u]) {
                    hStack[++hTop] = v;
                    headStack[hTop] = v;
                }
            }
            if (heavy[u] != -1) {
                hStack[++hTop] = heavy[u];
                headStack[hTop] = h;
            }
        }
    }

    private long getDP1(int u) {
        return queryST(1, 1, n, pos[u], pos[tail[head[u]]]).pref;
    }

    private long getDP2(int u) {
        return queryST(1, 1, n, pos[u], pos[tail[head[u]]]).ans;
    }

    private void buildST(int node, int L, int R) {
        if (L == R) {
            updateLeaf(node, nodeAtPos[L]);
            return;
        }
        int mid = (L + R) >> 1;
        buildST(node << 1, L, mid);
        buildST(node << 1 | 1, mid + 1, R);
        pull(node);
    }

    private void updateLeaf(int node, int u) {
        long fL1 = Math.max(0, lightF[u].getMax());
        long fL2 = Math.max(0, lightF[u].getSecondMax());
        long gL = lightG[u].getMax();

        tSum[node] = weights[u];
        tPref[node] = weights[u] + fL1;
        tSuff[node] = weights[u] + fL1;
        tAns[node] = Math.max(gL, weights[u] + fL1 + fL2);
    }

    private void pull(int node) {
        int l = node << 1, r = node << 1 | 1;
        tSum[node] = tSum[l] + tSum[r];
        tPref[node] = Math.max(tPref[l], tSum[l] + tPref[r]);
        tSuff[node] = Math.max(tSuff[r], tSum[r] + tSuff[l]);
        tAns[node] = Math.max(Math.max(tAns[l], tAns[r]), tSuff[l] + tPref[r]);
    }

    private void updateST(int node, int L, int R, int idx, int u) {
        if (L == R) {
            updateLeaf(node, u);
            return;
        }
        int mid = (L + R) >> 1;
        if (idx <= mid) updateST(node << 1, L, mid, idx, u);
        else updateST(node << 1 | 1, mid + 1, R, idx, u);
        pull(node);
    }

    private NodeResult queryST(int node, int L, int R, int qL, int qR) {
        if (qL <= L && R <= qR) return new NodeResult(tSum[node], tPref[node], tSuff[node], tAns[node]);
        int mid = (L + R) >> 1;
        if (qR <= mid) return queryST(node << 1, L, mid, qL, qR);
        if (qL > mid) return queryST(node << 1 | 1, mid + 1, R, qL, qR);
        NodeResult lRes = queryST(node << 1, L, mid, qL, qR);
        NodeResult rRes = queryST(node << 1 | 1, mid + 1, R, qL, qR);
        return new NodeResult(
            lRes.sum + rRes.sum,
            Math.max(lRes.pref, lRes.sum + rRes.pref),
            Math.max(rRes.suff, rRes.sum + lRes.suff),
            Math.max(Math.max(lRes.ans, rRes.ans), lRes.suff + rRes.pref)
        );
    }

    public void updateFragmentValue(int x, long v) {
        weights[x] = v;
        int curr = x;
        while (curr != 0) {
            int h = head[curr];
            int p = parent[h];
            
            long oldF = getDP1(h);
            long oldG = getDP2(h);

            updateST(1, 1, n, pos[curr], curr);

            if (p != 0) {
                long newF = getDP1(h);
                long newG = getDP2(h);
                lightF[p].remove(oldF);
                lightG[p].remove(oldG);
                lightF[p].add(newF);
                lightG[p].add(newG);
                curr = p;
            } else break;
        }
    }

    public long calculateMaxHarmony() {
        return getDP2(1);
    }

    // Helper classes
    private static class NodeResult {
        long sum, pref, suff, ans;
        NodeResult(long s, long p, long su, long a) { sum = s; pref = p; suff = su; ans = a; }
    }

    private static class MaxHeap {
        PriorityQueue<Long> in = new PriorityQueue<>(Collections.reverseOrder());
        PriorityQueue<Long> out = new PriorityQueue<>(Collections.reverseOrder());
        void add(long v) { in.add(v); }
        void remove(long v) { out.add(v); }
        private void normalize() {
            while (!out.isEmpty() && in.peek().equals(out.peek())) {
                in.poll(); out.poll();
            }
        }
        long getMax() {
            normalize();
            return in.isEmpty() ? -INF : in.peek();
        }
        long getSecondMax() {
            long m1 = getMax();
            if (m1 == -INF) return -INF;
            in.poll();
            long m2 = getMax();
            in.add(m1);
            return m2;
        }
    }
}