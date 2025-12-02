/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package finalproject;

import java.util.Random;

/* Lightweight 2D value-noise (Perlin-like) */
public class SimplexNoise {
    private final int largestFeature;
    private final float persistence;
    private final int seed;

    // permutation for hashing
    private final int[] p = new int[512];

    public SimplexNoise(int largestFeature, float persistance, int seed){
        this.largestFeature = largestFeature;
        this.persistence = persistance;
        this.seed = seed;

        int[] base = new int[256];
        for (int i = 0; i < 256; i++) base[i] = i;
        Random rng = new Random(seed);
        for (int i = 255; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int t = base[i]; base[i] = base[j]; base[j] = t;
        }
        for (int i = 0; i < 512; i++) p[i] = base[i & 255];
    }

    // fade curve and linear interpolation
    private static float fade(float t){ return t*t*t*(t*(t*6 - 15) + 10); }
    private static float lerp(float a, float b, float t){ return a + t*(b - a); }

    // pseudo-random gradient at integer lattice point
    private float grad(int x, int y){
        int h = p[(x + p[y & 255]) & 255] & 7; // 8 gradients
        switch(h){
            case 0: return  1;
            case 1: return -1;
            case 2: return  y >= 0 ? 1 : -1;
            case 3: return  y >= 0 ? -1 : 1;
            case 4: return  x >= 0 ? 1 : -1;
            case 5: return  x >= 0 ? -1 : 1;
            default:return 0.7071f; // diag-ish
        }
    }

    /* 2D value-noise, inputs can be any ints; returns ~[-1,1] */
    public float getNoise(int x, int y){
        // scale down big coordinates so "largestFeature" acts as base frequency
        float fx = x / (float)largestFeature;
        float fy = y / (float)largestFeature;

        int x0 = (int)Math.floor(fx);
        int y0 = (int)Math.floor(fy);
        int x1 = x0 + 1;
        int y1 = y0 + 1;

        float sx = fade(fx - x0);
        float sy = fade(fy - y0);

        // corner values via hashed gradients
        float n00 = grad(x0, y0);
        float n10 = grad(x1, y0);
        float n01 = grad(x0, y1);
        float n11 = grad(x1, y1);

        float ix0 = lerp(n00, n10, sx);
        float ix1 = lerp(n01, n11, sx);
        return lerp(ix0, ix1, sy); // ~[-1,1]
    }
}
