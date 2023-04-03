package com.jayfella.jme.vehicle.gui.lemur;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.material.Material;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.texture.Texture;
import com.simsilica.lemur.event.MouseEventControl;
import java.util.logging.Logger;
import jme3utilities.MyAsset;
import jme3utilities.mesh.DiscMesh;

/**
 * Heads-up display (HUD) for controlling physics simulation. This AppState
 * should be instantiated once and then enabled/disabled as needed. It directly
 * manages the pause button and the single-step button.
 */
public class PhysicsHud extends BaseAppState {
    // *************************************************************************
    // constants and loggers

    /**
     * Z-coordinate for most GUI geometries
     */
    final private static float guiZ = 1f;
    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(PhysicsHud.class.getName());
    // *************************************************************************
    // fields

    /**
     * height of the GUI viewport (in pixels)
     */
    private float viewPortHeight;
    /**
     * width of the GUI viewport (in pixels)
     */
    private float viewPortWidth;
    /**
     * visualize the pause button
     */
    private Geometry pauseButton;
    /**
     * visualize the single-step button
     */
    private Geometry singleStepButton;
    /**
     * indicate physics simulation is paused
     */
    private Material pauseMaterial;
    /**
     * indicate physics simulation is running
     */
    private Material runMaterial;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an enabled HUD.
     */
    public PhysicsHud() {
        super("Physics HUD");
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Test whether the physics simulation is paused.
     *
     * @return true if paused, otherwise false
     */
    public boolean isPhysicsPaused() {
        BulletAppState bas = getState(BulletAppState.class);
        float physicsSpeed = bas.getSpeed();
        if (physicsSpeed == 0f) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Single-step the physics simulation, assuming it's paused.
     */
    public void singleStepPhysics() {
        assert isPhysicsPaused();

        BulletAppState bas = getState(BulletAppState.class);
        PhysicsSpace space = bas.getPhysicsSpace();
        float timeStep = space.getAccuracy();
        space.update(timeStep, 0);
    }

    /**
     * Toggle the physics simulation between running and paused.
     */
    public void togglePhysicsPaused() {
        BulletAppState bas = getState(BulletAppState.class);
        if (isPhysicsPaused()) {
            bas.setSpeed(1f);
            showPauseButton(false);
            hideSingleStepButton();
        } else {
            bas.setSpeed(0f);
            showPauseButton(true);
            showSingleStepButton();
        }
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
        Camera camera = application.getCamera();
        this.viewPortHeight = camera.getHeight();
        this.viewPortWidth = camera.getWidth();

        // pre-load unshaded materials for buttons
        AssetManager manager = application.getAssetManager();
        Texture texture = manager.loadTexture("/Textures/Georg/pause.png");
        this.pauseMaterial = MyAsset.createUnshadedMaterial(manager, texture);

        texture = manager.loadTexture("/Textures/Georg/run.png");
        this.runMaterial = MyAsset.createUnshadedMaterial(manager, texture);
        /*
         * Construct a Geometry for the single-step button.
         */
        float radius = 0.025f * viewPortHeight;
        int numVertices = 25;
        Mesh mesh = new DiscMesh(radius, numVertices);
        singleStepButton = new Geometry("single-step button", mesh);
        texture = manager.loadTexture("/Textures/Georg/single-step.png");
        Material material = MyAsset.createUnshadedMaterial(manager, texture);
        singleStepButton.setMaterial(material);
        /*
         * Add an Expander to single-step the simulation.
         */
        Expander listener = new Expander(singleStepButton) {
            @Override
            public void onClick(boolean isPressed) {
                if (isPressed) {
                    singleStepPhysics();
                }
            }
        };
        Control control = new MouseEventControl(listener);
        singleStepButton.addControl(control);
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        hidePauseButton();
        hideSingleStepButton();
    }

    /**
     * Callback invoked whenever this AppState becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        boolean isPaused = isPhysicsPaused();
        showPauseButton(isPaused);
        if (isPaused) {
            showSingleStepButton();
        }
    }
    // *************************************************************************
    // private methods

    /**
     * Attach the specified Spatial to the GUI node.
     *
     * @param spatial (not null, alias created)
     */
    private void attachToGui(Spatial spatial) {
        SimpleApplication simpleApp = (SimpleApplication) getApplication();
        simpleApp.getGuiNode().attachChild(spatial);
    }

    /**
     * Hide the pause button.
     */
    private void hidePauseButton() {
        if (pauseButton != null) {
            pauseButton.removeFromParent();
            this.pauseButton = null;
        }
    }

    /**
     * Hide the single-step button.
     */
    private void hideSingleStepButton() {
        singleStepButton.removeFromParent();
    }

    /**
     * Display the pause/run button.
     *
     * @param paused true &rarr; show it in the "paused" state, false &rarr;
     * show it in the "running" state
     */
    private void showPauseButton(boolean paused) {
        hidePauseButton();

        float radius = 0.025f * viewPortHeight;
        int numVertices = 25;
        Mesh mesh = new DiscMesh(radius, numVertices);
        this.pauseButton = new Geometry("pause button", mesh);
        attachToGui(pauseButton);

        if (paused) {
            pauseButton.setMaterial(pauseMaterial);
        } else {
            pauseButton.setMaterial(runMaterial);
        }

        // Position the button in the viewport.
        float x = 0.8f * viewPortWidth;
        float y = 0.95f * viewPortHeight;
        pauseButton.setLocalTranslation(x, y, guiZ);

        //Add an Expander to toggle the simulation running/paused.
        Expander listener = new Expander(pauseButton) {
            @Override
            public void onClick(boolean isPressed) {
                if (isPressed) {
                    togglePhysicsPaused();
                }
            }
        };
        MouseEventControl control = new MouseEventControl(listener);
        pauseButton.addControl(control);
    }

    /**
     * Display the single-step button.
     */
    private void showSingleStepButton() {
        if (singleStepButton.getParent() == null) {
            attachToGui(singleStepButton);
        }

        // Position the button in the viewport.
        float x = 0.84f * viewPortWidth;
        float y = 0.95f * viewPortHeight;
        singleStepButton.setLocalTranslation(x, y, guiZ);
    }
}
