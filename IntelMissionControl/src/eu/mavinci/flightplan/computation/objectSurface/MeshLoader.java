/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.computation.objectSurface;

import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.ogc.collada.ColladaAbstractGeometry;
import gov.nasa.worldwind.ogc.collada.ColladaGeometry;
import gov.nasa.worldwind.ogc.collada.ColladaInput;
import gov.nasa.worldwind.ogc.collada.ColladaInstanceGeometry;
import gov.nasa.worldwind.ogc.collada.ColladaInstanceVisualScene;
import gov.nasa.worldwind.ogc.collada.ColladaMesh;
import gov.nasa.worldwind.ogc.collada.ColladaNode;
import gov.nasa.worldwind.ogc.collada.ColladaP;
import gov.nasa.worldwind.ogc.collada.ColladaRoot;
import gov.nasa.worldwind.ogc.collada.ColladaScene;
import gov.nasa.worldwind.ogc.collada.ColladaTriangles;
import gov.nasa.worldwind.ogc.collada.ColladaVisualScene;
import gov.nasa.worldwind.ogc.kml.KMLAbstractFeature;
import gov.nasa.worldwind.ogc.kml.KMLAbstractGeometry;
import gov.nasa.worldwind.ogc.kml.KMLDocument;
import gov.nasa.worldwind.ogc.kml.KMLLink;
import gov.nasa.worldwind.ogc.kml.KMLModel;
import gov.nasa.worldwind.ogc.kml.KMLPlacemark;
import gov.nasa.worldwind.ogc.kml.KMLRoot;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.xml.stream.XMLStreamException;
import net.java.joglutils.model.ModelFactory;
import net.java.joglutils.model.ModelLoadException;
import net.java.joglutils.model.geometry.Face;
import net.java.joglutils.model.geometry.Mesh;
import net.java.joglutils.model.geometry.Model;
import net.java.joglutils.model.geometry.TexCoord;

public class MeshLoader {

    public static final double epsilon = 1e-12;

    public static MMesh loadMesh(File file) throws ModelLoadException, IOException {
        Vector<MTriangle> triangles = null;

        MMesh mesh;
        Model model;

        switch (getFileExtension(file).toLowerCase()) {
        case "kml":
        case "kmz":
            mesh = loadFirstColladaModelFromKml(file);
            model = mesh.model;
            break;
        case "dae":
            model = loadColladaModel(file);
            mesh = new MMesh();
            mesh.model = model;
            break;
        case "obj":
            model = ModelFactory.createModel(file.getAbsolutePath());
            mesh = new MMesh();
            mesh.model = model;
            break;
        default:
            throw new ModelLoadException("Unsupported mesh format.");
        }
        // create triangle list out of all meshes in the dataset
        triangles = new Vector<>();
        for (int m = 0; m != model.getNumberOfMeshes(); m++) {
            Vector<MTriangle> trianglesSub = new Vector<>();

            Mesh meshA = model.getMesh(m);
            Vec4[] vecs = new Vec4[meshA.vertices.length];
            for (int i = 0; i != vecs.length; i++) {
                net.java.joglutils.model.geometry.Vec4 a0 = meshA.vertices[i];
                Vec4 a =
                    new Vec4(
                        a0.x, a0.z,
                        a0.y); // a0.w); //loading the w component break our alginment in some cases, since the value is
                               // 0 in the OBJ
                vecs[i] = a;
            }

            for (Face face : meshA.faces) {
                Vec4 a = vecs[face.vertIndex[0]];
                Vec4 b = vecs[face.vertIndex[1]];
                Vec4 c = vecs[face.vertIndex[2]];
                MTriangle triangle = new MTriangle(a, b, c);
                trianglesSub.addElement(triangle);
            }

            triangles.addAll(trianglesSub);
        }

        mesh.setTriangles(triangles);
        return mesh;
    }

    public static MMesh loadFirstColladaModelFromKml(File kmlFile) throws IOException, ModelLoadException {
        MMesh mesh = new MMesh();
        KMLRoot kmlRoot = KMLRoot.create(kmlFile);
        try {
            kmlRoot.parse();
        } catch (XMLStreamException e) {
            throw new ModelLoadException(e.getMessage());
        }

        KMLDocument document = (KMLDocument)kmlRoot.getFeature();
        KMLPlacemark placemark = null;

        for (KMLAbstractFeature feature : document.getFeatures()) {
            if (feature instanceof KMLPlacemark) {
                placemark = (KMLPlacemark)feature;
                break;
            }
        }

        if (placemark == null) {
            throw new ModelLoadException("KML: no placemark element found.");
        }

        KMLModel model = null;
        KMLAbstractGeometry kmlGeometry = placemark.getGeometry();
        if (kmlGeometry instanceof KMLModel) {
            model = (KMLModel)kmlGeometry;
        }

        if (model == null) {
            throw new ModelLoadException("KML: no model element found.");
        }

        mesh.origin = model.getLocation().getPosition();
        mesh.heading = model.getOrientation().getHeading();
        mesh.roll = model.getOrientation().getRoll();
        mesh.tilt = model.getOrientation().getTilt();

        String colladaFileName = null;
        KMLLink link = model.getLink();
        if (link != null) {
            colladaFileName = link.getHref();
        }

        if (colladaFileName == null) {
            throw new ModelLoadException("KML: no model link element found.");
        }

        mesh.model = loadColladaModel(new File(kmlFile.getParentFile(), colladaFileName));
        return mesh;
    }

    public static Model loadColladaModel(File colladaFile) throws IOException, ModelLoadException {
        ColladaRoot colladaRoot;
        try {
            colladaRoot = ColladaRoot.createAndParse(colladaFile);
        } catch (XMLStreamException e) {
            throw new ModelLoadException(e.getMessage());
        }

        if (colladaRoot == null) {
            throw new ModelLoadException("Collada: source type not supported.");
        }

        ColladaScene scene = colladaRoot.getScene();
        if (scene == null) {
            throw new ModelLoadException("Collada: no scene element found.");
        }

        ColladaInstanceVisualScene instanceVisualScene =
            (ColladaInstanceVisualScene)scene.getField("instance_visual_scene");
        if (instanceVisualScene == null) {
            throw new ModelLoadException("Collada: no instance_visual_scene element found.");
        }

        ColladaVisualScene visualScene = instanceVisualScene.get();
        if (visualScene == null) {
            throw new ModelLoadException("Collada: cannot resolve instance_visual_scene.");
        }

        List<ColladaGeometry> colladaGeometries = new ArrayList<ColladaGeometry>();
        List<Matrix> colladaMatrices = new ArrayList<Matrix>();
        for (ColladaNode node : visualScene.getNodes()) {
            for (ColladaInstanceGeometry instanceGeometry : node.getGeometries()) {
                colladaMatrices.add(node.getMatrix());
                colladaGeometries.add(instanceGeometry.get());
            }
        }

        if (colladaGeometries.isEmpty()) {
            throw new ModelLoadException("Collada: scene contains no geometries.");
        }

        Model model = new Model(null);

        for (int geometryIndex = 0; geometryIndex < colladaGeometries.size(); ++geometryIndex) {
            Matrix currentTransform = colladaMatrices.get(geometryIndex);
            ColladaMesh colladaMesh = colladaGeometries.get(geometryIndex).getMesh();
            if (colladaMesh == null) {
                continue;
            }

            Mesh mesh = new Mesh();
            for (ColladaTriangles triangles : colladaMesh.getTriangles()) {
                final int baseVertexOffset = addVerticesToMesh(mesh, triangles, currentTransform);
                final int baseTexCoordOffset = addTexCoordsToMesh(mesh, triangles);

                Object primitivesField = triangles.getField("p");
                if (!(primitivesField instanceof ColladaP)) {
                    throw new ModelLoadException("Collada: no p element found.");
                }

                final int totalPrimitiveCount = triangles.getCount();
                final int vertexIndexOffset = getVertexIndexOffset(triangles);
                final int texCoordIndexOffset = getTexCoordIndexOffset(triangles);
                final int[] primitiveIndices = ((ColladaP)primitivesField).getIndices();
                final int primitiveStride = triangles.getInputs().size();
                final int baseFaceOffset = addFacesToMesh(mesh, totalPrimitiveCount);

                int currentIndex = 0;
                for (int i = 0; i < totalPrimitiveCount; ++i) {
                    final int vertexIndex0 =
                        primitiveIndices[currentIndex * primitiveStride + vertexIndexOffset] + baseVertexOffset;
                    final int vertexIndex1 =
                        primitiveIndices[(currentIndex + 1) * primitiveStride + vertexIndexOffset] + baseVertexOffset;
                    final int vertexIndex2 =
                        primitiveIndices[(currentIndex + 2) * primitiveStride + vertexIndexOffset] + baseVertexOffset;
                    final int texCoordIndex0 =
                        primitiveIndices[currentIndex * primitiveStride + texCoordIndexOffset] + baseTexCoordOffset;
                    final int texCoordIndex1 =
                        primitiveIndices[(currentIndex + 1) * primitiveStride + texCoordIndexOffset]
                            + baseTexCoordOffset;
                    final int texCoordIndex2 =
                        primitiveIndices[(currentIndex + 2) * primitiveStride + texCoordIndexOffset]
                            + baseTexCoordOffset;
                    mesh.faces[i + baseFaceOffset] = new Face();
                    mesh.faces[i + baseFaceOffset].coordIndex =
                        new int[] {texCoordIndex0, texCoordIndex1, texCoordIndex2};
                    mesh.faces[i + baseFaceOffset].vertIndex = new int[] {vertexIndex0, vertexIndex1, vertexIndex2};
                    currentIndex += 3;
                }
            }

            /*for (ColladaPolylist polylist : colladaMesh.getPolylists()) {
                            final int baseVertexOffset = addVerticesToMesh(mesh, polylist, currentTransform);
                            final int baseTexCoordOffset = addTexCoordsToMesh(mesh, polylist);
                            AbstractXMLEventParser vcount = (AbstractXMLEventParser)polylist.getField("vcount");

                            ArrayList<Integer> primitiveCounts = new ArrayList<>();
                            for (String s : vcount.getCharacters().split(" ")) {
                                Integer n = Integer.parseInt(s);
                                primitiveCounts.add(n);
                            }

                            Object primitivesField = polylist.getField("p");
                            if (!(primitivesField instanceof ColladaP)) {
                                throw new ModelLoadException("Collada: no p element found.");
                            }

                            final int vertexIndexOffset = getVertexIndexOffset(polylist);
                            final int texCoordIndexOffset = getTexCoordIndexOffset(polylist);
                            final int[] primitiveIndices = ((ColladaP)primitivesField).getIndices();
                            final int primitiveStride = polylist.getInputs().size();
                            final int baseFaceOffset = addFacesToMesh(mesh, primitiveCounts.size());

                            int currentIndex = 0;
                            for (int i = 0; i < primitiveCounts.size(); ++i) {
                                final int primitiveCount = primitiveCounts.get(i).intValue();
                                if (primitiveCount == 3) {
                                    final int vertexIndex0 = primitiveIndices[currentIndex * primitiveStride + vertexIndexOffset] + baseVertexOffset;
                                    final int vertexIndex1 = primitiveIndices[(currentIndex + 1) * primitiveStride + vertexIndexOffset] + baseVertexOffset;
                                    final int vertexIndex2 = primitiveIndices[(currentIndex + 2) * primitiveStride + vertexIndexOffset] + baseVertexOffset;
                                    final int texCoordIndex0 = primitiveIndices[currentIndex * primitiveStride + texCoordIndexOffset] + baseTexCoordOffset;
                                    final int texCoordIndex1 = primitiveIndices[(currentIndex + 1) * primitiveStride + texCoordIndexOffset] + baseTexCoordOffset;
                                    final int texCoordIndex2 = primitiveIndices[(currentIndex + 2) * primitiveStride + texCoordIndexOffset] + baseTexCoordOffset;
                                    mesh.faces[i + baseFaceOffset] = new Face();
                                    mesh.faces[i + baseFaceOffset].coordIndex = new int[] { texCoordIndex0, texCoordIndex1, texCoordIndex2 };
                                    mesh.faces[i + baseFaceOffset].vertIndex = new int[] { vertexIndex0, vertexIndex1, vertexIndex2 };
                                } else if (primitiveCount == 4) {
                                    throw new ModelLoadException("Collada: quad primitives are not supported.");
                                } else {
                                    throw new ModelLoadException("Collada: polylist contains primitives other than triangles and quads.");
                                }

                                currentIndex += primitiveCount;
                            }
                        }
            */
            model.addMesh(mesh);
        }

        return model;
    }

    private static int addVerticesToMesh(Mesh mesh, ColladaAbstractGeometry geometry, Matrix transform)
            throws ModelLoadException {
        Vec4[] verticesToBeAdded = readVertices(geometry);
        for (int i = 0; i < verticesToBeAdded.length; ++i) {
            verticesToBeAdded[i] = verticesToBeAdded[i].transformBy3(transform);
        }

        if (mesh.vertices != null) {
            net.java.joglutils.model.geometry.Vec4[] newVertices =
                new net.java.joglutils.model.geometry.Vec4[mesh.vertices.length + verticesToBeAdded.length];

            for (int i = 0; i < mesh.vertices.length; ++i) {
                newVertices[i] = mesh.vertices[i];
            }

            for (int i = 0; i < newVertices.length; ++i) {
                newVertices[mesh.vertices.length + i] =
                    new net.java.joglutils.model.geometry.Vec4(
                        (float)verticesToBeAdded[i].x,
                        (float)verticesToBeAdded[i].z,
                        (float)verticesToBeAdded[i].y,
                        (float)verticesToBeAdded[i].w);
            }

            int vertexOffset = mesh.vertices.length;
            mesh.vertices = newVertices;
            mesh.numOfVerts = mesh.vertices.length;
            return vertexOffset;
        } else {
            mesh.vertices = new net.java.joglutils.model.geometry.Vec4[verticesToBeAdded.length];
            for (int i = 0; i < verticesToBeAdded.length; ++i) {
                mesh.vertices[i] =
                    new net.java.joglutils.model.geometry.Vec4(
                        (float)verticesToBeAdded[i].x,
                        (float)verticesToBeAdded[i].z,
                        (float)verticesToBeAdded[i].y,
                        (float)verticesToBeAdded[i].w);
            }

            mesh.numOfVerts = mesh.vertices.length;
            return 0;
        }
    }

    private static int addTexCoordsToMesh(Mesh mesh, ColladaAbstractGeometry geometry) throws ModelLoadException {
        if (mesh.texCoords != null) {
            TexCoord[] texCoordsToBeAdded = readTexCoords(geometry);
            TexCoord[] newTexCoords = new TexCoord[mesh.texCoords.length + texCoordsToBeAdded.length];

            for (int i = 0; i < mesh.texCoords.length; ++i) {
                newTexCoords[i] = mesh.texCoords[i];
            }

            for (int i = 0; i < newTexCoords.length; ++i) {
                newTexCoords[mesh.texCoords.length + i] = texCoordsToBeAdded[i];
            }

            int texCoordOffset = mesh.texCoords.length;
            mesh.texCoords = newTexCoords;
            mesh.numTexCoords = mesh.texCoords.length;
            return texCoordOffset;
        } else {
            mesh.texCoords = readTexCoords(geometry);
            mesh.numTexCoords = mesh.texCoords.length;
            return 0;
        }
    }

    private static int addFacesToMesh(Mesh mesh, int count) {
        if (mesh.faces != null) {
            Face[] newFaces = new Face[mesh.faces.length + count];
            for (int i = 0; i < mesh.faces.length; ++i) {
                newFaces[i] = mesh.faces[i];
            }

            int faceOffset = mesh.faces.length;
            mesh.faces = newFaces;
            mesh.numOfFaces = mesh.faces.length;
            return faceOffset;
        } else {
            mesh.faces = new Face[count];
            mesh.numOfFaces = mesh.faces.length;
            return 0;
        }
    }

    private static Vec4[] readVertices(ColladaAbstractGeometry geometry) throws ModelLoadException {
        float[] floats = geometry.getVertexAccessor().getFloats();
        if (floats.length % 3 != 0) {
            throw new ModelLoadException("Collada: vertex element count is not a multiple of 3.");
        }

        Vec4[] result = new Vec4[floats.length / 3];
        for (int i = 0; i < floats.length / 3; ++i) {
            result[i] = new Vec4(floats[i * 3], floats[i * 3 + 1], floats[i * 3 + 2], 1);
        }

        return result;
    }

    private static net.java.joglutils.model.geometry.TexCoord[] readTexCoords(ColladaAbstractGeometry geometry)
            throws ModelLoadException {
        float[] floats = geometry.getTexCoordAccessor(null).getFloats();
        if (floats.length % 2 != 0) {
            throw new ModelLoadException("Collada: texcoord element count is not a multiple of 2.");
        }

        net.java.joglutils.model.geometry.TexCoord[] result =
            new net.java.joglutils.model.geometry.TexCoord[floats.length / 2];
        for (int i = 0; i < floats.length / 2; ++i) {
            result[i] = new net.java.joglutils.model.geometry.TexCoord(floats[i * 2], floats[i * 2 + 1]);
        }

        return result;
    }

    private static int getVertexIndexOffset(ColladaAbstractGeometry geometry) {
        for (ColladaInput colladaInput : geometry.getInputs()) {
            if (colladaInput.getSemantic().equals("VERTEX")) {
                return colladaInput.getOffset();
            }
        }

        return 0;
    }

    private static int getTexCoordIndexOffset(ColladaAbstractGeometry geometry) {
        for (ColladaInput colladaInput : geometry.getInputs()) {
            if (colladaInput.getSemantic().equals("TEXCOORD")) {
                return colladaInput.getOffset();
            }
        }

        return 0;
    }

    private static String getFileExtension(File file) {
        String extension = "";
        String fileName = file.getName();

        int i = fileName.lastIndexOf('.');
        int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));

        if (i > p) {
            extension = fileName.substring(i + 1);
        }

        return extension;
    }
}
