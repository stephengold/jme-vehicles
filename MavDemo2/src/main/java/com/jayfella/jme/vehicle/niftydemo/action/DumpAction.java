package com.jayfella.jme.vehicle.niftydemo.action;

import com.jayfella.jme.vehicle.Prop;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.niftydemo.MavDemo2;
import com.jayfella.jme.vehicle.niftydemo.state.DemoState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import java.util.logging.Logger;
import jme3utilities.minie.AppDataFilter;
import jme3utilities.minie.PhysicsDumper;

/**
 * Process actions that start with the word "dump".
 *
 * @author Stephen Gold sgold@sonic.net
 */
final class DumpAction {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(DumpAction.class.getName());
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private DumpAction() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Process an ongoing action that starts with the word "dump".
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action is handled, otherwise false
     */
    static boolean processOngoing(String actionString) {
        MavDemo2 app = MavDemo2.getApplication();
        DemoState demoState = MavDemo2.getDemoState();
        PhysicsDumper dumper = MavDemo2.dumper;
        BulletAppState bas = MavDemo2.findAppState(BulletAppState.class);

        PhysicsSpace physicsSpace = bas.getPhysicsSpace();
        Prop selectedProp = demoState.getSelectedProp();
        Vehicle selectedVehicle = demoState.getVehicles().getSelected();

        boolean handled = true;
        switch (actionString) {
            case Action.dumpCamera:
                Camera camera = app.getCamera();
                dumper.dump(camera);
                break;

            case Action.dumpPhysics:
                dumper.dump(bas);
                break;

            case Action.dumpPhysicsSpace:
                dumper.dump(physicsSpace);
                break;

            case Action.dumpProp:
                if (selectedProp != null) {
                    AppDataFilter selectedPropFilter
                            = new AppDataFilter(selectedProp);
                    dumper.dump(physicsSpace, "", selectedPropFilter);
                }
                break;

            case Action.dumpPropNode:
                if (selectedProp != null) {
                    Node propNode = selectedProp.getNode();
                    dumper.dump(propNode);
                }
                break;

            case Action.dumpRenderManager:
                RenderManager renderManager = app.getRenderManager();
                dumper.dump(renderManager);
                break;

            case Action.dumpRootNode:
                Node rootNode = app.getRootNode();
                dumper.dump(rootNode);
                break;

            case Action.dumpStateManager:
                AppStateManager stateManager = app.getStateManager();
                dumper.dump(stateManager);
                break;

            case Action.dumpVehicle:
                if (selectedVehicle != null) {
                    AppDataFilter selectedVehicleFilter
                            = new AppDataFilter(selectedVehicle);
                    dumper.dump(physicsSpace, "", selectedVehicleFilter);
                }
                break;

            case Action.dumpVehicleNode:
                if (selectedVehicle != null) {
                    Node vehicleNode = selectedVehicle.getNode();
                    dumper.dump(vehicleNode);
                }
                break;

            case Action.dumpViewPort:
                ViewPort viewPort = app.getViewPort();
                dumper.dump(viewPort);
                break;

            default:
                handled = false;
        }

        if (handled) {
            System.out.println();
            System.out.flush();
        }

        return handled;
    }
}
