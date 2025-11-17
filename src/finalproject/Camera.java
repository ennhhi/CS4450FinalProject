/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package finalproject;

import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.BufferUtils;
import java.nio.FloatBuffer;

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
        float xOffset = dist * (float)Math.sin(Math.toRadians(yaw));
        float zOffset = dist * (float)Math.cos(Math.toRadians(yaw));
        
        x -= xOffset;
        z += zOffset;
        
        FloatBuffer lightPosition = BufferUtils.createFloatBuffer(4);
        lightPosition.put(x-=xOffset).put(y).put(z+=zOffset).put(1.0f).flip();
        glLight(GL_LIGHT0, GL_POSITION, lightPosition);
    }

    public void moveBackward(float dist) { 
        float xOffset = dist * (float)Math.sin(Math.toRadians(yaw));
        float zOffset = dist * (float)Math.cos(Math.toRadians(yaw));
        
        FloatBuffer lightPosition = BufferUtils.createFloatBuffer(4);
        lightPosition.put(x-=xOffset).put(y).put(z+=zOffset).put(1.0f).flip();
        glLight(GL_LIGHT0, GL_POSITION, lightPosition);
        
        moveForward(-dist); 
    }

    public void strafeLeft(float dist) {
        x -= dist * (float)Math.sin(Math.toRadians(yaw + 90f));
        z += dist * (float)Math.cos(Math.toRadians(yaw + 90f));
    }

    public void strafeRight(float dist) { strafeLeft(-dist); }

    public void moveUp(float dist)   { 
        float xOffset = dist * (float)Math.sin(Math.toRadians(yaw));
        float zOffset = dist * (float)Math.cos(Math.toRadians(yaw));
        
        y += dist; 
        
        FloatBuffer lightPosition = BufferUtils.createFloatBuffer(4);
        lightPosition.put(x-=xOffset).put(y).put(z+=zOffset).put(1.0f).flip();
        glLight(GL_LIGHT0, GL_POSITION, lightPosition);
    }
    public void moveDown(float dist) { 
        float xOffset = dist * (float)Math.sin(Math.toRadians(yaw));
        float zOffset = dist * (float)Math.cos(Math.toRadians(yaw));
        
        y -= dist; 
        
        FloatBuffer lightPosition = BufferUtils.createFloatBuffer(4);
        lightPosition.put(x-=xOffset).put(y).put(z+=zOffset).put(1.0f).flip();
        glLight(GL_LIGHT0, GL_POSITION, lightPosition);
    }

    // call each frame before drawing world
    public void lookThrough() {
        GL11.glRotatef(pitch, 1f, 0f, 0f);
        GL11.glRotatef(yaw,   0f, 1f, 0f);
        GL11.glTranslatef(-x, -y, -z);
        FloatBuffer lightPosition = BufferUtils.createFloatBuffer(4);
        lightPosition.put(x).put(y).put(z).put(1.0f).flip();
        glLight(GL_LIGHT0, GL_POSITION, lightPosition);
    }
}
