package CGFramework;

/*
  CG Sommer 2015

  Name, Vorname   :   Sven Riedel
  Matrikelnummer  :   11091276
  Aufgabenblatt   :   03
  Aufgabe         :   1
*/

import util.Mesh;
import static org.lwjgl.opengl.GL15.*;

public class Sphere {    
	public static Mesh createMesh( float radius, int latitudeBands, int longitudeBands )	{
		int vertexCount = ( longitudeBands + 1 ) * ( latitudeBands + 1 );
		
		float[] positions = new float[vertexCount * 3];
		float[] normals   = new float[vertexCount * 3];
		float[] texCoords = new float[vertexCount * 2];
		int[]   indices   = new int[vertexCount * 6];
	    
	    for( int latitude = 0; latitude <= latitudeBands; ++latitude ) {
	        for( int longitude = 0; longitude <= longitudeBands; ++longitude ) {
	            float theta    = latitude * (float) Math.PI / latitudeBands;
	            float phi      = longitude * 2.0f * (float) Math.PI / longitudeBands;
	            float sinTheta = (float) Math.sin( theta );
	            float sinPhi   = (float) Math.sin( phi );
	            float cosTheta = (float) Math.cos( theta );
	            float cosPhi   = (float) Math.cos( phi );
	            
	            float x = cosPhi * sinTheta;
	            float y = cosTheta;
	            float z = sinPhi * sinTheta;
	            float u = 1 - ( longitude / longitudeBands );
	            float v = latitude / latitudeBands;
	            
	            int vertex = latitude * longitudeBands + longitude;

	            positions[ vertex * 3 + 0 ] = (radius * x);
	            positions[ vertex * 3 + 1 ] = (radius * y);
	            positions[ vertex * 3 + 2 ] = (radius * z);

	/*            normals[ vertex * 3 + 0 ] = x;
	            normals[ vertex * 3 + 1 ] = y;
	            normals[ vertex * 3 + 2 ] = z;

	            texCoords[ vertex * 2 + 0 ] = u;
	            texCoords[ vertex * 2 + 1 ] = v; */
	        }
	    }

	    for( int latitude = 0; latitude < latitudeBands; ++latitude ) {
	        for( int longitude = 0; longitude < longitudeBands; ++longitude ) {
	            int first  = ( latitude * longitudeBands ) + longitude;
	            int second = first + longitudeBands;
	            
	            int face = latitude * longitudeBands + longitude;

	            indices[ face * 6 + 0 ] = first + 1;
	            indices[ face * 6 + 1 ] = second;
	            indices[ face * 6 + 2 ] = first;
	            
	            indices[ face * 6 + 3 ] = first + 1;
	            indices[ face * 6 + 4 ] = second + 1;
	            indices[ face * 6 + 5 ] = second;
	        }
	    }
	    
		Mesh mesh = new Mesh( GL_STATIC_DRAW );
		mesh.setAttribute( 0, positions, 3 );
		mesh.setAttribute( 1, Sandbox.generateVertexNormals(positions,indices), 3 );
		mesh.setIndices( indices );
		
		return mesh;  
	}
        
        
}
