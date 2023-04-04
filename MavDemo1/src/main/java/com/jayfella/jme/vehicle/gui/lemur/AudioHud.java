package com.jayfella.jme.vehicle.gui.lemur;

import com.jayfella.jme.vehicle.GlobalAudio;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import com.simsilica.lemur.event.MouseEventControl;
import java.util.logging.Logger;
import jme3utilities.MyAsset;
import jme3utilities.mesh.DiscMesh;
import jme3utilities.mesh.RoundedRectangle;

/**
 * Heads-up display (HUD) for controlling audio. This AppState should be
 * instantiated once and then enabled/disabled as needed. It directly manages
 * the mute button and the master-volume control.
 */
public class AudioHud
        extends BaseAppState
        implements GlobalAudio {
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
            = Logger.getLogger(AudioHud.class.getName());
    // *************************************************************************
    // fields

    /**
     * true&rarr;the slider is being dragged
     */
    private boolean isDraggingSlider = false;
    /**
     * true&rarr;audio is globally muted, false&rarr;audio enabled
     */
    private static boolean isGloballyMuted = false;
    /**
     * overall audio volume when not muted (log scale, &ge;0, &le;1)
     */
    private static float masterVolume = 0.5f;
    /**
     * height of the GUI viewport (in pixels)
     */
    private float viewPortHeight;
    /**
     * width of the GUI viewport (in pixels)
     */
    private float viewPortWidth;
    /**
     * visualize the expanding/sliding part of the master-volume GUI control
     */
    private Geometry mvSlider;
    /**
     * visualize the mute button
     */
    private Geometry muteButton;
    /**
     * indicate audio is muted
     */
    private Material muteMaterial;
    /**
     * indicate audio is sounding
     */
    private Material soundMaterial;
    /**
     * visualize the master-volume GUI control
     */
    final private Node mvNode = new Node("Audio Master-Volume Node");
    // *************************************************************************
    // constructors

    /**
     * Instantiate an enabled HUD.
     */
    public AudioHud() {
        super("Audio HUD");
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Toggle the audio between enabled and muted.
     */
    public void toggleMuted() {
        isGloballyMuted = !isGloballyMuted;
        if (isEnabled()) {
            showMuteButton();
            if (isGloballyMuted) {
                hideMvControl();
            } else {
                showMvControl();
            }
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
        Texture texture = manager.loadTexture("/Textures/Georg/mute.png");
        this.muteMaterial = MyAsset.createUnshadedMaterial(manager, texture);

        texture = manager.loadTexture("/Textures/Georg/sound.png");
        this.soundMaterial = MyAsset.createUnshadedMaterial(manager, texture);

        // pre-load the master-volume materials
        texture = manager.loadTexture("/Textures/Georg/left-triangle.png");
        Material mvBackgroundMaterial
                = MyAsset.createUnshadedMaterial(manager, texture);
        ColorRGBA color = new ColorRGBA(1f, 1f, 0f, 1f);
        Material mvSliderMaterial = MyAsset.createUnshadedMaterial(manager, color);

        // Position the MV control in the viewport.
        float x = 0.713f * viewPortWidth;
        float y = 0.95f * viewPortHeight;
        mvNode.setLocalTranslation(x, y, guiZ);

        // Attach a rounded-rectangle Geometry for the background.
        float sliderTravel = 0.088f * viewPortHeight;
        float cornerRadius = 0.005f * viewPortHeight;
        float bgWidth = 0.1f * viewPortHeight;
        float bgHeight = 0.05f * viewPortHeight;
        float x1 = -bgWidth / 2;
        float x2 = bgWidth / 2;
        float y1 = -bgHeight / 2;
        float y2 = bgHeight / 2;
        float zNormal = +1f;
        Mesh bgMesh
                = new RoundedRectangle(x1, x2, y1, y2, cornerRadius, zNormal);
        Geometry background = new Geometry("Master-volume Background", bgMesh);
        mvNode.attachChild(background);
        background.setMaterial(mvBackgroundMaterial);
        background.move(0f, 0f, -0.1f); // slightly behind the slider

        // Attach a rounded-rectangle Geometry for the slider.
        float sliderWidth = bgWidth - sliderTravel;
        assert sliderWidth >= 2 * cornerRadius;
        float sliderHeight = bgHeight;
        x1 = -sliderWidth / 2;
        x2 = sliderWidth / 2;
        y1 = -sliderHeight / 2;
        y2 = sliderHeight / 2;
        zNormal = +1f;
        Mesh sliderMesh
                = new RoundedRectangle(x1, x2, y1, y2, cornerRadius, zNormal);
        this.mvSlider = new Geometry("Master-volume Slider", sliderMesh);
        mvNode.attachChild(mvSlider);
        mvSlider.setMaterial(mvSliderMaterial);

        float dx = sliderTravel * (masterVolume - 0.5f);
        mvSlider.setLocalTranslation(dx, 0f, 0f);

        // Add an Expander to the slider, to enable dragging.
        Expander listener = new Expander(mvSlider) {
            @Override
            public void onClick(boolean isPressed) {
                isDraggingSlider = isPressed;
            }
        };
        MouseEventControl control = new MouseEventControl(listener);
        mvSlider.addControl(control);
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        hideMvControl();
        hideMuteButton();
    }

    /**
     * Callback invoked whenever this AppState becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        if (!isGloballyMuted) {
            showMvControl();
        }
        showMuteButton();
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

        if (isDraggingSlider) {
            InputManager inputManager = getApplication().getInputManager();
            Vector2f cursor = inputManager.getCursorPosition();
            float dx = cursor.x - mvNode.getWorldTranslation().x;
            float halfWidth = 0.044f * viewPortHeight;
            dx = FastMath.clamp(dx, -halfWidth, halfWidth);
            mvSlider.setLocalTranslation(dx, 0f, 0f);

            masterVolume = (dx + halfWidth) / (2 * halfWidth);
        }
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
        if (isGloballyMuted) {
            result = 0f;
        } else {
            result = FastMath.pow(0.003f, 1f - masterVolume);
        }

        return result;
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
     * Hide the master-volume control.
     */
    private void hideMvControl() {
        mvNode.removeFromParent();
    }

    /**
     * Hide the mute button.
     */
    private void hideMuteButton() {
        if (muteButton != null) {
            muteButton.removeFromParent();
            this.muteButton = null;
        }
    }

    /**
     * Display the master-volume control.
     */
    private void showMvControl() {
        hideMvControl();
        attachToGui(mvNode);
    }

    /**
     * Display the mute/sound button.
     */
    private void showMuteButton() {
        hideMuteButton();

        float radius = 0.025f * viewPortHeight;
        int numVertices = 25;
        Mesh mesh = new DiscMesh(radius, numVertices);
        muteButton = new Geometry("mute button", mesh);
        attachToGui(muteButton);

        if (isGloballyMuted) {
            muteButton.setMaterial(muteMaterial);
        } else {
            muteButton.setMaterial(soundMaterial);
        }

        // Position the button in the viewport.
        float x = 0.76f * viewPortWidth;
        float y = 0.95f * viewPortHeight;
        muteButton.setLocalTranslation(x, y, guiZ);

        // Add an Expander to toggle the sound on/muted.
        Expander listener = new Expander(muteButton) {
            @Override
            public void onClick(boolean isPressed) {
                if (isPressed) {
                    toggleMuted();
                }
            }
        };
        MouseEventControl control = new MouseEventControl(listener);
        muteButton.addControl(control);
    }
}
