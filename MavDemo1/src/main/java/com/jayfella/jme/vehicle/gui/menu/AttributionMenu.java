package com.jayfella.jme.vehicle.gui.menu;

import com.jayfella.jme.vehicle.examples.Attribution;
import com.jayfella.jme.vehicle.gui.CompassState;
import com.jayfella.jme.vehicle.gui.lemur.AudioHud;
import com.jayfella.jme.vehicle.gui.lemur.CameraNameState;
import com.jayfella.jme.vehicle.gui.lemur.PhysicsHud;
import com.jayfella.jme.vehicle.lemurdemo.MavDemo1;
import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.Materials;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.simsilica.lemur.Button;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * An AnimatedMenu to display attributions.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class AttributionMenu extends AnimatedMenu {
    // *************************************************************************
    // constants and loggers

    /**
     * scale factor applied to the text nodes
     */
    final private static float scaleFactor = 1.6f;
    /**
     * cycle interval (in seconds)
     */
    final private static int cycleSeconds = 30;
    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(AttributionMenu.class.getName());
    /**
     * payload message, to be displayed
     */
    final private static String attributionMessage = Attribution.plainMessage(
            Attribution.opelGtRetopo,
            Attribution.fordRanger,
            Attribution.nissanGtr,
            Attribution.hcr2Buggy,
            Attribution.hcr2Rotator,
            Attribution.modernHatchbackLowPoly,
            Attribution.batcPack,
            Attribution.raceSuit,
            Attribution.classicMotorcycle);
    // *************************************************************************
    // fields

    /**
     * first copy of the message
     */
    private BitmapText bitmapText1;
    /**
     * 2nd copy of the message
     */
    private BitmapText bitmapText2;
    /**
     * height of the GUI viewport (in pixels)
     */
    private float viewPortHeight;
    /**
     * width of the GUI viewport (in pixels)
     */
    private float viewPortWidth;
    /**
     * visual presentation
     */
    final private Node node = new Node("Attribution Node");
    // *************************************************************************
    // AnimatedMenuState methods

    /**
     * Generate the items for this menu.
     *
     * @return a new array of GUI buttons
     */
    @Override
    protected List<Button> createItems() {
        List<Button> result = new ArrayList<>(1);
        addButton(result, "<< Back",
                source -> animateOut(() -> goTo(new MainMenu())));

        return result;
    }

    /**
     * Callback invoked after this AppState is attached but before onEnable().
     *
     * @param application the application instance (not null)
     */
    @Override
    protected void initialize(Application application) {
        super.initialize(application);

        Camera camera = application.getCamera();
        viewPortHeight = camera.getHeight();
        viewPortWidth = camera.getWidth();
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        node.removeFromParent();

        getState(AudioHud.class).setEnabled(true);
        getState(CameraNameState.class).setEnabled(true);
        getState(CompassState.class).setEnabled(true);
        getState(PhysicsHud.class).setEnabled(true);

        super.onDisable();
    }

    /**
     * Callback invoked whenever this menu becomes both attached and enabled.
     */
    @Override
    protected void onEnable() {
        super.onEnable();

        getState(AudioHud.class).setEnabled(false);
        getState(CameraNameState.class).setEnabled(false);
        getState(CompassState.class).setEnabled(false);
        getState(PhysicsHud.class).setEnabled(false);

        node.detachAllChildren();
        setupShutter();
        setupText();
        MavDemo1.getApplication().getGuiNode().attachChild(node);
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
        /*
         * Re-position the text nodes
         * to create the illusion of continuous scrolling.
         */
        float textWidth = scaleFactor * bitmapText1.getLineWidth();
        float x = (viewPortWidth - textWidth) / 2;
        float textHeight = scaleFactor * bitmapText1.getHeight();

        long millis = System.currentTimeMillis();
        long cycleMillis = 1000L * cycleSeconds;
        float seconds = 0.001f * (millis % cycleMillis);
        float y1 = viewPortHeight + textHeight * (seconds / cycleSeconds);
        bitmapText1.setLocalTranslation(x, y1, 0f);

        float y2 = y1 - textHeight;
        bitmapText2.setLocalTranslation(x, y2, 0f);
    }
    // *************************************************************************
    // private methods

    /**
     * Create and attach a Quad to hide what's happening in the main scene.
     */
    private void setupShutter() {
        AssetManager assetManager = MavDemo1.getApplication().getAssetManager();
        Material material = new Material(assetManager, Materials.UNSHADED);
        material.setColor("Color", new ColorRGBA(0f, 0f, 0f, 1f));

        Mesh mesh = new Quad(viewPortWidth, viewPortHeight);
        Geometry shutter = new Geometry("shutter", mesh);
        node.attachChild(shutter);

        shutter.move(0f, 0f, -1f);
        shutter.setMaterial(material);
    }

    /**
     * Create and attach a TrueTypeNode containing the attribution message.
     */
    private void setupText() {
        MavDemo1 application = MavDemo1.getApplication();
        AssetManager assetManager = application.getAssetManager();
        BitmapFont bigFont
                = assetManager.loadFont("/Interface/Fonts/Default.fnt");

        bitmapText1 = new BitmapText(bigFont);
        node.attachChild(bitmapText1);
        bitmapText1.setLocalScale(scaleFactor);
        bitmapText1.setText(attributionMessage);

        bitmapText2 = new BitmapText(bigFont);
        node.attachChild(bitmapText2);
        bitmapText2.setLocalScale(scaleFactor);
        bitmapText2.setText(attributionMessage);
    }
}
