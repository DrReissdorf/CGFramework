package CGFramework.aufgabe10_1;
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

public class Sandbox {
	private ShaderProgram shaderProgram;
	private ArrayList<Mesh> meshes = new ArrayList<>();
    private ArrayList<Model> models = new ArrayList<>();
	private ArrayList<ModelTexture> modelTextures = new ArrayList<>();
	private Light light;
	private Mat4            modelMatrix;
	private Mat4            viewMatrix;
	private int             windowWidth;
	private int             windowHeight;
	
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
        shaderProgram = new ShaderProgram( getPathForPackage() + "Color_vs.glsl", getPathForPackage() + "Color_fs.glsl" );
		modelMatrix   = new Mat4();
		viewMatrix    = Mat4.translation( 0.0f, 0.0f, -3.0f );
		meshes        = new ArrayList<Mesh>();

		createLight();
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
			CGFramework.aufgabe10_1.Main.exit();
		
		if( Key.justPressed(Keyboard.KEY_F) )
			CGFramework.aufgabe10_1.Main.toggleFullscreen();
		
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
		float near   = 0.01f;
		float far    = 500.0f;
		float lightFov    = 90.0f;

		Mat4 lightProjectionMatrix = Mat4.perspective(lightFov, windowWidth, windowHeight, near, far);
		glViewport( 0, 0, windowWidth, windowHeight );
		light.moveAroundCenter();
		this.drawMeshes( Mat4.lookAt(light.getPosition(), new Vec3(), new Vec3(0,1,0)), lightProjectionMatrix );
	}	
	
	public void drawMeshes( Mat4 viewMatrix, Mat4 projMatrix ) {
		shaderProgram.useProgram();
		shaderProgram.setUniform( "uModel",      modelMatrix );
		shaderProgram.setUniform( "uView",       viewMatrix );  
		shaderProgram.setUniform( "uProjection", projMatrix );

        shaderProgram.setUniform("uInvertedUView",      new Mat4(viewMatrix).inverse() );
        shaderProgram.setUniform("uNormalMat", createNormalMat(modelMatrix));
        shaderProgram.setUniform("uLightPos", light.getPosition());
        shaderProgram.setUniform("uLightColor", light.getColor());
        shaderProgram.setUniform("uLightRange", light.getRange());

		for( Model model : models ) {
            shaderProgram.setUniform("uShininess", model.getModelTexture().getShininess());
            shaderProgram.setUniform("uReflectivity", model.getModelTexture().getReflectivity());
            model.getMesh().draw(GL_TRIANGLES );
		}
	}
	
	private void createMeshes() {
		loadObj("Meshes/monkey_scene.obj");
	}

	private void createTextures() {
		modelTextures.add(new ModelTexture(new Texture("Textures/dragon.png"), 1f, 32));
	}

	private void createModels() {
        models.add(new Model(meshes.get(0), modelTextures.get(0)));
    }

	private void createLight() {
        light = new Light(new Vec3(3,3,3), new Vec3(1,1,1),50f,0.03f);
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
