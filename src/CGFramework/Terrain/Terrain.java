package CGFramework.Terrain;

import CGFramework.render.model.ModelTexture;
import CGFramework.render.model.RawMesh;
import math.Vec3;

/**
 * Created by S on 11.06.2015.
 */
public class Terrain {
    public static final float SIZE = 10;

    private float x;
    private float z;
    private RawMesh rawMesh;
    private ModelTexture modelTexture;
    private Vec3 position;

    public Terrain(int gridX, int gridZ, ModelTexture texture, RawMesh rawMesh) {
        this.modelTexture = modelTexture;
        this.x = gridX * SIZE;
        this.z = gridZ * SIZE;
        this.rawMesh = rawMesh;
        this.position = new Vec3(-x/2,0,-z/2);
    }

    public float getX() {
        return x;
    }

    public float getZ() {
        return z;
    }

    public RawMesh getRawMesh() {
        return rawMesh;
    }

    public ModelTexture getModelTexture() {
        return modelTexture;
    }

    public Vec3 getPosition() {
        return position;
    }

    public void setPosition(Vec3 position) {
        this.position = position;
    }
}
