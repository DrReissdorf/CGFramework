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
import math.Mat4;
import math.Vec3;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import util.*;

import java.io.File;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL30.*;

import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;

public class Sandbox {
	private ShaderProgram ShaderProgram;
	private ShaderProgram ShaderProgramShadow;
    private ArrayList<Light> lights = new ArrayList<>();
	private ArrayList<Mesh> meshes = new ArrayList<>();
    private ArrayList<Model> models = new ArrayList<>();
	private ArrayList<ModelTexture> modelTextures = new ArrayList<>();

	private Light light;

	private Mat4            modelMatrix;
	private Mat4            viewMatrix;
	private int             windowWidth;
	private int             windowHeight;

	private int shadowFrameBuffer;
	private int shadowTextureID;
	private int shadowMapSize;
    private Texture shadowMapTexture;
	
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
        ShaderProgram = new ShaderProgram( getPathForPackage() + "Color_vs.glsl", getPathForPackage() + "Color_fs.glsl" );

		modelMatrix   = new Mat4();
		viewMatrix    = Mat4.translation( 0.0f, 0.0f, -3.0f );
		meshes        = new ArrayList<>();

		createLight();
		createMeshes();
		createTextures();
        createModels();

		shadowMapSize = 1024;
		setupShadowMap(shadowMapSize);
        shadowMapTexture = new Texture(shadowTextureID);

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

		light.moveAroundCenter();
		//SET UP VIEW AND PROJECTION MATRICES
		Mat4 projectionMatrix = Mat4.perspective( fov, windowWidth, windowHeight, near, far );
		Mat4 lightProjectionMatrix = Mat4.perspective(lightFov, shadowMapSize, shadowMapSize, 0.1f, light.getRange());
		Mat4 lightViewMatrix = Mat4.lookAt(light.getPosition(), new Vec3(), new Vec3(0, 1, 0));

		//SHADOW MAPPING RENDER
		renderShadowMap(lightViewMatrix, lightProjectionMatrix);

		//NORMAL RENDER
		this.drawMeshes( viewMatrix, projectionMatrix, lightViewMatrix, lightProjectionMatrix );
	}	
	
	public void drawMeshes( Mat4 viewMatrix, Mat4 projMatrix, Mat4 lightViewMatrix, Mat4 lightProjectionMatrix ) {
		glCullFace(GL_BACK);
		ShaderProgram.useProgram();
		ShaderProgram.setUniform( "uModel",      modelMatrix );
		ShaderProgram.setUniform( "uView",       viewMatrix );
		ShaderProgram.setUniform( "uProjection", projMatrix );

		ShaderProgram.setUniform( "uLightProjection", lightProjectionMatrix );
		ShaderProgram.setUniform( "uLightView", lightViewMatrix );

        ShaderProgram.setUniform("uInvertedUView",      new Mat4(viewMatrix).inverse() );
        ShaderProgram.setUniform("uNormalMat", createNormalMat(modelMatrix));

        ShaderProgram.setUniform("uLightPos", light.getPosition());
        ShaderProgram.setUniform("uLightColor", light.getColor());
        ShaderProgram.setUniform("uLightRange", light.getRange());

		ShaderProgram.setUniform("uShadowmap", shadowMapTexture);

		for( Model model : models ) {
            ShaderProgram.setUniform("uShininess", model.getModelTexture().getShininess());
            ShaderProgram.setUniform("uReflectivity", model.getModelTexture().getReflectivity());
            model.getMesh().draw(GL_TRIANGLES );
		}
	}

	private void renderShadowMap(Mat4 lightViewMatrix, Mat4 lightProjectionMatrix)  {
		glBindFramebuffer( GL_FRAMEBUFFER, shadowFrameBuffer);
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		glViewport( 0, 0, shadowMapSize, shadowMapSize );
		drawMeshesShadow(lightViewMatrix, lightProjectionMatrix);

		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glClearColor(0, 0, 0, 1);
		glViewport( 0, 0, windowWidth, windowHeight );
	}

	public void drawMeshesShadow ( Mat4 lightViewMatrix, Mat4 lightProjectionMatrix ) {
		glCullFace(GL_BACK);
		ShaderProgramShadow.useProgram();
		ShaderProgramShadow.setUniform( "uView",       lightViewMatrix );
		ShaderProgramShadow.setUniform( "uProjection", lightProjectionMatrix );

		for( Model model : models ) {
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
		light = new Light(new Vec3(3,3,3), new Vec3(1,1,1),15f,0.03f);
    }

	private void setupShadowMap( int shadowMapSize ) {
		ShaderProgramShadow = new ShaderProgram( getPathForPackage() + "Shadowmap_vs.glsl", getPathForPackage() + "Shadowmap_fs.glsl" );

		// Create Texture
		shadowTextureID = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, shadowTextureID);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT24, shadowMapSize, shadowMapSize,
				0, GL_DEPTH_COMPONENT, GL_FLOAT, (FloatBuffer) null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);

		//Create floatbuffer to represent whitecolor for border_color
		float[] whiteColor = {1,1,1,1};
		glTexParameter(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, createFloatBuffer(whiteColor));

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
		glBindTexture(GL_TEXTURE_2D, 0);

		//Create Framebuffer
		shadowFrameBuffer = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, shadowFrameBuffer);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, shadowTextureID, 0);
		glReadBuffer(GL_NONE);
		glDrawBuffer(GL_NONE);

		int err = glCheckFramebufferStatus(GL_FRAMEBUFFER);
		if( err != GL_FRAMEBUFFER_COMPLETE) {
			System.out.println("Frame buffer is not complete. Error: " + err);
			System.exit(-1);
		}

		glBindFramebuffer(GL_FRAMEBUFFER, 0);
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
	
	public void onResize( int width, int height ) {
		windowWidth  = width;
		windowHeight = height;
	}

    private FloatBuffer createFloatBuffer(float[] floats) {
        FloatBuffer fb = BufferUtils.createFloatBuffer(floats.length);
        fb.put(floats);
        fb.flip();
        return fb;
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
