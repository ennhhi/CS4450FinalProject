/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package finalproject;

import java.nio.FloatBuffer;
import java.io.InputStream;
import java.util.Random;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;


public class Chunk {
    static final int CHUNK_SIZE = 30;
    static final int CUBE_LENGTH = 2;
    private Block[][][] Blocks;
    private int VBOVertexHandle;
    private int VBOColorHandle;
    private int StartX, StartY, StartZ;
    private Random r;
    
    private int VBOTextureHandle;
    private Texture texture;
    
    public void render(){
        glPushMatrix();
            glBindBuffer(GL_ARRAY_BUFFER, VBOVertexHandle);
            glVertexPointer(3, GL_FLOAT, 0, 0L);
            glBindBuffer(GL_ARRAY_BUFFER, VBOColorHandle);
            glColorPointer(3, GL_FLOAT, 0, 0L);
            glBindBuffer(GL_ARRAY_BUFFER, VBOTextureHandle);
            org.lwjgl.opengl.GL11.glBindTexture(org.lwjgl.opengl.GL11.GL_TEXTURE_2D, texture.getTextureID());
            glTexCoordPointer(2,GL_FLOAT,0,0L);
            glDrawArrays(GL_QUADS, 0, CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE * 24);
        glPopMatrix();
    }
    
    public void rebuildMesh(float startX, float startY, float startZ) {
        VBOColorHandle   = glGenBuffers();
        VBOVertexHandle  = glGenBuffers();
        VBOTextureHandle = glGenBuffers();

        // preallocate "worst case" sizes
        FloatBuffer VertexPositionData = BufferUtils.createFloatBuffer(
                (CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE) * 6 * 12);
        FloatBuffer VertexColorData = BufferUtils.createFloatBuffer(
                (CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE) * 6 * 12);
        FloatBuffer VertexTextureData = BufferUtils.createFloatBuffer(
                (CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE) * 6 * 12);

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                for (int y = 0; y < CHUNK_SIZE; y++) {

                    // ----- KEY GUARD: skip AIR cells -----
                    Block block = Blocks[x][y][z];
                    if (block == null) {
                        continue;
                    }

                    float wx = startX + x * CUBE_LENGTH;
                    float wy = /* startY + */ y * CUBE_LENGTH;
                    float wz = startZ + z * CUBE_LENGTH;

                    VertexPositionData.put(createCube(wx, wy, wz));
                    VertexColorData.put(createCubeVertexCol(getCubeColor(block)));
                    VertexTextureData.put(createTexCube(0f, 0f, block));
                }
            }
        }

        VertexPositionData.flip();
        VertexColorData.flip();
        VertexTextureData.flip();

        glBindBuffer(GL_ARRAY_BUFFER, VBOVertexHandle);
        glBufferData(GL_ARRAY_BUFFER, VertexPositionData, GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, VBOColorHandle);
        glBufferData(GL_ARRAY_BUFFER, VertexColorData, GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, VBOTextureHandle);
        glBufferData(GL_ARRAY_BUFFER, VertexTextureData, GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    
    public float[] createCubeVertexCol(float[] CubeColorArray){
        float[] cubeColors = new float[CubeColorArray.length * 4 * 6];
        for (int i = 0; i < cubeColors.length; i++){
            cubeColors[i] = CubeColorArray[i%CubeColorArray.length];
        }
        return cubeColors;
    }
    
    public float[] createCube(float x, float y, float z){
        int offset = CUBE_LENGTH / 2;
        return new float[] {
            // TOP QUAD
            x + offset, y + offset, z,
            x - offset, y + offset, z,
            x - offset, y + offset, z - CUBE_LENGTH,
            x + offset, y + offset, z - CUBE_LENGTH,
            // BOTTOM QUAD
            x + offset, y - offset, z - CUBE_LENGTH,
            x - offset, y - offset, z - CUBE_LENGTH,
            x - offset, y - offset, z,
            x + offset, y - offset, z,
            // FRONT QUAD
            x + offset, y + offset, z - CUBE_LENGTH,
            x - offset, y + offset, z - CUBE_LENGTH,
            x - offset, y - offset, z - CUBE_LENGTH,
            x + offset, y - offset, z - CUBE_LENGTH,
            // BACK QUAD
            x + offset, y - offset, z,
            x - offset, y - offset, z,
            x - offset, y + offset, z,
            x + offset, y + offset, z,
            // LEFT QUAD
            x - offset, y + offset, z - CUBE_LENGTH,
            x - offset, y + offset, z,
            x - offset, y - offset, z,
            x - offset, y - offset, z - CUBE_LENGTH,
            // RIGHT QUAD
            x + offset, y + offset, z,
            x + offset, y + offset, z - CUBE_LENGTH,
            x + offset, y - offset, z - CUBE_LENGTH,
            x + offset, y - offset, z };
    }
    
    // temporary coloring for troubleshooting, revert to all white when done
    public float[] getCubeColor(Block block) {
        return new float[]{1,1,1};
        
        /*
        if (block == null) return new float[]{0,0,0};
        switch (block.GetID()) {
            case 1: return new float[]{0.3f, 0.9f, 0.3f};  // GRASS green
            case 2: return new float[]{0.95f, 0.9f, 0.5f}; // SAND yellow
            case 3: return new float[]{0.4f, 0.6f, 1.0f};  // WATER blue
            case 4: return new float[]{0.6f, 0.3f, 0.1f};  // DIRT brown
            case 5: return new float[]{0.7f, 0.7f, 0.7f};  // STONE gray
            case 6: return new float[]{0.2f, 0.2f, 0.2f};  // BEDROCK dark
            default: return new float[]{1,1,1};
        }*/
    }

    
    public static float[] createTexCube(float x, float y, Block block) {
        float offset = (1024f/16)/1024f;
        switch (block.GetID()) {
            case 1: //grass
                return new float[] {
                // BOTTOM QUAD(DOWN=+Y)
                x + offset*3, y + offset*10,
                x + offset*2, y + offset*10,
                x + offset*3, y + offset*9,
                x + offset*2, y + offset*9,
                // TOP!
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*3, y + offset*1,
                x + offset*4, y + offset*1,
                // FRONT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*3, y + offset*1,
                x + offset*4, y + offset*1,
                // BACK QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*3, y + offset*1,
                x + offset*4, y + offset*1,
                // LEFT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*3, y + offset*1,
                x + offset*4, y + offset*1,
                // RIGHT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*3, y + offset*1,
                x + offset*4, y + offset*1
            };
                
            case 2: //sand
                return new float[] {
                // BOTTOM QUAD(DOWN=+Y)
                x + offset*3, y + offset*0,
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*1,
                // TOP!
                x + offset*3, y + offset*0,
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*1,
                // FRONT QUAD
                x + offset*3, y + offset*0,
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*1,
                // BACK QUAD
                x + offset*3, y + offset*0,
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*1,
                // LEFT QUAD
                x + offset*3, y + offset*0,
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*1,
                // RIGHT QUAD
                x + offset*3, y + offset*0,
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*1
            };
                
            case 3: // water
                return new float[] {
                // BOTTOM QUAD(DOWN=+Y)
                x + offset*15, y + offset*0,
                x + offset*15, y + offset*1,
                x + offset*14, y + offset*0,
                x + offset*14, y + offset*1,
                // TOP!
                x + offset*15, y + offset*0,
                x + offset*15, y + offset*1,
                x + offset*14, y + offset*0,
                x + offset*14, y + offset*1,
                // FRONT QUAD
                x + offset*15, y + offset*0,
                x + offset*15, y + offset*1,
                x + offset*14, y + offset*0,
                x + offset*14, y + offset*1,
                // BACK QUAD
                x + offset*15, y + offset*0,
                x + offset*15, y + offset*1,
                x + offset*14, y + offset*0,
                x + offset*14, y + offset*1,
                // LEFT QUAD
                x + offset*15, y + offset*0,
                x + offset*15, y + offset*1,
                x + offset*14, y + offset*0,
                x + offset*14, y + offset*1,
                // RIGHT QUAD
                x + offset*15, y + offset*0,
                x + offset*15, y + offset*1,
                x + offset*14, y + offset*0,
                x + offset*14, y + offset*1
            };
            
            case 4:
                return new float[] {
                // BOTTOM QUAD(DOWN=+Y)
                x + offset*3, y + offset*0,
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*2, y + offset*1,
                // TOP!
                x + offset*3, y + offset*0,
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*2, y + offset*1,
                // FRONT QUAD
                x + offset*3, y + offset*0,
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*2, y + offset*1,
                // BACK QUAD
                x + offset*3, y + offset*0,
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*2, y + offset*1,
                // LEFT QUAD
                x + offset*3, y + offset*0,
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*2, y + offset*1,
                // RIGHT QUAD
                x + offset*3, y + offset*0,
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*2, y + offset*1
            };
                
            case 5: //stone
                return new float[] {
                // BOTTOM QUAD(DOWN=+Y)
                x + offset*1, y + offset*0,
                x + offset*1, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*2, y + offset*1,
                // TOP!
                x + offset*1, y + offset*0,
                x + offset*1, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*2, y + offset*1,
                // FRONT QUAD
                x + offset*1, y + offset*0,
                x + offset*1, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*2, y + offset*1,
                // BACK QUAD
                x + offset*1, y + offset*0,
                x + offset*1, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*2, y + offset*1,
                // LEFT QUAD
                x + offset*1, y + offset*0,
                x + offset*1, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*2, y + offset*1,
                // RIGHT QUAD
                x + offset*1, y + offset*0,
                x + offset*1, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*2, y + offset*1
            };
                
            case 6: //bedrock
                return new float[] {
                // BOTTOM QUAD(DOWN=+Y)
                x + offset*1, y + offset*1,
                x + offset*1, y + offset*2,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*2,
                // TOP!
                x + offset*1, y + offset*1,
                x + offset*1, y + offset*2,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*2,
                // FRONT QUAD
                x + offset*1, y + offset*1,
                x + offset*1, y + offset*2,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*2,
                // BACK QUAD
                x + offset*1, y + offset*1,
                x + offset*1, y + offset*2,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*2,
                // LEFT QUAD
                x + offset*1, y + offset*1,
                x + offset*1, y + offset*2,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*2,
                // RIGHT QUAD
                x + offset*1, y + offset*1,
                x + offset*1, y + offset*2,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*2,
            };
        }
        return new float[] {};
    }
  
    public Chunk(int startX, int startY, int startZ){
        try (InputStream in = Chunk.class.getClassLoader().getResourceAsStream("finalproject/terrain.png")) {
            if (in == null) {
                throw new RuntimeException("terrain.png not found at finalproject/terrain.png");
            }
            texture = TextureLoader.getTexture("PNG", in, org.lwjgl.opengl.GL11.GL_NEAREST);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Blocks = new Block[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
        SimplexNoise noise = new SimplexNoise(64, 0.5f, new Random().nextInt());

        // controls how smooth the hills are
        double scale = 0.0175;      // smaller = smoother terrain
        double baseH = CHUNK_SIZE * 0.4;  // average ground topY
        double amp   = CHUNK_SIZE * 0.3; // vertical variation

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                // calculate smooth topY for this column
                double nx = (startX + x) * scale;
                double nz = (startZ + z) * scale;
                double h = noise.getNoise((int)(nx * 256), (int)(nz * 256))
                         + 0.5 * noise.getNoise((int)(nx * 512), (int)(nz * 512));
                h /= 1.5;
                int topY = (int)Math.round(baseH + h * amp);
                if (topY < 1) topY = 1;
                if (topY > CHUNK_SIZE - 1) topY = CHUNK_SIZE - 1;

                // fill the column
                for (int y = 0; y < CHUNK_SIZE; y++) {
                    Block.BlockType type;

                    if (y == 0) {
                        // bedrock floor
                        type = Block.BlockType.BlockType_Bedrock;
                        Blocks[x][y][z] = new Block(type);
                    } else if (y < topY - 1) {
                        // bulk stone up to just under the surface
                        type = Block.BlockType.BlockType_Stone;
                        Blocks[x][y][z] = new Block(type);
                    } else if (y == topY - 1) {
                        // a single dirt layer under the top
                        type = Block.BlockType.BlockType_Dirt;
                        Blocks[x][y][z] = new Block(type);
                    } else if (y == topY) {
                        // the surface block (varies by topY band)
                        if (topY <= baseH * 0.55)      type = Block.BlockType.BlockType_Water;
                        else if (topY <= baseH * 0.75) type = Block.BlockType.BlockType_Sand;
                        else                            type = Block.BlockType.BlockType_Grass;
                        Blocks[x][y][z] = new Block(type);
                    } else {
                        // AIR above the surface
                        Blocks[x][y][z] = null;
                    }
                }
            }
        }

        VBOColorHandle = glGenBuffers();
        VBOVertexHandle = glGenBuffers();
        VBOTextureHandle = glGenBuffers();
        StartX = startX;
        StartY = startY;
        StartZ = startZ;
        rebuildMesh(startX, startY, startZ);
    }
}

