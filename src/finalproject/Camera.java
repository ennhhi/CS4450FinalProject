/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package finalproject;

import org.lwjgl.opengl.GL11;

/**
 * simple FPS-style camera
 */
public class Camera {
    private float x, y, z;
    private float yaw;   // left-right
    private float pitch; // up-down
    public Chunk chunk;

    public Camera(float x, float y, float z) {
        this.x = x; this.y = y; this.z = z;
        this.yaw = 0f; this.pitch = 0f;
        chunk = new Chunk((int)this.x, (int)this.y, (int)this.z);
    }

    public void yaw(float amount)   { yaw += amount; }
    public void pitch(float amount) {
        pitch += amount;
        if (pitch > 90f)  pitch = 90f;
        if (pitch < -90f) pitch = -90f;
    }

    public void moveForward(float dist) {
        float yawRad = (float)Math.toRadians(yaw);
        float xOffset = dist * (float)Math.sin(yawRad);
        float zOffset = dist * (float)Math.cos(yawRad);

        x += xOffset;
        z -= zOffset;
    }

    public void moveBackward(float dist) {
        float yawRad = (float)Math.toRadians(yaw);
        float xOffset = dist * (float)Math.sin(yawRad);
        float zOffset = dist * (float)Math.cos(yawRad);

        x -= xOffset;
        z += zOffset;
    }

    public void strafeLeft(float dist) {
        x -= dist * (float)Math.sin(Math.toRadians(yaw + 90f));
        z += dist * (float)Math.cos(Math.toRadians(yaw + 90f));
    }

    public void strafeRight(float dist) { strafeLeft(-dist); }

    public void moveUp(float dist)   {         
        y += dist; 
    }
    public void moveDown(float dist) { 
        y -= dist;
    }

    // keep camera position inside given bounds
    public void clampToBounds(float minX, float maxX,
                              float minY, float maxY,
                              float minZ, float maxZ) {
        if (x < minX) x = minX;
        if (x > maxX) x = maxX;

        if (y < minY) y = minY;
        if (y > maxY) y = maxY;

        if (z < minZ) z = minZ;
        if (z > maxZ) z = maxZ;
    }

    // call each frame before drawing world
    public void lookThrough() {
        GL11.glRotatef(pitch, 1f, 0f, 0f);
        GL11.glRotatef(yaw,   0f, 1f, 0f);
        GL11.glTranslatef(-x, -y, -z);
    }
    
    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public void setY(float y) {
        this.y = y;
    }
}
