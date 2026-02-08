package com.jayfella.jme.vehicle.gui;

import com.jayfella.jme.vehicle.VehicleSteering;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.texture.Texture;
import java.util.logging.Logger;
import jme3utilities.Loadable;
import jme3utilities.MyAsset;
import jme3utilities.mesh.RectangleMesh;

/**
 * Appstate to animate a steering wheel in the GUI node. New instances are
 * disabled by default.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class SteeringWheelState extends BaseAppState implements Loadable {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(SteeringWheelState.class.getName());
    // *************************************************************************
    // fields

    /**
     * radius of the wheel (in pixels)
     */
    final private float radius;

    private Geometry geometry;
    private Material material;
    /**
     * scene-graph node to attach GUI spatials
     */
    private Node guiNode;
    /**
     * coordinates of the wheel's center (in pixels, from the bottom left of the
     * GUI viewport)
     */
    final private Vector3f center = new Vector3f();
    /**
     * corresponding vehicle, or null if none assigned
     */
    private VehicleSteering vehicle;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a disabled AppState.
     *
     * @param radius the radius of the wheel (in pixels, &ge;0)
     * @param center the coordinates for the wheel's center (in pixels, from the
     * bottom left of the GUI viewport)
     */
    public SteeringWheelState(float radius, Vector3f center) {
        super("Steering Wheel");
        super.setEnabled(false);

        this.radius = radius;
        this.center.set(center);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Associate a vehicle with this AppState prior to enabling it.
     *
     * @param vehicle the vehicle to use (or null for none)
     */
    public void setVehicle(VehicleSteering vehicle) {
        assert !isEnabled();
        this.vehicle = vehicle;
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
        guiNode = ((SimpleApplication) application).getGuiNode();

        AssetManager assetManager = application.getAssetManager();
        load(assetManager);

        // Construct a Geometry for the steering wheel.
        RectangleMesh mesh
                = new RectangleMesh(-radius, +radius, -radius, +radius, +1f);
        this.geometry = new Geometry("steering wheel", mesh);
        geometry.setMaterial(material);

        // Position the Geometry in the GUI viewport.
        geometry.move(center);
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        geometry.removeFromParent();
    }

    /**
     * Callback invoked whenever this AppState becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        guiNode.attachChild(geometry);
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

        // Re-orient the steering wheel.
        float angle = vehicle.steeringWheelAngle();
        Quaternion orientation = new Quaternion(); // TODO garbage
        orientation.fromAngles(0f, 0f, angle);
        geometry.setLocalRotation(orientation);
    }
    // *************************************************************************
    // Loadable methods

    /**
     * Load the assets of this Loadable without attaching them to anything.
     *
     * @param assetManager for loading assets (not null)
     */
    @Override
    public void load(AssetManager assetManager) {
        Texture texture
                = assetManager.loadTexture("/Textures/Georg/steering.png");
        this.material = MyAsset.createUnshadedMaterial(assetManager, texture);
        RenderState ars = material.getAdditionalRenderState();
        ars.setBlendMode(RenderState.BlendMode.Alpha);
    }
}
