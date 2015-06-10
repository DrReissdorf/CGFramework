package CGFramework.models;

import math.Vec3;
import util.Mesh;
import util.Texture;

/**
 *
 * @author Sven Riedel
 */
public class Model {
    private Mesh mesh;
    private Texture texture;
    private float shininess;
    private float reflectivity;
    private Vec3 position;
    private Vec3 color;

    public Model(Mesh mesh, Texture texture, Vec3 position, Vec3 color,float shininess, float reflectivity) {
        this.mesh = mesh;
        this.color = color;
        this.shininess = shininess;
        this.reflectivity = reflectivity;
        this.position = position;
        this.texture = texture;
    }

    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
    }

    public float getShininess() {
        return shininess;
    }

    public void setShininess(float shineDamper) {
        this.shininess = shineDamper;
    }

    public float getReflectivity() {
        return reflectivity;
    }

    public void setReflectivity(float reflectivity) {
        this.reflectivity = reflectivity;
    }

    public Vec3 getPosition() {
        return position;
    }

    public void setPosition(Vec3 position) {
        this.position = position;
    }

    public Vec3 getColor() {
        return color;
    }

    public void setColor(Vec3 color) {
        this.color = color;
    }
}
