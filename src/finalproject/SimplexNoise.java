/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package finalproject;

public class SimplexNoise {
    int largestFeature;
    float persistance;
    int seed;
    
    public SimplexNoise(int largestFeature, float persistance, int seed){
        this.largestFeature = largestFeature;
        this.persistance = persistance;
        this.seed = seed;
    }
    
    public float getNoise(int noiseWidth, int noiseH){
        return 0.1f;
    }
}
