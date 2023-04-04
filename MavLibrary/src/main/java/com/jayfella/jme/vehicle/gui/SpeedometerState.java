package com.jayfella.jme.vehicle.gui;

import com.jayfella.jme.vehicle.SpeedUnit;
import com.jayfella.jme.vehicle.VehicleSpeed;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.Materials;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Line;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import java.util.logging.Logger;
import jme3utilities.MyAsset;
import jme3utilities.math.MyMath;

/**
 * Appstate to manage an analog speedometer in the GUI node. New instances are
 * enabled by default.
 *
 * Derived from the SpeedometerState class in the Advanced Vehicles project.
 */
public class SpeedometerState extends BaseAppState {
    // *************************************************************************
    // constants and loggers

    /**
     * important needle rotations (measured CCW from +X, in radians)
     */
    final private static float thetaMin = -1f; // "pegged" to the right
    final private static float theta0 = FastMath.PI - thetaMin; // speed=0
    /**
     * color for dial markings
     */
    final private static ColorRGBA markingColor = ColorRGBA.White.clone();
    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(SpeedometerState.class.getName());
    // *************************************************************************
    // fields

    /**
     * font for labels
     */
    private BitmapFont font;

    private float prevTheta = theta0;
    private Node guiNode;
    final private Node needleNode = new Node("Speedometer Needle");
    final private Node node;
    /**
     * reusable temporary Quaternion
     */
    final private Quaternion tmpRotation = new Quaternion();

    final private SpeedUnit speedUnit;
    /**
     * corresponding Vehicle
     */
    final private VehicleSpeed vehicle;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an enabled speedometer for the specified vehicle.
     *
     * @param vehicle the corresponding Vehicle (not null)
     * @param speedUnit the units to display (not null)
     */
    public SpeedometerState(VehicleSpeed vehicle, SpeedUnit speedUnit) {
        this.vehicle = vehicle;
        this.speedUnit = speedUnit;

        node = new Node("Speedometer");
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

        node.attachChild(needleNode);
        needleNode.setLocalTranslation(100f, 100f, 1f);

        font = assetManager.loadFont("/Interface/Fonts/Default.fnt");
        Node fixedNode = createFixedNode(assetManager);
        node.attachChild(fixedNode);

        String needlePath = "/Textures/Georg/speedo_needle_2.png";
        Texture needleTexture = assetManager.loadTexture(needlePath);
        Image image = needleTexture.getImage();
        int width = image.getWidth();
        int height = image.getHeight();
        Quad needleMesh = new Quad(width, height);
        Geometry needleGeometry
                = new Geometry("Speedometer Needle", needleMesh);
        needleNode.attachChild(needleGeometry);

        Material material = new Material(assetManager, Materials.UNSHADED);
        needleGeometry.setMaterial(material);
        material.setTexture("ColorMap", needleTexture);
        material.getAdditionalRenderState()
                .setBlendMode(RenderState.BlendMode.Alpha);

        needleGeometry.setLocalTranslation(
                -(width / 2f),
                -(width / 2f) - 7f,
                0f);

        BitmapText speedLabel = new BitmapText(font);
        node.attachChild(speedLabel);
        speedLabel.setColor(markingColor);
        speedLabel.setText(speedUnit.toString());
        float labelWidth = speedLabel.getLineWidth();
        speedLabel.setLocalTranslation(100f - labelWidth / 2, 30f, 1f);

        node.setLocalTranslation(
                application.getCamera().getWidth() - 200f - 20f,
                20f,
                0f
        );
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        node.removeFromParent();
    }

    /**
     * Callback invoked whenever this AppState becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
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

        float speed = vehicle.forwardSpeed(speedUnit);
        float maxSpeed = vehicle.maxForwardSpeed(speedUnit);
        float speedFraction = speed / maxSpeed;
        float theta = MyMath.lerp(speedFraction, theta0, thetaMin);

        // a slight lag, because a physical needle cannot pivot instantly
        this.prevTheta = MyMath.lerp(0.5f, prevTheta, theta);

        this.prevTheta = FastMath.clamp(prevTheta, thetaMin, theta0);
        tmpRotation.fromAngles(0f, 0f, prevTheta - FastMath.HALF_PI);
        needleNode.setLocalRotation(tmpRotation);

        // update the Lemur labels, which are mainly for testing
        //String unit = speedUnit.toString().toLowerCase();
        //String labelText = String.format("%.0f %s", FastMath.abs(speed), unit);
        //speedLabel.setText(labelText);
        //int gearNum = vehicle.getGearBox().getActiveGearNum();
        //labelText = Integer.toString(gearNum + 1);
        //gearLabel.setText(labelText);
    }
    // *************************************************************************
    // private methods

    private Node buildNumNode(float maxSpeed, int stepSpeed, float radius) {
        Node result = new Node("Speedometer Numbers");

        AssetManager assetManager = getApplication().getAssetManager();
        Material markingMaterial
                = MyAsset.createUnshadedMaterial(assetManager, markingColor);

        Vector3f innerOffset = new Vector3f();
        Vector3f outerOffset = new Vector3f();

        for (int intSpeed = 0;; intSpeed += stepSpeed) {
            float fraction = intSpeed / maxSpeed;
            float theta = MyMath.lerp(fraction, theta0, thetaMin);
            if (theta < thetaMin) {
                break;
            }
            BitmapText label = new BitmapText(font);
            result.attachChild(label);
            String text = Integer.toString(intSpeed);
            label.setColor(markingColor);
            label.setText(text);

            float cos = FastMath.cos(theta);
            float sin = FastMath.sin(theta);
            float lineHeight = label.getLineHeight();
            float lineWidth = label.getLineWidth();
            float x = radius * cos - lineWidth / 2;
            float y = radius * sin + lineHeight / 2;
            label.setLocalTranslation(x, y, 0f);

            // Generate a Mesh for the corresponding radial marking.
            float innerRadius = 60f;
            innerOffset.x = innerRadius * cos;
            innerOffset.y = innerRadius * sin;
            float outerRadius = 68f;
            outerOffset.x = outerRadius * cos;
            outerOffset.y = outerRadius * sin;
            Line radialMesh = new Line(innerOffset, outerOffset);

            Geometry radial = new Geometry("Speedometer Radial", radialMesh);
            result.attachChild(radial);
            radial.setMaterial(markingMaterial);
        }

        return result;
    }

    /**
     * Build the Node for the fixed parts of the speedometer, including the
     * background and numbers.
     *
     * @param assetManager for loading assets (not null)
     * @return the new Node
     */
    private Node createFixedNode(AssetManager assetManager) {
        String path = "/Textures/Georg/speedo_bg_2.png";
        Texture backgroundTexture = assetManager.loadTexture(path);
        Image image = backgroundTexture.getImage();
        int height = image.getHeight();
        int width = image.getWidth();

        Geometry backgroundGeom = new Geometry(
                "Speedometer Background", new Quad(width, height));
        backgroundGeom.setLocalTranslation(-width / 2f, -height / 2f, -1f);

        Material material = new Material(assetManager, Materials.UNSHADED);
        backgroundGeom.setMaterial(material);
        material.setTexture("ColorMap", backgroundTexture);
        material.getAdditionalRenderState()
                .setBlendMode(RenderState.BlendMode.Alpha);

        int maxSpeed = (int) vehicle.maxForwardSpeed(speedUnit);
        int stepSpeed = 10 * (1 + maxSpeed / 160);
        Node result = buildNumNode(maxSpeed, stepSpeed, width / 2f - 20f);
        result.attachChild(backgroundGeom);
        result.setLocalTranslation(width / 2f, height / 2f, -1f);

        return result;
    }
}
