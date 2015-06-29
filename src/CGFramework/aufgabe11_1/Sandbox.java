package CGFramework.aufgabe11_1;
/* 
 * Cologne University of Applied Sciences
 * Institute for Media and Imaging Technologies - Computer Graphics Group
 *
 * Copyright (c) 2014 Cologne University of Applied Sciences. All rights reserved.
 *
 * This source code is property of the Cologne University of Applied Sciences. Any redistribution
 * and use in source and binary forms, with or without modification, requires explicit permission. 
 */

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

import java.io.File;
import java.util.ArrayList;

import CGFramework.*;
import math.Mat4;
import math.Vec3;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import util.*;

public class Sandbox
{
	private ShaderProgram postProcessShaderProgram;
	private ArrayList<Mesh> meshes = new ArrayList<>();
    private ArrayList<Model> models = new ArrayList<>();
	private ArrayList<ModelTexture> modelTextures = new ArrayList<>();
	private Mat4            modelMatrix;
	private Mat4            viewMatrix;
	private int             windowWidth;
	private int             windowHeight;
	private Mesh quadMesh;
	
	/**
	 * @param width The horizontal window size in pixels
	 * @param height The vertical window size in pixels
	 */
	public Sandbox( int width, int height )	{
		windowWidth   = width;
		windowHeight  = height;
		// The shader program source files must be put into the same package as the Sandbox class file. This simplifies the 
        // handling in the lab exercise (i.e. for when uploading to Ilias or when correcting) since all code of one student
        // is kept in one package. In productive code the shaders would be put into the 'resource' directory.
		postProcessShaderProgram = new ShaderProgram( getPathForPackage() + "Postprocess_vs.glsl", getPathForPackage() + "Postprocess_fs.glsl" );
		modelMatrix   = new Mat4();
		viewMatrix    = Mat4.translation( 0.0f, 0.0f, -3.0f );
		meshes        = new ArrayList<Mesh>();

		createMeshes();
        createTextures();
        createModels();
		glEnable( GL_DEPTH_TEST );
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
	}
	
	   /**
     * @return The path to directory where the source file of this class is located.
     */
    private String getPathForPackage() {
        String locationOfSources = "src";
        String packageName = this.getClass().getPackage().getName();
        String path = locationOfSources + File.separator + packageName.replace(".", File.separator ) + File.separator;
        return path;
    }
	
	/**
	 * @param deltaTime The time in seconds between the last two frames
	 */
	public void update( float deltaTime )
	{
		if( Key.justReleased(Keyboard.KEY_ESCAPE) )
			CGFramework.aufgabe11_1.Main.exit();
		
		if( Key.justPressed(Keyboard.KEY_F) )
			Main.toggleFullscreen();
		
		float cameraSpeed = 5.0f * deltaTime;
		
		if( Key.isPressed(Keyboard.KEY_W) )
			viewMatrix.mul( Mat4.translation(0.0f, 0.0f, cameraSpeed) );
		
		if( Key.isPressed(Keyboard.KEY_S) )
			viewMatrix.mul( Mat4.translation(0.0f, 0.0f, -cameraSpeed) );

		if( Key.isPressed(Keyboard.KEY_A) )
			viewMatrix.mul( Mat4.translation(cameraSpeed, 0.0f, 0.0f) );

		if( Key.isPressed(Keyboard.KEY_D) )
			viewMatrix.mul( Mat4.translation(-cameraSpeed, 0.0f, 0.0f) );

		if( Key.isPressed(Keyboard.KEY_SPACE) )
			viewMatrix.mul( Mat4.translation(0.0f, -cameraSpeed, 0.0f) );

		if( Key.isPressed(Keyboard.KEY_LSHIFT) )
			viewMatrix.mul( Mat4.translation(0.0f, cameraSpeed, 0.0f) );
		
		if( Mouse.isButtonDown(0) ) {
			float rotationScale = 0.01f;
			float deltaX = (float) Mouse.getDX();
			float deltaY = (float) Mouse.getDY();
			Mat4 rotationX = Mat4.rotation( Vec3.yAxis(),  deltaX * rotationScale );
			Mat4 rotationY = Mat4.rotation( Vec3.xAxis(), -deltaY * rotationScale );
			modelMatrix = rotationY.mul( rotationX ).mul( modelMatrix );
		}
	}
	
	public void draw() {
		glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );
		float fov    = 60.0f;
		float near   = 0.01f;
		float far    = 500.0f;
		Mat4 projectionMatrix = Mat4.perspective( fov, windowWidth, windowHeight, near, far );
		glViewport( 0, 0, windowWidth, windowHeight );
		this.drawMeshes( viewMatrix, projectionMatrix );
	}	
	
	public void drawMeshes( Mat4 viewMatrix, Mat4 projMatrix ) {
		postProcessShaderProgram.useProgram();
		postProcessShaderProgram.setUniform( "uView",       viewMatrix );
		postProcessShaderProgram.setUniform( "uProjection", projMatrix );
		postProcessShaderProgram.setUniform("uTexture", models.get(0).getModelTexture().getTexture());
		quadMesh.draw();
	}

	private void createMeshes() {
		loadObj("Meshes/dragon.obj");
		createQuad();
	}
	private void createTextures() {
        modelTextures.add(new ModelTexture(new Texture("Textures/dragon.png"), 1f, 32));
	}
	private void createModels() {
        models.add(new Model(meshes.get(0), modelTextures.get(0)));
    }

	private void createQuad() {
		float[] positions = {-1.0f, -1.0f, 0.0f,
							 -1.0f, 1.0f, 0.0f,
							  1.0f, 1.0f,0.0f,
							  1.0f, -1.0f,0.0f};

		int[] indices = {0,3,2,
						 2,1,0};

		float[] textureCoords = {0,0,0,
								1,0,0,
								1,1,0,
								0,1,0};

		Mesh mesh = new Mesh( GL_STATIC_DRAW );
		mesh.setAttribute( 0, positions, 3 );
	//	mesh.setAttribute( 1, normals, 3 );
		mesh.setAttribute( 2, textureCoords, 3 );
		mesh.setIndices( indices );

		quadMesh = mesh;
	}

	private void loadObj( String filename )
	{
		OBJContainer        objContainer = OBJContainer.loadFile( filename );
		ArrayList<OBJGroup> objGroups    = objContainer.getGroups();
		
		for( OBJGroup group : objGroups )
		{
			float[] positions = group.getPositions();
			float[] normals   = group.getNormals();
			int[]   indices   = group.getIndices();
			float[] textureCoords	  = group.getTexCoords();
			
			Mesh mesh = new Mesh( GL_STATIC_DRAW );
			mesh.setAttribute( 0, positions, 3 );
			mesh.setAttribute( 1, normals, 3 );
			mesh.setAttribute( 2, textureCoords, 3 );
			mesh.setIndices( indices );
			
			meshes.add( mesh );
		}
	}
	
	public void onResize( int width, int height )
	{
		windowWidth  = width;
		windowHeight = height;
	}

    private Mat4 createNormalMat(Mat4 modelMatrix) {
        return Mat4.inverse(modelMatrix).transpose();
    }
}
