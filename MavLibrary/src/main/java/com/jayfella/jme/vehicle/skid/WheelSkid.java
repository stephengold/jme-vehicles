package com.jayfella.jme.vehicle.skid;

import com.jayfella.jme.vehicle.VehicleWorld;
import com.jayfella.jme.vehicle.part.Wheel;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.objects.VehicleWheel;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.mesh.IndexBuffer;
import com.jme3.util.BufferUtils;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * A single continuous skidmark, composed of straight sections and rendered by a
 * single Geometry.
 *
 * Derived from the SkidMarkManager class in the Advanced Vehicles project.
 */
class WheelSkid {
    // *************************************************************************
    // constants and loggers

    /**
     * height of the skidmark above the pavement (in world units)
     */
    final private static float height = 0.02f;
    /**
     * minimum distance travelled before starting a new section (in world
     * units), bigger means better performance but less smooth
     */
    final private static float minLength = 0.5f;
    final private static float minLengthSquared = minLength * minLength;
    /**
     * minimum sideslip speed in wu/s to begin showing a skid
     */
    final private static float SKID_FX_SPEED = 0.25f;
    /**
     * number of axes in the coordinate system
     */
    final private static int numAxes = 3;
    /**
     * number of vertices per triangle
     */
    final public static int vpt = 3;
    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(WheelSkid.class.getName());
    // *************************************************************************
    // fields

    /**
     * width of this skidmark (in world units)
     */
    final private float width;
    /**
     * Geometry to visualize this skidmark
     */
    final private Geometry geometry;
    /**
     * index of last section used, or -1 if no section used
     */
    private int lastSectionUsed = -1;
    /**
     * capacity of the Mesh in terms of sections
     */
    private int meshSize;
    /**
     * list of sections
     */
    final private List<SkidmarkSection> sections = new ArrayList<>(33);

    final private Wheel wheel;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a continuous skidmark with the specified width.
     *
     * @param wheel the wheel that's generating the skidmark (not null)
     * @param assetManager for loading assets (not null)
     * @param tireWidth the desired width for this skidmark (in world units,
     * &gt;0)
     */
    WheelSkid(Wheel wheel, AssetManager assetManager, float tireWidth) {
        this.wheel = wheel;
        this.width = tireWidth;
        this.geometry = createGeometry(assetManager, 32);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Access the Geometry used to render the marks.
     *
     * @return the pre-existing instance (not null)
     */
    Geometry getGeometry() {
        return geometry;
    }

    /**
     * Translate the entire skidmark by the specified offset.
     *
     * @param offset the desired offset (in world coordinates, not null,
     * unaffected)
     */
    public void translateAll(Vector3f offset) {
        clearMesh();

        Mesh mesh = geometry.getMesh();
        int numSections = sections.size();
        for (int sectionI = 0; sectionI < numSections; ++sectionI) {
            SkidmarkSection section = sections.get(sectionI);
            section.translate(offset);
            section.appendToMesh(mesh, sectionI);
        }
        geometry.setMesh(mesh); // This triggers the necessary bounds refresh.
    }

    void update(float tpf) {
        float skidFraction = wheel.skidFraction();
        if (skidFraction < SKID_FX_SPEED) {
            this.lastSectionUsed = -1;
        } else {
            skidFraction = smoothstep(SKID_FX_SPEED, 1f, skidFraction);
            VehicleWheel vehicleWheel = wheel.getVehicleWheel();
            Vector3f normal = vehicleWheel.getCollisionNormal();
            assert normal.isUnitVector() : normal;
            Vector3f location = vehicleWheel.getCollisionLocation();
            this.lastSectionUsed = addSection(
                    location, normal, skidFraction, lastSectionUsed);
        }
    }
    // *************************************************************************
    // private methods

    /**
     * Add a section to this skidmark. The alpha component of the vertex color
     * indicates the skid intensity.
     *
     * @param pavementLocation the final pavement location for the new section
     * (in world coordinates, not null, unaffected)
     * @param normal the final normal for the new section (a unit vector in
     * world coordinates, not null, unaffected)
     * @param opacity the final intensity for the new section (typically between
     * 0 and 1)
     * @param prevIndex the index of the previous final section (&ge;0,
     * &lt;maxSections) or -1 to start a new skidmark
     * @return the index of the final section (&ge;0, &lt;maxSections) or -1 if
     * the skid has ended
     */
    private int addSection(Vector3f pavementLocation, Vector3f normal,
            float opacity, int prevIndex) {
        if (opacity < 0f) {
            return -1;
        }
        float clampedOpacity = FastMath.saturate(opacity);

        SkidmarkSection previous;
        if (prevIndex == -1) {
            if (sections.size() > 1) {
                VehicleWorld world = wheel.getVehicle().getWorld();
                world.getDecalManager().addCloneOf(geometry);
            }
            sections.clear();
            clearMesh();
            previous = null;
        } else {
            previous = sections.get(prevIndex);
            float lengthSquared = previous.distanceSquared(pavementLocation);
            if (lengthSquared < minLengthSquared) {
                return prevIndex;
            }
        }

        SkidmarkSection section = new SkidmarkSection(pavementLocation, normal,
                height, clampedOpacity, previous, width / 2f);
        sections.add(section);
        int numSections = sections.size();
        int sectionIndex = numSections - 1;
        Mesh mesh;
        if (numSections - 1 > meshSize) {
            // Recreate the Mesh from sections, making each buffer 4x larger.
            mesh = createMesh(4 * meshSize);
            for (int i = 0; i < sectionIndex; ++i) {
                SkidmarkSection s = sections.get(i);
                s.appendToMesh(mesh, i);
            }
        } else {
            mesh = geometry.getMesh();
        }
        /*
         * Add 4 mesh vertices (2 triangles) in order to connect the new section
         * to its predecessor.
         */
        section.appendToMesh(mesh, sectionIndex);
        geometry.setMesh(mesh); // This triggers the necessary bounds refresh.

        return sectionIndex;
    }

    /**
     * Reset the Mesh so that it can be reused for a new skidmark.
     */
    private void clearMesh() {
        Mesh mesh = geometry.getMesh();

        IndexBuffer indexBuffer = mesh.getIndexBuffer();
        Buffer buffer = indexBuffer.getBuffer();
        buffer.clear();
        buffer.flip();

        FloatBuffer colorBuffer = mesh.getFloatBuffer(VertexBuffer.Type.Color);
        colorBuffer.clear();
        colorBuffer.flip();

        FloatBuffer normalBuffer
                = mesh.getFloatBuffer(VertexBuffer.Type.Normal);
        normalBuffer.clear();
        normalBuffer.flip();

        FloatBuffer positionBuffer
                = mesh.getFloatBuffer(VertexBuffer.Type.Position);
        positionBuffer.clear();
        positionBuffer.flip();

        FloatBuffer tangentBuffer
                = mesh.getFloatBuffer(VertexBuffer.Type.Tangent);
        tangentBuffer.clear();
        tangentBuffer.flip();

        FloatBuffer uvBuffer = mesh.getFloatBuffer(VertexBuffer.Type.TexCoord);
        uvBuffer.clear();
        uvBuffer.flip();

        mesh.setDynamic();
        mesh.updateCounts();

        assert mesh.getTriangleCount() == 0 : mesh.getTriangleCount();
        assert mesh.getVertexCount() == 0 : mesh.getVertexCount();
    }

    private Geometry createGeometry(
            AssetManager assetManager, int numSections) {
        Mesh mesh = createMesh(numSections);
        Geometry result = new Geometry("Skidmark", mesh);

        Material material
                = assetManager.loadMaterial("Materials/Vehicles/SkidMark.j3m");
        result.setMaterial(material);
        result.setQueueBucket(RenderQueue.Bucket.Transparent);

        return result;
    }

    private Mesh createMesh(int numSections) {
        int numTriangles = numSections * 2;
        int numVertices = numSections * 4;
        int numIndices = numTriangles * vpt;
        IndexBuffer indexBuffer
                = IndexBuffer.createIndexBuffer(numVertices, numIndices);
        Buffer buffer = indexBuffer.getBuffer();
        buffer.flip();

        FloatBuffer colorBuffer
                = BufferUtils.createFloatBuffer(numVertices * 4);
        colorBuffer.flip();

        FloatBuffer normalBuffer
                = BufferUtils.createFloatBuffer(numVertices * numAxes);
        normalBuffer.flip();

        FloatBuffer positionBuffer
                = BufferUtils.createFloatBuffer(numVertices * numAxes);
        positionBuffer.flip();

        FloatBuffer tangentBuffer
                = BufferUtils.createFloatBuffer(numVertices * 4);
        tangentBuffer.flip();

        FloatBuffer uvBuffer = BufferUtils.createFloatBuffer(numVertices * 2);
        uvBuffer.flip();

        Mesh result = new Mesh();
        VertexBuffer.Format ibFormat = indexBuffer.getFormat();
        result.setBuffer(VertexBuffer.Type.Index, vpt, ibFormat, buffer);
        result.setBuffer(VertexBuffer.Type.Color, 4, colorBuffer);
        result.setBuffer(VertexBuffer.Type.Normal, numAxes, normalBuffer);
        result.setBuffer(VertexBuffer.Type.Position, numAxes, positionBuffer);
        result.setBuffer(VertexBuffer.Type.Tangent, 4, tangentBuffer);
        result.setBuffer(VertexBuffer.Type.TexCoord, 2, uvBuffer);

        result.updateBound();
        result.updateCounts();
        this.meshSize = numSections;

        return result;
    }

    private static float smoothstep(float a, float b, float x) {
        if (x < a) {
            return 0;
        } else if (x > b) {
            return 1;
        }
        float xx = (x - a) / (b - a);
        return xx * xx * (3 - 2 * xx);
    }
}
