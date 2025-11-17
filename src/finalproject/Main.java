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

    private Camera camera;
    
    private FloatBuffer lightPosition;
    private FloatBuffer whiteLight;

    public static void main(String[] args) throws Exception {
        new Main().run();
    }

    private void run() throws LWJGLException {
        initDisplay();
        initGL();

        camera = new Camera(0f, 50f, 90f); // start a little back & up
        Mouse.setGrabbed(true);          // lock cursor for FPS feel

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
        
        initLightArrays();
        glLight(GL_LIGHT0, GL_POSITION, lightPosition); //sets our light’s position
        glLight(GL_LIGHT0, GL_SPECULAR, whiteLight);//sets our specular light
        glLight(GL_LIGHT0, GL_DIFFUSE, whiteLight);//sets our diffuse light
        glLight(GL_LIGHT0, GL_AMBIENT, whiteLight);//sets our ambient light
        glEnable(GL_LIGHTING);//enables our lighting
        glEnable(GL_LIGHT0);//enables light0


        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glClearColor(0.12f, 0.14f, 0.18f, 1f);
        
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);
        glEnable(GL_DEPTH_TEST);
        
        glEnable(GL_TEXTURE_2D);
        glEnableClientState (GL_TEXTURE_COORD_ARRAY);
    }

    private void gameLoop() {
        while (!Display.isCloseRequested()) {
            handleInput();

            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            GL11.glLoadIdentity();
            camera.lookThrough();

            // draw a cube at origin
            //drawColoredCube(2.0f);
            camera.chunk.render();

            Display.update();
            Display.sync(60); // cap at 60fps
        }
    }
    
    private void initLightArrays() {
        lightPosition = BufferUtils.createFloatBuffer(4);
        lightPosition.put(0.0f).put(0.0f).put(0.0f).put(1.0f).flip();
        whiteLight = BufferUtils.createFloatBuffer(4);
        whiteLight.put(1.0f).put(1.0f).put(1.0f).put(0.0f).flip();
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

    /* NO LONGER USED AFTER CHECKPOINT 1
    private void drawColoredCube(float size) {
        float s = size / 2f;

        GL11.glBegin(GL11.GL_QUADS);

        // +X (right) - red
        GL11.glColor3f(1f, 0f, 0f);
        GL11.glVertex3f(+s, -s, -s);
        GL11.glVertex3f(+s, -s, +s);
        GL11.glVertex3f(+s, +s, +s);
        GL11.glVertex3f(+s, +s, -s);

        // -X (left) - green
        GL11.glColor3f(0f, 1f, 0f);
        GL11.glVertex3f(-s, -s, +s);
        GL11.glVertex3f(-s, -s, -s);
        GL11.glVertex3f(-s, +s, -s);
        GL11.glVertex3f(-s, +s, +s);

        // +Y (top) - blue
        GL11.glColor3f(0f, 0f, 1f);
        GL11.glVertex3f(-s, +s, -s);
        GL11.glVertex3f(+s, +s, -s);
        GL11.glVertex3f(+s, +s, +s);
        GL11.glVertex3f(-s, +s, +s);

        // -Y (bottom) - yellow
        GL11.glColor3f(1f, 1f, 0f);
        GL11.glVertex3f(-s, -s, +s);
        GL11.glVertex3f(+s, -s, +s);
        GL11.glVertex3f(+s, -s, -s);
        GL11.glVertex3f(-s, -s, -s);

        // +Z (front) - magenta
        GL11.glColor3f(1f, 0f, 1f);
        GL11.glVertex3f(-s, -s, +s);
        GL11.glVertex3f(-s, +s, +s);
        GL11.glVertex3f(+s, +s, +s);
        GL11.glVertex3f(+s, -s, +s);

        // -Z (back) - cyan
        GL11.glColor3f(0f, 1f, 1f);
        GL11.glVertex3f(+s, -s, -s);
        GL11.glVertex3f(+s, +s, -s);
        GL11.glVertex3f(-s, +s, -s);
        GL11.glVertex3f(-s, -s, -s);

        GL11.glEnd();

        // reset color for anything else
        GL11.glColor3f(1f, 1f, 1f);
    } */
}