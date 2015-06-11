package CGFramework.objectstorender;

import CGFramework.Light;
import CGFramework.MasterMeshRenderer;
import CGFramework.entities.Entity;
import CGFramework.meshgenerators.Sphere;
import CGFramework.meshgenerators.TerrainGen;
import CGFramework.models.Model;
import CGFramework.models.ModelTexture;
import CGFramework.models.RawMesh;
import math.Vec3;
import util.OBJContainer;
import util.OBJGroup;
import util.Texture;

import java.util.ArrayList;
import java.util.HashMap;

import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;

/**
 * Created by S on 11.06.2015.
 */
public class RenderObjects {
    private static ArrayList<RawMesh> meshesTriangles;
    private static ArrayList<Model> modelList;
    private static ArrayList<ModelTexture> modelTextureList;
    private static ArrayList<Entity> entityList;
    private static ArrayList<Light> lightsArray;
    private static HashMap<String, ModelTexture> modelTextureHashMap;
    private static Vec3[] lightPosArray;
    private static Vec3[] lightColorArray;
    private static float[] lightRange;

    public static void createEntities(MasterMeshRenderer masterMeshRenderer) {
        createLights();
        createMeshes();
        createTextures();

        entityList = new ArrayList<>();
        entityList.add(new Entity( new Model(meshesTriangles.get(2), modelTextureHashMap.get("stone")), new Vec3(-12f,0f,-6f), 0f, 0f, 0f, 1f ));
        entityList.add(new Entity( new Model(meshesTriangles.get(0), modelTextureHashMap.get("dragon")), new Vec3(2f,0f,-3f), 0f, 0f, 0f, 1f ));
        entityList.add(new Entity( new Model(meshesTriangles.get(1), modelTextureHashMap.get("stone")), new Vec3(0f,0f,-3f), 0f, 0f, 0f, 1f ));

        for(int i=0 ; i<lightsArray.size() ; i++) {
            Light light = lightsArray.get(i);
            entityList.add( new Entity(new Model( meshesTriangles.get(3), modelTextureHashMap.get("ground")), new Vec3(0f,0f,0f),0f,0f,0f,1f, light ) );
            lightPosArray[i] = light.getPosition();
            lightColorArray[i] = light.getColor();
            lightRange[i] = light.getRange();
        }

        for(Entity entity : entityList) {
            masterMeshRenderer.processEntity(entity);  // Entities an renderer geben
        }
    }

    private static void createLights() {
        lightsArray = new ArrayList<>();
        lightsArray.add(new Light(new Vec3(2, 3f, 2), new Vec3(1f, 1f, 1f),5,2f,0.02f));
        lightsArray.add(new Light(new Vec3(-2, 1, 2), new Vec3(1f, 1f, 1f),3f , 2f,0.04f));
        lightsArray.add(new Light(new Vec3(0, 1, -2), new Vec3(0f, 1f, 1f),3f ,2f,0.03f));
        lightsArray.add(new Light(new Vec3(2, 1, 2) , new Vec3(1f, 0f, 0f),3f   ,2f,0.05f));
        lightsArray.add(new Light(new Vec3(-2, 1, 2), new Vec3(1f, 0f, 1f),3f ,2f,0.06f));
        lightsArray.add(new Light(new Vec3(0, 1, -2), new Vec3(1f, 1f, 1f),3f ,2f,0.07f));

        lightPosArray = new Vec3[lightsArray.size()];
        lightColorArray = new Vec3[lightsArray.size()];
        lightRange = new float[lightsArray.size()];
    }



    private static void createMeshes() {
        meshesTriangles = new ArrayList<>();
        meshesTriangles.add(loadObjRaw("Meshes/dragon.obj"));
        meshesTriangles.add(loadObjRaw("Meshes/monkey_scene.obj"));
        meshesTriangles.add(TerrainGen.generateTerrain());
        meshesTriangles.add(Sphere.createMesh(0.1f, 15, 15));
    }

    private static void createTextures() {
        modelTextureList = new ArrayList<>();
        modelTextureHashMap = new HashMap<>();
        modelTextureHashMap.put("dragon",new ModelTexture(new Texture("Textures/dragon.png"),32,1f));
        modelTextureHashMap.put("ground",new ModelTexture(new Texture("Textures/ground.png"),32,1f));
        modelTextureHashMap.put("stone",new ModelTexture(new Texture("Textures/Stone.jpg"),32,1f));
        modelTextureHashMap.put("schachbrett",new ModelTexture(new Texture("Textures/schachbrett.jpg"),32,1f));
        modelTextureHashMap.put("woodplanks", new ModelTexture(new Texture("Textures/WoodPlanks.jpg"),32,1f));
    }

    private static RawMesh loadObjRaw(String filename ) {
        OBJContainer objContainer = OBJContainer.loadFile(filename);
        ArrayList<OBJGroup> objGroups = objContainer.getGroups();

        for (OBJGroup group : objGroups) {
            float[] positions = group.getPositions();
            float[] normals = group.getNormals();
            int[] indices = group.getIndices();
            float[] texturePositions = group.getTexCoords();

            RawMesh rawMesh = new RawMesh(GL_STATIC_DRAW);
            rawMesh.setAttribute(0, positions, 3);
            rawMesh.setAttribute(1, normals, 3);
            rawMesh.setAttribute(2, texturePositions, 3);
            rawMesh.setIndices(indices);

            return rawMesh;
        }
        return null;
    }

    public static ArrayList<RawMesh> getMeshesTriangles() {
        return meshesTriangles;
    }

    public static ArrayList<Model> getModelList() {
        return modelList;
    }

    public static ArrayList<ModelTexture> getModelTextureList() {
        return modelTextureList;
    }

    public static ArrayList<Entity> getEntityList() {
        return entityList;
    }

    public static ArrayList<Light> getLightsArray() {
        return lightsArray;
    }

    public static Vec3[] getLightPosArray() {
        return lightPosArray;
    }

    public static Vec3[] getLightColorArray() {
        return lightColorArray;
    }

    public static float[] getLightRange() {
        return lightRange;
    }

    public static void setLightPosArray(Vec3[] lightPosArray) {
        RenderObjects.lightPosArray = lightPosArray;
    }

    public static void setLightColorArray(Vec3[] lightColorArray) {
        RenderObjects.lightColorArray = lightColorArray;
    }

    public static void setLightRange(float[] lightRange) {
        RenderObjects.lightRange = lightRange;
    }
}
