package com.jayfella.jme.vehicle.debug.parts;

import com.jayfella.jme.vehicle.Vehicle;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.RollupPanel;
import com.simsilica.lemur.props.PropertyPanel;

public class SuspensionEditor extends Container {
    // *************************************************************************
    // fields

    final private Vehicle vehicle;
    // *************************************************************************
    // constructors

    public SuspensionEditor(Vehicle vehicle) {
        this.vehicle = vehicle;

        addChild(createRestLengthRollup());
        addChild(createMaxForceRollup());
        addChild(createStiffnessRollup());
        addChild(createCompressionRollup());
        addChild(createDampingRollup());
    }
    // *************************************************************************
    // private methods

    private RollupPanel createRestLengthRollup() {

        PropertyPanel propertyPanel = new PropertyPanel("glass");

        for (int i = 0; i < vehicle.countWheels(); ++i) {
            propertyPanel.addFloatProperty(
                    "Wheel " + i, vehicle.getWheel(i).getSuspension(),
                    "restLength", 0.01f, 1.0f, 0.01f);
        }

        return new RollupPanel("Height", propertyPanel, "glass");
    }

    private RollupPanel createMaxForceRollup() {

        PropertyPanel propertyPanel = new PropertyPanel("glass");

        for (int i = 0; i < vehicle.countWheels(); ++i) {
            propertyPanel.addFloatProperty(
                    "Wheel " + i, vehicle.getWheel(i).getSuspension(),
                    "maxForce", 500, 25000, 0.1f);
        }

        return new RollupPanel("Max Force", propertyPanel, "glass");
    }

    private RollupPanel createStiffnessRollup() {

        PropertyPanel propertyPanel = new PropertyPanel("glass");

        for (int i = 0; i < vehicle.countWheels(); ++i) {
            propertyPanel.addFloatProperty(
                    "Wheel " + i, vehicle.getWheel(i).getSuspension(),
                    "stiffness", 1, 200, 0.1f);
        }

        return new RollupPanel("Stiffness", propertyPanel, "glass");
    }

    private RollupPanel createCompressionRollup() {

        PropertyPanel propertyPanel = new PropertyPanel("glass");

        for (int i = 0; i < vehicle.countWheels(); ++i) {
            propertyPanel.addFloatProperty(
                    "Wheel " + i, vehicle.getWheel(i).getSuspension(),
                    "compressDamping", 0.01f, 1.0f, 0.01f);
        }

        return new RollupPanel("Compression", propertyPanel, "glass");
    }

    private RollupPanel createDampingRollup() {

        PropertyPanel propertyPanel = new PropertyPanel("glass");

        for (int i = 0; i < vehicle.countWheels(); ++i) {
            propertyPanel.addFloatProperty(
                    "Wheel " + i, vehicle.getWheel(i).getSuspension(),
                    "relaxDamping", 0.01f, 1.0f, 0.01f);
        }

        return new RollupPanel("Damping", propertyPanel, "glass");
    }
}
