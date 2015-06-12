package CGFramework.render;

import CGFramework.Transformation;
import CGFramework.render.model.Entity;
import CGFramework.render.model.Light;
import CGFramework.render.model.Model;
import math.Mat4;
import CGFramework.shader.ShaderProgram;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

/**
 * Created by S on 11.06.2015.
 */
public class MasterMeshRenderer {
    private final String pathToShader = "src/CGFramework/shader/";
    private final String vertexShaderName = "Color_vs.glsl";
    private final String fragmentShaderName = "Color_fs.glsl";

    private Map<Model, List<Entity>> entityMap = new HashMap<>();
    private int lightCnt;
    private ShaderProgram shaderProgram;
    private boolean moveLights = false;
    private Mat4 transMat;


    public MasterMeshRenderer() {
        shaderProgram = new ShaderProgram(pathToShader+vertexShaderName, pathToShader+fragmentShaderName);
        shaderProgram.useProgram();
    }

    public void renderAllEntities(Mat4 modelMatrix, Mat4 viewMatrix, Mat4 projMatrix) {
        lightCnt = 0;

        shaderProgram.setUniform("uView", viewMatrix);
        shaderProgram.setUniform("uProjection", projMatrix);
        shaderProgram.setUniform("uInvertedUView", new Mat4(viewMatrix).inverse());

        for(Model model : entityMap.keySet()) {
            glBindVertexArray( model.getRawMesh().getM_iVAOid() );
            shaderProgram.setUniform("uTexture", model.getModelTexture().getTexture());
            shaderProgram.setUniform("uShininess", model.getModelTexture().getShininess());
            shaderProgram.setUniform("uReflectivity", model.getModelTexture().getReflectivity());
            List<Entity> batch = entityMap.get(model);

            for(Entity entity : batch) {
                if(entity.getLight() != null) {
                    Light light = entity.getLight();
                    ModelInitializer.getLightPosArray()[lightCnt] = light.getPosition();
                    ModelInitializer.getLightColorArray()[lightCnt] = light.getColor();
                    ModelInitializer.getLightRange()[lightCnt] = light.getRange();
                    if(moveLights) light.updatePosition();
                    transMat = Transformation.createTransMat(modelMatrix, light.getPosition(), entity.getScale());
                    shaderProgram.setUniform("uModel", transMat);
                    shaderProgram.setUniform("uLightMat", modelMatrix);
                    shaderProgram.setUniform("uNormalMat", createNormalMat(transMat));
                    lightCnt++;
                } else {
                    transMat = Transformation.createTransMat(modelMatrix, entity.getPosition(), entity.getRotX(), entity.getRotY(), entity.getRotZ(),entity.getScale());
                    shaderProgram.setUniform("uModel",transMat);
                    shaderProgram.setUniform("uLightMat", modelMatrix);
                    shaderProgram.setUniform("uNormalMat", createNormalMat(transMat));
                }
                glDrawElements( GL_TRIANGLES, model.getRawMesh().getM_iNumIndices(), GL_UNSIGNED_INT, 0 );
            }


            glBindVertexArray( 0 );
        }
        shaderProgram.setUniform("uLightPosArray", ModelInitializer.getLightPosArray());
        shaderProgram.setUniform("uLightColorArray", ModelInitializer.getLightColorArray());
        shaderProgram.setUniform("uLightRange", ModelInitializer.getLightRange());
    }

    public void renderEntitiesOld(Mat4 modelMatrix, Mat4 viewMatrix, Mat4 projMatrix) {
        shaderProgram.setUniform("uView", viewMatrix);
        shaderProgram.setUniform("uProjection", projMatrix);
        shaderProgram.setUniform("uInvertedUView", new Mat4(viewMatrix).inverse());

        lightCnt = 0;
        for(Entity entity : ModelInitializer.getEntityList()) {

            if (entity.getLight() != null) {
                Light light = entity.getLight();
                ModelInitializer.getLightPosArray()[lightCnt] = light.getPosition();
                ModelInitializer.getLightColorArray()[lightCnt] = light.getColor();
                ModelInitializer.getLightRange()[lightCnt] = light.getRange();
                if (moveLights) light.updatePosition();
                transMat = Transformation.createTransMat(modelMatrix, light.getPosition(), entity.getScale());
                shaderProgram.setUniform("uModel", transMat);
                shaderProgram.setUniform("uLightMat", modelMatrix);
                shaderProgram.setUniform("uNormalMat", createNormalMat(transMat));
                shaderProgram.setUniform("uTexture", entity.getModel().getModelTexture().getTexture());
                shaderProgram.setUniform("uShininess", entity.getModel().getModelTexture().getShininess());
                shaderProgram.setUniform("uReflectivity", entity.getModel().getModelTexture().getReflectivity());
                lightCnt++;
                entity.getModel().getRawMesh().draw();
            } else {
                transMat = Transformation.createTransMat(modelMatrix, entity.getPosition(), entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale());
                shaderProgram.setUniform("uModel", transMat);
                shaderProgram.setUniform("uLightMat", modelMatrix);
                shaderProgram.setUniform("uNormalMat", createNormalMat(transMat));
                shaderProgram.setUniform("uTexture", entity.getModel().getModelTexture().getTexture());
                shaderProgram.setUniform("uShininess", entity.getModel().getModelTexture().getShininess());
                shaderProgram.setUniform("uReflectivity", entity.getModel().getModelTexture().getReflectivity());
                entity.getModel().getRawMesh().draw();
            }
        }

            shaderProgram.setUniform("uLightPosArray", ModelInitializer.getLightPosArray());
            shaderProgram.setUniform("uLightColorArray", ModelInitializer.getLightColorArray());
            shaderProgram.setUniform("uLightRange", ModelInitializer.getLightRange());
        }

    public void processEntity(Entity entity) {
        Model entityModel = entity.getModel();
        List<Entity> batch = entityMap.get(entityModel);
        if(batch!=null) {
            batch.add(entity);
        } else {
            List<Entity> newBatch = new ArrayList<>();
            newBatch.add(entity);
            entityMap.put(entityModel,newBatch);
        }
    }

    private static Mat4 createNormalMat(Mat4 modelMatrix) {
        return Mat4.inverse(modelMatrix).transpose();
    }

    public boolean isMoveLights() {
        return moveLights;
    }

    public void setMoveLights(boolean moveLights) {
        this.moveLights = moveLights;
    }
}
