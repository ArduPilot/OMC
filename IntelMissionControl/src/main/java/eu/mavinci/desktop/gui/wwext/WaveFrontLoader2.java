/*
 * This Source is licenced under the NASA OPEN SOURCE AGREEMENT VERSION 1.3
 *
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 *
 * Modifications by MAVinci GmbH, Germany (C) 2009-2016:
 * adapted from nasa sources to support own 3d model loadings
 */
package eu.mavinci.desktop.gui.wwext;

import com.intel.missioncontrol.PublishSource;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.desktop.main.debug.Debug;
import net.java.joglutils.model.ModelLoadException;
import net.java.joglutils.model.ResourceRetriever;
import net.java.joglutils.model.geometry.Bounds;
import net.java.joglutils.model.geometry.Face;
import net.java.joglutils.model.geometry.Material;
import net.java.joglutils.model.geometry.Mesh;
import net.java.joglutils.model.geometry.Model;
import net.java.joglutils.model.geometry.TexCoord;
import net.java.joglutils.model.geometry.Vec4;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/** @author RodgersGB restructured by Peter Schauß */
@PublishSource(module = "World Wind", licenses = "nasa-world-wind")
public class WaveFrontLoader2 {
    public static final String VERTEX_DATA = "v ";
    public static final String NORMAL_DATA = "vn ";
    public static final String TEXTURE_DATA = "vt ";
    public static final String FACE_DATA = "f ";
    public static final String SMOOTHING_GROUP = "s ";
    public static final String GROUP = "g ";
    public static final String OBJECT = "o ";
    public static final String COMMENT = "#";
    public static final String EMPTY = "";

    int vertexTotal = 0;
    int textureTotal = 0;
    int normalTotal = 0;

    // the model
    private Model model = null;
    /** Bounds of the model */
    private Bounds bounds = new Bounds();
    /** Center of the model */
    private Vec4 center = new Vec4(0.0f, 0.0f, 0.0f);

    private String baseDir = null;

    protected void init() {
        vertexTotal = 0;
        textureTotal = 0;
        normalTotal = 0;
        model = null;
        bounds = new Bounds();
        center = new Vec4(0.0f, 0.0f, 0.0f);
        baseDir = null;
    }

    /** Creates a new instance of myWaveFrontLoader */
    public WaveFrontLoader2() {}

    public void printVecArray(String name, Vector<Vec4> vs) {
        // System.out.println(name+":");
        // System.out.print("[");
        for (Vec4 v : vs) {
            System.out.format("v %.0f %.0f %.0f %.0f\n", v.x, v.y, v.z, v.w);
            // System.out.print("("+v[i].x+","+v[i].y+","+v[i].z+","+v[i].w+"),");
        }
        // System.out.print("[");
    }

    public void printFaceArray(String name, Vector<Face> fs) {
        // System.out.println(name+":");
        // System.out.print("[");
        for (Face f : fs) {
            System.out.format(
                "f %s %s %s\n",
                Arrays.toString(f.vertIndex), Arrays.toString(f.coordIndex), Arrays.toString(f.normalIndex));
            // System.out.print("("+v[i].x+","+v[i].y+","+v[i].z+","+v[i].w+"),");
        }
        // System.out.print("[");
    }

    /**
     * not group statement used
     *
     * @param br
     * @return
     * @throws ModelLoadException
     * @throws IOException
     */
    private Model load(BufferedReader br) throws ModelLoadException, IOException {
        Debug.getLog().fine("Loading mesh...");
        String line = null;
        Mesh mesh = new Mesh();
        Vector<Vec4> vertices = new Vector<Vec4>();
        Vector<TexCoord> texCoords = new Vector<TexCoord>();
        Vector<Vec4> normals = new Vector<Vec4>();
        Vector<Face> faces = new Vector<Face>();
        Set<String> groups = new HashSet<String>();
        Set<String> allGroups = new HashSet<String>();
        Map<String, Vector<Integer>> groupMap = new HashMap<String, Vector<Integer>>();
        Integer currentMaterial = -1;

        while ((line = br.readLine()) != null) {
            if (lineIs(COMMENT, line)) {
                // ignore comments
                numComments++;
                continue;
            } else if (line.length() == 0) {
                // ignore empty lines
                continue;
            } else if (lineIs(GROUP, line)) {
                groups = parseGroups(line);
                allGroups.addAll(groups);
            } else if (lineIs(OBJECT, line)) {
                groups = parseGroups(line); // handle object as group
                allGroups.addAll(groups);
            } else if (lineIs(VERTEX_DATA, line)) {
                Vec4 p = parsePoint(line);
                vertices.add(p);
                bounds.calc(p);
            } else if (lineIs(TEXTURE_DATA, line)) {
                String s[] = line.split("\\s+");
                TexCoord texCoord = new TexCoord();
                texCoord.u = Float.parseFloat(s[1]);
                texCoord.v = Float.parseFloat(s[2]);

                texCoords.add(texCoord);
            } else if (lineIs(NORMAL_DATA, line)) {
                normals.add(parsePoint(line));
            } else if (lineIs(FACE_DATA, line)) {
                Face f = parseFace(line);
                if (currentMaterial != -1) {
                    f.materialID = currentMaterial;
                }

                faces.add(f);
                for (String s : groups) {
                    if (groupMap.containsKey(s)) {
                        groupMap.get(s).add(faces.size() - 1);
                    } else {
                        Vector<Integer> l = new Vector<Integer>();
                        l.add(faces.size() - 1);
                        groupMap.put(s, l);
                    }
                }
            } else if (lineIs("mtllib ", line)) {
                processMaterialLib(line);
            } else if (lineIs("usemtl ", line)) {
                int materialId = processMaterialType(line, mesh);
                currentMaterial = materialId;
            }
        }

        mesh.faces = new Face[0];

        mesh.faces = faces.toArray(mesh.faces);
        mesh.numOfFaces = mesh.faces.length;

        mesh.normals = new Vec4[0];
        mesh.normals = normals.toArray(mesh.normals);
        mesh.vertices = new Vec4[0];
        mesh.vertices = vertices.toArray(mesh.vertices);
        mesh.numOfVerts = mesh.vertices.length;
        mesh.hasTexture = false;
        mesh.texCoords = new TexCoord[0];
        if (texCoords.size() > 0) {
            // mesh.hasTexture = true;
            mesh.texCoords = texCoords.toArray(mesh.texCoords);
        }

        mesh.numTexCoords = mesh.texCoords.length;
        mesh.bounds = bounds;

        if (Float.isInfinite(bounds.max.x) || Float.isNaN(bounds.max.x))
            throw new RuntimeException("bounds.max.x is out of bounds");
        if (Float.isInfinite(bounds.max.y) || Float.isNaN(bounds.max.y))
            throw new RuntimeException("bounds.max.y is out of bounds");
        if (Float.isInfinite(bounds.max.z) || Float.isNaN(bounds.max.z))
            throw new RuntimeException("bounds.max.z is out of bounds");

        if (Float.isNaN(bounds.min.x) || Float.isInfinite(bounds.min.x))
            throw new RuntimeException("bounds.min.x is out of bounds");
        if (Float.isNaN(bounds.min.y) || Float.isInfinite(bounds.min.y))
            throw new RuntimeException("bounds.min.y is out of bounds");
        if (Float.isNaN(bounds.min.z) || Float.isInfinite(bounds.min.z))
            throw new RuntimeException("bounds.min.z is out of bounds");

        // Calculate the center of the model
        center.x = 0.5f * (bounds.max.x + bounds.min.x);
        center.y = 0.5f * (bounds.max.y + bounds.min.y);
        center.z = 0.5f * (bounds.max.z + bounds.min.z);
        // mesh.hasTexture = false; // TOD remove
        model.addMesh(mesh);
        // mesh.hasTexture = true;
        Debug.getLog().fine("hasTexture: " + mesh.hasTexture);

        // System.out.println("mesh:"+mesh);
        // System.out.println("faces" + mesh.faces.length + " "+ Arrays.asList(mesh.faces));
        // System.out.println("normals" +mesh.normals.length + " "+ Arrays.asList(mesh.normals));
        // System.out.println("vertices" +mesh.vertices.length + " "+ Arrays.asList(mesh.vertices));
        // System.out.println("texCoords" + mesh.texCoords.length + " "+Arrays.asList(mesh.texCoords));

        // printVecArray("vertices",normals);
        // printFaceArray("faces",faces);
        Debug.getLog().fine(this.bounds.toString());
        model.setBounds(this.bounds);
        model.setCenterPoint(this.center);
        model.setUseLighting(true);
        model.setRenderModel(true);
        model.setRenderAsWireframe(false);
        model.setRenderModelBounds(false);
        model.setRenderObjectBounds(false);
        model.setUseTexture(true);
        model.setRenderPicker(false);
        model.centerModelOnPosition(true);
        // model.centerModelOnPosition(true); //do this only if it is meaningful for the current model

        return model;
    }

    private Set<String> parseGroups(String line) {
        final String s[] = line.split("\\s+");

        Set<String> set = new HashSet<String>();
        set.addAll(Arrays.asList(s));
        return set;
    }

    int numComments = 0;

    public synchronized Model load(String path) throws ModelLoadException {
        init();
        model = new Model(path);

        baseDir = "";
        String tokens[] = path.split("/");
        for (int i = 0; i < tokens.length - 1; i++) {
            baseDir += tokens[i] + "/";
        }

        try (InputStream stream = ResourceRetriever.getResourceAsInputStream(model.getSource());
             BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
            // Open a file handle and read the models data
            return load(br);
        } catch (IOException e) {
            throw new ModelLoadException("Caught IO exception: " + e);
        }

    }

    private boolean lineIs(String type, String line) {
        return line.startsWith(type);
    }

    private Face parseFace(String line) {
        String s[] = line.split("\\s+");
        if (line.contains("//")) { // Pattern is present if obj has no texture
            for (int loop = 1; loop < s.length; loop++) {
                s[loop] = s[loop].replaceAll("//", "/-1/"); // insert -1 for missing vt data
            }
        }

        Face face = new Face(s.length - 1);

        for (int loop = 1; loop < s.length; loop++) {
            String s1 = s[loop];
            String[] temp = s1.split("/");

            if (temp.length > 0) { // we have vertex data
                if (Integer.valueOf(temp[0]) < 0) {
                    // TODO handle relative vertex data
                } else {
                    face.vertIndex[loop - 1] = Integer.valueOf(temp[0]) - 1 - this.vertexTotal;
                    // System.out.println("found vertex index: " + face.vertIndex[loop-1]);
                }
            }

            if (temp.length > 1) { // we have texture data
                if (Integer.valueOf(temp[1]) < 0) {
                    face.coordIndex[loop - 1] = 0;
                } else {
                    face.coordIndex[loop - 1] = Integer.valueOf(temp[1]) - 1 - this.textureTotal;
                    // System.out.println("found texture index: " + face.coordIndex[loop-1]);
                }
            }

            if (temp.length > 2) { // we have normal data
                face.normalIndex[loop - 1] = Integer.valueOf(temp[2]) - 1 - this.normalTotal;
                // System.out.println("found normal index: " + face.normalIndex[loop-1]);
            }
        }

        return face;
    }

    private Vec4 parsePoint(String line) {
        Vec4 point = new Vec4();

        final String s[] = line.split("\\s+");

        point.x = Float.parseFloat(s[1]);
        point.y = Float.parseFloat(s[2]);
        point.z = Float.parseFloat(s[3]);

        return point;
    }

    private void processMaterialLib(String mtlData) {
        String s[] = mtlData.split("\\s+");

        // Material mat = new Material();
        Boolean bLoadMaterialFile = false;
        try (InputStream stream = ResourceRetriever.getResourceAsInputStream(baseDir + s[1])) {
            loadMaterialFile(stream);
            bLoadMaterialFile = true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (!bLoadMaterialFile) {
            try (InputStream stream2 = new FileInputStream(baseDir + s[1])) {
                loadMaterialFile(stream2);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private int processMaterialType(String line, Mesh mesh) {
        String s[] = line.split("\\s+");

        int materialID = -1;

        for (int i = 0; i < model.getNumberOfMaterials(); i++) {
            Material mat = model.getMaterial(i);

            if (mat.strName.equals(s[1])) {
                materialID = i;
                // System.out.println("material "+mat.strName+":"+i+" assigned to mesh");
                if (mat.strFile != null) {
                    mesh.hasTexture |= true;
                }

                break;
            }
        }

        return materialID;
        // mesh.materialID = materialID;
    }

    public Material loadMaterialFile(InputStream stream) {
        Material mat = null;
        int texId = 0;

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            String line;

            while ((line = br.readLine()) != null) {
                String parts[] = line.trim().split("\\s+");

                if (parts[0].equals("newmtl")) {
                    if (mat != null) {
                        model.addMaterial(mat);
                    }

                    mat = new Material();
                    mat.strName = parts[1];
                    // System.out.println("newmatl: "+mat.strName);
                    mat.textureId = texId;
                    texId++;
                } else if (parts[0].equals("Ks") && mat != null) {
                    mat.specularColor = parseColor(line);
                } else if (parts[0].equals("Ns")) {
                    if (parts.length > 1 && mat != null) {
                        mat.shininess = Float.valueOf(parts[1]);
                    }
                } else if (parts[0].equals("d")) {;
                } else if (parts[0].equals("illum")) {;
                } else if (parts[0].equals("Ka") && mat != null) {
                    mat.ambientColor = parseColor(line);
                    // System.out.println("ambient color: "+mat.ambientColor);
                } else if (parts[0].equals("Kd") && mat != null) {
                    mat.diffuseColor = parseColor(line);
                    // mat.ambientColor = mat.diffuseColor; //TODO remove
                    // System.out.println("diffuseColor: "+mat.diffuseColor);
                } else if (parts[0].equals("map_Kd")) {
                    if (parts.length > 1 && mat != null) {
                        mat.strFile = /* baseDir + */ parts[1];
                    }
                } else if (parts[0].equals("map_Ka")) {
                    if (parts.length > 1 && mat != null) {
                        mat.strFile = /* baseDir + */ parts[1];
                    }
                }
            }

            br.close();
            model.addMaterial(mat);
            // System.out.println("added material "+mat.strName+" to model");

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return mat;
    }

    private Color parseColor(String line) {
        String parts[] = line.trim().split("\\s+");

        Color color = new Color(Float.valueOf(parts[1]), Float.valueOf(parts[2]), Float.valueOf(parts[3]));

        return color;
    }

    // public static void main(String[] args) {
    // WaveFrontLoader2 loader = new WaveFrontLoader2();
    // try {
    // loader.load("C:\\Documents and Settings\\RodgersGB\\My
    // Documents\\Projects\\JOGLUTILS\\src\\net\\java\\joglutils\\examples\\models\\obj\\penguin.obj");
    // } catch (ModelLoadException ex) {
    // ex.printStackTrace();
    // }
    // }
}
