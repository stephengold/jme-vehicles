package com.jayfella.jme.vehicle.debug.parts;

import com.jayfella.jme.vehicle.Vehicle;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.RollupPanel;
import com.simsilica.lemur.props.PropertyPanel;

public class ChassisEditor extends Container {
    // *************************************************************************
    // constructors

    public ChassisEditor(Vehicle vehicle) {
        String styleName = "glass";
        PropertyPanel propertyPanel = new PropertyPanel(styleName);
        propertyPanel.addFloatProperty(
                "Mass", vehicle, "mass", 1f, 5000f, 0.1f);
        RollupPanel panel = new RollupPanel("Mass", propertyPanel, styleName);
        addChild(panel);
    }
}
