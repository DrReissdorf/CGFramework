package CGFramework;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.Loader;
import math.Vec2;
import math.Vec3;
import util.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by S on 07.06.2015.
 */
public class OBJLoader {
    private static final int POSITION = 0;
    private static final int TEXCOORD = 1;
    private static final int NORMAL = 2;

    private ArrayList<OBJGroup> facegroups;


    private OBJLoader()
    {
        facegroups = new ArrayList<OBJGroup>();
    }


    public ArrayList<OBJGroup> getGroups()
    {
        return facegroups;
    }

    public static OBJGroup loadObjModel(String fileName) {
        FileReader fr = null;
        OBJLoader  loader = new OBJLoader();
        String path    = FileIO.pathOf(fileName);
        int    pathEnd = 1 + Math.max( path.lastIndexOf('\\'), path.lastIndexOf('/') );
        String folder  = path.substring( 0, pathEnd );

        HashMap<String, OBJMaterial> materials    = new HashMap<String, OBJMaterial>();
        OBJMaterial currentMaterial  = new OBJMaterial();
        String      currentGroupName = "default";
        materials.put(  currentGroupName, currentMaterial );

        try{
            fr = new FileReader(new File("resources/Meshes/"+fileName+".obj"));
        } catch (FileNotFoundException e) {
            System.err.println("Couldn't load file!");
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(fr);
        String line;
        List<Vec3> vertices = new ArrayList<>();
        List<Vec2> textures = new ArrayList<>();
        List<Vec3> normals = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        float[] verticesArray = null;
        float[] normalsArray = null;
        float[] textureArray = null;
        int[] indicesArray = null;
        try{
            while(true) {
                line = br.readLine();

                String[] currentLine = line.split(" ");
                if(line.startsWith("v ")) {
                    Vec3 vertex = new Vec3( Float.parseFloat(currentLine[1]),
                            Float.parseFloat(currentLine[2]),
                            Float.parseFloat((currentLine[3])));
                    vertices.add(vertex);
                } else if(line.startsWith("vt ")) {
                    Vec2 texture = new Vec2(Float.parseFloat(currentLine[1]), Float.parseFloat(currentLine[2]));
                    textures.add(texture);
                } else if(line.startsWith("vn ")) {
                    Vec3 normal = new Vec3( Float.parseFloat(currentLine[1]),
                            Float.parseFloat(currentLine[2]),
                            Float.parseFloat((currentLine[3])));
                    normals.add(normal);
                } else if(line.startsWith("f ")) {
                    textureArray = new float[vertices.size()*2];
                    normalsArray = new float[vertices.size()*3];
                    break;
                } else if( line.equalsIgnoreCase("mtllib") )
                {
                    materials = OBJMaterial.parseMTL( folder, currentLine[1] );
                }
                else if( line.equalsIgnoreCase("usemtl") )
                {
                    String      materialName = currentLine[1];
                    OBJMaterial tempMaterial = materials.get( materialName );

                    if( tempMaterial != null )
                        currentMaterial = tempMaterial;
                }
            }

            while(line != null) {
                if(!line.startsWith("f ")) {
                    line = br.readLine();
                    continue;
                }
                String[] currentLine = line.split(" ");
                String[] vertex1 = currentLine[1].split("/");
                String[] vertex2 = currentLine[2].split("/");
                String[] vertex3 = currentLine[3].split("/");

                processVertex(vertex1,indices,textures,normals,textureArray,normalsArray);
                processVertex(vertex2,indices,textures,normals,textureArray,normalsArray);
                processVertex(vertex3,indices,textures,normals,textureArray,normalsArray);
                line = br.readLine();
            }
            br.close();

        } catch(Exception e) {
            e.printStackTrace();
        }

        verticesArray = new float[vertices.size()*3];
        indicesArray = new int[indices.size()] ;

        int vertexPointer = 0;
        for(Vec3 vertex:vertices) {
            verticesArray[vertexPointer++] = vertex.x;
            verticesArray[vertexPointer++] = vertex.y;
            verticesArray[vertexPointer++] = vertex.z;
        }

        for(int i=0 ; i<indices.size() ; i++) {
            indicesArray[i] = indices.get(i);
        }
        return new OBJGroup(verticesArray,normalsArray,textureArray,indicesArray,null);
    }

    private static void processVertex(String[] vertexData, List<Integer> indices, List<Vec2> textures, List<Vec3> normals, float[] textureArray, float[] normalsArray) {
        int currentVertexPointer = Integer.parseInt(vertexData[0]) - 1;
        indices.add(currentVertexPointer);
        Vec2 currentTex = textures.get(Integer.parseInt(vertexData[1])-1);
        textureArray[currentVertexPointer*2] = currentTex.x;
        textureArray[currentVertexPointer*2+1] = 1 - currentTex.y;
        Vec3 currentNorm = normals.get(Integer.parseInt(vertexData[2])-1);
        normalsArray[currentVertexPointer*3] = currentNorm.x;
        normalsArray[currentVertexPointer*3+1] = currentNorm.y;
        normalsArray[currentVertexPointer*3+2] = currentNorm.z;
    }

    /*  public static Mesh loadObjModel(String fileName) {
        FileReader fr = null;
        try{
            fr = new FileReader(new File("resources/Meshes/"+fileName+".obj"));
        } catch (FileNotFoundException e) {
            System.err.println("Couldn't load file!");
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(fr);
        String line;
        List<Vec3> vertices = new ArrayList<>();
        List<Vec2> textures = new ArrayList<>();
        List<Vec3> normals = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        float[] verticesArray = null;
        float[] normalsArray = null;
        float[] textureArray = null;
        int[] indicesArray = null;
        try{
            while(true) {
                line = br.readLine();
                String[] currentLine = line.split(" ");
                if(line.startsWith("v ")) {
                    Vec3 vertex = new Vec3( Float.parseFloat(currentLine[1]),
                                            Float.parseFloat(currentLine[2]),
                                            Float.parseFloat((currentLine[3])));
                    vertices.add(vertex);
                } else if(line.startsWith("vt ")) {
                    Vec2 texture = new Vec2(Float.parseFloat(currentLine[1]), Float.parseFloat(currentLine[2]));
                    textures.add(texture);
                } else if(line.startsWith("vn ")) {
                    Vec3 normal = new Vec3( Float.parseFloat(currentLine[1]),
                                            Float.parseFloat(currentLine[2]),
                                            Float.parseFloat((currentLine[3])));
                    normals.add(normal);
                } else if(line.startsWith("f ")) {
                    textureArray = new float[vertices.size()*2];
                    normalsArray = new float[vertices.size()*3];
                    break;
                }
            }

            while(line != null) {
                if(!line.startsWith("f ")) {
                    line = br.readLine();
                    continue;
                }
                String[] currentLine = line.split(" ");
                String[] vertex1 = currentLine[1].split("/");
                String[] vertex2 = currentLine[2].split("/");
                String[] vertex3 = currentLine[3].split("/");

                processVertex(vertex1,indices,textures,normals,textureArray,normalsArray);
                processVertex(vertex2,indices,textures,normals,textureArray,normalsArray);
                processVertex(vertex3,indices,textures,normals,textureArray,normalsArray);
                line = br.readLine();
            }
            br.close();

        } catch(Exception e) {
            e.printStackTrace();
        }

        verticesArray = new float[vertices.size()*3];
        indicesArray = new int[indices.size()] ;

        int vertexPointer = 0;
        for(Vec3 vertex:vertices) {
            verticesArray[vertexPointer++] = vertex.x;
            verticesArray[vertexPointer++] = vertex.y;
            verticesArray[vertexPointer++] = vertex.z;
        }

        for(int i=0 ; i<indices.size() ; i++) {
            indicesArray[i] = indices.get(i);
        }
        return null;
    } */
}
