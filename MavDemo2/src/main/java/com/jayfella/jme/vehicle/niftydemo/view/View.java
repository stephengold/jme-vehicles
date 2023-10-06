package com.jayfella.jme.vehicle.niftydemo.view;

import com.jayfella.jme.vehicle.Prop;
import com.jayfella.jme.vehicle.PropWorld;
import com.jayfella.jme.vehicle.Sky;
import com.jayfella.jme.vehicle.VehicleWorld;
import com.jayfella.jme.vehicle.examples.skies.QuarrySky;
import com.jayfella.jme.vehicle.gui.VehiclePointsState;
import com.jayfella.jme.vehicle.niftydemo.MavDemo2;
import com.jayfella.jme.vehicle.niftydemo.state.DemoState;
import com.jayfella.jme.vehicle.niftydemo.state.PropProposal;
import com.jme3.app.Application;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import java.util.List;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MySpatial;
import jme3utilities.SimpleAppState;
import jme3utilities.debug.PerformanceAppState;
import jme3utilities.ui.ActionApplication;

/**
 * The SimpleAppState responsible for 3-D visualization. It manages the default
 * root node and its scene processors.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class View extends SimpleAppState {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(View.class.getName());
    // *************************************************************************
    // fields

    /**
     * scene-graph subtree for visualizing the PropProposal
     */
    final private Node propProposalNode = new Node("prop proposal");
    /**
     * performance-monitoring mode
     */
    private PerformanceMode performanceMode = PerformanceMode.Off;
    /**
     * selected sky, including lights and post-processing (not null)
     */
    private Sky sky = new QuarrySky();
    /**
     * options for debug visualization of swept spheres
     */
    final private SweptSphereFilter ssFilter = new SweptSphereFilter();
    /**
     * options for debug visualization of physics joints and shapes
     */
    final private ViewPhysics viewPhysics = new ViewPhysics();
    // *************************************************************************
    // constructors

    /**
     * Instantiate with the default settings.
     */
    public View() {
        super(true);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Access the sky.
     *
     * @return the pre-existing instance (not null)
     */
    public Sky getSky() {
        assert sky != null;
        return sky;
    }

    /**
     * Test whether the specified view flag is set.
     *
     * @param viewFlag which flag to test (not null)
     * @return true if visualization is enabled, otherwise false
     */
    public boolean isEnabled(ViewFlags viewFlag) {
        boolean result;

        switch (viewFlag) {
            case PropSpheres:
            case VehicleSpheres:
                result = ssFilter.isEnabled(viewFlag);
                break;

            case Shadows:
                result = Sky.areShadowsEnabled();
                break;

            case VehiclePoints:
                VehiclePointsState vps
                        = stateManager.getState(VehiclePointsState.class);
                result = vps.isEnabled();
                break;

            default:
                result = viewPhysics.isEnabled(viewFlag);
        }
        return result;
    }

    /**
     * Calculate the world locations where the mouse ray intersects the near and
     * far planes of the main camera.
     *
     * @param storeNear storage for the near location (modified if not null)
     * @param storeFar storage for the far location (modified if not null)
     */
    public void mouseRay(Vector3f storeNear, Vector3f storeFar) {
        Vector2f screenXY = inputManager.getCursorPosition();

        // Convert screen coordinates to world coordinates.
        if (storeNear != null) {
            cam.getWorldCoordinates(screenXY, 0f, storeNear);
        }
        if (storeFar != null) {
            cam.getWorldCoordinates(screenXY, 1f, storeFar);
        }
    }

    /**
     * Save the current scene (less controls) to a J3O file and then immediately
     * terminate the application. For debugging.
     */
    public void saveScene() {
        List<Spatial> list = MySpatial.listSpatials(rootNode);
        for (Spatial spatial : list) {
            int numControls = spatial.getNumControls();
            for (int controlI = numControls - 1; controlI >= 0; --controlI) {
                Control control = spatial.getControl(controlI);
                spatial.removeControl(control);
            }
        }

        String filePath = ActionApplication.filePath("saveScene.j3o");
        Heart.writeJ3O(filePath, rootNode);
        System.exit(0);
    }

    /**
     * Select the next PerformanceMode in the cycle.
     */
    public void selectNextPerformanceMode() {
        switch (performanceMode) {
            case Off:
                this.performanceMode = PerformanceMode.JmeStats;
                break;

            case JmeStats:
                this.performanceMode = PerformanceMode.DebugPas;
                break;

            case DebugPas:
                this.performanceMode = PerformanceMode.Off;
                break;

            default:
        }
    }

    /**
     * Configure the specified view flag.
     *
     * @param viewFlag which flag to set (not null)
     * @param newValue true to enable visualization, false to disable it
     */
    public void setEnabled(ViewFlags viewFlag, boolean newValue) {
        switch (viewFlag) {
            case PropSpheres:
            case VehicleSpheres:
                ssFilter.setEnabled(viewFlag, newValue);
                break;

            case Shadows:
                Sky.setShadowsEnabled(newValue);
                break;

            case VehiclePoints:
                VehiclePointsState vps
                        = stateManager.getState(VehiclePointsState.class);
                vps.setEnabled(newValue);
                break;

            default:
                viewPhysics.setEnabled(viewFlag, newValue);
        }
    }

    /**
     * Replace the currently selected sky with the specified one.
     *
     * @param newSky (not null)
     */
    public void setSky(Sky newSky) {
        if (sky != null) {
            sky.removeFromWorld();
        }
        this.sky = newSky;
        DemoState demoState = MavDemo2.getDemoState();
        VehicleWorld world = demoState.getWorld();
        sky.addToWorld(world);
    }
    // *************************************************************************
    // SimpleAppState methods

    /**
     * Initialize this state on the first update after it gets attached. Should
     * be invoked only by a subclass or by the AppStateManager.
     *
     * @param sm the manager for this state (not null)
     * @param app the application which owns this state (not null)
     */
    @Override
    public void initialize(AppStateManager sm, Application app) {
        super.initialize(sm, app);

        BulletAppState bas = MavDemo2.findAppState(BulletAppState.class);
        bas.setDebugEnabled(true);
        bas.setDebugFilter(viewPhysics);
        bas.setDebugSweptSphereFilter(ssFilter);

        rootNode.attachChild(propProposalNode);
    }

    /**
     * Callback to update this state prior to rendering. (Invoked once per frame
     * while the state is attached and enabled.)
     *
     * @param elapsedTime the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void update(float elapsedTime) {
        super.update(elapsedTime);

        updatePerformance();
        updatePropProposal();
    }
    // *************************************************************************
    // private methods

    private void updatePerformance() {
        PerformanceAppState pas
                = MavDemo2.findAppState(PerformanceAppState.class);
        StatsAppState sas = stateManager.getState(StatsAppState.class);
        switch (performanceMode) {
            case DebugPas:
                pas.setEnabled(true);
                sas.setDisplayFps(false);
                sas.setDisplayStatView(false);
                break;

            case JmeStats:
                pas.setEnabled(false);
                sas.setDisplayFps(true);
                sas.setDisplayStatView(true);
                break;

            case Off:
                pas.setEnabled(false);
                sas.setDisplayFps(false);
                sas.setDisplayStatView(false);
                break;

            default:
                String message = String
                        .format("invalid PerformanceMode: %s", performanceMode);
                throw new IllegalStateException(message);
        }
    }

    /**
     * Visualize the PropProposal (if active) and update its validity and
     * location.
     */
    private void updatePropProposal() {
        propProposalNode.detachAllChildren();

        DemoState demoState = MavDemo2.getDemoState();
        PropProposal proposal = demoState.getPropProposal();
        if (!proposal.isActive()) {
            return;
        }

        float minCosine = 0.8f;
        float spacing = 0f;
        Vector3f supportLocation = new Vector3f();
        PhysicsRigidBody body = demoState
                .pickSupportBody(minCosine, spacing, supportLocation);
        if (body == null) {
            proposal.invalidate();
            return;
        }

        Prop prop = proposal.create();
        prop.load(assetManager);
        Quaternion orientation = proposal.orientation(null);
        float dropHeight = 1.5f * prop.scaledHeight(orientation);
        Vector3f dropLocation = supportLocation.add(0f, dropHeight, 0f);
        PropWorld world = demoState.getWorld();
        Vector3f startLocation
                = prop.findStartLocation(world, dropLocation, orientation);
        Node cgmRoot = prop.getNode();
        cgmRoot.removeControl(RigidBodyControl.class);
        cgmRoot.setLocalTranslation(startLocation);
        propProposalNode.attachChild(cgmRoot);
    }
}
