package CGFramework;

import util.Mesh;

/**
 * Created by S on 16.06.2015.
 */
public class Model {
    private Mesh mesh;
    private ModelTexture modelTexture;

    public Model(Mesh mesh, ModelTexture modelTexture) {
        this.mesh = mesh;
        this.modelTexture = modelTexture;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public ModelTexture getModelTexture() {
        return modelTexture;
    }
}
