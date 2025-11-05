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
        x += dist * (float)Math.sin(Math.toRadians(yaw));
        z -= dist * (float)Math.cos(Math.toRadians(yaw));
    }

    public void moveBackward(float dist) { moveForward(-dist); }

    public void strafeLeft(float dist) {
        x -= dist * (float)Math.sin(Math.toRadians(yaw + 90f));
        z += dist * (float)Math.cos(Math.toRadians(yaw + 90f));
    }

    public void strafeRight(float dist) { strafeLeft(-dist); }

    public void moveUp(float dist)   { y += dist; }
    public void moveDown(float dist) { y -= dist; }

    // call each frame before drawing world
    public void lookThrough() {
        GL11.glRotatef(pitch, 1f, 0f, 0f);
        GL11.glRotatef(yaw,   0f, 1f, 0f);
        GL11.glTranslatef(-x, -y, -z);
    }
}
