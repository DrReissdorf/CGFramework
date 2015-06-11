package CGFramework.meshgenerators;

import CGFramework.models.RawMesh;
import util.Mesh;
import static org.lwjgl.opengl.GL15.*;

public class Sphere {    
	public static RawMesh createMesh( float radius, int latitudeBands, int longitudeBands )	{
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

	            normals[ vertex * 3 + 0 ] = x;
	            normals[ vertex * 3 + 1 ] = y;
	            normals[ vertex * 3 + 2 ] = z;

	            texCoords[ vertex * 2 + 0 ] = u;
	            texCoords[ vertex * 2 + 1 ] = v;
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
	    
		RawMesh rawMesh = new RawMesh( GL_STATIC_DRAW );
		rawMesh.setAttribute( 0, positions, 3 );
		rawMesh.setAttribute( 1, normals, 3 );
		rawMesh.setAttribute( 2, texCoords, 3 );
		rawMesh.setIndices( indices );
		
		return rawMesh;
	}
        
        
}
