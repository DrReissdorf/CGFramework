package CGFramework.render.model;

import org.lwjgl.BufferUtils;
import util.IntArrayList;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class RawMesh {
    private int   m_iUsage;
    private int   m_iVAOid;
    private int   m_iIndexBufferID;
    private int   m_iNumIndices;

    IntArrayList m_AttribBuffers;
    IntArrayList m_AttribComponents;
    IntArrayList m_AttribLocations;


    /**
     * @param usage Specifies the expected usage pattern of the data store.
     * The symbolic constant must be GL_STREAM_DRAW, GL_STREAM_READ, GL_STREAM_COPY,
     * GL_STATIC_DRAW, GL_STATIC_READ, GL_STATIC_COPY, GL_DYNAMIC_DRAW, GL_DYNAMIC_READ, or GL_DYNAMIC_COPY.
     */
    public RawMesh( int usage )
    {
        m_iUsage         = usage;
        m_iVAOid         = 0;
        m_iIndexBufferID = glGenBuffers();
        m_iNumIndices    = 0;

        m_AttribBuffers    = new IntArrayList( 1 );
        m_AttribComponents = new IntArrayList( 1 );
        m_AttribLocations  = new IntArrayList( 1 );

        this.rebuildVAO();
    }


    /**
     * @param attribLocation The attribute location to set
     * @param values
     * @param componentsPerAttrib Specifies the number of components per generic vertex attribute. Must be either 1, 2, 3 or 4.
     */
    public void setAttribute( int attribLocation, float[] values, int componentsPerAttrib )
    {
        int index    = m_AttribLocations.indexOf( attribLocation );
        int bufferID = 0;

        if( index < 0 )
        {
            bufferID = glGenBuffers();
            m_AttribBuffers.add(    bufferID );
            m_AttribLocations.add(  attribLocation );
            m_AttribComponents.add( componentsPerAttrib );
        }
        else
        {
            bufferID = m_AttribBuffers.get( index );
            m_AttribLocations.set(  index, attribLocation );
            m_AttribComponents.set( index, componentsPerAttrib );
        }

        FloatBuffer attribData = BufferUtils.createFloatBuffer(values.length);
        attribData.put( values, 0, values.length );
        attribData.flip();

        glBindBuffer( GL_ARRAY_BUFFER, bufferID );
        glBufferData( GL_ARRAY_BUFFER, 0,          m_iUsage );
        glBufferData( GL_ARRAY_BUFFER, attribData, m_iUsage );
        glBindBuffer( GL_ARRAY_BUFFER, 0 );

        this.rebuildVAO();
    }


    public void setIndices( int[] indices )
    {
        m_iNumIndices = indices.length;

        IntBuffer indexBuffer = BufferUtils.createIntBuffer( indices.length );
        indexBuffer.put( indices, 0, indices.length );
        indexBuffer.flip();

        glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, m_iIndexBufferID );
        glBufferData( GL_ELEMENT_ARRAY_BUFFER, 0,           m_iUsage );
        glBufferData( GL_ELEMENT_ARRAY_BUFFER, indexBuffer, m_iUsage );
        glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, 0 );

        this.rebuildVAO();
    }


    /**
     * Deletes all internally created OpenGL Resources (vertex and index buffers).<br>
     * Externally created buffers set via setVertexBuffer() will not be deleted and are expected to be freed elsewhere.
     */
    public void freeGLResources()
    {
        for( int i = 0; i < m_AttribBuffers.size(); ++i )
            glDeleteBuffers( m_AttribBuffers.get(i) );

        m_AttribBuffers.clear();
        m_AttribLocations.clear();
        m_AttribComponents.clear();

        glDeleteBuffers( m_iIndexBufferID );
        glDeleteVertexArrays( m_iVAOid );
    }


    /**
     * Draws the mesh as a set of indexed triangles.
     */
    public void draw()
    {
        glBindVertexArray( m_iVAOid );
        glDrawElements( GL_TRIANGLES, m_iNumIndices, GL_UNSIGNED_INT, 0 );
        glBindVertexArray( 0 );
    }


    /**
     * Draws the mesh using the given mode.
     * @param mode GL_POINTS, GL_LINE_STRIP, GL_LINE_LOOP, GL_LINES, GL_LINE_STRIP_ADJACENCY,
     * GL_LINES_ADJACENCY, GL_TRIANGLE_STRIP, GL_TRIANGLE_FAN, GL_TRIANGLES,
     * GL_TRIANGLE_STRIP_ADJACENCY, GL_TRIANGLES_ADJACENCY and GL_PATCHES are accepted.
     */
    public void draw( int mode )
    {
        glBindVertexArray( m_iVAOid );
        glDrawElements( mode, m_iNumIndices, GL_UNSIGNED_INT, 0 );
        glBindVertexArray( 0 );
    }


    /**
     * Draws a specific range of indices using the given mode;
     * @param mode GL_POINTS, GL_LINE_STRIP, GL_LINE_LOOP, GL_LINES, GL_LINE_STRIP_ADJACENCY,
     * GL_LINES_ADJACENCY, GL_TRIANGLE_STRIP, GL_TRIANGLE_FAN, GL_TRIANGLES,
     * GL_TRIANGLE_STRIP_ADJACENCY, GL_TRIANGLES_ADJACENCY and GL_PATCHES are accepted.
     * @param indexOffset The first index to use
     * @param indexCount The number of indices to use
     */
    public void drawRange( int mode, int indexOffset, int indexCount )
    {
        long byteOffset = (long) indexOffset * 4;
        glBindVertexArray( m_iVAOid );
        glDrawElements( mode, indexCount, GL_UNSIGNED_INT, byteOffset );
        glBindVertexArray( 0 );
    }


    private void rebuildVAO()
    {
        if( m_iVAOid != 0 )
            glDeleteVertexArrays( m_iVAOid );

        m_iVAOid = glGenVertexArrays();

        glBindVertexArray( m_iVAOid );
        glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, m_iIndexBufferID );

        for( int i = 0; i < m_AttribBuffers.size(); ++i )
        {
            glBindBuffer( GL_ARRAY_BUFFER, m_AttribBuffers.get(i) );
            glEnableVertexAttribArray( m_AttribLocations.get(i) );
            glVertexAttribPointer( m_AttribLocations.get(i), m_AttribComponents.get(i), GL_FLOAT, false, 0, 0 );
        }

        glBindVertexArray( 0 );

        for( int i = 0; i < m_AttribLocations.size(); ++i )
            glDisableVertexAttribArray( m_AttribLocations.get(i) );

        glBindBuffer( GL_ARRAY_BUFFER, 0 );
        glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, 0 );
    }

    public int getM_iUsage() {
        return m_iUsage;
    }

    public int getM_iVAOid() {
        return m_iVAOid;
    }

    public int getM_iIndexBufferID() {
        return m_iIndexBufferID;
    }

    public int getM_iNumIndices() {
        return m_iNumIndices;
    }

    public IntArrayList getM_AttribBuffers() {
        return m_AttribBuffers;
    }

    public IntArrayList getM_AttribLocations() {
        return m_AttribLocations;
    }

    public IntArrayList getM_AttribComponents() {
        return m_AttribComponents;
    }
}