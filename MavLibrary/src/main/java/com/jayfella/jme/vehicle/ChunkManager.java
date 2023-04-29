package com.jayfella.jme.vehicle;

import com.jayfella.jme.vehicle.skid.SkidMarksState;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import jme3utilities.math.Vector3i;

/**
 * An AppState to manage a World that's decomposed into equal-sized, rectangular
 * chunks.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class ChunkManager extends BaseAppState {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(ChunkManager.class.getName());
    // *************************************************************************
    // fields

    /**
     * chunk currently occupying the scene's origin
     */
    private Vector3i originChunk = Vector3i.zero;
    /**
     * map attached chunks to scene-graph nodes
     */
    final private Map<Vector3i, Node> idToNode = new HashMap<>(99);
    /**
     * where to attach chunks in the scene graph
     */
    final private Node parent = new Node("Chunks");
    /**
     * dimensions of each chunk (in world units, all components positive)
     */
    final private Vector3f chunkDimensions = new Vector3f(1e9f, 1e9f, 1e9f);
    /**
     * reusable temporary vectors
     */
    final private static Vector3f tmpLocation = new Vector3f();
    final private static Vector3f tmpOffset = new Vector3f();
    /**
     * World being managed
     */
    private World world;
    // *************************************************************************
    // constructors

    /**
     * A no-arg constructor to avoid javadoc warnings from JDK 18.
     */
    public ChunkManager() {
        // do nothing
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Determine which chunk is at the scene's origin.
     *
     * @return the chunk's ID
     */
    public Vector3i originChunk() {
        return originChunk;
    }

    /**
     * Alter which World is managed.
     *
     * @param world the World to manage (may be null, alias created)
     */
    void setWorld(World world) {
        detachAll();
        this.world = world;
        if (world != null) {
            world.chunkDimensions(chunkDimensions);
        }
        this.originChunk = Vector3i.zero;
    }
    // *************************************************************************
    // BaseAppState methods

    /**
     * Callback invoked after this AppState is detached or during application
     * shutdown if the state is still attached. onDisable() is called before
     * this cleanup() method if the state is enabled at the time of cleanup.
     *
     * @param application the application instance (not null)
     */
    @Override
    protected void cleanup(Application application) {
        // do nothing
    }

    /**
     * Callback invoked after this AppState is attached but before onEnable().
     *
     * @param application the application instance (not null)
     */
    @Override
    protected void initialize(Application application) {
        // do nothing
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        parent.removeFromParent();
    }

    /**
     * Callback invoked whenever this AppState becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        SimpleApplication simple = (SimpleApplication) getApplication();
        simple.getRootNode().attachChild(parent);
    }

    /**
     * Callback to update this AppState, invoked once per frame when the
     * AppState is both attached and enabled.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void update(float tpf) {
        super.update(tpf);
        /*
         * If the main camera has strayed too far from the scene origin,
         * warp it one chunk closer.
         */
        Vector3f location = getApplication().getCamera().getLocation();
        if (location.x > +chunkDimensions.x) {
            translateAll(-1, 0, 0);

        } else if (location.x < -chunkDimensions.x) {
            translateAll(+1, 0, 0);

        } else if (location.y > +chunkDimensions.y) {
            translateAll(0, -1, 0);

        } else if (location.y < -chunkDimensions.y) {
            translateAll(0, +1, 0);

        } else if (location.z > +chunkDimensions.z) {
            translateAll(0, 0, -1);

        } else if (location.z < -chunkDimensions.z) {
            translateAll(0, 0, +1);
        }

        if (world == null) {
            return;
        }

        Set<Vector3i> visibleSet = world.listNearbyChunks();

        int numAttached = idToNode.size();
        Vector3i[] oldChunks = new Vector3i[numAttached];
        idToNode.keySet().toArray(oldChunks);
        for (Vector3i chunk : oldChunks) {
            if (!visibleSet.contains(chunk)) {
                Node oldNode = idToNode.remove(chunk);
                oldNode.removeFromParent();
            }
        }

        for (Vector3i chunk : visibleSet) {
            if (!idToNode.containsKey(chunk)) {
                Node newNode = world.createChunk(chunk);
                parent.attachChild(newNode);
                idToNode.put(chunk, newNode);
            }

            Node node = idToNode.get(chunk);
            locateCenter(chunk, tmpOffset);
            node.setLocalTranslation(tmpOffset);
        }
    }
    // *************************************************************************
    // private methods

    /**
     * Detach all chunks from the scene.
     */
    private void detachAll() {
        for (Node oldNode : idToNode.values()) {
            oldNode.removeFromParent();
        }
        idToNode.clear();
    }

    /**
     * Locate the center of the identified chunk.
     *
     * @param chunk the chunk to locate (not null)
     * @param storeResult storage for the result (modified if not null)
     * @return a location vector in scene coordinates (either storeResult or a
     * new instance)
     */
    private Vector3f locateCenter(Vector3i chunk, Vector3f storeResult) {
        Vector3f result = (storeResult == null) ? new Vector3f() : storeResult;

        result.x = chunk.xDiff(originChunk);
        result.y = chunk.yDiff(originChunk);
        result.z = chunk.zDiff(originChunk);
        result.multLocal(chunkDimensions);

        return result;
    }

    /**
     * Translate all non-plane physics objects by the specified number of chunks
     * and translate the origin chunk by the opposite amount.
     *
     * @param deltaX the number of chunks in the +X direction
     * @param deltaY the number of chunks in the +Y direction
     * @param deltaZ the number of chunks in the +Z direction
     */
    private void translateAll(int deltaX, int deltaY, int deltaZ) {
        originChunk = originChunk.subtract(deltaX, deltaY, deltaZ);

        float xOffset = chunkDimensions.x * deltaX;
        float yOffset = chunkDimensions.y * deltaY;
        float zOffset = chunkDimensions.z * deltaZ;
        tmpOffset.set(xOffset, yOffset, zOffset);

        translateCameras(tmpOffset);
        world.getDecalManager().translateAll(tmpOffset);
        translatePcos(tmpOffset);
        getState(SkidMarksState.class).translateAll(tmpOffset);
        getState(TireSmokeEmitter.class).translateAll(tmpOffset);
    }

    private void translateCameras(Vector3f offset) {
        Camera camera = getApplication().getCamera();
        tmpLocation.set(camera.getLocation());
        tmpLocation.addLocal(offset);
        camera.setLocation(tmpLocation);
    }

    // TODO handle parallel physics, characters, ghosts, etcetera
    private void translatePcos(Vector3f offset) {
        BulletAppState bulletAppState = getState(BulletAppState.class);
        PhysicsSpace physicsSpace = bulletAppState.getPhysicsSpace();
        Collection<PhysicsCollisionObject> pcos = physicsSpace.getPcoList();

        for (PhysicsCollisionObject pco : pcos) {
            if (pco instanceof PhysicsRigidBody
                    && !pco.getCollisionShape().isInfinite()) {
                PhysicsRigidBody body = (PhysicsRigidBody) pco;
                body.getPhysicsLocation(tmpLocation);
                tmpLocation.addLocal(offset);
                body.setPhysicsLocation(tmpLocation);
            }
        }
    }
}
