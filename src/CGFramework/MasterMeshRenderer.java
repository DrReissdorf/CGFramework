package CGFramework;

import CGFramework.entities.Entity;
import CGFramework.models.Model;
import CGFramework.objectstorender.RenderObjects;
import math.Mat4;
import util.ShaderProgram;

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
    private Map<Model, List<Entity>> entityMap = new HashMap<>();
    private int lightCnt;

    public void renderAllEntities(ShaderProgram shaderProgram, ShaderProgAdd shaderProgAdd, Mat4 modelMatrix, Mat4 viewMatrix, Mat4 projMatrix) {
        lightCnt = 0;

        shaderProgram.useProgram();
        shaderProgram.setUniform("uView", viewMatrix);
        shaderProgram.setUniform("uProjection", projMatrix);
        shaderProgram.setUniform("uInvertedUView", new Mat4(viewMatrix).inverse());

        for(Model model : entityMap.keySet()) {
            glBindVertexArray( model.getRawMesh().getM_iVAOid() );
            List<Entity> batch = entityMap.get(model);
            for(Entity entity : batch) {
                if(entity.getLight() != null) {
                    Light light = entity.getLight();
                    RenderObjects.getLightPosArray()[lightCnt] = light.getPosition();
                    RenderObjects.getLightColorArray()[lightCnt] = light.getColor();
                    RenderObjects.getLightRange()[lightCnt] = light.getRange();
                    light.updatePosition();
                    Mat4 transMat = Transformation.createTransMat(modelMatrix, light.getPosition(), entity.getScale());
                    shaderProgram.setUniform("uModel", transMat);
                    shaderProgram.setUniform("uLightMat", modelMatrix);
                    shaderProgram.setUniform("uNormalMat", createNormalMat(transMat));
                    lightCnt++;
                } else {
                    Mat4 transMat = Transformation.createTransMat(modelMatrix, entity.getPosition(), entity.getScale());
                    shaderProgram.setUniform("uModel",transMat);
                    shaderProgram.setUniform("uLightMat", modelMatrix);
                    shaderProgram.setUniform("uNormalMat", createNormalMat(transMat));
                }

                shaderProgram.setUniform("uTexture", entity.getModel().getModelTexture().getTexture());
                shaderProgram.setUniform("uShininess", entity.getModel().getModelTexture().getShininess());
                shaderProgram.setUniform("uReflectivity", entity.getModel().getModelTexture().getReflectivity());

                glDrawElements( GL_TRIANGLES, entity.getModel().getRawMesh().getM_iNumIndices(), GL_UNSIGNED_INT, 0 );
            }
        }

        shaderProgram.setUniform("numberOfLights",RenderObjects.getLightPosArray().length);
        shaderProgAdd.setUniform("uLightPosArray", RenderObjects.getLightPosArray());
        shaderProgAdd.setUniform("uLightColorArray", RenderObjects.getLightColorArray());
        shaderProgAdd.setUniform("uLightRange", RenderObjects.getLightRange());

        glBindVertexArray( 0 );
    }

    private static Mat4 createNormalMat(Mat4 modelMatrix) {
        Mat4 normalMat;
        normalMat = new Mat4(modelMatrix);
        normalMat.inverse();
        return normalMat.transpose();
    }

    public void addModeltoMap(Model model) {
        entityMap.put(model, new ArrayList<>());
    }

    public void addToEntityMap(Entity entity) {
        entityMap.get(entity.getModel()).add(entity);
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

    private String getPathForPackage() {
        String locationOfSources = "src";
        String packageName = this.getClass().getPackage().getName();
        String path = locationOfSources + File.separator + packageName.replace(".", File.separator) + File.separator;
        return path;
    }
}
