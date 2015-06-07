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
    Sun sun;
    private final float SHININESS_METAL = 20f;
    private boolean enableSpecular;
    private boolean enableSun;
    private ArrayList<Model> lightModel;
    private Light light;
    private ArrayList<Model> modelList;
    private final Mat4 einheitsMatrix;
    private Mat4 matrix, transformationMatrix;
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

        if(enableSun) {
            shaderProgram.setUniform("L_sun", sun.getLightDirection());
            shaderProgram.setUniform("sunlightColor", sun.getColor());
        } else {
            shaderProgram.setUniform("L_sun", new Vec3());
            shaderProgram.setUniform("sunlightColor", new Vec3(0f,0f,0f));
        }

        shaderProgram.setUniform("lightPosition", light.getPosition());
        shaderProgram.setUniform("lightColor", light.getColor());
        shaderProgram.setUniform("lightRange", light.getRange());

        if(enableSpecular) shaderProgram.setUniform("enableSpecular", 1);
        else shaderProgram.setUniform("enableSpecular", 0);

        glCullFace(GL_BACK);

        /* DRAW MONKEY */
        for(int i=0 ; i<modelList.size() ;i++) {
            Model model = modelList.get(i);
            shaderProgram.setUniform("modelColor", model.getColor());
            shaderProgram.setUniform("shininess", model.getShininess());
            shaderProgram.setUniform("reflectivity", model.getReflectivity());
            shaderProgram.setUniform("uModel", Transformation.createTransMat(modelMatrix, model.getPosition(), 1f));
            model.getMesh().draw();
        }

        /* DRAW SUN AND LIGHT*/
        for(int i=0 ; i<lightModel.size() ;i++) {
            Model model = lightModel.get(i);
            shaderProgram.setUniform("modelColor", model.getColor());
            shaderProgram.setUniform("shininess", model.getShininess());
            shaderProgram.setUniform("reflectivity", model.getReflectivity());
            shaderProgram.setUniform("uModel", Transformation.createTransMat(einheitsMatrix, model.getPosition().x, model.getPosition().y, model.getPosition().z, 1f)); // new MAT4 because we dont want the spheres to move when we turn the monkey
            model.getMesh().draw();
        }
    }

    private void createMeshes() {
       meshesTriangles.add(loadObj("Meshes/tank.obj"));
     //   meshesTriangles.add(loadObjNew("scene"));
        meshesTriangles.add(Sphere.createMesh(1f, 30, 30));
    }

    private void createModels() {
        //shininess metal ca.10-20
        modelList.add(new Model(meshesTriangles.get(0), new Vec3(0, 0, 0), ModelColor.silver(), 125, 1f));
      //  for( int i= -10 ; i<30 ; i+=5) modelList.add(new Model(meshesTriangles.get(0), new Vec3(-i, -0.1f, -i), ModelColor.silver(), 50, 0.5f));
        lightModel.add(new Model( Sphere.createMesh(1f, 30, 30),sun.getPosition(), Color.yellow(),75f,2f ) );
        lightModel.add(new Model( Sphere.createMesh(1f, 30, 30),light.getPosition(),Color.lightBlue(),75f,1f ) );
    }

    private void initLights() {
        sun = new Sun(new Vec3(20f,20f,20f),new Vec3(1f, 1f, 1f));
        light = new Light(new Vec3(-10, 5, -10), new Vec3(0.3f, 0.3f, 1.0f),30);
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
                if (Keyboard.isKeyDown(Keyboard.KEY_P)) {
                    activateOrtho = !activateOrtho;
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_1)) {
                    enableSpecular = !enableSpecular;
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_2)) {
                    enableSun = !enableSun;
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

    private Mesh loadObjNew(String filename ) {
        OBJGroup objGroup = OBJLoader.loadObjModel(filename);

        float[] positions = objGroup.getPositions();
        float[] normals = objGroup.getNormals();
        int[] indices = objGroup.getIndices();
        //normals = generateVertexNormals(positions, indices);

        Mesh mesh = new Mesh(GL_STATIC_DRAW);
        mesh.setAttribute(0, positions, 3);
        mesh.setAttribute(1, normals, 3);
        mesh.setIndices(indices);

        return mesh;
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
