package CGFramework;

import math.Vec3;

/**
 * Created by svenmaster on 24.06.15.
 */
public class Entity {
    private Model model;

    private Vec3 position;

    private float rotX=0, rotY=0, rotZ=0;

    public Entity(Model model, Vec3 position) {
        this.model = model;
        this.position = position;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public Vec3 getPosition() {
        return position;
    }

    public void setPosition(Vec3 position) {
        this.position = position;
    }

    public void increaseRotation(float dx, float dy, float dz) {
        rotX += dx;
        rotY += dy;
        rotZ += dz;

        if(rotX >= 360) rotX -= 360;
        if(rotY >= 360) rotY -= 360;
        if(rotZ >= 360) rotZ -= 360;
    }
}
