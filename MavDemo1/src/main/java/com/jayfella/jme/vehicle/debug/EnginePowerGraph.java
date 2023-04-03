package com.jayfella.jme.vehicle.debug;

import com.jayfella.jme.vehicle.part.Engine;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.Materials;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ImageRaster;
import com.jme3.util.BufferUtils;
import java.util.logging.Logger;

final public class EnginePowerGraph extends Geometry {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(EnginePowerGraph.class.getName());
    // *************************************************************************
    // fields

    final private Engine engine;
    final private int height;
    final private int width;

    final private ImageRaster imageRaster;
    final private Texture2D texture;
    // *************************************************************************
    // constructors

    public EnginePowerGraph(AssetManager assetManager, Engine engine, int width, int height) {
        super("Engine Graph");

        this.engine = engine;
        this.width = width;
        this.height = height;

        Image image = new Image();
        image.setWidth(width);
        image.setHeight(height);
        image.setFormat(Image.Format.BGRA8);
        image.setData(BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4));

        imageRaster = ImageRaster.create(image);
        texture = new Texture2D(image);

        setMesh(new Quad(width, height));

        setMaterial(new Material(assetManager, Materials.UNSHADED));
        getMaterial().setTexture("ColorMap", texture);

        drawGraph();
    }
    // *************************************************************************
    // new methods exposed

    public void drawGraph() {

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                imageRaster.setPixel(x, y, ColorRGBA.DarkGray);
            }
        }

        float redlineRpm = engine.redlineRpm();
        for (int x = 0; x < width; ++x) {
            float rpm = map(x, 0f, width, 0f, redlineRpm);
            float y = FastMath.clamp(engine.powerFraction(rpm) * height, 0, height - 1);
            imageRaster.setPixel(x, (int) y, ColorRGBA.Yellow);
        }
    }
    // *************************************************************************
    // private methods

    private static float map(float value, float oldMin, float oldMax, float newMin, float newMax) {
        return (((value - oldMin) * (newMax - newMin)) / (oldMax - oldMin)) + newMin;
    }
}
