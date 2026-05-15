package del.ifs24010;

import java.util.*;

public class Program {
    private static final long INF = 1_000_000_000_000_000_0L; 
    private int n;
    private long[] weights;
    private int[] parent, heavy, head, pos, tail, size, nodeAtPos;
    private int curPos;
    
    // Array-based Adjacency List (Sangat cepat & tanpa warning)
    private int[] headEdge, nextEdge, toEdge;

    private long[] tSum, tPref, tSuff, tAns;
    private MaxHeap[] lightF, lightG;

    public void initializeLibrary(int n, long[] w, int[] p) {
        this.n = n;
        this.weights = w;
        this.headEdge = new int[n + 1];
        this.nextEdge = new int[n + 1];
        this.toEdge = new int[n + 1];
        Arrays.fill(headEdge, -1);
        
        this.parent = new int[n + 1];
        for (int i = 2; i <= n; i++) {
            this.parent[i] = p[i];
            toEdge[i] = i;
            nextEdge[i] = headEdge[p[i]];
            headEdge[p[i]] = i;
        }

        heavy = new int[n + 1]; head = new int[n + 1];
        pos = new int[n + 1]; tail = new int[n + 1];
        size = new int[n + 1]; nodeAtPos = new int[n + 1];
        lightF = new MaxHeap[n + 1]; lightG = new MaxHeap[n + 1];
        for (int i = 1; i <= n; i++) {
            lightF[i] = new MaxHeap();
            lightG[i] = new MaxHeap();
        }

        // 1. Iterative DFS untuk menghitung size & heavy child (Aman dari StackOverflow)
        int[] visitOrder = new int[n + 1];
        int[] stack = new int[n + 1];
        int top = 0, vIdx = 0;
        stack[++top] = 1;
        while (top > 0) {
            int u = stack[top--];
            visitOrder[++vIdx] = u;
            for (int e = headEdge[u]; e != -1; e = nextEdge[e]) stack[++top] = toEdge[e];
        }
        for (int i = n; i >= 1; i--) {
            int u = visitOrder[i];
            size[u] = 1; heavy[u] = -1;
            int maxS = -1;
            for (int e = headEdge[u]; e != -1; e = nextEdge[e]) {
                int v = toEdge[e];
                size[u] += size[v];
                if (size[v] > maxS) { maxS = size[v]; heavy[u] = v; }
            }
        }

        // 2. DFS HLD (Hanya rekursif ke light children, kedalaman aman log N)
        curPos = 0;
        decompose(1, 1);

        tSum = new long[4 * n + 1]; tPref = new long[4 * n + 1];
        tSuff = new long[4 * n + 1]; tAns = new long[4 * n + 1];
        build(1, 1, n);
    }

    private void decompose(int u, int h) {
        head[u] = h; pos[u] = ++curPos; nodeAtPos[curPos] = u; tail[h] = u;
        if (heavy[u] != -1) decompose(heavy[u], h);
        for (int e = headEdge[u]; e != -1; e = nextEdge[e]) {
            int v = toEdge[e];
            if (v != heavy[u]) {
                decompose(v, v);
                lightF[u].add(getDP1(v));
                lightG[u].add(getDP2(v));
            }
        }
    }

    private long getDP1(int u) { return query(1, 1, n, pos[u], pos[tail[head[u]]]).pref; }
    private long getDP2(int u) { return query(1, 1, n, pos[u], pos[tail[head[u]]]).ans; }

    private void build(int node, int L, int R) {
        if (L == R) { updateLeaf(node, nodeAtPos[L]); return; }
        int mid = (L + R) >> 1;
        build(node << 1, L, mid); build(node << 1 | 1, mid + 1, R);
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

    public void updateFragmentValue(int x, long v) {
        weights[x] = v;
        while (x != 0) {
            int h = head[x], p = parent[h];
            long oldF = getDP1(h), oldG = getDP2(h);
            updateSeg(1, 1, n, pos[x], x);
            if (p != 0) {
                long newF = getDP1(h), newG = getDP2(h);
                lightF[p].remove(oldF); lightG[p].remove(oldG);
                lightF[p].add(newF); lightG[p].add(newG);
                x = p;
            } else break;
        }
    }

    private void updateSeg(int node, int L, int R, int p, int u) {
        if (L == R) { updateLeaf(node, u); return; }
        int mid = (L + R) >> 1;
        if (p <= mid) updateSeg(node << 1, L, mid, p, u);
        else updateSeg(node << 1 | 1, mid + 1, R, p, u);
        pull(node);
    }

    private Node query(int node, int L, int R, int qL, int qR) {
        if (qL <= L && R <= qR) return new Node(tSum[node], tPref[node], tSuff[node], tAns[node]);
        int mid = (L + R) >> 1;
        if (qR <= mid) return query(node << 1, L, mid, qL, qR);
        if (qL > mid) return query(node << 1 | 1, mid + 1, R, qL, qR);
        Node l = query(node << 1, L, mid, qL, qR), r = query(node << 1 | 1, mid + 1, R, qL, qR);
        return new Node(l.sum + r.sum, Math.max(l.pref, l.sum + r.pref), 
                        Math.max(r.suff, r.sum + l.suff), Math.max(Math.max(l.ans, r.ans), l.suff + r.pref));
    }

    public long calculateMaxHarmony() { return getDP2(1); }

    private static class Node {
        long sum, pref, suff, ans;
        Node(long s, long p, long su, long a) { sum = s; pref = p; suff = su; ans = a; }
    }

    private static class MaxHeap {
        PriorityQueue<Long> in = new PriorityQueue<>(Collections.reverseOrder());
        PriorityQueue<Long> out = new PriorityQueue<>(Collections.reverseOrder());
        void add(long v) { in.add(v); }
        void remove(long v) { out.add(v); }
        void normalize() { while (!out.isEmpty() && in.peek().equals(out.peek())) { in.poll(); out.poll(); } }
        long getMax() { normalize(); return in.isEmpty() ? -INF : in.peek(); }
        long getSecondMax() {
            long m1 = getMax(); if (m1 == -INF) return -INF;
            in.poll(); long m2 = getMax(); in.add(m1); return m2;
        }
    }
}