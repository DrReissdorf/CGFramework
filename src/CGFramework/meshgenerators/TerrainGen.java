package CGFramework.meshgenerators;

import CGFramework.models.RawMesh;
import util.Mesh;

import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;

/**
 * Created by S on 11.06.2015.
 */
public class TerrainGen {
    private static final int VERTEX_COUNT = 128;
    public static final float SIZE = 10;

    public static RawMesh generateTerrain(){
        int count = VERTEX_COUNT * VERTEX_COUNT;
        float[] positions = new float[count * 3];
        float[] normals = new float[count * 3];
        float[] textureCoords = new float[count*2];
        int[] indices = new int[6*(VERTEX_COUNT-1)*(VERTEX_COUNT-1)];
        int vertexPointer = 0;
        for(int i=0;i<VERTEX_COUNT;i++){
            for(int j=0;j<VERTEX_COUNT;j++){
                positions[vertexPointer*3] = (float)j/((float)VERTEX_COUNT - 1) * SIZE;
                positions[vertexPointer*3+1] = 0;
                positions[vertexPointer*3+2] = (float)i/((float)VERTEX_COUNT - 1) * SIZE;
                normals[vertexPointer*3] = 0;
                normals[vertexPointer*3+1] = 1;
                normals[vertexPointer*3+2] = 0;
                textureCoords[vertexPointer*2] = (float)j/((float)VERTEX_COUNT - 1);
                textureCoords[vertexPointer*2+1] = (float)i/((float)VERTEX_COUNT - 1);
                vertexPointer++;
            }
        }
        int pointer = 0;
        for(int gz=0;gz<VERTEX_COUNT-1;gz++){
            for(int gx=0;gx<VERTEX_COUNT-1;gx++){
                int topLeft = (gz*VERTEX_COUNT)+gx;
                int topRight = topLeft + 1;
                int bottomLeft = ((gz+1)*VERTEX_COUNT)+gx;
                int bottomRight = bottomLeft + 1;
                indices[pointer++] = topLeft;
                indices[pointer++] = bottomLeft;
                indices[pointer++] = topRight;
                indices[pointer++] = topRight;
                indices[pointer++] = bottomLeft;
                indices[pointer++] = bottomRight;
            }
        }

        RawMesh rawMesh = new RawMesh( GL_STATIC_DRAW );
        rawMesh.setAttribute( 0, positions, 3 );
        rawMesh.setAttribute( 1, normals, 3 );
        rawMesh.setAttribute( 2, textureCoords, 3 );
        rawMesh.setIndices( indices );

        return rawMesh;
    }
}
