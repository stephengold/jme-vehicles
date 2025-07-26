package com.jayfella.jme.vehicle.debug.parts;

import com.jayfella.jme.vehicle.Vehicle;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.RollupPanel;
import com.simsilica.lemur.props.PropertyPanel;

public class WheelsEditor extends Container {
    // *************************************************************************
    // fields

    final private Vehicle vehicle;
    // *************************************************************************
    // constructors

    public WheelsEditor(Vehicle vehicle) {
        this.vehicle = vehicle;

        addChild(createGripRollup());
        addChild(createSizeRollup());
        addChild(createPowerRollup());
    }
    // *************************************************************************
    // private methods

    private RollupPanel createGripRollup() {
        PropertyPanel propertyPanel = new PropertyPanel("glass");

        for (int i = 0; i < vehicle.countWheels(); ++i) {
            propertyPanel.addFloatProperty("Wheel " + i, vehicle.getWheel(i),
                    "grip", 0.01f, 1.0f, 0.01f);
        }

        return new RollupPanel("Grip", propertyPanel, "glass");
    }

    private RollupPanel createSizeRollup() {
        PropertyPanel propertyPanel = new PropertyPanel("glass");

        for (int i = 0; i < vehicle.countWheels(); ++i) {
            propertyPanel.addFloatProperty("Wheel " + i, vehicle.getWheel(i),
                    "diameter", 0.25f, 4.0f, 0.1f);
        }

        return new RollupPanel("Size", propertyPanel, "glass");
    }

    private RollupPanel createPowerRollup() {
        PropertyPanel propertyPanel = new PropertyPanel("glass");

        for (int i = 0; i < vehicle.countWheels(); ++i) {
            propertyPanel.addFloatProperty("Wheel " + i, vehicle.getWheel(i),
                    "powerFraction", 0f, 1f, 0.01f);
        }

        return new RollupPanel("Power", propertyPanel, "glass");
    }
}
