package CGFramework;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import math.Mat3;
import math.Mat4;
import math.Vec2;
import math.Vec3;
import math.Vec4;
import org.lwjgl.BufferUtils;
import util.FileIO;
import util.Texture;

public class ShaderProgram {
    private final  int maxActiveTextures  = 8;
    private static int activeTexture = 0;

    private HashMap<String, Integer> m_UniformLocations;
    private int                      m_Program;


    public ShaderProgram( String vertexFile, String fragmentFile )
    {
        m_UniformLocations = new HashMap<String, Integer>();

        String vertexSource   = FileIO.readTXT(vertexFile);
        String fragmentSource = FileIO.readTXT( fragmentFile );

        this.createProgram( vertexSource, fragmentSource );
    }


    private void createProgram( String vertexSource, String fragmentSource )
    {
        m_Program = glCreateProgram();

        int vertexShader   = this.createShader( vertexSource, GL_VERTEX_SHADER );
        int fragmentShader = this.createShader( fragmentSource, GL_FRAGMENT_SHADER );

        glAttachShader( m_Program, vertexShader );
        glAttachShader( m_Program, fragmentShader );
        glLinkProgram(  m_Program );

        glDeleteShader( vertexShader );
        glDeleteShader( fragmentShader );

        if ( glGetProgram(m_Program, GL_LINK_STATUS) == GL_FALSE )
        {
            ShaderProgram.printLog( m_Program );
        }

        glValidateProgram( m_Program );

        if ( glGetProgram(m_Program, GL_VALIDATE_STATUS) == GL_FALSE )
        {
            ShaderProgram.printLog( m_Program );
        }
    }


    private int createShader( String shaderSource, int type )
    {
        int shader = glCreateShader( type );

        glShaderSource( shader, shaderSource );
        glCompileShader( shader );

        if ( glGetShader( shader, GL_COMPILE_STATUS ) == GL_FALSE )
        {
            ShaderProgram.printLog( shader );
        }

        return shader;
    }


    private int getUniformLocation( String uniformName )
    {
        Integer cachedLocation = m_UniformLocations.get( uniformName );

        if( cachedLocation == null )
        {
            int location = glGetUniformLocation( m_Program, uniformName );
            m_UniformLocations.put( uniformName, location );

            return location;
        }

        return cachedLocation;
    }


    private static void printLog( int obj )
    {
        IntBuffer iVal = BufferUtils.createIntBuffer( 1 );
        glGetShader( obj, GL_INFO_LOG_LENGTH, iVal );

        int length = iVal.get();

        if (length > 1)
        {
            ByteBuffer infoLog = BufferUtils.createByteBuffer( length );
            iVal.flip();

            glGetShaderInfoLog( obj, iVal, infoLog );

            byte[] infoBytes = new byte[length];
            infoLog.get( infoBytes );

            String out = new String( infoBytes );
            System.out.println( "Shader log:\n" + out );
        }
    }


    public void useProgram()
    {
        glUseProgram( m_Program );
    }


    public void setUniform( String uniformName, int value )
    {
        glUniform1i( this.getUniformLocation(uniformName), value );
    }


    public void setUniform( String uniformName, float value )
    {
        glUniform1f( this.getUniformLocation(uniformName), value );
    }


    public void setUniform( String uniformName, Vec2 vec )
    {
        glUniform2f( this.getUniformLocation(uniformName), vec.x, vec.y );
    }


    public void setUniform( String uniformName, Vec3 vec )
    {
        glUniform3f( this.getUniformLocation(uniformName), vec.x, vec.y, vec.z );
    }


    public void setUniform( String uniformName, Vec4 vec )
    {
        glUniform4f( this.getUniformLocation(uniformName), vec.x, vec.y, vec.z, vec.w );
    }


    public void setUniform( String uniformName, Mat3 mat )
    {
        glUniformMatrix3( this.getUniformLocation(uniformName), false, mat.toFloatBuffer() );
    }


    public void setUniform( String uniformName, Mat4 mat )
    {
        glUniformMatrix4( this.getUniformLocation(uniformName), false, mat.toFloatBuffer() );
    }

    public void setUniform( String uniformName, Texture texture )
    {
        int textureSlot = GL_TEXTURE0 + activeTexture;
        int textureID   = texture.getID();

        glActiveTexture( textureSlot );
        glBindTexture( GL_TEXTURE_2D, textureID );
        glUniform1i( this.getUniformLocation(uniformName), activeTexture );

        activeTexture = (activeTexture + 1) % maxActiveTextures;
    }

    public void setUniform( String uniformName, Vec3[] vecArray ) {
        FloatBuffer fb = BufferUtils.createFloatBuffer(vecArray.length*3);
        float[] temp = new float[vecArray.length*3];

        for(int i=0 ; i<vecArray.length ; i++) {
            temp[i*3] = vecArray[i].x;
            temp[i*3+1] = vecArray[i].y;
            temp[i*3+2] = vecArray[i].z;
        }

        fb.put(temp);
        fb.flip();
        glUniform3(this.getUniformLocation(uniformName), fb);
    }

    public void setUniform( String uniformName, Vec4[] vecArray ) {
        FloatBuffer fb = BufferUtils.createFloatBuffer(vecArray.length*4);
        float[] temp = new float[vecArray.length*4];

        for(int i=0 ; i<vecArray.length ; i++) {
            temp[i*3] = vecArray[i].x;
            temp[i*3+1] = vecArray[i].y;
            temp[i*3+2] = vecArray[i].z;
            temp[i*3+3] = vecArray[i].w;
        }

        fb.put(temp);
        fb.flip();
        glUniform4(this.getUniformLocation(uniformName), fb);
    }

    public void setUniform( String uniformName, float[] floats ) {
        FloatBuffer fb = BufferUtils.createFloatBuffer(floats.length);
        fb.put(floats);
        fb.flip();
        glUniform1( this.getUniformLocation(uniformName), fb);
    }
}
