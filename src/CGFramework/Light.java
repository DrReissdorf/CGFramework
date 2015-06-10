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
    private float[] posArray;
    private int posCounter = 0;

    public Light(Vec3 position, Vec3 color, float range, float moveRadius, float movingSpeed) {
        this.position = position;
        this.color = color;
        this.range = range;
        posArray = createLightPosArray(moveRadius,movingSpeed);
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

    public float[] getPosArray() {
        return posArray;
    }

    public void setPosArray(float[] posArray) {
        this.posArray = posArray;
    }

    public int getPosCounter() {
        return posCounter;
    }

    public void setPosCounter(int posCounter) {
        this.posCounter = posCounter;
    }

    public void incPosCounter() {
        if(posCounter >= posArray.length-2) posCounter = 0;
        else posCounter +=2;
    }

    private float[] createLightPosArray(float radius, float movingSpeed) {
        float x = 0;
        float[] positions = new float[(int) (((Math.PI * 2) / movingSpeed) * 2) + 1];
        if (positions.length % 2 != 0) positions = new float[(int) (((Math.PI * 2) / movingSpeed) * 2) + 2];

        for (int i = 0; i < positions.length; i += 2) {
            positions[i] = 0 + (float) sin(x) * radius;
            positions[i + 1] = 0 + (float) cos(x) * radius;
            x += movingSpeed;
        }

        return positions;
    }

    public void updatePosition() {
        position = new Vec3(posArray[posCounter], position.y, posArray[posCounter+1]);
        incPosCounter();
    }
}
