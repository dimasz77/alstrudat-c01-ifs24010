package del.ifs24010;

import java.util.Arrays;
import java.util.PriorityQueue;

public class Program {
    private static final long NEG_INF = -10000000000000000L;
    private int n;
    private long[] weights;
    private int[] parent;
    private int[] headEdge, nextEdge, toEdge;
    private int edgeCount;
    private int[] depth, size, heavy, head, pos, tail;
    private int currentPos;
    private MultiSet[] lightF, lightG;
    private long[] initF, initG, trFa, trFb, trGa, trGc;
    private long qFa, qFb, qGa, qGc;

    private static class MultiSet {
        PriorityQueue<Long> maxHeap = new PriorityQueue<>();
        PriorityQueue<Long> removeHeap = new PriorityQueue<>();

        void add(long val) { maxHeap.add(-val); }
        void remove(long val) { removeHeap.add(-val); }

        void clean() {
            while (!maxHeap.isEmpty() && !removeHeap.isEmpty() && maxHeap.peek().equals(removeHeap.peek())) {
                maxHeap.poll(); removeHeap.poll();
            }
        }

        long max() {
            clean();
            return maxHeap.isEmpty() ? NEG_INF : -maxHeap.peek();
        }

        long[] top2WithZero() {
            clean();
            long[] res = {0, 0};
            if (maxHeap.isEmpty()) return res;
            
            long v1 = -maxHeap.poll();
            if (v1 > 0) res[0] = v1;
            
            clean();
            if (!maxHeap.isEmpty()) {
                long v2 = -maxHeap.peek();
                if (v2 > 0) res[1] = v2;
            }
            maxHeap.add(-v1);
            return res;
        }
    }

    public void initializeLibrary(int n, long[] w, int[] p) {
        this.n = n; this.weights = w; this.parent = p;
        headEdge = new int[n + 1]; Arrays.fill(headEdge, -1);
        nextEdge = new int[n + 1]; toEdge = new int[n + 1];
        edgeCount = 0;
        
        for (int i = 2; i <= n; i++) {
            toEdge[edgeCount] = i;
            nextEdge[edgeCount] = headEdge[p[i]];
            headEdge[p[i]] = edgeCount++;
        }
        
        depth = new int[n + 1]; size = new int[n + 1]; heavy = new int[n + 1];
        head = new int[n + 1]; pos = new int[n + 1]; tail = new int[n + 1];
        int[] bfs = new int[n + 1];
        int headBfs = 0, tailBfs = 0;
        bfs[tailBfs++] = 1;
        
        while (headBfs < tailBfs) {
            int u = bfs[headBfs++];
            for (int e = headEdge[u]; e != -1; e = nextEdge[e]) {
                int v = toEdge[e];
                depth[v] = depth[u] + 1;
                bfs[tailBfs++] = v;
            }
        }
        
        Arrays.fill(size, 1);
        for (int i = n - 1; i >= 0; i--) {
            int u = bfs[i];
            int maxSub = 0;
            for (int e = headEdge[u]; e != -1; e = nextEdge[e]) {
                int v = toEdge[e];
                size[u] += size[v];
                if (size[v] > maxSub) { maxSub = size[v]; heavy[u] = v; }
            }
        }
        
        int[] hldHead = new int[n + 1]; Arrays.fill(hldHead, -1);
        int[] hldNext = new int[n + 1], hldTo = new int[n + 1];
        int hldEdgeCount = 0;
        
        for (int i = 1; i <= n; i++) {
            for (int e = headEdge[i]; e != -1; e = nextEdge[e]) {
                int v = toEdge[e];
                if (v != heavy[i]) {
                    hldTo[hldEdgeCount] = v; hldNext[hldEdgeCount] = hldHead[i]; hldHead[i] = hldEdgeCount++;
                }
            }
            if (heavy[i] != 0) {
                hldTo[hldEdgeCount] = heavy[i]; hldNext[hldEdgeCount] = hldHead[i]; hldHead[i] = hldEdgeCount++;
            }
        }
        
        int[] stack = new int[n + 1], edgePtr = new int[n + 1];
        System.arraycopy(hldHead, 0, edgePtr, 0, n + 1);
        int top = 0;
        stack[++top] = 1; head[1] = 1;
        
        while (top > 0) {
            int u = stack[top];
            if (pos[u] == 0) {
                pos[u] = ++currentPos;
                tail[head[u]] = u;
            }
            int e = edgePtr[u];
            if (e != -1) {
                edgePtr[u] = hldNext[e];
                int v = hldTo[e];
                head[v] = (v == heavy[u]) ? head[u] : v;
                stack[++top] = v;
            } else {
                top--;
            }
        }
        
        trFa = new long[4 * n + 1]; trFb = new long[4 * n + 1];
        trGa = new long[4 * n + 1]; trGc = new long[4 * n + 1];
        build(1, 1, n);
        
        initF = new long[n + 1]; initG = new long[n + 1];
        lightF = new MultiSet[n + 1]; lightG = new MultiSet[n + 1];
        for (int i = 1; i <= n; i++) {
            lightF[i] = new MultiSet(); lightG[i] = new MultiSet();
        }
        
        for (int i = n - 1; i >= 0; i--) {
            int u = bfs[i];
            for (int e = headEdge[u]; e != -1; e = nextEdge[e]) {
                int v = toEdge[e];
                if (v != heavy[u]) {
                    lightF[u].add(initF[v]); lightG[u].add(initG[v]);
                }
            }
            updateBaseNode(u);
            if (u == head[u]) {
                qFa = NEG_INF; qFb = 0; qGa = NEG_INF; qGc = NEG_INF;
                query(1, 1, n, pos[u], pos[tail[u]]);
                initF[u] = qFa; initG[u] = qGa;
            }
        }
    }

    private void build(int node, int start, int end) {
        trFa[node] = NEG_INF; trFb[node] = 0; trGa[node] = NEG_INF; trGc[node] = NEG_INF;
        if (start == end) return;
        int mid = (start + end) / 2;
        build(2 * node, start, mid); build(2 * node + 1, mid + 1, end);
    }

    private void merge(int node, int left, int right) {
        trFa[node] = Math.max(trFa[left], trFb[left] + trFa[right]);
        trFb[node] = trFb[left] + trFb[right];
        trGa[node] = Math.max(trGa[left], Math.max(trGa[right], trGc[left] + trFa[right]));
        trGc[node] = Math.max(trGc[right], trGc[left] + trFb[right]);
    }

    private void updateSegTree(int node, int start, int end, int idx, long fa, long fb, long ga, long gc) {
        if (start == end) {
            trFa[node] = fa; trFb[node] = fb; trGa[node] = ga; trGc[node] = gc;
            return;
        }
        int mid = (start + end) / 2;
        if (idx <= mid) updateSegTree(2 * node, start, mid, idx, fa, fb, ga, gc);
        else updateSegTree(2 * node + 1, mid + 1, end, idx, fa, fb, ga, gc);
        merge(node, 2 * node, 2 * node + 1);
    }

    private void updateBaseNode(int u) {
        long w = weights[u];
        long[] top2 = lightF[u].top2WithZero();
        long l1 = top2[0], l2 = top2[1], mlg = lightG[u].max();
        long fa = w + l1, fb = w, ga = Math.max(mlg, w + l1 + l2), gc = w + l1;
        updateSegTree(1, 1, n, pos[u], fa, fb, ga, gc);
    }

    private void query(int node, int start, int end, int l, int r) {
        if (l <= start && end <= r) {
            long nfa = Math.max(qFa, qFb + trFa[node]);
            long nfb = qFb + trFb[node];
            long nga = Math.max(qGa, Math.max(trGa[node], qGc + trFa[node]));
            long ngc = Math.max(trGc[node], qGc + trFb[node]);
            qFa = nfa; qFb = nfb; qGa = nga; qGc = ngc;
            return;
        }
        int mid = (start + end) / 2;
        if (l <= mid) query(2 * node, start, mid, l, r);
        if (r > mid) query(2 * node + 1, mid + 1, end, l, r);
    }

    public void updateFragmentValue(int x, long v) {
        weights[x] = v;
        int u = x;
        while (u > 0) {
            int h = head[u], t = tail[h], p = parent[h];
            long oldF = 0, oldG = 0;
            if (p > 0) {
                qFa = NEG_INF; qFb = 0; qGa = NEG_INF; qGc = NEG_INF;
                query(1, 1, n, pos[h], pos[t]);
                oldF = qFa; oldG = qGa;
            }
            
            updateBaseNode(u);
            
            if (p > 0) {
                qFa = NEG_INF; qFb = 0; qGa = NEG_INF; qGc = NEG_INF;
                query(1, 1, n, pos[h], pos[t]);
                long newF = qFa, newG = qGa;
                
                // OPTIMASI FINAL: EARLY BREAK
                // Kalau nilainya tidak berubah, kita BERHENTI mengecek ke atas.
                if (oldF == newF && oldG == newG) {
                    break;
                }
                
                lightF[p].remove(oldF); lightF[p].add(newF);
                lightG[p].remove(oldG); lightG[p].add(newG);
            }
            u = p;
        }
    }

    public long calculateMaxHarmony() {
        qFa = NEG_INF; qFb = 0; qGa = NEG_INF; qGc = NEG_INF;
        query(1, 1, n, pos[1], pos[tail[1]]);
        return qGa;
    }
}