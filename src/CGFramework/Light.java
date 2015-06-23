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

    private float rotX;
    private float rotY;
    private float rotZ;

    float distanceToOrigin;
    private float circleMoveSpeed;
    private float circleMoveAngle = 0;
    private float circleMoveRadius;

    public Light(Vec3 position, Vec3 color, float range) {
        this.position = position;
        this.color = color;
        this.range = range;
    }

    public Light(Vec3 position, Vec3 color, float range, float circleMoveRadius, float circleMoveSpeed) {
        this.position = position;
        this.color = color;
        this.range = range;
        this.circleMoveSpeed = circleMoveSpeed;
        this.circleMoveRadius = circleMoveRadius;
        distanceToOrigin = Vec3.length( new Vec3().sub(position) );
        System.out.println("dist: "+distanceToOrigin);
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

    public void moveOnCircle() {
        position.x = 0 + (float)Math.sin(circleMoveAngle) * distanceToOrigin;
        position.z = 0 + (float)Math.cos(circleMoveAngle) * distanceToOrigin;

        circleMoveAngle += circleMoveSpeed;
    }
}
