package CGFramework;

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
import static org.lwjgl.opengl.GL30.*;

import java.io.File;
import java.util.ArrayList;

import static org.lwjgl.util.glu.GLU.gluErrorString;

import CGFramework.meshgenerators.Sphere;
import CGFramework.models.Model;
import math.Mat4;
import math.Vec3;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import util.*;

public class Sandbox {
    private final ShaderProgram shaderProgram;
    private final ArrayList<Mesh> meshesTriangles;
    private Mat4 modelMatrix, viewMatrix, rotationX, rotationY, projectionMatrix;
    private int windowWidth, windowHeight;
    private float deltaX, deltaY;
    private float rotationScale = 0.01f;
    public static float fov = 60.0f;
    private float near = 0.01f;
    private float far = 500.0f;
    private float cameraSpeed;

    /**
     * **** Variablen fuer Praktikumsaufgaben *****
     */
    ShaderProgAdd shaderProgAdd;

    private Vec3[] lightPosArray;
    private Vec3[] lightColorArray;
    private float[] lightRange;
    private boolean moveLight= true;
    private ArrayList<Model> lightModel;
    private ArrayList<Light> lightsArray;
    private ArrayList<Model> modelList;
    private ArrayList<Texture> textureArray;
    private final Mat4 einheitsMatrix;
    private boolean activateOrtho = false;

    /**
     * ***********************
     */
    private boolean vSync = true;

    /**
     * @param width  The horizontal window size in pixels
     * @param height The vertical window size in pixels
     */
    public Sandbox(int width, int height) {
        windowWidth = width;
        windowHeight = height;
        // The shader program source files must be put into the same package as the Sandbox class file. This simplifies the 
        // handling in the lab exercise (i.e. for when uploading to Ilias or when correcting) since all code of one student
        // is kept in one package. In productive code the shaders would be put into the 'resource' directory.
        shaderProgram = new ShaderProgram(getPathForPackage() + "Color_vs.glsl", getPathForPackage() + "Color_fs.glsl");
        shaderProgAdd = new ShaderProgAdd(getPathForPackage() + "Color_vs.glsl", getPathForPackage() + "Color_fs.glsl");

        einheitsMatrix = new Mat4();
        modelMatrix = new Mat4();
        viewMatrix = Mat4.translation(0.0f, 0.0f, -3.0f);

        meshesTriangles = new ArrayList<Mesh>();
        modelList = new ArrayList<Model>();

        initGL();

        initLights();
        initTextures();
        createMeshes();
        createModels();
    }

    /**
     * @return The path to directory where the source file of this class is
     * located.
     */
    private String getPathForPackage() {
        String locationOfSources = "src";
        String packageName = this.getClass().getPackage().getName();
        String path = locationOfSources + File.separator + packageName.replace(".", File.separator) + File.separator;
        return path;
    }

    /**
     * @param deltaTime The time in seconds between the last two frames
     */
    public void update(float deltaTime) {
        int errorFlag = glGetError();
        if (errorFlag != GL_NO_ERROR) {
            System.err.println(gluErrorString(errorFlag));
        }
        cameraSpeed = 5.0f * deltaTime;
        inputListener();
    }

    public void draw() {   // runs after update         
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        // glClearColor(0.8f,0.8f,1,1); //hintergrund weiss

        if (activateOrtho) {
            projectionMatrix = Mat4.orthographic(-3f, 3f, 1.7f, -1.7f, near, far);
        } else {
            projectionMatrix = Mat4.perspective(fov, windowWidth, windowHeight, near, far);
        }

        glViewport(0, 0, windowWidth, windowHeight);

        this.drawMeshes(viewMatrix, projectionMatrix);
    }

    public void drawMeshes(Mat4 viewMatrix, Mat4 projMatrix) {  //runs in draw()
        shaderProgram.useProgram();

        shaderProgram.setUniform("uView", viewMatrix);
        shaderProgram.setUniform("uProjection", projMatrix);
        shaderProgram.setUniform("uInvertedUView", new Mat4(viewMatrix).inverse());
        shaderProgram.setUniform("uNormalMat", createNormalMat(modelMatrix));


        glCullFace(GL_BACK);
        renderLights();
        renderModels();
    }

    private void renderLights() {
        lightPosArray = new Vec3[lightsArray.size()];
        lightColorArray = new Vec3[lightsArray.size()];
        lightRange = new float[lightsArray.size()];

        for(int i=0 ; i<lightsArray.size() ; i++) {
            Light l = lightsArray.get(i);
            Model model = lightModel.get(i);
            if(moveLight) l.updatePosition();
            lightPosArray[i] = l.getPosition();
            lightColorArray[i] = l.getColor();
            lightRange[i] = l.getRange();
            shaderProgram.setUniform("uModel", Transformation.createTransMat(modelMatrix, l.getPosition(), 1f));
            model.getMesh().draw();
        }

        shaderProgAdd.setUniform("uLightPosArray", lightPosArray);
        shaderProgAdd.setUniform("uLightColorArray", lightColorArray);
        shaderProgAdd.setUniform("uLightRange", lightRange);
    }

    private void renderModels() {
        for(int i=0 ; i<modelList.size() ;i++) {
            Model model = modelList.get(i);
            shaderProgram.setUniform("uTexture", model.getTexture() );
            shaderProgram.setUniform("uModelColor", model.getColor());
            shaderProgram.setUniform("uShininess", model.getShininess());
            shaderProgram.setUniform("uReflectivity", model.getReflectivity());
            shaderProgram.setUniform("uModel", modelMatrix);
            model.getMesh().draw();
        }
    }

    private void createMeshes() {
        meshesTriangles.add(loadObj("Meshes/monkey_scene.obj"));
    }

    private void initTextures() {
        textureArray = new ArrayList<Texture>();
        textureArray.add(new Texture("Textures/dragon.png"));
        textureArray.add(new Texture("Textures/ground.png"));
        textureArray.add(new Texture("Textures/Stone.jpg"));
        textureArray.add(new Texture("Textures/schachbrett.jpg"));
    }

    private void createModels() {
        modelList.add(new Model(meshesTriangles.get(0), textureArray.get(2), new Vec3(0, 0, 0), ModelColor.silver(), 32, 1f));

        lightModel = new ArrayList<>();
        for(int i=0 ; i<lightsArray.size() ; i++) {
            Light l = lightsArray.get(i);
            lightModel.add(new Model( Sphere.createMesh(0.2f, 30, 30), textureArray.get(1), l.getPosition(),l.getColor(),75f,1f ) );
        }
    }

    private void initLights() {
        lightsArray = new ArrayList<>();

        lightsArray.add(new Light(new Vec3(2, 1f, 2), new Vec3(1f, 1f, 1f),5f   ,2f,0.02f));
        lightsArray.add(new Light(new Vec3(-2, 1, 2), new Vec3(0f, 1f, 0f),3f ,2f,0.04f));
        lightsArray.add(new Light(new Vec3(0, 1, -2), new Vec3(0f, 1f, 1f),3f ,2f,0.03f));
        lightsArray.add(new Light(new Vec3(2, 1, 2) , new Vec3(1f, 0f, 0f),3f   ,2f,0.05f));
        lightsArray.add(new Light(new Vec3(-2, 1, 2), new Vec3(1f, 0f, 1f),3f ,2f,0.06f));
        lightsArray.add(new Light(new Vec3(0, 1, -2), new Vec3(1f, 1f, 1f),3f ,2f,0.07f));
    }

    private void initGL() {
        glEnable(GL_CULL_FACE);
      //  glClearDepth (1.0f);                                        // Depth Buffer Setup
      //  glDepthFunc (GL_LEQUAL);                                    // The Type Of Depth Testing (Less Or Equal)
        glEnable (GL_DEPTH_TEST);                                   // Enable Depth Testing
      //  glShadeModel (GL_SMOOTH);                                   // Select Smooth Shading
      //  glHint (GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);         // Set Perspective Calculations To Most Accurate
    }

    private Mat4 createNormalMat(Mat4 modelMatrix) {
        Mat4 normalMat;
        normalMat = new Mat4(modelMatrix);
        normalMat.inverse();
        return normalMat.transpose();
    }

    private void inputListener() {
        if (Mouse.isButtonDown(1)) {
            deltaX = (float) Mouse.getDX();
            deltaY = (float) Mouse.getDY();
            rotationX = Mat4.rotation(Vec3.yAxis(), deltaX * rotationScale);
            rotationY = Mat4.rotation(Vec3.xAxis(), -deltaY * rotationScale);
            viewMatrix = rotationY.mul(rotationX).mul(viewMatrix);
        }

        if (Mouse.isButtonDown(0)) {
            deltaX = (float) Mouse.getDX();
            deltaY = (float) Mouse.getDY();
            rotationX = Mat4.rotation(Vec3.yAxis(), deltaX * rotationScale);
            rotationY = Mat4.rotation(Vec3.xAxis(), -deltaY * rotationScale);
            modelMatrix = rotationY.mul(rotationX).mul(modelMatrix);
        }

        while (Keyboard.next()) { // recognizes just one press, holding button still results in one pressfffff
            if (Keyboard.getEventKeyState()) {
                if (Keyboard.isKeyDown(Keyboard.KEY_V)) {
                    vSync = !vSync;
                    Display.setVSyncEnabled(vSync);
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_O)) {
                    activateOrtho = !activateOrtho;
                }
                if(Keyboard.isKeyDown(Keyboard.KEY_1)) {
                    moveLight = !moveLight;
                }
            }
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
            fov += 0.4f;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
            fov -= 0.4f;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                viewMatrix.mul(Mat4.translation(0.0f, 0.0f, cameraSpeed * 10));
            } else viewMatrix.mul(Mat4.translation(0.0f, 0.0f, cameraSpeed));
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                viewMatrix.mul(Mat4.translation(0.0f, 0.0f, -cameraSpeed * 10));
            } else viewMatrix.mul(Mat4.translation(0.0f, 0.0f, -cameraSpeed));
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
            viewMatrix.mul(Mat4.translation(cameraSpeed, 0.0f, 0.0f));
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
            viewMatrix.mul(Mat4.translation(-cameraSpeed, 0.0f, -0.0f));
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
            viewMatrix.mul(Mat4.translation(0.0f, -cameraSpeed, 0.0f));
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_C)) {
            viewMatrix.mul(Mat4.translation(0.0f, +cameraSpeed, 0.0f));
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            Main.exit();
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_F)) {
            Main.toggleFullscreen();
        }
    }

    public void onResize(int width, int height) {
        windowWidth = width;
        windowHeight = height;
    }

    /**
     * @param filename path to obj file
     * @return number of indices
     */
    private Mesh loadObj(String filename ) {
        OBJContainer objContainer = OBJContainer.loadFile(filename);
        ArrayList<OBJGroup> objGroups = objContainer.getGroups();

        for (OBJGroup group : objGroups) {
            float[] positions = group.getPositions();
            float[] normals = group.getNormals();
            int[] indices = group.getIndices();
            float[] texturePositions = group.getTexCoords();

            Mesh mesh = new Mesh(GL_STATIC_DRAW);
            mesh.setAttribute(0, positions, 3);
            mesh.setAttribute(1, normals, 3);
            mesh.setAttribute(2, texturePositions, 3);
            mesh.setIndices(indices);

            return mesh;
        }
        return null;
    }

}
