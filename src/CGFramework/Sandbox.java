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

import java.io.File;
import java.util.ArrayList;

import static org.lwjgl.util.glu.GLU.gluErrorString;

import CGFramework.Terrain.Terrain;
import CGFramework.render.*;
import CGFramework.render.model.*;
import math.Mat4;
import math.Vec3;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import CGFramework.shader.ShaderProgram;

public class Sandbox {
    private Mat4 modelMatrix, viewMatrix, rotationX, rotationY, projectionMatrix;
    private int windowWidth, windowHeight;
    private float deltaX, deltaY;
    private float rotationScale = 0.01f;
    public static float fov = 60.0f;
    private float near = 0.01f;
    private float far = 500.0f;
    private float cameraSpeed;

    private MasterMeshRenderer masterMeshRenderer;
    private boolean activateOrtho = false;

    private boolean vSync = true;

    /**
     * @param width  The horizontal window size in pixels
     * @param height The vertical window size in pixels
     */
    public Sandbox(int width, int height) {
        windowWidth = width;
        windowHeight = height;

        modelMatrix = new Mat4();
        viewMatrix = Mat4.translation(0.0f, 0.0f, -3.0f);

        masterMeshRenderer = new MasterMeshRenderer();
        initGL();

        ModelInitializer.createEntities(masterMeshRenderer);
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
        glCullFace(GL_BACK);

       // masterMeshRenderer.renderAllEntities(modelMatrix, viewMatrix, projMatrix);
        masterMeshRenderer.renderEntitiesOld(modelMatrix, viewMatrix, projMatrix);
    }

    private void initGL() {
        glEnable(GL_CULL_FACE);
      //  glClearDepth (1.0f);                                        // Depth Buffer Setup
      //  glDepthFunc (GL_LEQUAL);                                    // The Type Of Depth Testing (Less Or Equal)
        glEnable(GL_DEPTH_TEST);                                   // Enable Depth Testing
  //      glShadeModel (GL_SMOOTH);                                   // Select Smooth Shading
      //  glHint (GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);         // Set Perspective Calculations To Most Accurate
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
                    masterMeshRenderer.setMoveLights(!masterMeshRenderer.isMoveLights());
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
}
