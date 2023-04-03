package com.jayfella.jme.vehicle.viewer;

import com.jayfella.jme.vehicle.tire.PacejkaTireModel;
import com.jayfella.jme.vehicle.tire.TireSettings;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.TabbedPanel;
import com.simsilica.lemur.props.PropertyPanel;

public class TireEditor extends Container {

    public TireEditor(PacejkaTireModel tireModel) {
        PropertyPanel latProps = createPropertyPanel(tireModel.getLateral());
        PropertyPanel lngProps = createPropertyPanel(tireModel.getLongitudinal());
        PropertyPanel mntProps = createPropertyPanel(tireModel.getAlignMoment());

        TabbedPanel tabbedPanel = new TabbedPanel();
        tabbedPanel.addTab("Lateral", latProps);
        tabbedPanel.addTab("Longitudinal", lngProps);
        tabbedPanel.addTab("Align Moment", mntProps);

        PropertyPanel tireProps = new PropertyPanel("glass");
        tireProps.addFloatProperty(
                "Load", tireModel, "load", 0, tireModel.getMaxLoad(), 0.1f);

        Button printButton = new Button("Print Data");
        printButton.addClickCommands(source -> System.out.println(tireModel));

        addChild(tabbedPanel);
        addChild(tireProps);
        addChild(printButton);
    }

    private static PropertyPanel createPropertyPanel(TireSettings tireSettings) {

        PropertyPanel result = new PropertyPanel("glass");

        // maximum load
        // propertyPanel.addFloatProperty("Max Load", tireSettings, "maxLoad", 1, 20000, 0.001f);
        // slip angle
        // affects how quickly grip will drop
        result.addFloatProperty("SlipAngle C", tireSettings, "slipAngleCoefficientC", 0, 5, 0.001f);

        // affects how "tight" the curve is.
        result.addFloatProperty("SlipAngle B", tireSettings, "slipAngleCoefficientB", 0, 30, 0.001f);

        // affects how much the slip "drops off".0...
        result.addFloatProperty("SlipAngle E", tireSettings, "slipAngleCoefficientE", -2, 2, 0.001f);

        // load / force
        // I'm not certain of the difference between these two...
        // affects how much load can be applied.
        result.addFloatProperty("Load KA", tireSettings, "loadCoefficientKA", 0, 10, 0.001f);

        // affects how much load can be applied to the tire.
        result.addFloatProperty("Load KB", tireSettings, "loadCoefficientKB", 0, 0.0005f, 0.000001f);

        return result;
    }

}
