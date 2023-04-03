package com.jayfella.jme.vehicle.debug;

import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.part.Wheel;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.Materials;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;

public class TireDataState extends BaseAppState {
    // *************************************************************************
    // fields

    final private static int graphHeight = 100;
    final private static int graphWidth = 200;

    // 3 needles per wheel.
    final private Geometry[][] needles;

    private Node guiNode;
    final private Node node;

    final private TireGraph[] tireGraphs;
    final private Vehicle vehicle;
    // *************************************************************************
    // constructors

    public TireDataState(Vehicle vehicle) {
        this.vehicle = vehicle;
        this.tireGraphs = new TireGraph[vehicle.countWheels()];
        this.needles = new Geometry[vehicle.countWheels()][3];

        this.node = new Node("Tire Data Node");
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
        this.guiNode = ((SimpleApplication) application).getGuiNode();

        float space = 10;

        int x = 0;
        int y = 0;

        for (int i = 0; i < tireGraphs.length; ++i) {

            Node graphNode = new Node("");
            graphNode.setLocalTranslation(x, y, 0);

            x += graphWidth + space;

            if ((i + 1) % 2 == 0) {
                y -= graphHeight + space;
                x = 0;
            }

            AssetManager assetManager = application.getAssetManager();
            TireGraph tireGraph = new TireGraph(
                    assetManager, vehicle.getWheel(i).getTireModel(),
                    graphWidth, graphHeight);
            graphNode.attachChild(tireGraph);

            tireGraph.setBackgroundColor(ColorRGBA.DarkGray);
            tireGraph.setLateralColor(ColorRGBA.Red);
            tireGraph.setLongitudinalColor(ColorRGBA.Yellow);
            tireGraph.setMomentColor(ColorRGBA.Green);

            Geometry lateralNeedle
                    = createNeedle(assetManager, tireGraph.getLateralColor());
            Geometry longitudeNeedle = createNeedle(
                    assetManager, tireGraph.getLongitudinalColor());
            Geometry momentNeedle
                    = createNeedle(assetManager, tireGraph.getMomentColor());

            graphNode.attachChild(lateralNeedle);
            graphNode.attachChild(longitudeNeedle);
            graphNode.attachChild(momentNeedle);

            node.attachChild(graphNode);

            this.needles[i][0] = lateralNeedle;
            this.needles[i][1] = longitudeNeedle;
            this.needles[i][2] = momentNeedle;

            this.tireGraphs[i] = tireGraph;
        }

        node.setLocalTranslation(space, (graphHeight) + (space * 2), -1);
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

        for (int i = 0; i < vehicle.countWheels(); ++i) {

            tireGraphs[i].drawGraph();

            Wheel wheel = vehicle.getWheel(i);

            // float lat = wheel.getTireModel().getLateralValue();
            // lat /= wheel.getTireModel().getMaxLoad();
            // lat *= graphWidth;
            float lat = wheel.calculateLateralSlipAngle();
            lat /= wheel.getMaxSteerAngle();
            lat *= graphWidth;

            float lng = wheel.calculateLongitudinalSlipAngle();
            lng /= FastMath.QUARTER_PI;
            //System.out.println(lng);
            //lng /= 1000;
            lng *= graphWidth;

            float mnt = wheel.getTireModel().getMomentValue();

            needles[i][0].setLocalTranslation(lat, 0, 0);
            needles[i][1].setLocalTranslation(lng, 0, 0);
            needles[i][2].setLocalTranslation(mnt, 0, 0);
        }
    }
    // *************************************************************************
    // private methods

    private static Geometry createNeedle(AssetManager assetManager, ColorRGBA color) {
        Geometry result = new Geometry("Needle", new Quad(1, graphHeight));
        result.setMaterial(new Material(assetManager, Materials.UNSHADED));
        result.getMaterial().setColor("Color", color);

        return result;
    }

    private void drawGraph(int i) {
        tireGraphs[i].drawGraph();
    }
}
