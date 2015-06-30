package CGFramework.myself;

import CGFramework.*;
import math.Mat4;
import math.Vec3;
import org.lwjgl.BufferUtils;
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
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL30.*;

import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;

public class Sandbox {
	private CGFramework.MyShaderProgram myShaderProgram;
	private CGFramework.MyShaderProgram myShaderProgramShadow;
	private CGFramework.MyShaderProgram postProcessShaderProgram;
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
        myShaderProgram = new CGFramework.MyShaderProgram( getPathForPackage() + "Color_vs.glsl", getPathForPackage() + "Color_fs.glsl" );
		postProcessShaderProgram = new CGFramework.MyShaderProgram( getPathForPackage() + "Postprocess_vs.glsl", getPathForPackage() + "Postprocess_fs.glsl" );

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
		setupShadowMap(shadowMapSize);
        shadowMapTexture = new Texture(shadowTextureID);

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
		inputListener(deltaTime);
	}
	
	public void draw() {
		float fov       = 60.0f;
		float near      = 0.01f;
		float far       = 500.0f;
		float lightFov  = 90f;

		lightList.get(0).moveAroundCenter();
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
		drawMeshes();
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
		drawMeshesShadow(lightViewMatrix, lightProjectionMatrix);
	}

	public void drawMeshesShadow ( Mat4 lightViewMatrix, Mat4 lightProjectionMatrix ) {
		glCullFace(GL_BACK);
		myShaderProgramShadow.useProgram();
		myShaderProgramShadow.setUniform( "uView",       lightViewMatrix );
		myShaderProgramShadow.setUniform( "uProjection", lightProjectionMatrix );

		for( Entity entity : entityList) {
			myShaderProgramShadow.setUniform( "uModel", Transformation.createTransMat(modelMatrix, entity.getPosition(),1f));
			entity.getModel().getMesh().draw(GL_TRIANGLES );
		}
	}

	private int createTextureBuffer(int width, int height) {
		int texbuf = glGenTextures();
		glBindTexture( GL_TEXTURE_2D, texbuf );
		glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
		glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
		glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR );
		glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR );
		glTexImage2D( GL_TEXTURE_2D, 0, GL_RGB16F, width, height,
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
		glEnable(GL_FRAMEBUFFER_SRGB);
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

	private void setupShadowMap( int shadowMapSize ) {
		myShaderProgramShadow = new CGFramework.MyShaderProgram( getPathForPackage() + "Shadowmap_vs.glsl", getPathForPackage() + "Shadowmap_fs.glsl" );

		// Create Texture
		shadowTextureID = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, shadowTextureID);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT24, shadowMapSize, shadowMapSize,
				0, GL_DEPTH_COMPONENT, GL_FLOAT, (FloatBuffer) null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);

		//Create floatbuffer to represent whitecolor for border_color
		glTexParameter(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, createFloatBuffer(new float[]{1,1,1,1}));

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
		glBindTexture( GL_TEXTURE_2D, postProcessTextureID );
		glTexImage2D( GL_TEXTURE_2D, 0, EXTTextureSRGB.GL_SRGB_EXT, windowWidth, windowHeight,
				0, GL_RGB, GL_FLOAT, (ByteBuffer)null );
		glBindTexture( GL_TEXTURE_2D, 0 );

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
