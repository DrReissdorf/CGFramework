package CGFramework;

import util.Texture;

/**
 * Created by S on 16.06.2015.
 */
public class ModelTexture {
    private Texture texture;
    private float reflectivity;
    private float shininess;

    public ModelTexture(Texture texture, float reflectivity, float shininess) {
        this.texture = texture;
        this.reflectivity = reflectivity;
        this.shininess = shininess;
    }

    public Texture getTexture() {
        return texture;
    }

    public float getReflectivity() {
        return reflectivity;
    }

    public float getShininess() {
        return shininess;
    }
}
