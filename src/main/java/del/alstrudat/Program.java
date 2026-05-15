package del.alstrudat;

import java.util.*;

/**
 * Maximum Path Sum on Tree with point updates.
 *
 * Key insight:
 *   bestDown[v] = w[v] + max(0, max over children c of bestDown[c])
 *   ansContrib[v] = w[v] + top1_child + top2_child  (best path with v as highest node)
 *   global answer = max over all v of ansContrib[v]
 *
 * On update w[x] = newVal:
 *   1. Update w[x], recompute bestDown[x] and ansContrib[x].
 *   2. Propagate to parent(x), recompute bestDown[parent] and ansContrib[parent].
 *   3. Continue until bestDown stops changing or we reach root.
 *
 * We maintain a segment tree over HLD positions to get global max of ansContrib in O(log N).
 */
public class Program {

    private int n;
    private long[] w;
    private int[] parent;
    private int[][] children;

    // bestDown[v] = w[v] + max(0, max child bestDown)
    private long[] bestDown;
    // ansContrib[v] = best path sum with v as top = w[v] + top1 + top2 non-negative child bestDowns
    private long[] ansContrib;

    // For each node, track its top-2 children bestDown (non-negative)
    private long[] top1; // best child bestDown (floored at 0)
    private long[] top2; // second best child bestDown (floored at 0)
    private int[] top1child; // which child gives top1

    // Segment tree for max ansContrib
    private long[] seg; // max segment tree
    private int[] pos;  // pos[v] = index in seg tree
    private int[] nodeAtPos; // nodeAtPos[pos[v]] = v

    @SuppressWarnings("unchecked")
    public void initializeLibrary(int n, long[] w, int[] p) {
        this.n = n;
        this.w = w.clone();
        parent = new int[n + 1];
        parent[1] = 0;
        for (int i = 2; i <= n; i++) parent[i] = p[i];

        // Build children lists
        int[] cnt = new int[n + 1];
        for (int i = 2; i <= n; i++) cnt[p[i]]++;
        children = new int[n + 1][];
        for (int i = 1; i <= n; i++) children[i] = new int[cnt[i]];
        int[] idx = new int[n + 1];
        for (int i = 2; i <= n; i++) {
            int par = p[i];
            children[par][idx[par]++] = i;
        }

        bestDown = new long[n + 1];
        ansContrib = new long[n + 1];
        top1 = new long[n + 1];
        top2 = new long[n + 1];
        top1child = new int[n + 1];
        Arrays.fill(top1child, -1);

        pos = new int[n + 1];
        nodeAtPos = new int[n + 1];

        // BFS order for pos assignment
        int[] bfs = new int[n + 1];
        int head = 0, tail = 0;
        bfs[tail++] = 1;
        while (head < tail) {
            int v = bfs[head++];
            pos[v] = head - 1; // 0-based
            nodeAtPos[head - 1] = v;
            for (int c : children[v]) bfs[tail++] = c;
        }

        // postOrder = reverse of BFS = process leaves before parents
        int[] postOrder = new int[n];
        for (int i = 0; i < n; i++) postOrder[i] = bfs[n - 1 - i];

        // Compute bestDown and ansContrib bottom-up
        for (int i = 0; i < n; i++) {
            int v = postOrder[i];
            recompute(v);
        }

        // Build segment tree
        seg = new long[4 * n + 4];
        buildSeg(1, 0, n - 1);
    }

    /**
     * Recompute bestDown[v] and ansContrib[v] from children.
     */
    private void recompute(int v) {
        long b1 = 0, b2 = 0;
        int c1 = -1;
        for (int c : children[v]) {
            long d = bestDown[c];
            if (d > b1) {
                b2 = b1;
                b1 = d;
                c1 = c;
            } else if (d > b2) {
                b2 = d;
            }
        }
        top1[v] = b1;
        top2[v] = b2;
        top1child[v] = c1;
        bestDown[v] = w[v] + b1;
        ansContrib[v] = w[v] + b1 + b2;
    }

    // Segment tree: point update, global max query
    private void buildSeg(int node, int l, int r) {
        if (l == r) {
            seg[node] = ansContrib[nodeAtPos[l]];
            return;
        }
        int mid = (l + r) / 2;
        buildSeg(2 * node, l, mid);
        buildSeg(2 * node + 1, mid + 1, r);
        seg[node] = Math.max(seg[2 * node], seg[2 * node + 1]);
    }

    private void updateSeg(int node, int l, int r, int i, long val) {
        if (l == r) {
            seg[node] = val;
            return;
        }
        int mid = (l + r) / 2;
        if (i <= mid) updateSeg(2 * node, l, mid, i, val);
        else updateSeg(2 * node + 1, mid + 1, r, i, val);
        seg[node] = Math.max(seg[2 * node], seg[2 * node + 1]);
    }

    public void updateFragmentValue(int x, long v) {
        w[x] = v;
        // Recompute x and propagate up
        int cur = x;
        while (cur != 0) {
            long oldBestDown = bestDown[cur];
            long oldAnsContrib = ansContrib[cur];
            recompute(cur);
            // Update seg tree at pos[cur]
            if (ansContrib[cur] != oldAnsContrib) {
                updateSeg(1, 0, n - 1, pos[cur], ansContrib[cur]);
            }
            // If bestDown didn't change, no need to propagate further
            if (bestDown[cur] == oldBestDown) break;
            cur = parent[cur];
        }
    }

    public long calculateMaxHarmony() {
        return seg[1];
    }
}
