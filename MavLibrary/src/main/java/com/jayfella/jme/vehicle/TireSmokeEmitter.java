package com.jayfella.jme.vehicle;

import com.jayfella.jme.vehicle.part.Wheel;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.texture.Texture;
import java.util.logging.Logger;

/**
 * Visualize a vehicle's tire smoke, using one ParticleEmitter per wheel.
 *
 * Derived from the TyreSmokeEmitter class in the Advanced Vehicles project.
 */
public class TireSmokeEmitter extends BaseAppState {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(TireSmokeEmitter.class.getName());
    // *************************************************************************
    // fields

    /**
     * reusable temporary ColorRGBA
     */
    final private static ColorRGBA tmpColor = new ColorRGBA();
    private Node rootNode;
    /**
     * emitters, one for each wheel
     */
    private ParticleEmitter[] emitters;
    /**
     * reusable temporary Vector3f
     */
    final private static Vector3f tmpLocation = new Vector3f();

    final private Vehicle vehicle;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an AppState for the specified Vehicle.
     *
     * @param vehicle which Vehicle to visualize smoke for (not null)
     */
    public TireSmokeEmitter(Vehicle vehicle) {
        this.vehicle = vehicle;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Translate all emitters by the specified offset.
     *
     * @param offset the desired offset (in world coordinates, not null,
     * unaffected)
     */
    public void translateAll(Vector3f offset) {
        for (ParticleEmitter emitter : emitters) {
            emitter.killAllParticles(); // TODO warp particles
            emitter.move(offset);
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
        int numWheels = vehicle.countWheels();
        this.emitters = new ParticleEmitter[numWheels];

        this.rootNode = ((SimpleApplication) getApplication()).getRootNode();
        AssetManager assetManager = application.getAssetManager();

        for (int wheelIndex = 0; wheelIndex < numWheels; ++wheelIndex) {
            Wheel wheel = vehicle.getWheel(wheelIndex);
            wheel.tireSmokeColor(tmpColor);
            ParticleEmitter smoke = createEmitter(assetManager, tmpColor);
            emitters[wheelIndex] = smoke;

            wheel.getVehicleWheel().getLocation(tmpLocation);
            smoke.setLocalTranslation(tmpLocation);
            smoke.setName("Tire Smoke");
            smoke.setShadowMode(RenderQueue.ShadowMode.Off);
        }
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        int numWheels = vehicle.countWheels();
        for (int wheelIndex = 0; wheelIndex < numWheels; ++wheelIndex) {
            emitters[wheelIndex].removeFromParent();
        }
    }

    /**
     * Callback invoked whenever this AppState becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        int numWheels = vehicle.countWheels();
        for (int wheelIndex = 0; wheelIndex < numWheels; ++wheelIndex) {
            rootNode.attachChild(emitters[wheelIndex]);
        }
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

        int numWheels = vehicle.countWheels();
        for (int wheelIndex = 0; wheelIndex < numWheels; ++wheelIndex) {
            ParticleEmitter emitter = emitters[wheelIndex];

            Wheel wheel = vehicle.getWheel(wheelIndex);
            float particlesPerSecond;
            if (vehicle.isBurningRubber()) {
                particlesPerSecond = 15f;
            } else {
                float skidFraction = wheel.skidFraction();
                particlesPerSecond = 50f * (skidFraction - 0.25f);
            }

            if (particlesPerSecond > 0f) {
                wheel.getVehicleWheel().getCollisionLocation(tmpLocation);
                emitter.setLocalTranslation(tmpLocation);

                wheel.tireSmokeColor(tmpColor);
                emitter.setStartColor(tmpColor);
                tmpColor.a = 0f;
                emitter.setEndColor(tmpColor);

            } else {
                particlesPerSecond = 0f;
            }
            emitter.setParticlesPerSec(particlesPerSecond);
        }
    }
    // *************************************************************************
    // private methods

    private static ParticleEmitter createEmitter(
            AssetManager assetManager, ColorRGBA startColor) {
        int numParticles = 80;
        ParticleEmitter result = new ParticleEmitter(
                "Emitter", ParticleMesh.Type.Triangle, numParticles);

        Material material = new Material(
                assetManager, "Common/MatDefs/Misc/Particle.j3md");
        result.setMaterial(material);
        RenderState ars = material.getAdditionalRenderState();
        ars.setBlendMode(RenderState.BlendMode.Alpha);

        String assetPath = "/Textures/Georg/smoke.png";
        Texture texture = assetManager.loadTexture(assetPath);
        material.setTexture("Texture", texture);

        result.setGravity(0f, -0.2f, 0f); // less dense than the ambient air
        result.setHighLife(3f);
        result.setLowLife(1f);

        result.setStartColor(startColor);
        ColorRGBA endColor = startColor.clone();
        endColor.a = 0f;
        result.setEndColor(endColor);

        result.setStartSize(0f);
        result.setEndSize(3f);

        return result;
    }
}
