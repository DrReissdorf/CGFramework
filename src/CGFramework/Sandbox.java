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

import java.io.File;
import java.util.ArrayList;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import math.Mat3;
import math.Mat4;
import math.Vec3;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
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
    public static String specularModel = "Phong-Model";
    Mat4 normalMat;
    private float[] light1PosArray,light2PosArray;
    private int light1PosCounter=0,light2PosCounter=0;
    private final float SHININESS_METAL = 20f;
    private boolean isPhong = true;
    private ArrayList<Model> lightModel;
    private Light light1;
    private Light light2;
    private ArrayList<Model> modelList;
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

        einheitsMatrix = new Mat4();
        modelMatrix = new Mat4();
        viewMatrix = Mat4.translation(0.0f, 0.0f, -3.0f);

        lightModel = new ArrayList<>();
        meshesTriangles = new ArrayList<Mesh>();
        modelList = new ArrayList<Model>();

        initLights();
        createMeshes();
        createModels();

        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
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

        shaderProgram.setUniform("light1Position", new Vec3(light1PosArray[light1PosCounter], light1.getPosition().y, light1PosArray[light1PosCounter+1]));
        shaderProgram.setUniform("light1Color", light1.getColor());
        shaderProgram.setUniform("light1Range", light1.getRange());

        shaderProgram.setUniform("light2Position", new Vec3(light2PosArray[light2PosCounter], light2.getPosition().y, light2PosArray[light2PosCounter+1]));
        shaderProgram.setUniform("light2Color", light2.getColor());
        shaderProgram.setUniform("light2Range", light2.getRange());

        if(isPhong) shaderProgram.setUniform("isPhong", 1);
        else shaderProgram.setUniform("isPhong", 0);

        normalMat = new Mat4(modelMatrix);
        normalMat.inverse();
        normalMat.transpose();
        shaderProgram.setUniform("normalMat", normalMat);

        glCullFace(GL_BACK);

        /* DRAW MONKEY */
        for(int i=0 ; i<modelList.size() ;i++) {
            Model model = modelList.get(i);
            shaderProgram.setUniform("modelColor", model.getColor());
            shaderProgram.setUniform("shininess", model.getShininess());
            shaderProgram.setUniform("reflectivity", model.getReflectivity());
            shaderProgram.setUniform("uModel", (modelMatrix));
            model.getMesh().draw();
        }

        /* DRAW SUN AND LIGHT*/
        for(int i=0 ; i<lightModel.size() ;i++) {
            Model model = lightModel.get(i);
            shaderProgram.setUniform("modelColor", model.getColor());
            shaderProgram.setUniform("shininess", model.getShininess());
            shaderProgram.setUniform("reflectivity", model.getReflectivity());
            if(model.equals(lightModel.get(0)))
            shaderProgram.setUniform("uModel", Transformation.createTransMat(modelMatrix, light1PosArray[light1PosCounter], model.getPosition().y, light1PosArray[light1PosCounter+1], 1f)); // new MAT4 because we dont want the spheres to move when we turn the monkey
            else shaderProgram.setUniform("uModel", Transformation.createTransMat(modelMatrix, light2PosArray[light2PosCounter], model.getPosition().y, light2PosArray[light2PosCounter + 1], 1f));
            model.getMesh().draw();
        }

        if(light1PosCounter >= light1PosArray.length-2) light1PosCounter = 0;
        else light1PosCounter+=2;

        if(light2PosCounter >= light2PosArray.length-2) light2PosCounter = 0;
        else light2PosCounter+=2;
    }

    private void createMeshes() {
        meshesTriangles.add(loadObj("Meshes/monkey_scene.obj"));
    }

    private void createModels() {
        //shininess metal ca.10-20
        modelList.add(new Model(meshesTriangles.get(0), new Vec3(0, 0, 0), ModelColor.silver(), 32, 0.7f));
        lightModel.add(new Model( Sphere.createMesh(0.2f, 30, 30),light1.getPosition(),Color.red(),75f,1f ) );
        lightModel.add(new Model( Sphere.createMesh(0.2f, 30, 30),light2.getPosition(),Color.green(),75f,1f ) );
    }

    private void initLights() {
        light1 = new Light(new Vec3(1, 1, 1), new Vec3(1f, 0f, 0f),3);
        light1PosArray = createLightPosArray(2f,0.01f);
        light2 = new Light(new Vec3(1, 1, 1), new Vec3(0f, 1f, 0f),3);
        light2PosArray = createLightPosArray(2f,0.02f);

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
                if (Keyboard.isKeyDown(Keyboard.KEY_P)) {
                    isPhong = !isPhong;
                    if(isPhong) specularModel = "Phong-Model";
                    else specularModel = "Blinn-Phong-Model";
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
            //normals = generateVertexNormals(positions, indices);

            Mesh mesh = new Mesh(GL_STATIC_DRAW);
            mesh.setAttribute(0, positions, 3);
            mesh.setAttribute(1, normals, 3);
            mesh.setIndices(indices);

            return mesh;
        }
        return null;
    }

    private float[] createLightPosArray(float radius, float movingSpeed) {
        float x = 0;
        float[] positions = new float[(int) (((Math.PI * 2) / movingSpeed) * 2) + 1];
        if (positions.length % 2 != 0) positions = new float[(int) (((Math.PI * 2) / movingSpeed) * 2) + 2];

        for (int i = 0; i < positions.length; i += 2) {
            positions[i] = 0 + (float) sin(x) * radius;
            positions[i + 1] = 0 + (float) cos(x) * radius;
            x += movingSpeed;
        }

        return positions;
    }

    public static float[] generateVertexNormals(float[] positions, int[] indices) {
        Face[] faces;
        Vec3[] vertexNormalsVectors = new Vec3[positions.length/3];
        float[] vertexNormals;

        for(int i=0; i<vertexNormalsVectors.length ; i++) {
            vertexNormalsVectors[i] = new Vec3();
        }

        faces = new Face[indices.length/3];

        System.out.println("faces size : "+faces.length);
        System.out.println("indices size : "+indices.length);
        System.out.println("positions size : "+positions.length);

        for(int i=0 ; i<indices.length ; i+=3) {
            faces[i/3] =  new Face(new Vec3(positions[ indices[i]*3 ], positions[ (indices[i]*3)+1 ], positions[ (indices[i]*3)+2 ]),
                                    new Vec3(positions[ indices[i+1]*3 ], positions[ (indices[i+1]*3)+1 ], positions[ (indices[i+1]*3)+2 ]),
                                    new Vec3(positions[ indices[i+2]*3 ], positions[ (indices[i+2]*3)+1 ], positions[ (indices[i+2]*3)+2 ]));

            faces[i/3].addToIndicesList(indices[i],indices[i+1],indices[i+2]);
        }

        for(int i=0 ; i<faces.length ; i++) {
            for(int j=0 ; j<3 ; j++) {
                vertexNormalsVectors[faces[i].getIndicesList().get(j)].add(faces[i].getNormal());
                vertexNormalsVectors[faces[i].getIndicesList().get(j)] = vertexNormalsVectors[faces[i].getIndicesList().get(j)].normalize();
            }
        }

        vertexNormals = new float[vertexNormalsVectors.length*3];

        for(int i=0 ; i<vertexNormalsVectors.length ; i++) {
            vertexNormals[i*3] = vertexNormalsVectors[i].x;
            vertexNormals[(i*3)+1] = vertexNormalsVectors[i].y;
            vertexNormals[(i*3)+2] = vertexNormalsVectors[i].z;
        }

        return vertexNormals;
    }
}
