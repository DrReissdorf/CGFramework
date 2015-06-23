package CGFramework;

import math.Vec3;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 *
 * @author Sven Riedel
 */
public class Light {
    private Vec3 position;
    private Vec3 color;
    private float range;

    private float rotX;
    private float rotY;
    private float rotZ;

    public Light(Vec3 position, Vec3 color, float range) {
        this.position = position;
        this.color = color;
        this.range = range;
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

    public float getRange() {
        return range;
    }

    public void setRange(float range) {
        this.range = range;
    }

    public float getRotX() {
        return rotX;
    }

    public void setRotX(float rotX) {
        this.rotX = rotX;
    }

    public float getRotY() {
        return rotY;
    }

    public void setRotY(float rotY) {
        this.rotY = rotY;
    }

    public float getRotZ() {
        return rotZ;
    }

    public void setRotZ(float rotZ) {
        this.rotZ = rotZ;
    }
}
