package com.jayfella.jme.vehicle.gui;

import com.atr.jme.font.TrueTypeFont;
import com.atr.jme.font.asset.TrueTypeKeyMesh;
import com.atr.jme.font.shape.TrueTypeNode;
import com.atr.jme.font.util.Style;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import java.util.logging.Logger;
import jme3utilities.Loadable;
import jme3utilities.MyAsset;
import jme3utilities.mesh.RoundedRectangle;

/**
 * AppState to display white text on a blue oval, updating it as needed.
 *
 * @author Stephen Gold sgold@sonic.net
 */
abstract public class CartoucheState
        extends BaseAppState
        implements Loadable {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(CartoucheState.class.getName());
    // *************************************************************************
    // fields

    /**
     * center X as a fraction of the viewport width
     */
    final private float xFraction;
    /**
     * center Y as a fraction of the viewport height
     */
    final private float yFraction;
    /**
     * pre-loaded Material for the background
     */
    private Material bgMaterial;
    /**
     * relevant spatials, to be attached to the GUI node
     */
    final private Node node;
    /**
     * text currently displayed, or null if the Node isn't populated
     */
    private String displayedText = null;
    /**
     * pre-loaded font for the text
     */
    private TrueTypeFont droidFont;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an enabled CartoucheState.
     *
     * @param name a name for the new AppState
     * @param xFraction the center X as a fraction of the viewport width
     * @param yFraction the center Y as a fraction of the viewport width
     */
    protected CartoucheState(String name, float xFraction, float yFraction) {
        super(name);

        this.node = new Node(name + " Node");
        this.xFraction = xFraction;
        this.yFraction = yFraction;
    }
    // *************************************************************************
    // new protected methods

    /**
     * Compare the specified text to the displayed text and repopulate the Node
     * if they differ.
     *
     * @param text the text to display (may be null)
     */
    protected void displayText(String text) {
        if (displayedText == null || !displayedText.equals(text)) {
            repopulateNode(text);
        }
    }

    /**
     * Access the Node that gets attached to the GUI node.
     *
     * @return the pre-existing instance
     */
    protected Node getNode() {
        return node;
    }

    /**
     * Repopulate the Node from scratch.
     *
     * @param text the text to display (may be null)
     */
    protected void repopulateNode(String text) {
        node.detachAllChildren();
        this.displayedText = null;
        if (text == null) {
            return;
        }

        // Attach a TrueTypeNode for the text.
        int kerning = 0;
        ColorRGBA textColor = ColorRGBA.White.clone();
        TrueTypeNode ttNode = droidFont.getText(text, kerning, textColor);
        node.attachChild(ttNode);
        float textWidth = ttNode.getWidth();
        if (textWidth < 0.001f) {
            textWidth = 0.001f;
        }
        float x = -textWidth / 2;
        float textHeight = ttNode.getHeight();
        float y = textHeight / 2;
        ttNode.setLocalTranslation(x, y, 1f);

        // Attach a rounded-rectangle Geometry for the background.
        float cornerRadius = 0.25f * textHeight;
        float bgWidth = textWidth + 2 * cornerRadius;
        float bgHeight = textHeight + 2 * cornerRadius;
        float x1 = -bgWidth / 2;
        float x2 = bgWidth / 2;
        float y1 = -bgHeight / 2;
        float y2 = bgHeight / 2;
        float zNormal = +1f;
        Mesh bgMesh
                = new RoundedRectangle(x1, x2, y1, y2, cornerRadius, zNormal);
        Geometry background = new Geometry("camera name background", bgMesh);
        node.attachChild(background);
        background.setMaterial(bgMaterial);

        this.displayedText = text;
    }

    /**
     * Update the displayed text.
     */
    abstract protected void updateNode();
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
        AssetManager assetManager = application.getAssetManager();
        load(assetManager);

        // Position the Node in the viewport.
        Camera camera = application.getCamera();
        float viewPortHeight = camera.getHeight();
        float viewPortWidth = camera.getWidth();
        float x = xFraction * viewPortWidth;
        float y = yFraction * viewPortHeight;
        node.setLocalTranslation(x, y, 0f);

    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        // Detach the Node from the GUI.
        node.removeFromParent();
    }

    /**
     * Callback invoked whenever this AppState becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        updateNode();

        // Attach the Node to the GUI.
        SimpleApplication simpleApp = (SimpleApplication) getApplication();
        Node guiNode = simpleApp.getGuiNode();
        guiNode.attachChild(node);
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
        updateNode();
    }
    // *************************************************************************
    // Loadable methods

    /**
     * Load the assets of this instance without attaching them to any scene.
     *
     * @param assetManager for loading assets (not null)
     */
    @Override
    public void load(AssetManager assetManager) {
        // pre-load the Droid font
        AssetKey<TrueTypeFont> assetKey = new TrueTypeKeyMesh(
                "Interface/Fonts/DroidSerifBold-aMPE.ttf", Style.Plain, 18);
        this.droidFont = assetManager.loadAsset(assetKey);

        // pre-load the background Material
        ColorRGBA bgColor = new ColorRGBA(0.1f, 0.2f, 0.5f, 1f);
        this.bgMaterial = MyAsset.createUnshadedMaterial(assetManager, bgColor);
    }
}
