package com.jayfella.jme.vehicle;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.bounding.BoundingSphere;
import com.jme3.bounding.BoundingVolume;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.LightProbe;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.shadow.EdgeFilteringMode;
import com.jme3.texture.Texture;
import java.util.List;
import java.util.logging.Logger;
import jme3utilities.Loadable;
import jme3utilities.MyMesh;
import jme3utilities.Validate;
import jme3utilities.mesh.Octasphere;

/**
 * A simulated sky with its associated lights and post-processing.
 *
 * Derived from the Main class in the Advanced Vehicles project.
 */
abstract public class Sky implements Loadable {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger = Logger.getLogger(Sky.class.getName());
    // *************************************************************************
    // fields

    /**
     * modulate the color/intensity of light probes
     */
    private static AmbientLight ambientLight;
    /**
     * main directional light
     */
    private static DirectionalLight directionalLight;
    /**
     * shadow renderer for the main light
     */
    private static DirectionalLightShadowRenderer shadowRenderer;
    /**
     * loaded LightProbe
     */
    private LightProbe probe;
    /**
     * application instance
     */
    private static SimpleApplication simpleApp;
    /**
     * root of the loaded sky model
     */
    private Spatial loadedCgm;
    // *************************************************************************
    // constructors

    /**
     * A no-arg constructor to avoid javadoc warnings from JDK 18.
     */
    protected Sky() {
        // do nothing
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Add this Sky to the specified world. The class must first be initialized.
     *
     * @param world where to add (not null)
     */
    public void addToWorld(VehicleWorld world) {
        if (ambientLight == null
                || directionalLight == null
                || shadowRenderer == null) {
            throw new IllegalStateException("Class is not initialized.");
        }

        if (loadedCgm == null) {
            AssetManager assetManager = world.getAssetManager();
            load(assetManager);
        }

        Node parentNode = world.getParentNode();
        parentNode.addLight(probe);
        parentNode.attachChild(loadedCgm);
    }

    /**
     * Test whether shadows are enabled.
     *
     * @return true if enabled, otherwise false
     */
    public static boolean areShadowsEnabled() {
        boolean result;
        if (shadowRenderer == null) {
            result = false;
        } else {
            ViewPort mainViewPort = simpleApp.getViewPort();
            List<SceneProcessor> list = mainViewPort.getProcessors();
            result = list.contains(shadowRenderer);
        }

        return result;
    }

    /**
     * Access the C-G model.
     *
     * @return the pre-existing Spatial, or null if not yet loaded
     */
    final public Spatial getCgm() {
        return loadedCgm;
    }

    /**
     * Add lights and shadows to the scene. Should only be invoked once, after
     * setApplication() but before addToWorld(). TODO why synchronized?
     */
    public static synchronized void initialize() {
        if (simpleApp == null) {
            throw new IllegalStateException("The application is not set.");
        }
        if (ambientLight != null
                || directionalLight != null
                || shadowRenderer != null) {
            String message = "The class is already initialized.";
            throw new IllegalStateException(message);
        }

        Node rootNode = simpleApp.getRootNode();
        rootNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        // Add an AmbientLight.
        ambientLight = new AmbientLight();
        rootNode.addLight(ambientLight);

        // Add a DirectionalLight.
        directionalLight = new DirectionalLight();
        rootNode.addLight(directionalLight);

        // Add shadows.
        setShadowsEnabled(true);
    }

    /**
     * Remove this Sky from the world to which it was added.
     */
    public void removeFromWorld() {
        loadedCgm.getParent().removeLight(probe);
        loadedCgm.removeFromParent();
    }

    /**
     * Specify the application instance. Can only be invoked once.
     *
     * @param application the application instance (not null, alias created)
     */
    public static void setApplication(SimpleApplication application) {
        Validate.nonNull(application, "application");
        if (simpleApp != null) {
            throw new IllegalStateException("The application is already set.");
        }

        simpleApp = application;
    }

    /**
     * Enable or disable shadows.
     *
     * @param newSetting true to enable, false to disable
     */
    public static void setShadowsEnabled(boolean newSetting) {
        ViewPort mainViewPort = simpleApp.getViewPort();
        boolean oldSetting = areShadowsEnabled();

        if (oldSetting && !newSetting) {
            assert shadowRenderer != null;
            mainViewPort.removeProcessor(shadowRenderer);

        } else if (newSetting && !oldSetting) {
            if (shadowRenderer == null) { // Create the renderer.
                AssetManager assetManager = simpleApp.getAssetManager();
                shadowRenderer = new DirectionalLightShadowRenderer(
                        assetManager, 4_096, 4);
                shadowRenderer
                        .setEdgeFilteringMode(EdgeFilteringMode.PCFPOISSON);
                shadowRenderer.setLight(directionalLight);
                shadowRenderer.setShadowIntensity(0.3f);
                shadowRenderer.setShadowZExtend(256f);
                shadowRenderer.setShadowZFadeLength(128f);
            }

            mainViewPort.addProcessor(shadowRenderer);
        }
    }
    // *************************************************************************
    // new protected methods

    /**
     * Configure the loaded C-G model and LightProbe.
     *
     * @param cgmRoot the root of the desired model (not null)
     * @param probe the desired LightProbe (not null)
     */
    final protected void build(Spatial cgmRoot, LightProbe probe) {
        Validate.nonNull(cgmRoot, "model root");
        Validate.nonNull(probe, "probe");

        this.loadedCgm = cgmRoot;
        this.probe = probe;
    }

    /**
     * Generate a sky geometry from an equirectangular texture asset. This
     * method has a couple advantages over
     * com.jme3.util.SkyFactory.createSky(AssetManager, Texture, EnvMapType):
     * <ul>
     * <li>It adds fewer triangles to the scene: 32 instead of 160.</li>
     * <li>It uses a custom J3MD to avoid JME issue #1414.</li>
     * </ul>
     *
     * @param assetManager for loading assets (not null)
     * @param textureAssetPath the path to the texture asset (not null, not
     * empty)
     * @return a new orphan Geometry
     */
    protected static Spatial
            createSky(AssetManager assetManager, String textureAssetPath) {
        // Load and configure the Texture.
        boolean flipY = true;
        TextureKey textureKey = new TextureKey(textureAssetPath, flipY);
        Texture texture = assetManager.loadTexture(textureKey);
        texture.setAnisotropicFilter(1);

        // Construct the Material.
        String matDefAssetPath = "/MatDefs/SkyEquirec.j3md";
        Material material = new Material(assetManager, matDefAssetPath);
        material.setTexture("Texture", texture);
        material.setVector3("NormalScale", new Vector3f(1f, 1f, 1f));

        // Construct the BoundingVolume, a very large sphere.
        float boundRadius = Float.POSITIVE_INFINITY;
        BoundingVolume boundingSphere
                = new BoundingSphere(boundRadius, Vector3f.ZERO);

        // Construct the Mesh, an Octasphere with 32 triangles.
        int numRefineSteps = 1;
        float meshRadius = 1_000f;
        Octasphere sphereMesh = new Octasphere(numRefineSteps, meshRadius);
        MyMesh.reverseNormals(sphereMesh);
        MyMesh.reverseWinding(sphereMesh);

        // Construct the Geometry.
        Geometry result = new Geometry("Sky Sphere", sphereMesh);
        result.setCullHint(Spatial.CullHint.Never);
        result.setMaterial(material);
        result.setModelBound(boundingSphere);
        result.setQueueBucket(RenderQueue.Bucket.Sky);
        result.setShadowMode(RenderQueue.ShadowMode.Off);

        return result;
    }

    /**
     * Access the AmbientLight that was added by initialize(), used to modulate
     * the color/intensity of light probes.
     *
     * @return the pre-existing instance (not null)
     */
    final protected static AmbientLight getAmbientLight() {
        assert ambientLight != null;
        return ambientLight;
    }

    /**
     * Access the application instance.
     *
     * @return the pre-existing instance (not null)
     */
    final protected static SimpleApplication getApplication() {
        assert simpleApp != null;
        return simpleApp;
    }

    /**
     * Access the DirectionalLight that was added by initialize().
     *
     * @return the pre-existing instance (not null)
     */
    final protected static DirectionalLight getDirectionalLight() {
        assert directionalLight != null;
        return directionalLight;
    }

    /**
     * Access the shadow renderer that was added by initialize().
     *
     * @return the pre-existing instance (not null)
     */
    final protected static DirectionalLightShadowRenderer getShadowRenderer() {
        assert shadowRenderer != null;
        return shadowRenderer;
    }
    // *************************************************************************
    // Loadable methods

    /**
     * Load this Sky from assets.
     *
     * @param assetManager for loading assets (not null)
     */
    @Override
    public void load(AssetManager assetManager) {
        assert loadedCgm == null : "The model is already loaded.";
    }
}
