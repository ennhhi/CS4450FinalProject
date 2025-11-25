/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package finalproject;

/* ignore these 2 lines, used for changing Run settings when pulling from repo
-Djava.library.path=C:\Users\kaila\Desktop\CS4450\lwjgl-2.9.2\lwjgl-2.9.2\native\windows --enable-native-access=ALL-UNNAMED
-Djava.library.path="C:\Users\Husain\Desktop\Ch5\Fall 2025\CS 4450\lwjgl-2.9.2\lwjgl-2.9.2\native\windows" --enable-native-access=ALL-UNNAMED
*/

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.BufferUtils;
import java.nio.FloatBuffer;


public class Main {

    private static final int WIDTH = 640;
    private static final int HEIGHT = 480;

    private static final float MOUSE_SENS = 0.18f;
    private static final float MOVE_SPEED = 0.3f;
    
    private final float WORLD_MIN = 0f;
    private final float WORLD_MAX = Chunk.CHUNK_SIZE * Chunk.CUBE_LENGTH;

    private Camera camera;
    
    private FloatBuffer lightPosition;
    private FloatBuffer whiteLight;
    
    private static final float EYE_HEIGHT = 3.0f;
    private static final float FALL_SPEED_PER_FRAME = 0.2f;
    
    private float timeOfDay = 0.0f;

    public static void main(String[] args) throws Exception {
        new Main().run();
    }

    private void run() throws LWJGLException {
        initDisplay();
        initGL();

        camera = new Camera(0f, 60f, 0f); // start a little back & up
        Mouse.setGrabbed(true);           // lock cursor for FPS feel

        gameLoop();

        Mouse.setGrabbed(false);
        Display.destroy();
    }

    private void initDisplay() throws LWJGLException {
        Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
        Display.setTitle("CS4450 Final Program");

        // center using physical desktop resolution (DPI-safe)
        DisplayMode dm = Display.getDesktopDisplayMode();
        int x = (dm.getWidth()  - WIDTH)  / 2;
        int y = (dm.getHeight() - HEIGHT) / 2;
        Display.setLocation(x, y);

        Display.create();
    }

    private void initGL() {
        GL11.glViewport(0, 0, WIDTH, HEIGHT);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GLU.gluPerspective(70f, (float) WIDTH / (float) HEIGHT, 0.1f, 1000f);

        // ----- LIGHT SETUP (properties only) -----
        initLightArrays();

        FloatBuffer ambient = BufferUtils.createFloatBuffer(4);
        ambient.put(0.8f).put(0.8f).put(0.8f).put(1.0f).flip();

        glLight(GL_LIGHT0, GL_AMBIENT,  ambient);
        glLight(GL_LIGHT0, GL_DIFFUSE,  whiteLight);
        glLight(GL_LIGHT0, GL_SPECULAR, whiteLight);

        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);
        glShadeModel(GL_SMOOTH);
        
        // ---- FILL LIGHT (shadow softening) ----
        glEnable(GL_LIGHT1);

        FloatBuffer fillDiffuse = BufferUtils.createFloatBuffer(4);
        fillDiffuse.put(0.25f).put(0.25f).put(0.25f).put(1.0f).flip();
        glLight(GL_LIGHT1, GL_DIFFUSE, fillDiffuse);

        FloatBuffer fillAmbient = BufferUtils.createFloatBuffer(4);
        fillAmbient.put(0.1f).put(0.1f).put(0.1f).put(1.0f).flip();
        glLight(GL_LIGHT1, GL_AMBIENT, fillAmbient);

        // opposite direction from main sun
        FloatBuffer fillDir = BufferUtils.createFloatBuffer(4);
        fillDir.put(-1.0f).put(-1.0f).put(1.0f).put(0.0f).flip();
        glLight(GL_LIGHT1, GL_POSITION, fillDir);

        // ----- MODELVIEW SETUP -----
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glClearColor(0.12f, 0.14f, 0.18f, 1f);

        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);
        glEnable(GL_DEPTH_TEST);

        glEnable(GL_TEXTURE_2D);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        
        glEnableClientState(GL_NORMAL_ARRAY);
        glEnable(GL_NORMALIZE);
    }

    private void gameLoop() {
        while (!Display.isCloseRequested()) {
            handleInput();
            
            camera.clampToBounds(WORLD_MIN, WORLD_MAX, WORLD_MIN, WORLD_MAX, WORLD_MIN, WORLD_MAX);

            float groundY = getGroundYUnderCamera();
            float targetY = groundY + EYE_HEIGHT;
            float currentY = camera.getY();

            if (currentY > targetY) {
                // We are above the ground -> fall down
                float newY = currentY - FALL_SPEED_PER_FRAME;

                // Don't go below the ground
                if (newY < targetY) {
                    newY = targetY;
                }

                camera.setY(newY);
            } else {
                // We are at or below the ground -> snap to ground
                camera.setY(targetY);
            }
            
            updateDayNightCycle();

            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            GL11.glLoadIdentity();
            camera.lookThrough();

            glLight(GL_LIGHT0, GL_POSITION, lightPosition);
            
            camera.chunk.render();

            Display.update();
            Display.sync(60); // cap at 60fps
        }
    }
    
    private void initLightArrays() {
        lightPosition = BufferUtils.createFloatBuffer(4);
        lightPosition.put(1.0f).put(1.0f).put(-1.0f).put(0.0f).flip();
        whiteLight = BufferUtils.createFloatBuffer(4);
        whiteLight.put(1.0f).put(1.0f).put(1.0f).put(1.0f).flip();
    }

    private void handleInput() {
        // quit on esc
        if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            Display.destroy();
            System.exit(0);
        }

        // mouse look (FPS-style)
        float dx = Mouse.getDX();
        float dy = Mouse.getDY();
        camera.yaw(dx * MOUSE_SENS);
        camera.pitch(-dy * MOUSE_SENS);

        // flat plane movement (WASD or arrows)
        boolean forward = Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_UP);
        boolean back    = Keyboard.isKeyDown(Keyboard.KEY_S) || Keyboard.isKeyDown(Keyboard.KEY_DOWN);
        boolean left    = Keyboard.isKeyDown(Keyboard.KEY_A) || Keyboard.isKeyDown(Keyboard.KEY_LEFT);
        boolean right   = Keyboard.isKeyDown(Keyboard.KEY_D) || Keyboard.isKeyDown(Keyboard.KEY_RIGHT);

        if (forward) camera.moveForward(MOVE_SPEED);
        if (back)    camera.moveBackward(MOVE_SPEED);
        if (left)    camera.strafeLeft(MOVE_SPEED);
        if (right)   camera.strafeRight(MOVE_SPEED);

        // vertical movement (space = up, left shift = down)
        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE))  camera.moveUp(MOVE_SPEED);
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) camera.moveDown(MOVE_SPEED);
    }
    
    private float getGroundYUnderCamera() {
        float camX = camera.getX();
        float camZ = camera.getZ();

        // Convert world coordinates to block indices inside the chunk
        float startX = 0; // or 0 if your chunk starts at 0
        float startZ = 0;

        int blockX = (int) ((camX - startX) / Chunk.CUBE_LENGTH);
        int blockZ = (int) ((camZ - startZ) / Chunk.CUBE_LENGTH);

        // Clamp to valid range so we don't go out of the chunk array
        if (blockX < 0) blockX = 0;
        if (blockX >= Chunk.CHUNK_SIZE) blockX = Chunk.CHUNK_SIZE - 1;

        if (blockZ < 0) blockZ = 0;
        if (blockZ >= Chunk.CHUNK_SIZE) blockZ = Chunk.CHUNK_SIZE - 1;

        // Use the helper you added in Chunk
        var topBlockYIndex = camera.chunk.getTopSolidY(blockX, blockZ);

        // Convert block index to world Y coordinate of the top surface of that block
        float startY = 0; // often 0
        float groundY = startY + topBlockYIndex * Chunk.CUBE_LENGTH;

        return groundY;
    }
    
    private void updateDayNightCycle() {
        // Advance time; tweak timeOfDay for faster/slower cycle
        timeOfDay += 0.005f;
        if (timeOfDay > Math.PI * 2) {
            timeOfDay -= (float)(Math.PI * 2);
        }

        // Sun direction moves in a vertical circle
        float sunX = (float) Math.cos(timeOfDay);
        float sunY = (float) Math.sin(timeOfDay);
        float sunZ = 0.3f; // slight tilt so it’s not perfectly flat

        // Update lightPosition (directional light => w = 0)
        lightPosition.clear();
        lightPosition.put(sunX).put(sunY).put(sunZ).put(0.0f).flip();

        // Brightness factor t: 0 = darkest night, 1 = brightest day
        float t = (float) ((sunY + 1.0) / 2.0); // map [-1,1] → [0,1]
        if (t < 0f) t = 0f;
        if (t > 1f) t = 1f;

        // --- Main sun light (GL_LIGHT0) ---

        // Diffuse: strong in day, very weak at night
        FloatBuffer diffuse = BufferUtils.createFloatBuffer(4);
        diffuse
            .put(0.1f + 0.9f * t)  // R
            .put(0.1f + 0.9f * t)  // G
            .put(0.15f + 0.85f * t) // B
            .put(1.0f)
            .flip();
        glLight(GL_LIGHT0, GL_DIFFUSE, diffuse);

        // Ambient: a bit higher at night so it’s not pitch black
        FloatBuffer ambient = BufferUtils.createFloatBuffer(4);
        ambient
            .put(0.25f - 0.15f * t) // brighter at night, dimmer in day
            .put(0.25f - 0.15f * t)
            .put(0.30f - 0.15f * t)
            .put(1.0f)
            .flip();
        glLight(GL_LIGHT0, GL_AMBIENT, ambient);

        // --- Sky color (background) ---

        // Blend between night (dark blue) and day (light blue)
        float skyR = 0.02f + 0.10f * t;
        float skyG = 0.02f + 0.22f * t;
        float skyB = 0.08f + 0.35f * t;
        GL11.glClearColor(skyR, skyG, skyB, 1.0f);
    }
}