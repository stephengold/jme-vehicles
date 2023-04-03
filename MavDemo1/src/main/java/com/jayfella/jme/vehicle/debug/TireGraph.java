package com.jayfella.jme.vehicle.debug;

import com.jayfella.jme.vehicle.tire.PacejkaTireModel;
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

/**
 * Derived from the TireGraph class in the Advanced Vehicles project.
 */
final public class TireGraph extends Geometry {
    final private PacejkaTireModel tireModel;
    final private int height;
    final private int width;

    final private ImageRaster imageRaster;
    final private Texture2D texture;

    final private static float maxSlipAngle = FastMath.QUARTER_PI; // 45 degrees

    private ColorRGBA backgroundColor = ColorRGBA.White;
    private ColorRGBA lineColor = ColorRGBA.DarkGray;
    private ColorRGBA lateralColor = ColorRGBA.Red;
    private ColorRGBA longitudinalColor = ColorRGBA.Black;
    private ColorRGBA momentColor = ColorRGBA.Green;

    public TireGraph(AssetManager assetManager, PacejkaTireModel tireModel, int width, int height) {
        super("Tire Graph");

        this.tireModel = tireModel;
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

    public Texture2D getTexture() {
        return texture;
    }

    public ColorRGBA getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(ColorRGBA backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public ColorRGBA getLineColor() {
        return lineColor;
    }

    public void setLineColor(ColorRGBA lineColor) {
        this.lineColor = lineColor;
    }

    public ColorRGBA getLateralColor() {
        return lateralColor;
    }

    public void setLateralColor(ColorRGBA lateralColor) {
        this.lateralColor = lateralColor;
    }

    public ColorRGBA getLongitudinalColor() {
        return longitudinalColor;
    }

    public void setLongitudinalColor(ColorRGBA longitudinalColor) {
        this.longitudinalColor = longitudinalColor;
    }

    public ColorRGBA getMomentColor() {
        return momentColor;
    }

    public void setMomentColor(ColorRGBA momentColor) {
        this.momentColor = momentColor;
    }

    public void drawGraph() {
        // draw a background with lines
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {

                if (y == height / 2) {
                    imageRaster.setPixel(x, y, lineColor);
                } else {
                    imageRaster.setPixel(x, y, backgroundColor);
                }
            }
        }

        // lateral
        for (int x = 0; x < width; ++x) {
            float xUnit = x / (float) width;
            float slipAngle = map(xUnit, 0, 1, 0, maxSlipAngle);

            float lat = tireModel.calcLateralTireForce(slipAngle);
            lat = map(lat, -tireModel.getMaxLoad(), tireModel.getMaxLoad(), 0, height);
            int pixelY = (int) FastMath.clamp(lat, 0, height - 1);

            imageRaster.setPixel(x, pixelY, lateralColor);
        }

        // longitudinal
        for (int x = 0; x < width; ++x) {
            float xUnit = x / (float) width;
            float slipAngle = map(xUnit, 0, 1, 0, maxSlipAngle);

            float lng = tireModel.calcLongitudeTireForce(slipAngle);
            lng = map(lng, -tireModel.getMaxLoad(), tireModel.getMaxLoad(), 0, height);
            int pixelY = (int) FastMath.clamp(lng, 0, height - 1);

            imageRaster.setPixel(x, pixelY, longitudinalColor);
        }

        // align moment
        for (int x = 0; x < width; ++x) {
            float xUnit = x / (float) width;
            float slipAngle = map(xUnit, 0, 1, 0, maxSlipAngle);

            float mnt = tireModel.calcAlignMoment(slipAngle);
            mnt = map(mnt, -tireModel.getMaxLoad(), tireModel.getMaxLoad(), 0, height);
            int pixelY = (int) FastMath.clamp(mnt, 0, height - 1);

            imageRaster.setPixel(x, pixelY, momentColor);
        }
    }

    private static float map(float value, float oldMin, float oldMax, float newMin, float newMax) {
        return (((value - oldMin) * (newMax - newMin)) / (oldMax - oldMin)) + newMin;
    }
}
