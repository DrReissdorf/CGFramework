package util;
/* 
 * Cologne University of Applied Sciences
 * Institute for Media and Imaging Technologies - Computer Graphics Group
 *
 * Copyright (c) 2012 Cologne University of Applied Sciences. All rights reserved.
 *
 * This source code is property of the Cologne University of Applied Sciences. Any redistribution
 * and use in source and binary forms, with or without modification, requires explicit permission. 
 */




import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import math.Mat3;
import math.Mat4;
import math.Vec2;
import math.Vec3;
import math.Vec4;
import org.lwjgl.BufferUtils;



public class ShaderProgram 
{
	private final  int maxActiveTextures  = 8;
	private static int activeTexture = 0;
	
	private HashMap<String, Integer> m_UniformLocations;
	private int                      m_Program;
	
	
	public ShaderProgram( String vertexFile, String fragmentFile )
	{
		m_UniformLocations = new HashMap<String, Integer>();
		
		String vertexSource   = FileIO.readTXT( vertexFile );
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
}
