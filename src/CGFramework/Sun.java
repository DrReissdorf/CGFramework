package CGFramework;

import math.Vec3;

/**
 * Created by S on 02.06.2015.
 */
public class Sun {
    private Vec3 position;
    private Vec3 lightDirection;
    private Vec3 color;

    public Sun(Vec3 position, Vec3 color) {
        this.position = position;
        this.color = color;
        lightDirection = Vec3.normalize(calcDirectionToZero(position));
    }

    private Vec3 calcDirectionToZero(Vec3 b) {
        return Vec3.normalize(b.sub(new Vec3()));
    }

    public Vec3 getColor() {
        return color;
    }

    public void setColor(Vec3 color) {
        this.color = color;
    }

    public Vec3 getPosition() {
        return position;
    }

    public void setPosition(Vec3 position) {
        this.position = position;
    }

    public Vec3 getLightDirection() {
        return lightDirection;
    }

    public void setLightDirection(Vec3 lightDirection) {
        this.lightDirection = lightDirection;
    }
}
