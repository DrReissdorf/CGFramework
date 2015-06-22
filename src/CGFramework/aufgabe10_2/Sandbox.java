package CGFramework.aufgabe10_2;
/* 
 * Cologne University of Applied Sciences
 * Institute for Media and Imaging Technologies - Computer Graphics Group
 *
 * Copyright (c) 2014 Cologne University of Applied Sciences. All rights reserved.
 *
 * This source code is property of the Cologne University of Applied Sciences. Any redistribution
 * and use in source and binary forms, with or without modification, requires explicit permission. 
 */

import CGFramework.Light;
import CGFramework.Model;
import CGFramework.ModelTexture;
import CGFramework.aufgabe10_2.Main;
import math.Mat4;
import math.Vec3;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import util.*;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL30.*;

import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;

public class Sandbox {
	private CGFramework.ShaderProgram shaderProgram;
	private CGFramework.ShaderProgram shaderProgramShadow;
    private ArrayList<Light> lights = new ArrayList<>();
	private ArrayList<Mesh> meshes = new ArrayList<>();
    private ArrayList<Model> models = new ArrayList<>();
	private ArrayList<ModelTexture> modelTextures = new ArrayList<>();
    Vec3[] lightPositions;
    Vec3[] lightColors;
    float[] lightRanges;
	private Mat4            modelMatrix;
	private Mat4            viewMatrix;
	private int             windowWidth;
	private int             windowHeight;

	private Mat4 lightViewMatrix;
	private Mat4 lightProjectionMatrix;
	private int shadowFrameBuffer;
	private int shadowTextureID;
	private int depthrenderbuffer;

	
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
        shaderProgram = new CGFramework.ShaderProgram( getPathForPackage() + "Color_vs.glsl", getPathForPackage() + "Color_fs.glsl" );

		modelMatrix   = new Mat4();
		viewMatrix    = Mat4.translation( 0.0f, 0.0f, -3.0f );
		meshes        = new ArrayList<Mesh>();

		createLights();
		createMeshes();
		createTextures();
        createModels();

		setupShadowMap(1024);

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
			Main.exit();
		
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
		float fov    = 60.0f;
		float near   = 0.01f;
		float far    = 500.0f;

		//SHADOW MAPPING RENDER
		renderShadowMap();

		//NORMAL RENDER
	//	Mat4 projectionMatrix = Mat4.perspective( fov, windowWidth, windowHeight, near, far );
	//	this.drawMeshes( viewMatrix, projectionMatrix );
	}	
	
	public void drawMeshes( Mat4 viewMatrix, Mat4 projMatrix ) {
		shaderProgram.useProgram();
		shaderProgram.setUniform( "uModel",      modelMatrix );
		shaderProgram.setUniform( "uView",       viewMatrix );  
		shaderProgram.setUniform( "uProjection", projMatrix );

		shaderProgram.setUniform( "uLightProjection", lightProjectionMatrix );
		shaderProgram.setUniform( "uLightView", lightViewMatrix );

        shaderProgram.setUniform("uInvertedUView",      new Mat4(viewMatrix).inverse() );
        shaderProgram.setUniform("uNormalMat", createNormalMat(modelMatrix));
        shaderProgram.setUniform("uLightPosArray", lightPositions);
        shaderProgram.setUniform("uLightColorArray", lightColors);
        shaderProgram.setUniform("uLightRange", lightRanges);

		shaderProgram.setUniformShadowTexture("uShadowmap", shadowTextureID);

		for( Model model : models ) {
       //     shaderProgram.setUniform("uTexture",model.getModelTexture().getTexture());
            shaderProgram.setUniform("uShininess", model.getModelTexture().getShininess());
            shaderProgram.setUniform("uReflectivity", model.getModelTexture().getReflectivity());


            model.getMesh().draw(GL_TRIANGLES );
		}
	}

	private void renderShadowMap() {
		glBindTexture(GL_TEXTURE_2D, 0);//To make sure the texture is not bound
		glBindFramebuffer( GL_FRAMEBUFFER, 0);
	//	glBindFramebuffer( GL_FRAMEBUFFER, shadowFrameBuffer);

		glViewport( 0, 0, 1024, 1024 );
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
	//	GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);



		lightProjectionMatrix = Mat4.perspective(90, windowWidth, windowHeight, 0.01f, 500.0f);
//		lightProjectionMatrix = Mat4.orthographic(5,5,5,5,0.01f,500f);
		lights.get(0).increaseRotation(0,0.2f,0);
		createLightArrays(lights);

		int err = glCheckFramebufferStatus(GL_FRAMEBUFFER);
		if( err != GL_FRAMEBUFFER_COMPLETE) {
			System.out.println("Frame buffer is not complete. Error: " + err);
			System.exit(-1);
		}

		lightViewMatrix = Mat4.lookAt(lightPositions[0], new Vec3(), new Vec3(0, 1, 0));

		//render shadow scene
		drawMeshesShadow();

	//	glBindFramebuffer(GL_FRAMEBUFFER, 0);
//		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
//		GL11.glClearColor(0, 0, 0, 1);
//		glViewport( 0, 0, windowWidth, windowHeight );
	}

	public void drawMeshesShadow ( ) {
		glCullFace(GL_BACK);
		shaderProgramShadow.useProgram();
		shaderProgramShadow.setUniform( "uModel",      modelMatrix );
		shaderProgramShadow.setUniform( "uView",       lightViewMatrix );
		shaderProgramShadow.setUniform( "uProjection", lightProjectionMatrix );

		for( Model model : models ) {
			model.getMesh().draw(GL_TRIANGLES );
		}
	}

	private void setupShadowMap( int shadowMapSize ) {
		shaderProgramShadow = new CGFramework.ShaderProgram( getPathForPackage() + "Shadowmap_vs.glsl", getPathForPackage() + "Shadowmap_fs.glsl" );

		// Create Texture
		shadowTextureID = glGenTextures();
		glBindTexture( GL_TEXTURE_2D, shadowTextureID );
		glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER );
		glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER );
		glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR );
		glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR );
		glTexImage2D( GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, shadowMapSize, shadowMapSize,
				0, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer)null );
		glBindTexture( GL_TEXTURE_2D, 0 );

		//Create Framebuffer
		shadowFrameBuffer = glGenFramebuffers();
		glBindFramebuffer( GL_FRAMEBUFFER, shadowFrameBuffer );

		glFramebufferTexture2D( GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,
				GL_TEXTURE_2D, shadowTextureID, 0 );

		glReadBuffer(GL_NONE);
		glDrawBuffer(GL_NONE);

		depthrenderbuffer = glGenRenderbuffers();
		glBindRenderbuffer(GL_RENDERBUFFER, depthrenderbuffer);

		glRenderbufferStorage(GL_RENDERBUFFER, GL_RGBA, shadowMapSize, shadowMapSize);

		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
				GL_RENDERBUFFER, depthrenderbuffer);


		glBindFramebuffer( GL_FRAMEBUFFER, 0 );

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

	private void createLights() {
        lights.add( new Light(new Vec3(3,3,3), new Vec3(1,1,1),10f));

    }

	private void createLightArrays(List<Light> lightList) {
		lightPositions = new Vec3[lightList.size()];
		lightColors = new Vec3[lightList.size()];
		lightRanges = new float[lightList.size()];

		for(int i=0 ; i<lightList.size() ; i++) {
			lightPositions[i] = lights.get(i).getPosition();
			lightColors[i]      = lights.get(i).getColor();
			lightRanges[i]      = lights.get(i).getRange();
		}

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
