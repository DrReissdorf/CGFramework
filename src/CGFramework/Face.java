package CGFramework;

import math.Vec3;

import java.util.ArrayList;

/**
 *
 * @author Sven Riedel
 */
public class Face {
    private Vec3 a, b, c, normal;
    private ArrayList<Vec3> indicesVectorList;
    private ArrayList<Integer> indicesList;
    
    public Face(Vec3 a, Vec3 b, Vec3 c) {
        this.a = a;
        this.b = b;
        this.c = c;
        normal = calcNormal(a,b,c);
        indicesVectorList = new ArrayList<>();
        indicesList = new ArrayList<>();

        indicesVectorList.add(a);
        indicesVectorList.add(b);
        indicesVectorList.add(c);
    }
    
    public Face(float v11, float v12, float v13, float v21, float v22, float v23, float v31, float v32, float v33) {
        this.a = new Vec3(v11,v12,v13);
        this.b = new Vec3(v21,v22,v23);
        this.c = new Vec3(v31,v32,v33);
        normal = calcNormal(a,b,c);
    }
    
    private Vec3 calcNormal(Vec3 v0, Vec3 v1, Vec3 v2) {
        Vec3 vecA = Vec3.sub(v1, v0);
        Vec3 vecB = Vec3.sub(v2, v0);

        return Vec3.normalize(Vec3.mul(Vec3.cross(vecA, vecB), 1/Vec3.length(Vec3.cross(vecA, vecB))));
    }

    public Vec3 getNormal() {
        return normal;
    }

    public void addToIndicesList(int indice1, int indice2, int indice3 ) {
        indicesList.add(indice1);
        indicesList.add(indice2);
        indicesList.add(indice3);
    }

    public ArrayList<Vec3> getIndicesVectorList() {
        return indicesVectorList;
    }

    public ArrayList<Integer> getIndicesList() {
        return indicesList;
    }
}
