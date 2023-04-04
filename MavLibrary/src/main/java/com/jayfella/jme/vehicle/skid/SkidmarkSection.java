package com.jayfella.jme.vehicle.skid;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.mesh.IndexBuffer;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import jme3utilities.math.MyVector3f;

/**
 * A straight section within a continuous skidmark.
 */
class SkidmarkSection {
    // *************************************************************************
    // fields

    /**
     * previous section in the skid, or null if this is the first
     */
    final private SkidmarkSection previous;
    /**
     * final opacity of this section (&ge;0, &lt;1)
     */
    final private float opacity;
    /**
     * final center location (in world coordinates)
     */
    final private Vector3f location = new Vector3f();
    /**
     * final location of the left edge (in world coordinates)
     */
    final private Vector3f locationLeft = new Vector3f();
    /**
     * final location of the right edge (in world coordinates)
     */
    final private Vector3f locationRight = new Vector3f();
    /**
     * final normal (a unit vector in world coordinates)
     */
    final private Vector3f normal = new Vector3f();
    /**
     * final tangent (unit vector in world coordinates)
     */
    final private Vector3f tangent = new Vector3f();
    // *************************************************************************
    // constructors

    /**
     * Instantiate a skidmark section.
     *
     * @param pavementLocation the final pavement location (in world
     * coordinates, not null, unaffected)
     * @param normal the final normal (a unit vector in world coordinates, not
     * null, unaffected)
     * @param height the height of the mark above the pavement (in meters)
     * @param opacity the final opacity (&ge;0, &le;1)
     * @param previous the previous section in the skid, or null if this is the
     * first
     * @param halfWidth half of the width of the skidmark (in world units,
     * &ge;0)
     */
    SkidmarkSection(Vector3f pavementLocation, Vector3f normal, float height,
            float opacity, SkidmarkSection previous, float halfWidth) {
        assert opacity >= 0f : opacity;
        assert opacity <= 1f : opacity;

        location.set(pavementLocation);
        MyVector3f.accumulateScaled(location, normal, height);

        this.normal.set(normal);
        this.opacity = opacity;
        this.previous = previous;

        if (previous != null) {
            // calculate the tangent direction
            location.subtract(previous.location, tangent)
                    .crossLocal(normal)
                    .normalizeLocal();

            // locate the left edge
            locationLeft.set(location);
            MyVector3f.accumulateScaled(locationLeft, tangent, -halfWidth);

            // locate the right edge
            locationRight.set(location);
            MyVector3f.accumulateScaled(locationRight, tangent, halfWidth);

            if (previous.previous == null) {
                /*
                 * Calculate the tangent, left edge, and right edge
                 * for the initial section of the skid.
                 */
                previous.tangent.set(tangent);

                previous.locationLeft.set(previous.location);
                MyVector3f.accumulateScaled(
                        previous.locationLeft, tangent, -halfWidth);

                previous.locationRight.set(previous.location);
                MyVector3f.accumulateScaled(
                        previous.locationRight, tangent, halfWidth);
            }
        }
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Append 4 vertices (2 triangles) to the specified Mesh, in order to
     * connect this section to its predecessor.
     *
     * @param mesh the Mesh to modify (not null)
     * @param sectionIndex the index of this section in the list (&ge;0)
     */
    void appendToMesh(Mesh mesh, int sectionIndex) {
        if (previous == null) {
            assert sectionIndex == 0 : sectionIndex;
            /*
             * The first section is ignored,
             * since there isn't anything to connect to yet.
             */
            return;
        }

        IndexBuffer indexBuffer = mesh.getIndexBuffer();
        Buffer buffer = indexBuffer.getBuffer();
        int capacity = buffer.capacity();
        buffer.limit(capacity);
        buffer.position((sectionIndex - 1) * 6);
        int vertexIndex = (sectionIndex - 1) * 4;
        indexBuffer.put(vertexIndex)
                .put(vertexIndex + 1)
                .put(vertexIndex + 2);
        indexBuffer.put(vertexIndex + 2)
                .put(vertexIndex + 1)
                .put(vertexIndex + 3);
        buffer.flip();

        FloatBuffer colorBuffer = mesh.getFloatBuffer(VertexBuffer.Type.Color);
        colorBuffer.limit(colorBuffer.capacity());
        colorBuffer.position(vertexIndex * 4);
        colorBuffer.put(0f).put(0f).put(0f).put(previous.opacity);
        colorBuffer.put(0f).put(0f).put(0f).put(previous.opacity);
        colorBuffer.put(0f).put(0f).put(0f).put(opacity);
        colorBuffer.put(0f).put(0f).put(0f).put(opacity);
        colorBuffer.flip();

        FloatBuffer normalBuffer
                = mesh.getFloatBuffer(VertexBuffer.Type.Normal);
        normalBuffer.limit(normalBuffer.capacity());
        normalBuffer.position(vertexIndex * 3);
        Vector3f pNormal = previous.normal;
        normalBuffer.put(pNormal.x).put(pNormal.y).put(pNormal.z);
        normalBuffer.put(pNormal.x).put(pNormal.y).put(pNormal.z);
        normalBuffer.put(normal.x).put(normal.y).put(normal.z);
        normalBuffer.put(normal.x).put(normal.y).put(normal.z);
        normalBuffer.flip();

        FloatBuffer positionBuffer
                = mesh.getFloatBuffer(VertexBuffer.Type.Position);
        positionBuffer.limit(positionBuffer.capacity());
        positionBuffer.position(vertexIndex * 3);
        Vector3f pLeft = previous.locationLeft;
        Vector3f pRight = previous.locationRight;
        positionBuffer.put(pLeft.x).put(pLeft.y).put(pLeft.z);
        positionBuffer.put(pRight.x).put(pRight.y).put(pRight.z);
        positionBuffer.put(locationLeft.x)
                .put(locationLeft.y)
                .put(locationLeft.z);
        positionBuffer.put(locationRight.x)
                .put(locationRight.y)
                .put(locationRight.z);
        positionBuffer.flip();

        FloatBuffer tangentBuffer
                = mesh.getFloatBuffer(VertexBuffer.Type.Tangent);
        tangentBuffer.limit(tangentBuffer.capacity());
        tangentBuffer.position(vertexIndex * 4);
        Vector3f pTangent = previous.tangent;
        tangentBuffer.put(pTangent.x).put(pTangent.y).put(pTangent.z).put(1f);
        tangentBuffer.put(pTangent.x).put(pTangent.y).put(pTangent.z).put(1f);
        tangentBuffer.put(tangent.x).put(tangent.y).put(tangent.z).put(1f);
        tangentBuffer.put(tangent.x).put(tangent.y).put(tangent.z).put(1f);
        tangentBuffer.flip();

        FloatBuffer uvBuffer = mesh.getFloatBuffer(VertexBuffer.Type.TexCoord);
        uvBuffer.limit(uvBuffer.capacity());
        uvBuffer.position(vertexIndex * 2);
        uvBuffer.put(0f).put(0f);
        uvBuffer.put(1f).put(0f);
        uvBuffer.put(0f).put(1f);
        uvBuffer.put(1f).put(1f);
        uvBuffer.flip();

        // Update the bounds.
        BoundingBox aabb = (BoundingBox) mesh.getBound();
        Vector3f max = new Vector3f(); // TODO garbage
        Vector3f min = new Vector3f();
        if (sectionIndex == 1) {
            max.set(pLeft);
            min.set(pLeft);
            BoundingBox.checkMinMax(min, max, pRight);
        } else {
            aabb.getMax(max);
            aabb.getMin(min);
        }
        BoundingBox.checkMinMax(min, max, locationLeft);
        BoundingBox.checkMinMax(min, max, locationRight);
        aabb.setMinMax(min, max);

        mesh.setDynamic();
        mesh.updateCounts();
    }

    /**
     * Calculate the squared distance from the final center location of this
     * section to the specified location.
     *
     * @param worldLocation the location (in world coordinates, not null,
     * unaffected)
     * @return the squared distance (in world units squared, &ge;0)
     */
    float distanceSquared(Vector3f worldLocation) {
        float result = worldLocation.distanceSquared(location);
        return result;
    }

    /**
     * Translate this section by the specified offset.
     *
     * @param offset the desired offset (in world coordinates, not null,
     * unaffected)
     */
    void translate(Vector3f offset) {
        location.addLocal(offset);
        locationLeft.addLocal(offset);
        locationRight.addLocal(offset);
    }
}
