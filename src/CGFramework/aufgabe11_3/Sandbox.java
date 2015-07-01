package CGFramework.aufgabe11_3;
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
import math.Mat4;
import math.Vec3;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.EXTTextureSRGB;
import util.*;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;

public class Sandbox {
	private ShaderProgram shaderProgram;
	private ShaderProgram postProcessShaderProgram;
	private ArrayList<Mesh> meshes = new ArrayList<>();

	private Light light;

	private Mat4            modelMatrix;
	private Mat4            viewMatrix;
	private int             windowWidth;
	private int             windowHeight;

	private int postProcessTextureID;
	private int postProcessFrameBuffer;
	private Texture postProcessTexture;
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
		shaderProgram = new ShaderProgram( getPathForPackage() + "Color_vs.glsl", getPathForPackage() + "Color_fs.glsl" );
		postProcessShaderProgram = new ShaderProgram( getPathForPackage() + "Postprocess_vs.glsl", getPathForPackage() + "Postprocess_fs.glsl" );

		modelMatrix   = new Mat4();
		viewMatrix    = Mat4.translation( 0.0f, 0.0f, -3.0f );
		meshes        = new ArrayList<>();

		createLight();
		createMeshes();

		postProcessTextureID = createTextureBuffer(width, height);
		postProcessTexture = new Texture(postProcessTextureID);
		postProcessFrameBuffer = createFrameBuffer(postProcessTextureID, width, height);

		glEnable(GL_DEPTH_TEST);
		glEnable(GL_CULL_FACE);
	}

	/**
	 * @param deltaTime The time in seconds between the last two frames
	 */
	public void update( float deltaTime ) {
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
		float lightFov    = 90f;

		//SET UP VIEW AND PROJECTION MATRICES
		Mat4 projectionMatrix = Mat4.perspective( fov, windowWidth, windowHeight, near, far );

		// RENDER TO TEXTURE
		drawToTexture(viewMatrix, projectionMatrix);

		//POST PROCESS
		this.drawMeshes();
	}

	public void drawMeshes() {
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glClearColor(0, 0, 0, 1);
		glViewport(0, 0, windowWidth, windowHeight);
		glCullFace(GL_BACK);

		postProcessShaderProgram.useProgram();
		postProcessShaderProgram.setUniform("uTexture", postProcessTexture);
		quadMesh.draw();
	}

	public void drawToTexture( Mat4 viewMatrix, Mat4 projMatrix ) {
		glBindFramebuffer(GL_FRAMEBUFFER, postProcessFrameBuffer);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glClearColor(0, 0, 0, 1);
		glViewport(0, 0, windowWidth, windowHeight);
		glCullFace(GL_BACK);

		shaderProgram.useProgram();
		shaderProgram.setUniform("uModel", modelMatrix);
		shaderProgram.setUniform("uView", viewMatrix);
		shaderProgram.setUniform("uProjection", projMatrix);

		shaderProgram.setUniform("uInvertedUView", new Mat4(viewMatrix).inverse());
		shaderProgram.setUniform("uNormalMat", createNormalMat(modelMatrix));

		shaderProgram.setUniform("uLightPos", light.getPosition());
		shaderProgram.setUniform("uLightColor", light.getColor());
		shaderProgram.setUniform("uLightRange", light.getRange());

		for( Mesh mesh : meshes ) {
			shaderProgram.setUniform("uShininess", 2000f);
			shaderProgram.setUniform("uReflectivity", 1f);
			mesh.draw(GL_TRIANGLES);
		}
	}

	private int createTextureBuffer(int width, int height) {
		int texbuf = glGenTextures();
		glBindTexture( GL_TEXTURE_2D, texbuf );
		glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
		glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
		glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR );
		glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR );
		glTexImage2D( GL_TEXTURE_2D, 0, EXTTextureSRGB.GL_SRGB_EXT, width, height,
				0, GL_RGB, GL_FLOAT, (ByteBuffer)null );
		glBindTexture( GL_TEXTURE_2D, 0 );
		return texbuf;
	}

	private int createFrameBuffer(int texbuf, int width, int height) {
		int framebuf = glGenFramebuffers();
		glBindFramebuffer( GL_FRAMEBUFFER, framebuf );
		glFramebufferTexture2D( GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
				GL_TEXTURE_2D, texbuf, 0 );
		int depthrenderbuffer = glGenRenderbuffers();
		glBindRenderbuffer(GL_RENDERBUFFER, depthrenderbuffer);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,
				GL_RENDERBUFFER, depthrenderbuffer);
		glBindFramebuffer( GL_FRAMEBUFFER, 0 );
		return framebuf;
	}

	private void createQuad() {
		float[] positions = {
				-1.0f, -1.0f, 0.0f,	//0	lower-left
				-1.0f, 1.0f, 0.0f,	//1 upper-left
				1.0f, 1.0f,0.0f,	//2 upper right
				1.0f, -1.0f,0.0f};	//3 lower right

		int[] indices = {
				0,3,2,
				2,1,0};

		float[] textureCoords = {
				0,0,0,
				0,1,0,
				1,1,0,
				1,0,0};

		Mesh mesh = new Mesh( GL_STATIC_DRAW );
		mesh.setAttribute( 0, positions, 3 );
		//	mesh.setAttribute( 1, normals, 3 );
		mesh.setAttribute( 2, textureCoords, 3 );
		mesh.setIndices( indices );

		quadMesh = mesh;
	}

	private void createMeshes() {
		loadObj("Meshes/dof_scene.obj");
		createQuad();
	}

	private void createLight() {
		light = new Light(new Vec3(0.0f,0.3f,2f), new Vec3(1,1,1),2000f,0.03f);
	}

	private void loadObj( String filename )	{
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

	public void onResize( int width, int height ) {
		windowWidth  = width;
		windowHeight = height;
	}

	private Mat4 createNormalMat(Mat4 modelMatrix) {
		return Mat4.inverse(modelMatrix).transpose();
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
}
