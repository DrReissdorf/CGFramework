package CGFramework;

import math.Vec3;

/**
 *
 * @author Sven Riedel
 */
public class Light {
    private Vec3 position;
    private Vec3 color;
    private float range;

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
}
