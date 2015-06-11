package CGFramework.models;

import util.Texture;

/**
 * Created by S on 11.06.2015.
 */
public class ModelTexture {
    private Texture texture;
    private float shininess;
    private float reflectivity;

    public ModelTexture(Texture texture, float shininess, float reflectivity) {
        this.texture = texture;
        this.shininess = shininess;
        this.reflectivity = reflectivity;
    }

    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public float getReflectivity() {
        return reflectivity;
    }

    public void setReflectivity(float reflectivity) {
        this.reflectivity = reflectivity;
    }

    public float getShininess() {
        return shininess;
    }

    public void setShininess(float shininess) {
        this.shininess = shininess;
    }
}
