package CGFramework.myself;

import CGFramework.*;
import math.Mat4;
import math.Vec3;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.EXTTextureSRGB;
import org.lwjgl.opengl.GL11;
import util.*;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;

public class Sandbox {
	private MyShaderProgram myShaderProgram;
	private MyShaderProgram myShaderProgramShadow;
	private MyShaderProgram postProcessShaderProgram;
    private ArrayList<Light> lightList = new ArrayList<>();
	private ArrayList<Mesh> meshes = new ArrayList<>();
    private ArrayList<Model> modelList = new ArrayList<>();
	private ArrayList<ModelTexture> modelTextureList = new ArrayList<>();
    private ArrayList<Entity> entityList = new ArrayList<>();

	private Vec3[] lightPositions;
	private Vec3[] lightColors;
	private float[] lightRanges;

	private Mat4            modelMatrix;
	private Mat4            viewMatrix;
	private int             windowWidth;
	private int             windowHeight;

	private int shadowFrameBuffer;
	private int shadowTextureID;
	private int shadowMapSize;
    private Texture shadowMapTexture;

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
        myShaderProgram = new MyShaderProgram( getPathForPackage() + "Color_vs.glsl", getPathForPackage() + "Color_fs.glsl" );
		postProcessShaderProgram = new MyShaderProgram( getPathForPackage() + "Postprocess_vs.glsl", getPathForPackage() + "Postprocess_fs.glsl" );
		myShaderProgramShadow = new MyShaderProgram( getPathForPackage() + "Shadowmap_vs.glsl", getPathForPackage() + "Shadowmap_fs.glsl" );

		modelMatrix   = new Mat4();
		viewMatrix    = Mat4.translation( 0.0f, 0.0f, -3.0f );
		meshes        = new ArrayList<>();

		createLights();
		createLightArrays(lightList);
		createMeshes();
		createTextures();
        createModels();
        createEntities();

		shadowMapSize = 1024;
		shadowTextureID = FrameBufferTextureFactory.setupShadowMapTextureBuffer(shadowMapSize, shadowMapSize);
		shadowMapTexture = new Texture(shadowTextureID);
		shadowFrameBuffer = FrameBufferFactory.setupShadowFrameBuffer(shadowTextureID);

		// To make texture resizable we need to assign it to 4k first, it just scales down
		postProcessTextureID = FrameBufferTextureFactory.setupPostProcessTextureBuffer(3840, 2160);
		postProcessTexture = new Texture(postProcessTextureID);
		postProcessFrameBuffer = FrameBufferFactory.setupPostProcessFrameBuffer(postProcessTextureID, 3840, 2160);
		resizeTexture(postProcessTextureID, width, height);

		glEnable(GL_DEPTH_TEST);
		glEnable(GL_CULL_FACE);
	}
	
	/**
	 * @param deltaTime The time in seconds between the last two frames
	 */
	public void update( float deltaTime ) {
		inputListener(deltaTime);
	}
	
	public void draw() {
		float fov       = 60.0f;
		float near      = 0.01f;
		float far       = 500.0f;
		float lightFov  = 90f;

	//	lightList.get(0).moveAroundCenter();
        createLightArrays(lightList);

		//SET UP VIEW AND PROJECTION MATRICES
		Mat4 projectionMatrix = Mat4.perspective( fov, windowWidth, windowHeight, near, far );
		Mat4 lightProjectionMatrix = Mat4.perspective(lightFov, shadowMapSize, shadowMapSize, 0.1f, lightList.get(0).getRange());
		Mat4 lightViewMatrix = Mat4.lookAt(lightPositions[0], new Vec3(), new Vec3(0, 1, 0));

		//SHADOW MAPPING RENDER
		renderShadowMap(lightViewMatrix, lightProjectionMatrix);

		//DRAW TO TEXTER
		this.drawToTexture(viewMatrix, projectionMatrix, lightViewMatrix, lightProjectionMatrix);

		//POST PROCESS THE TEXTURE
		drawTextureToScreen();
	}

	public void drawTextureToScreen() {
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glClearColor(0, 0, 0, 1);
		glViewport(0, 0, windowWidth, windowHeight);
		glCullFace(GL_BACK);

		postProcessShaderProgram.useProgram();
		postProcessShaderProgram.setUniform("uTexture", postProcessTexture);
		quadMesh.draw();
	}

	public void drawToTexture( Mat4 viewMatrix, Mat4 projMatrix, Mat4 lightViewMatrix, Mat4 lightProjectionMatrix ) {
		glBindFramebuffer(GL_FRAMEBUFFER, postProcessFrameBuffer);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glClearColor(0.5f, 0.7f, 1, 1);
		glViewport( 0, 0, windowWidth, windowHeight );
		glCullFace(GL_BACK);

		myShaderProgram.useProgram();
		myShaderProgram.setUniform( "uView",       viewMatrix );
		myShaderProgram.setUniform( "uProjection", projMatrix );

		myShaderProgram.setUniform( "uLightProjection", lightProjectionMatrix );
		myShaderProgram.setUniform( "uLightView", lightViewMatrix );

        myShaderProgram.setUniform("uInvertedUView",      new Mat4(viewMatrix).inverse() );
        myShaderProgram.setUniform("uNormalMat", createNormalMat(modelMatrix));

        myShaderProgram.setUniform("uLightPosArray", lightPositions);
        myShaderProgram.setUniform("uLightColorArray", lightColors);
        myShaderProgram.setUniform("uLightRange", lightRanges);

		myShaderProgram.setUniform("uShadowmap", shadowMapTexture);

		for( Entity entity : entityList) {
			myShaderProgram.setUniform("uModel", Transformation.createTransMat(modelMatrix, entity.getPosition(),1f));
			myShaderProgram.setUniform("uTexture", entity.getModel().getModelTexture().getTexture());
            myShaderProgram.setUniform("uShininess", entity.getModel().getModelTexture().getShininess());
            myShaderProgram.setUniform("uReflectivity", entity.getModel().getModelTexture().getReflectivity());
            entity.getModel().getMesh().draw(GL_TRIANGLES );
		}
	}

	private void renderShadowMap(Mat4 lightViewMatrix, Mat4 lightProjectionMatrix)  {
		glBindFramebuffer( GL_FRAMEBUFFER, shadowFrameBuffer);
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		glViewport( 0, 0, shadowMapSize, shadowMapSize );

		glCullFace(GL_BACK);
		myShaderProgramShadow.useProgram();
		myShaderProgramShadow.setUniform( "uView",       lightViewMatrix );
		myShaderProgramShadow.setUniform( "uProjection", lightProjectionMatrix );

		for( Entity entity : entityList) {
			myShaderProgramShadow.setUniform( "uModel", Transformation.createTransMat(modelMatrix, entity.getPosition(),1f));
			entity.getModel().getMesh().draw(GL_TRIANGLES );
		}
	}

	private void createMeshes() {
		loadObj("Meshes/monkey_scene.obj");
        loadObj("Meshes/dragon.obj");
		createQuad();
	}

	private void createTextures() {
		modelTextureList.add(new ModelTexture(new Texture("Textures/Stone.jpg"), 0.2f, 10));
        modelTextureList.add(new ModelTexture(new Texture("Textures/dragon.png"), 1f, 32));
	}

	private void createModels() {
        modelList.add(new Model(meshes.get(0), modelTextureList.get(0)));
        modelList.add(new Model(meshes.get(1), modelTextureList.get(1)));
    }

    private void createEntities() {
        entityList.add(new Entity(modelList.get(0), new Vec3(1,0,0)));
        entityList.add(new Entity(modelList.get(1), new Vec3(-1,0,-1)));
    }

	private void createLights() {
		lightList.add(new Light(new Vec3(3, 3, 3), new Vec3(1, 1, 1), 50f, 0.01f));
    }

	private void createLightArrays(List<Light> lightList) {
		lightPositions = new Vec3[lightList.size()];
		lightColors = new Vec3[lightList.size()];
		lightRanges = new float[lightList.size()];

		for(int i=0 ; i<lightList.size() ; i++) {
			lightPositions[i] = this.lightList.get(i).getPosition();
			lightColors[i]      = this.lightList.get(i).getColor();
			lightRanges[i]      = this.lightList.get(i).getRange();
		}

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
		mesh.setAttribute( 2, textureCoords, 3 );
		mesh.setIndices( indices );

		quadMesh = mesh;
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

		resizeTexture(postProcessTextureID, width, height);
	}

	private void resizeTexture(int textureID, int width, int height) {
		glBindTexture( GL_TEXTURE_2D, textureID );
		glTexImage2D( GL_TEXTURE_2D, 0, EXTTextureSRGB.GL_SRGB_EXT, width, height,
				0, GL_RGB, GL_FLOAT, (FloatBuffer)null );
		glBindTexture( GL_TEXTURE_2D, 0 );
	}

    private Mat4 createNormalMat(Mat4 modelMatrix) {
        return Mat4.inverse(modelMatrix).transpose();
    }

	private void inputListener(float deltaTime) {
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
