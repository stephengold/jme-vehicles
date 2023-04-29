package com.jayfella.jme.vehicle;

import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * Manage decal geometries, "aging them out" on a first-in, first-out (FIFO)
 * basis.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class DecalManager {
    // *************************************************************************
    // constants and loggers

    /**
     * maximum number of triangles to retain
     */
    final private static int maxTriangles = 9_999;
    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(DecalManager.class.getName());
    // *************************************************************************
    // fields

    /**
     * queue of retained decals, from oldest to newest
     */
    final private Deque<Geometry> fifo = new LinkedList<>();
    /**
     * assign unique names to inactive decals
     */
    private int nextId = 0;
    /**
     * total triangles across all retained decals
     */
    private int totalTriangles = 0;
    /**
     * parent the decals
     */
    final private Node decalNode = new Node("Decal Node");
    // *************************************************************************
    // constructors

    /**
     * A no-arg constructor to avoid javadoc warnings from JDK 18.
     */
    public DecalManager() {
        // do nothing
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Add a decal based on the specified template.
     *
     * @param active the template (not null, not empty, unaffected)
     */
    public void addCloneOf(Geometry active) {
        Geometry decal = (Geometry) active.deepClone();
        addDecal(decal);
    }

    /**
     * Add the specified decal to the queue.
     *
     * @param decal the decal to add (not null, not empty, alias created)
     */
    public void addDecal(Geometry decal) {
        int triangleCount = decal.getTriangleCount();
        assert triangleCount > 0 : triangleCount;

        decal.setName("decal #" + nextId);
        ++nextId;

        fifo.addLast(decal);
        decalNode.attachChild(decal);
        totalTriangles += triangleCount;

        purge();
    }

    /**
     * Access the scene-graph node which parents all the decals.
     *
     * @return the pre-existing instance (not null)
     */
    public Node getNode() {
        return decalNode;
    }

    /**
     * Remove just enough of the old decals to conform to the triangle limit.
     */
    public void purge() {
        while (totalTriangles > maxTriangles) {
            Geometry oldest = fifo.removeFirst();
            oldest.removeFromParent();

            int count = oldest.getTriangleCount();
            this.totalTriangles -= count;
        }
    }

    /**
     * Translate all decals by the specified offset.
     *
     * @param offset the desired offset (in world coordinates, not null,
     * unaffected)
     */
    public void translateAll(Vector3f offset) {
        for (Geometry geometry : fifo) {
            geometry.move(offset);
        }
    }
}
