package CGFramework.models;

import math.Vec3;
import util.Mesh;
import util.Texture;

/**
 *
 * @author Sven Riedel
 */
public class Model {
    private RawMesh rawMesh;
    private ModelTexture modelTexture;

    public Model(RawMesh rawMesh, ModelTexture modelTexture) {
        this.rawMesh = rawMesh;
        this.modelTexture = modelTexture;
    }

    public RawMesh getRawMesh() {
        return rawMesh;
    }

    public void setRawMesh(RawMesh rawMesh) {
        this.rawMesh = rawMesh;
    }

    public ModelTexture getModelTexture() {
        return modelTexture;
    }

    public void setModelTexture(ModelTexture modelTexture) {
        this.modelTexture = modelTexture;
    }
}
