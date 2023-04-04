package com.jayfella.jme.vehicle.view;

import com.github.stephengold.garrett.OrbitCamera;
import com.github.stephengold.garrett.Target;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.lemurdemo.MavDemo1;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.debug.BulletDebugAppState;
import com.jme3.math.Vector3f;
import java.util.logging.Logger;
import jme3utilities.Validate;
import jme3utilities.minie.NegativeAppDataFilter;

/**
 * A CameraController to orbit a target vehicle, jumping forward as needed to
 * maintain a clear line of sight in the vehicle's CollisionSpace. Two chasing
 * behaviors are implemented: FreeOrbit and StrictChase.
 *
 * @author Stephen Gold sgold@sonic.net
 */
final public class ChaseCamera extends CameraController {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(ChaseCamera.class.getName());
    // *************************************************************************
    // fields

    /**
     * configured target-chasing behavior (not null)
     */
    final private ChaseOption chaseOption;
    /**
     * how much to displace the camera target (0=center of mass, 1=back bumper)
     */
    final private float rearBias;
    /**
     * AppState to control the Camera
     */
    private OrbitCamera orbitCamera;
    /**
     * camera target
     */
    private Target target;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a controller that orbits (and optionally chases) a Target.
     *
     * @param chaseOption to configure chase behavior (not null)
     * @param rearBias how much to displace the camera target (0=center of mass,
     * 1=back bumper)
     */
    public ChaseCamera(ChaseOption chaseOption, float rearBias) {
        super(MavDemo1.getVehicle(), MavDemo1.getApplication().getCamera(),
                null);
        Validate.nonNull(chaseOption, "chase option");

        this.chaseOption = chaseOption;
        this.rearBias = rearBias;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Determine the configured ChaseOption.
     *
     * @return the enum value
     */
    public ChaseOption getChaseOption() {
        return chaseOption;
    }

    /**
     * Alter the offset of the camera from the target.
     *
     * @param desiredOffset the desired offset from the target (in world
     * coordinates)
     */
    public void setOffset(Vector3f desiredOffset) {
        Validate.finite(desiredOffset, "desired offset");
        orbitCamera.setOffset(desiredOffset);
    }

    @Override
    public void update(float tpf) {
        // do nothing
    }
    // *************************************************************************
    // VehicleCamera methods

    @Override
    public void attach() {
        orbitCamera = MavDemo1.findAppState(OrbitCamera.class);
        chaseOption.configure(orbitCamera);
        orbitCamera.setTarget(target);
        BulletDebugAppState.DebugAppStateFilter obstructionFilter
                = new NegativeAppDataFilter(vehicle);
        orbitCamera.setObstructionFilter(obstructionFilter);
        orbitCamera.setEnabled(true);
    }

    @Override
    public void detach() {
        orbitCamera.setEnabled(false);
    }

    /**
     * Alter which Vehicle the camera is targeting.
     *
     * @param newVehicle the desired target vehicle (not null)
     */
    @Override
    public void setVehicle(Vehicle newVehicle) {
        Validate.nonNull(newVehicle, "new vehicle");
        super.setVehicle(newVehicle);

        target = new Target() {
            @Override
            public Vector3f forwardDirection(Vector3f storeResult) {
                Vector3f result = newVehicle.forwardDirection(storeResult);
                return result;
            }

            @Override
            public PhysicsCollisionObject getTargetPco() {
                VehicleControl result = newVehicle.getVehicleControl();
                return result;
            }

            @Override
            public Vector3f locateTarget(Vector3f storeResult) {
                Vector3f result
                        = newVehicle.locateTarget(rearBias, storeResult);
                return result;
            }
        };
        if (orbitCamera != null) {
            orbitCamera.setTarget(target);
            BulletDebugAppState.DebugAppStateFilter obstructionFilter
                    = new NegativeAppDataFilter(newVehicle);
            orbitCamera.setObstructionFilter(obstructionFilter);
            orbitCamera.setRangeAndOffset();
        }
    }
}
