package de.vatterger.engine.util;

import java.util.function.IntBinaryOperator;

public class IntArraySort {
    public static void sort(int[] x, IntBinaryOperator op) {
        sort(x, 0, x.length, op);
    }

    public static void sort(int[] x, int off, int len, IntBinaryOperator op) {
        if (len < 7) {
            for (int i=off; i<len+off; i++)
                // Use custom comparator for insertion sort fallback
                for (int j=i; j>off && (op.applyAsInt(x[j-1], x[j]) > 0); j--)
                    swap(x, j, j-1);
            return;
        }

        int m = off + (len >> 1);
        if (len > 7) {
            int l = off;
            int n = off + len - 1;
            if (len > 40) {
                int s = len/8;
                l = med3(x, l,     l+s, l+2*s);
                m = med3(x, m-s,   m,   m+s);
                n = med3(x, n-2*s, n-s, n);
            }
            m = med3(x, l, m, n);
        }
        int v = x[m];

        int a = off, b = a, c = off + len - 1, d = c;
        while(true) {
            // Use custom comparator for checking elements
            while (b <= c && (op.applyAsInt(x[b], v) <= 0)) {
                if (x[b] == v)
                    swap(x, a++, b);
                b++;
            }
            // Use custom comparator for checking elements
            while (c >= b && (op.applyAsInt(x[c], v) >= 0)) {
                if (x[c] == v)
                    swap(x, c, d--);
                c--;
            }
            if (b > c)
                break;
            swap(x, b++, c--);
        }

        int s, n = off + len;
        s = Math.min(a-off, b-a  );  vecswap(x, off, b-s, s);
        s = Math.min(d-c,   n-d-1);  vecswap(x, b,   n-s, s);

        if ((s = b-a) > 1)
            sort(x, off, s, op);
        if ((s = d-c) > 1)
            sort(x, n-s, s, op);
    }

    private static final void swap(int x[], int a, int b) {
        int t = x[a];
        x[a] = x[b];
        x[b] = t;
    }

    private static final void vecswap(int x[], int a, int b, int n) {
        for (int i=0; i<n; i++, a++, b++)
            swap(x, a, b);
    }

    private static final int med3(int x[], int a, int b, int c) {
        return (x[a] < x[b] ?
                (x[b] < x[c] ? b : x[a] < x[c] ? c : a) :
                (x[b] > x[c] ? b : x[a] > x[c] ? c : a));
    }
}