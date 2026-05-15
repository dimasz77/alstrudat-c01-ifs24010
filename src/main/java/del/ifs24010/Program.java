package del.ifs24010;

import java.util.*;

/**
 * Implementasi Hybrid: Universal Segment Tree (untuk graf garis) 
 * dan Lazy Max-Heap Dynamic DP (untuk graf bercabang kompleks).
 */
public class Program {
    
    // Variabel SegTree
    private boolean isLine;
    private SegNode[] tree;
    private int[] posInSegTree;
    private long[] wSegTree;
    
    // Variabel Tree DP
    private int n;
    private long[] w;
    private int[] p;
    private long[] dp1;
    private long[] dp2;
    private MaxHeap[] heapDp1;
    private MaxHeap[] heapDp2;
    
    class SegNode {
        long sum, pref, suff, ans;
    }

    // Struktur Data Cepat pengganti TreeMap
    class MaxHeap {
        PriorityQueue<Long> in = new PriorityQueue<>(Collections.reverseOrder());
        PriorityQueue<Long> out = new PriorityQueue<>(Collections.reverseOrder());
        
        public void add(long v) { in.add(v); }
        public void remove(long v) { out.add(v); }
        
        public long getMax() {
            while(!out.isEmpty() && in.peek().equals(out.peek())) {
                in.poll(); out.poll();
            }
            return in.isEmpty() ? Long.MIN_VALUE : in.peek();
        }
        
        public boolean isEmpty() {
            getMax();
            return in.isEmpty();
        }
    }

    public void initializeLibrary(int n, long[] w, int[] p) {
        this.n = n;
        this.w = new long[n + 1];
        System.arraycopy(w, 1, this.w, 1, n);
        
        this.p = new int[n + 1];
        if (n > 1) System.arraycopy(p, 2, this.p, 2, n - 1);
        
        // Universal Path Detection: Cek max degree
        int[] degree = new int[n + 1];
        for (int i = 2; i <= n; i++) {
            degree[this.p[i]]++;
            degree[i]++;
        }
        
        isLine = true;
        for (int i = 1; i <= n; i++) {
            if (degree[i] > 2) {
                isLine = false; break;
            }
        }
        
        if (isLine) {
            flattenToSegmentTree(degree);
        } else {
            buildTreeGraph(); 
        }
    }
    
    // ==========================================================
    // BAGIAN 1: UNIVERSAL FLATTENING & SEGMENT TREE
    // ==========================================================
    private void flattenToSegmentTree(int[] degree) {
        int[] head = new int[n + 1];
        int[] next = new int[2 * n + 2];
        int[] to = new int[2 * n + 2];
        int edgeCnt = 0;
        Arrays.fill(head, -1);
        
        for (int i = 2; i <= n; i++) {
            int u = this.p[i], v = i;
            to[edgeCnt] = v; next[edgeCnt] = head[u]; head[u] = edgeCnt++;
            to[edgeCnt] = u; next[edgeCnt] = head[v]; head[v] = edgeCnt++;
        }
        
        int start = 1;
        for (int i = 1; i <= n; i++) {
            if (degree[i] <= 1) { start = i; break; } // Handle N=1 atau Endpoint
        }
        
        posInSegTree = new int[n + 1];
        wSegTree = new long[n + 1];
        int curr = start, prev = 0;
        
        for (int i = 1; i <= n; i++) {
            posInSegTree[curr] = i;
            wSegTree[i] = w[curr];
            for (int e = head[curr]; e != -1; e = next[e]) {
                if (to[e] != prev) {
                    prev = curr;
                    curr = to[e];
                    break;
                }
            }
        }
        
        tree = new SegNode[4 * n + 1];
        buildSegTree(1, 1, n);
    }

    private void buildSegTree(int node, int start, int end) {
        if (start == end) {
            tree[node] = new SegNode();
            tree[node].sum = wSegTree[start];
            tree[node].pref = wSegTree[start];
            tree[node].suff = wSegTree[start];
            tree[node].ans = wSegTree[start];
            return;
        }
        int mid = (start + end) / 2;
        buildSegTree(2 * node, start, mid);
        buildSegTree(2 * node + 1, mid + 1, end);
        tree[node] = new SegNode();
        mergeSegTree(node);
    }
    
    private void mergeSegTree(int node) {
        SegNode left = tree[2 * node];
        SegNode right = tree[2 * node + 1];
        tree[node].sum = left.sum + right.sum;
        tree[node].pref = Math.max(left.pref, left.sum + right.pref);
        tree[node].suff = Math.max(right.suff, right.sum + left.suff);
        tree[node].ans = Math.max(Math.max(left.ans, right.ans), left.suff + right.pref);
    }
    
    private void updateSegTree(int node, int start, int end, int idx, long val) {
        if (start == end) {
            wSegTree[idx] = val;
            tree[node].sum = val;
            tree[node].pref = val;
            tree[node].suff = val;
            tree[node].ans = val;
            return;
        }
        int mid = (start + end) / 2;
        if (start <= idx && idx <= mid) {
            updateSegTree(2 * node, start, mid, idx, val);
        } else {
            updateSegTree(2 * node + 1, mid + 1, end, idx, val);
        }
        mergeSegTree(node);
    }

    // ==========================================================
    // BAGIAN 2: LAZY MAX-HEAP DYNAMIC TREE DP
    // ==========================================================
    private void buildTreeGraph() {
        int[] outDegree = new int[n + 1];
        for (int i = 2; i <= n; i++) outDegree[this.p[i]]++;
        
        heapDp1 = new MaxHeap[n + 1];
        heapDp2 = new MaxHeap[n + 1];
        for (int i = 1; i <= n; i++) {
            if (outDegree[i] > 0) {
                heapDp1[i] = new MaxHeap();
                heapDp2[i] = new MaxHeap();
            }
        }
        
        int[] order = new int[n + 1];
        int headIdx = 0, tailIdx = 0;
        order[tailIdx++] = 1;
        
        int[] head = new int[n + 1];
        int[] next = new int[n + 1];
        int[] to = new int[n + 1];
        Arrays.fill(head, -1);
        for (int i = 2; i <= n; i++) {
            to[i] = i; next[i] = head[this.p[i]]; head[this.p[i]] = i;
        }
        
        while (headIdx < tailIdx) {
            int u = order[headIdx++];
            for (int e = head[u]; e != -1; e = next[e]) order[tailIdx++] = to[e];
        }
        
        dp1 = new long[n + 1];
        dp2 = new long[n + 1];
        
        for (int i = n - 1; i >= 0; i--) {
            int u = order[i];
            recomputeDP(u);
            if (this.p[u] != 0) {
                heapDp1[this.p[u]].add(Math.max(0, dp1[u]));
                heapDp2[this.p[u]].add(dp2[u]);
            }
        }
    }
    
    private void recomputeDP(int u) {
        long max1 = 0, max2 = 0;
        if (heapDp1[u] != null && !heapDp1[u].isEmpty()) {
            max1 = heapDp1[u].getMax();
            heapDp1[u].remove(max1);
            max2 = heapDp1[u].getMax();
            if (max2 == Long.MIN_VALUE) max2 = 0;
            heapDp1[u].add(max1);
        }
        
        long maxChildAns = Long.MIN_VALUE;
        if (heapDp2[u] != null && !heapDp2[u].isEmpty()) {
            maxChildAns = heapDp2[u].getMax();
        }
        
        dp1[u] = w[u] + max1;
        dp2[u] = w[u] + max1 + max2;
        if (maxChildAns != Long.MIN_VALUE) {
            dp2[u] = Math.max(dp2[u], maxChildAns);
        }
    }

    public void updateFragmentValue(int x, long v) {
        if (isLine) {
            updateSegTree(1, 1, n, posInSegTree[x], v);
            return;
        }
        
        w[x] = v;
        int curr = x;
        while (curr != 0) {
            long oldDp1 = dp1[curr];
            long oldDp2 = dp2[curr];
            
            recomputeDP(curr);
            
            if (dp1[curr] == oldDp1 && dp2[curr] == oldDp2) break;
            
            int parent = p[curr];
            if (parent != 0) {
                heapDp1[parent].remove(Math.max(0, oldDp1));
                heapDp2[parent].remove(oldDp2);
                
                heapDp1[parent].add(Math.max(0, dp1[curr]));
                heapDp2[parent].add(dp2[curr]);
            }
            curr = parent;
        }
    }

    public long calculateMaxHarmony() {
        if (isLine) return tree[1].ans;
        return dp2[1];
    }
}