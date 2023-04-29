package com.jayfella.jme.vehicle;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jme3utilities.Loadable;
import jme3utilities.MyCamera;
import jme3utilities.Validate;
import jme3utilities.math.Vector3i;

/**
 * A 3-D world, such as the Vehicle Playground. Includes a C-G model and a
 * single rigid body, but no lights, post-processors, or sky. Requires a
 * ChunkManager.
 *
 * @author Stephen Gold sgold@sonic.net
 */
abstract public class World
        implements Loadable, PropWorld, VehicleWorld {
    // *************************************************************************
    // constants and loggers

    /**
     * all chunks in a single-chunk world
     */
    final private static Set<Vector3i> defaultChunkSet = new HashSet<>(1, 1f);

    static {
        defaultChunkSet.add(Vector3i.zero);
    }
    // *************************************************************************
    // constructors

    /**
     * A no-arg constructor to avoid javadoc warnings from JDK 18.
     */
    protected World() {
        // do nothing
    }
    // *************************************************************************
    // fields

    /**
     * provide access to the AppStateManager, AssetManager, default Camera, etc
     */
    private Application application;
    /**
     * loaded CollisionShape
     */
    private CollisionShape loadedShape;
    /**
     * manage decals
     */
    final private DecalManager decalManager = new DecalManager();
    /**
     * Prop instances, ordered from oldest to newest
     */
    final private List<Prop> props = new ArrayList<>(64);
    /**
     * loaded C-G model of a prototypical chunk
     */
    private Node loadedCgm;
    /**
     * where to add probes and attach spatials
     */
    private Node parentNode;
    /**
     * collision object
     */
    private PhysicsRigidBody rigidBody;
    /**
     * simulate dynamic physics
     */
    private PhysicsSpace physicsSpace;
    // *************************************************************************
    // new methods exposed

    /**
     * Attach this World to the scene and PhysicsSpace of the specified
     * Application.
     *
     * @param application the application instance (not null, alias created)
     * @param parentNode where to add probes and attach spatials (not null,
     * alias created)
     * @param physicsSpace where to add bodies and joints (not null, alias
     * created)
     */
    public void attach(Application application, Node parentNode,
            PhysicsSpace physicsSpace) {
        Validate.nonNull(application, "application");
        Validate.nonNull(parentNode, "parent node");
        Validate.nonNull(physicsSpace, "physics space");

        this.application = application;
        this.parentNode = parentNode;
        this.physicsSpace = physicsSpace;

        if (loadedCgm == null) {
            AssetManager assetManager = getAssetManager();
            load(assetManager);
        }
        getChunkManager().setWorld(this);

        Node decalNode = decalManager.getNode();
        parentNode.attachChild(decalNode);

        this.rigidBody
                = new PhysicsRigidBody(loadedShape, PhysicsBody.massForStatic);
        getPhysicsSpace().add(rigidBody);
        rigidBody.setApplicationData(this);
        /*
         * Set the far clipping plane for this world.
         *
         * The dash camera sits close to the bodywork, so set the near clipping
         * plane accordingly.
         */
        Camera cam = getCamera();
        float near = 0.1f;
        float far = farDistance();
        MyCamera.setNearFar(cam, near, far);
    }

    /**
     * Determine the dimensions of each scene chunk, in scene units. For
     * single-chunk worlds, the result is always (1e9,1e9,1e9).
     *
     * @param storeResult storage for the result (not null)
     */
    public void chunkDimensions(Vector3f storeResult) {
        storeResult.set(1e9f, 1e9f, 1e9f);
    }

    /**
     * Count how many props are in this World.
     *
     * @return the count (&ge;0)
     */
    public int countProps() {
        int count = props.size();
        return count;
    }

    /**
     * Create a Node for the identified scene chunk.
     *
     * @param chunkId which scene chunk
     * @return a new Node
     */
    Node createChunk(Vector3i chunkId) {
        boolean cloneMaterials = false;
        Node result = loadedCgm.clone(cloneMaterials);

        return result;
    }

    /**
     * Detach this World from the scene and PhysicsSpace to which it was
     * attached.
     */
    public void detach() {
        PhysicsSpace space = (PhysicsSpace) rigidBody.getCollisionSpace();
        space.removeCollisionObject(rigidBody);

        decalManager.getNode().removeFromParent();
        getChunkManager().setWorld(null);

        this.parentNode = null;
        this.application = null;
    }

    /**
     * Determine the distance to the camera's far plane.
     *
     * @return the distance (in world units, &gt;0)
     */
    abstract public float farDistance();

    /**
     * Access the loaded C-G model.
     *
     * @return the pre-existing Node, or null if not yet loaded
     */
    public Node getCgm() {
        return loadedCgm;
    }

    /**
     * Access the loaded CollisionShape.
     *
     * @return the pre-existing instance, or null if not yet loaded
     */
    public CollisionShape getCollisionShape() {
        return loadedShape;
    }

    /**
     * Reposition the default Camera to the initial location and orientation for
     * this World.
     */
    abstract public void resetCameraPosition();
    // *************************************************************************
    // new protected methods

    /**
     * Access the application's default Camera.
     *
     * @return the pre-existing instance (not null)
     */
    protected Camera getCamera() {
        Camera result = application.getCamera();

        assert result != null;
        return result;
    }

    /**
     * Access the ChunkManager.
     *
     * @return the pre-existing instance (not null)
     */
    protected ChunkManager getChunkManager() {
        ChunkManager result = getStateManager().getState(ChunkManager.class);
        if (result == null) {
            throw new IllegalStateException("ChunkManager not found.");
        }

        return result;
    }

    /**
     * Enumerate all chunks that are near the scene origin. For single-chunk
     * worlds, the result is always (0,0,0).
     *
     * @return an unmodifiable collection of chunk IDs (not null)
     */
    protected Set<Vector3i> listNearbyChunks() {
        return Collections.unmodifiableSet(defaultChunkSet);
    }

    /**
     * Alter which C-G model is loaded.
     *
     * @param cgm the desired model
     */
    protected void setCgm(Node cgm) {
        this.loadedCgm = cgm;
    }

    /**
     * Alter which CollisionShape is loaded.
     *
     * @param shape the desired shape
     */
    protected void setCollisionShape(CollisionShape shape) {
        this.loadedShape = shape;
    }
    // *************************************************************************
    // Loadable methods

    /**
     * Load the assets of this World.
     *
     * @param assetManager for loading assets (not null)
     */
    @Override
    public void load(AssetManager assetManager) {
        // subclasses should override
    }
    // *************************************************************************
    // PropWorld/VehicleWorld methods

    /**
     * Add the specified Prop to this World.
     *
     * @param newProp (not null, not already added)
     */
    @Override
    public void addProp(Prop newProp) {
        Validate.nonNull(newProp, "new prop");
        Validate.require(!props.contains(newProp), "not been added already");

        props.add(newProp);
    }

    /**
     * Access the AssetManager.
     *
     * @return the pre-existing instance (not null)
     */
    @Override
    public AssetManager getAssetManager() {
        AssetManager result = application.getAssetManager();

        assert result != null;
        return result;
    }

    /**
     * Access the DecalManager.
     *
     * @return the pre-existing instance (not null)
     */
    @Override
    public DecalManager getDecalManager() {
        assert decalManager != null;
        return decalManager;
    }

    /**
     * Access the scene-graph node for adding probes and attaching spatials.
     *
     * @return the pre-existing instance (not null)
     */
    @Override
    public Node getParentNode() {
        assert parentNode != null;
        return parentNode;
    }

    /**
     * Access the PhysicsSpace.
     *
     * @return the pre-existing instance (not null)
     */
    @Override
    public PhysicsSpace getPhysicsSpace() {
        assert physicsSpace != null;
        return physicsSpace;
    }

    /**
     * Access the AppStateManager.
     *
     * @return the pre-existing instance (not null)
     */
    @Override
    public AppStateManager getStateManager() {
        AppStateManager result = application.getStateManager();
        assert result != null;
        return result;
    }

    /**
     * Enumerate props that have been added to this world and not yet removed.
     *
     * @return a new unmodifiable collection of pre-existing instances (not
     * null)
     */
    @Override
    public Collection<Prop> listProps() {
        Collection<Prop> result = Collections.unmodifiableCollection(props);
        return result;
    }

    /**
     * Remove the specified Prop from this World.
     *
     * @param prop (not null, previously added)
     */
    @Override
    public void removeProp(Prop prop) {
        Validate.nonNull(prop, "prop");
        Validate.require(props.contains(prop), "be previously added");

        props.remove(prop);
    }
    // *************************************************************************
    // Object methods

    /**
     * Represent this instance as a String.
     *
     * @return a descriptive string of text (not null, not empty)
     */
    @Override
    public String toString() {
        String className = getClass().getSimpleName();
        int hashCode = hashCode();
        String result = className + "@" + Integer.toHexString(hashCode);

        return result;
    }
}
