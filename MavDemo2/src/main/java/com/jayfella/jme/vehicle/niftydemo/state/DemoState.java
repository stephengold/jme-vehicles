package com.jayfella.jme.vehicle.niftydemo.state;

import com.jayfella.jme.vehicle.GlobalAudio;
import com.jayfella.jme.vehicle.Prop;
import com.jayfella.jme.vehicle.Sky;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.World;
import com.jayfella.jme.vehicle.examples.worlds.Playground;
import com.jayfella.jme.vehicle.niftydemo.MavDemo2;
import com.jayfella.jme.vehicle.niftydemo.view.Cameras;
import com.jayfella.jme.vehicle.niftydemo.view.View;
import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.system.Timer;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import jme3utilities.Validate;
import jme3utilities.math.MyVector3f;
import jme3utilities.math.noise.Generator;

/**
 * The "game state" of MavDemo2.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class DemoState
        implements GlobalAudio, PhysicsTickListener {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(DemoState.class.getName());
    // *************************************************************************
    // fields

    /**
     * true&rarr;audio is globally muted, false&rarr;audio enabled
     */
    private boolean isAudioGloballyMuted = false;
    /**
     * elapsed physics time (in seconds, &ge;0)
     */
    private double elapsedTime;
    /**
     * overall audio volume when not muted (log scale, &ge;0, &le;1)
     */
    private float maLogVolume = 0.5f;
    /**
     * pseudo-random number generator
     */
    final private Generator prGenerator;
    /**
     * number of timer ticks during the most recent physics tick
     */
    private long numTicks;
    /**
     * timer tick count at the start of the most recent physics tick
     */
    private long preTickCount;
    /**
     * selected Prop, or null if none
     */
    private Prop selectedProp;
    /**
     * proposal for the next Prop
     */
    private PropProposal propProposal;
    /**
     * state of all vehicles
     */
    final private Vehicles vehicles;
    /**
     * selected World (not null)
     */
    private World world = new Playground();
    // *************************************************************************
    // constructors

    /**
     * Instantiate an initial state with no vehicle and default settings.
     *
     * @param physicsSpace (not null)
     */
    public DemoState(PhysicsSpace physicsSpace) {
        this.elapsedTime = 0.0;
        this.prGenerator = new Generator();
        this.numTicks = 0;
        this.propProposal = new PropProposal();

        physicsSpace.addTickListener(this);

        MavDemo2 application = MavDemo2.getApplication();
        Sky.setApplication(application);
        Sky.initialize();

        Node rootNode = application.getRootNode();
        world.attach(application, rootNode, physicsSpace);

        this.vehicles = new Vehicles(this);

        View view = MavDemo2.findAppState(View.class);
        Sky sky = view.getSky();
        sky.addToWorld(world);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Add a new Prop based on the active proposal, select it, and deactivate
     * the proposal.
     */
    public void addProp() {
        if (!propProposal.isActive()) {
            return;
        }

        float minCosine = 0.8f;
        float spacing = 0f;
        Vector3f supportLocation = new Vector3f();
        PhysicsRigidBody body
                = pickSupportBody(minCosine, spacing, supportLocation);
        if (body == null) {
            propProposal.invalidate();
            return;
        }

        Prop prop = propProposal.create();
        AssetManager assetManager = world.getAssetManager();
        prop.load(assetManager);

        Quaternion orientation = propProposal.orientation(null);
        float dropHeight = 1.5f * prop.scaledHeight(orientation);
        Vector3f dropLocation = supportLocation.add(0f, dropHeight, 0f);
        prop.addToWorld(world, dropLocation, orientation);
        selectProp(prop);

        propProposal.setActive(false);
    }

    /**
     * Add the specified Vehicle and select it.
     *
     * @param vehicle (not null)
     */
    final public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
        vehicles.select(vehicle);

        Cameras.resetFov();
        world.resetCameraPosition();
    }

    /**
     * Delete all props.
     */
    public void deleteAllProps() {
        Collection<Prop> collection = world.listProps();
        int numProps = collection.size();
        Prop[] array = new Prop[numProps];
        collection.toArray(array);
        for (Prop prop : array) {
            prop.removeFromWorld();
        }

        this.selectedProp = null;
    }

    /**
     * Read the elapsed time.
     *
     * @return elapsed physics time (in seconds, &ge;0)
     */
    public double elapsedTime() {
        assert elapsedTime >= 0.0 : elapsedTime;
        return elapsedTime;
    }

    /**
     * Access the pseudo-random generator.
     *
     * @return the pre-existing instance (not null)
     */
    public Generator getPrGenerator() {
        assert prGenerator != null;
        return prGenerator;
    }

    /**
     * Access the PropProposal.
     *
     * @return the pre-existing instance (not null)
     */
    public PropProposal getPropProposal() {
        assert propProposal != null;
        return propProposal;
    }

    /**
     * Access the selected Prop.
     *
     * @return the pre-existing instance, or null if none
     */
    public Prop getSelectedProp() {
        return selectedProp;
    }

    /**
     * Access the list of vehicles.
     *
     * @return the pre-existing instance (not null)
     */
    public Vehicles getVehicles() {
        assert vehicles != null;
        return vehicles;
    }

    /**
     * Access the loaded World.
     *
     * @return the pre-existing instance (not null)
     */
    public World getWorld() {
        assert world != null;
        return world;
    }

    /**
     * Test whether audio is globally muted.
     *
     * @return true if muted, otherwise false
     */
    public boolean isMuted() {
        return isAudioGloballyMuted;
    }

    /**
     * Determine the overall audio volume when not muted
     *
     * @return the volume level (log scale, &ge;0, &le;1)
     */
    public float masterAudioLogVolume() {
        assert maLogVolume >= 0f : maLogVolume;
        assert maLogVolume <= 1f : maLogVolume;
        return maLogVolume;
    }

    /**
     * Pick the nearest supporting surface under the mouse cursor using a
     * physics ray.
     *
     * @param minCosine the minimum cosine of the slope angle (&le;1, &gt;-1)
     * @param spacing the extra separation in the normal direction (in psu)
     * @param storeLocation storage for the location in physics-space
     * coordinates (not null)
     * @return a pre-existing rigid body, or null if none found
     */
    public PhysicsRigidBody pickSupportBody(
            float minCosine, float spacing, Vector3f storeLocation) {
        Validate.inRange(minCosine, "min cosine", -1f, 1f);
        Validate.nonNull(storeLocation, "storage for location");

        Vector3f near = new Vector3f();
        Vector3f far = new Vector3f();
        View view = MavDemo2.findAppState(View.class);
        view.mouseRay(near, far);

        PhysicsSpace physicsSpace = world.getPhysicsSpace();
        List<PhysicsRayTestResult> hits = physicsSpace.rayTestRaw(near, far);

        // Find the closest contact that's flat enough.
        Vector3f closestWorldNormal = null;
        float closestFraction = 9f;
        PhysicsRigidBody closestBody = null;
        for (PhysicsRayTestResult hit : hits) {
            Vector3f worldNormal = hit.getHitNormalLocal(null);
            if (worldNormal.y > minCosine) {
                float hitFraction = hit.getHitFraction();
                if (hitFraction < closestFraction) {
                    PhysicsCollisionObject pco = hit.getCollisionObject();
                    if (pco instanceof PhysicsRigidBody) {
                        closestWorldNormal = worldNormal; // alias
                        closestFraction = hitFraction;
                        closestBody = (PhysicsRigidBody) pco;
                    }
                }
            }
        }

        if (closestBody != null) {
            Vector3f location
                    = MyVector3f.lerp(closestFraction, near, far, null);
            MyVector3f.accumulateScaled(location, closestWorldNormal, spacing);
            storeLocation.set(location);
        }

        return closestBody;
    }

    /**
     * Reset the elapsed-time accumulator.
     */
    public void resetElapsedTime() {
        this.elapsedTime = 0.0;
    }

    /**
     * Select the specified prop.
     *
     * @param prop the desired prop (alias created)
     */
    public void selectProp(Prop prop) {
        this.selectedProp = prop;
    }

    /**
     * Alter the overall audio volume when not muted.
     *
     * @param logVolume the desired volume (log scale, &ge;0, &le;1)
     */
    public void setMasterAudioLogVolume(float logVolume) {
        Validate.fraction(logVolume, "log volume");
        this.maLogVolume = logVolume;
    }

    /**
     * Alter whether audio is globally muted.
     *
     * @param setting true to mute all audio, false to enable audio
     */
    public void setMuted(boolean setting) {
        this.isAudioGloballyMuted = setting;
    }

    /**
     * Replace the selected vehicle with an unloaded one.
     *
     * @param newVehicle (not loaded)
     */
    public void setVehicle(Vehicle newVehicle) {
        AssetManager assetManager = world.getAssetManager();
        newVehicle.load(assetManager);

        vehicles.removeSelected();

        addVehicle(newVehicle);
        Cameras.update();
    }

    /**
     * Replace the current World with an unloaded one.
     *
     * @param newWorld (not loaded)
     */
    public void setWorld(World newWorld) {
        AssetManager assetManager = world.getAssetManager();
        newWorld.load(assetManager);

        Vehicle selectedVehicle = vehicles.getSelected();
        vehicles.removeAll();

        View view = MavDemo2.findAppState(View.class);
        Sky sky = view.getSky();
        sky.removeFromWorld();

        PhysicsSpace physicsSpace = world.getPhysicsSpace();
        Node parentNode = world.getParentNode();
        world.detach();
        this.world = newWorld;
        Application application = MavDemo2.getApplication();
        world.attach(application, parentNode, physicsSpace);

        sky.addToWorld(world);
        if (selectedVehicle != null) {
            addVehicle(selectedVehicle);
        }
    }

    /**
     * Calculate the duration of the most recent physics tick.
     *
     * @return the duration (in seconds, &ge;0)
     */
    public float tickDuration() {
        Timer timer = MavDemo2.getApplication().getTimer();
        long numTicksPerSecond = timer.getResolution();
        float result = numTicks / (float) numTicksPerSecond;

        assert result >= 0f : result;
        return result;
    }
    // *************************************************************************
    // GlobalAudio methods

    /**
     * Determine the effective global audio volume.
     *
     * @return the volume (linear scale, &ge;0, &le;1)
     */
    @Override
    public float effectiveVolume() {
        float result;
        if (isAudioGloballyMuted) {
            result = 0f;
        } else {
            result = FastMath.pow(0.003f, 1f - maLogVolume);
        }

        return result;
    }
    // *************************************************************************
    // PhysicsTickListener methods

    /**
     * Callback from Bullet, invoked just after the physics has been stepped.
     *
     * @param space the space that was just stepped (not null)
     * @param timeStep the time per physics step (in seconds, &ge;0)
     */
    @Override
    public void physicsTick(PhysicsSpace space, float timeStep) {
        assert space == world.getPhysicsSpace();
        assert timeStep >= 0f : timeStep;

        this.elapsedTime += timeStep;

        Timer timer = MavDemo2.getApplication().getTimer();
        this.numTicks = timer.getTime() - preTickCount;
    }

    /**
     * Callback from Bullet, invoked just before the physics is stepped.
     *
     * @param space the space that is about to be stepped (not null)
     * @param timeStep the time per physics step (in seconds, &ge;0)
     */
    @Override
    public void prePhysicsTick(PhysicsSpace space, float timeStep) {
        assert space == world.getPhysicsSpace();

        Timer timer = MavDemo2.getApplication().getTimer();
        this.preTickCount = timer.getTime();
    }
}
