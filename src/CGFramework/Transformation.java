package CGFramework;

import math.Mat4;
import math.Vec3;

/**
 *
 * @author svenmaster
 */
public class Transformation {
    private static final Mat4 einheitsMatrix = new Mat4();
    
    public static Mat4 createTransMat(Mat4 modelMatrix, float posX, float posY, float posZ, float scale) {
        Mat4 matrix;
        matrix = Mat4.mul(Mat4.scale(scale), einheitsMatrix);
        matrix = Mat4.mul(Mat4.translation(posX, posY, posZ), matrix);        
        matrix = Mat4.mul(modelMatrix, matrix);       //modelmatrix gets updated to rotation in update() method        
        return matrix;
    }
    public static Mat4 createTransMat(Mat4 modelMatrix, Vec3 vec, float scale) {
        Mat4 matrix;
        matrix = Mat4.mul(Mat4.scale(scale), einheitsMatrix);
        matrix = Mat4.mul(Mat4.translation(vec.x, vec.y, vec.z), matrix);        
        matrix = Mat4.mul(modelMatrix, matrix);       //modelmatrix gets updated to rotation in update() method        
        return matrix;
    }
    public static Mat4 createTransMat(Mat4 modelMatrix, float posX, float posY, float posZ, String rotAxis, float radiant, float scale) {
        Mat4 matrix;
        matrix = Mat4.mul(Mat4.scale(scale), einheitsMatrix);
        matrix = Mat4.mul(Mat4.scale(1f, 0.5f, 1f), matrix);
        
        if(rotAxis.equals("x")) matrix = Mat4.mul(Mat4.rotation(new Vec3(1,0,0), radiant), matrix); 
        if(rotAxis.equals("y")) matrix = Mat4.mul(Mat4.rotation(new Vec3(0,1,0), radiant), matrix); 
        if(rotAxis.equals("z")) matrix = Mat4.mul(Mat4.rotation(new Vec3(0,0,1), radiant), matrix); 
        
        matrix = Mat4.mul(Mat4.translation(posX, posY, posZ), matrix);
        
        matrix = Mat4.mul(modelMatrix, matrix);       //modelmatrix gets updated to rotation in update() method        
        return matrix;
    }
    public static Mat4 createTransMat(Mat4 modelMatrix, Vec3 vec, String rotAxis, float radiant, float scale) {
        Mat4 matrix;
        matrix = Mat4.mul(Mat4.scale(scale), einheitsMatrix);
        matrix = Mat4.mul(Mat4.scale(1f, 0.5f, 1f), matrix);
        
        if(rotAxis.equals("x")) matrix = Mat4.mul(Mat4.rotation(new Vec3(1,0,0), radiant), matrix); 
        if(rotAxis.equals("y")) matrix = Mat4.mul(Mat4.rotation(new Vec3(0,1,0), radiant), matrix); 
        if(rotAxis.equals("z")) matrix = Mat4.mul(Mat4.rotation(new Vec3(0,0,1), radiant), matrix); 
        
        matrix = Mat4.mul(Mat4.translation(vec.x, vec.y, vec.z), matrix);
        
        matrix = Mat4.mul(modelMatrix, matrix);       //modelmatrix gets updated to rotation in update() method        
        return matrix;
    }
    public static Mat4 createTransMat(Mat4 modelMatrix, Vec3 vec, String rotAxis1, float radiant1, String rotAxis2, float radiant2, float scale) {
        Mat4 matrix;
        matrix = Mat4.mul(Mat4.scale(scale), einheitsMatrix);
        matrix = Mat4.mul(Mat4.scale(1f, 0.5f, 1f), matrix);
        
        if(rotAxis1.equals("x")) matrix = Mat4.mul(Mat4.rotation(new Vec3(1,0,0), radiant1), matrix); 
        if(rotAxis1.equals("y")) matrix = Mat4.mul(Mat4.rotation(new Vec3(0,1,0), radiant1), matrix); 
        if(rotAxis1.equals("z")) matrix = Mat4.mul(Mat4.rotation(new Vec3(0,0,1), radiant1), matrix); 
        
        if(rotAxis2.equals("x")) matrix = Mat4.mul(Mat4.rotation(new Vec3(1,0,0), radiant2), matrix); 
        if(rotAxis2.equals("y")) matrix = Mat4.mul(Mat4.rotation(new Vec3(0,1,0), radiant2), matrix); 
        if(rotAxis2.equals("z")) matrix = Mat4.mul(Mat4.rotation(new Vec3(0,0,1), radiant2), matrix); 
        
        matrix = Mat4.mul(Mat4.translation(vec.x, vec.y, vec.z), matrix);
        
        matrix = Mat4.mul(modelMatrix, matrix);       //modelmatrix gets updated to rotation in update() method        
        return matrix;
    }
}
