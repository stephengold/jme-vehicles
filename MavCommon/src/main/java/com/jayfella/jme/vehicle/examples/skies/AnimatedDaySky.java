package com.jayfella.jme.vehicle.examples.skies;

import com.jayfella.jme.vehicle.Sky;
import com.jayfella.jme.vehicle.VehicleWorld;
import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.light.DirectionalLight;
import com.jme3.light.LightProbe;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import java.util.logging.Logger;
import jme3utilities.sky.LunarPhase;
import jme3utilities.sky.SkyControl;
import jme3utilities.sky.StarsOption;
import jme3utilities.sky.SunAndStars;
import jme3utilities.sky.Updater;

/**
 * An example of a daytime sky using SkyControl.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class AnimatedDaySky extends Sky {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(AnimatedDaySky.class.getName());
    /**
     * path to the LightProbe asset
     */
    final public static String lightProbeAssetPath
            = "/Textures/skies/moon/probe-day.j3o";
    // *************************************************************************
    // fields

    /**
     * manage the sky simulation
     */
    private SkyControl skyControl;
    // *************************************************************************
    // new methods exposed

    /**
     * Create and configure a SkyControl for this example.
     *
     * @param application the application instance (not null)
     * @return a new Control
     */
    public static SkyControl createSkyControl(Application application) {
        AssetManager assetManager = application.getAssetManager();
        Camera camera = application.getCamera();
        float cloudFlattening = 0.8f;
        boolean bottomDome = true;
        SkyControl result = new SkyControl(assetManager, camera,
                cloudFlattening, StarsOption.TopDome, bottomDome);
        result.setCloudiness(0.8f);
        result.setCloudsYOffset(0.4f);
        result.setPhase(LunarPhase.WAXING_GIBBOUS);
        result.setSunStyle("/Textures/skies/suns/chaotic.png");

        SunAndStars sunAndStars = result.getSunAndStars();
        sunAndStars.setHour(16f); // 4 p.m.
        sunAndStars.setObserverLatitude(0.63f); // 36 degrees north

        return result;
    }
    // *************************************************************************
    // Sky methods

    /**
     * Add this Sky to the specified world.
     *
     * @param world where to add (not null)
     */
    @Override
    public void addToWorld(VehicleWorld world) {
        super.addToWorld(world);

        // Configure the AmbientLight that was added by Sky.initialize().
        ColorRGBA ambientColor = new ColorRGBA(0.8f, 0.8f, 0.65f, 1f);
        getAmbientLight().setColor(ambientColor);

        // Configure the DirectionalLight that was added by Sky.initialize().
        Updater updater = skyControl.getUpdater();
        DirectionalLight mainLight = getDirectionalLight();
        updater.setMainLight(mainLight);
        updater.setMainMultiplier(1.2f);

        // Configure the shadow renderer that was added by Sky.initialize().
        DirectionalLightShadowRenderer shadowRenderer = getShadowRenderer();
        updater.addShadowRenderer(shadowRenderer);
    }

    /**
     * Load this Sky from assets.
     *
     * @param assetManager for loading assets (not null)
     */
    @Override
    public void load(AssetManager assetManager) {
        super.load(assetManager);

        Application application = getApplication();
        this.skyControl = createSkyControl(application);

        Node node = new Node();
        node.addControl(skyControl);
        skyControl.setEnabled(true);

        LightProbe probe
                = (LightProbe) assetManager.loadAsset(lightProbeAssetPath);
        build(node, probe);
    }
}
