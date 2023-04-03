package com.jayfella.jme.vehicle.niftydemo.tool;

import com.jayfella.jme.vehicle.niftydemo.MavDemo2;
import com.jayfella.jme.vehicle.niftydemo.state.DemoState;
import java.util.List;
import java.util.logging.Logger;
import jme3utilities.nifty.GuiScreenController;
import jme3utilities.nifty.SliderTransform;
import jme3utilities.nifty.Tool;

/**
 * The controller for the "Audio" tool in the main HUD of MavDemo2.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class AudioTool extends Tool {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(AudioTool.class.getName());
    /**
     * transform function for the hour slider
     */
    final private static SliderTransform volumeSt = SliderTransform.None;
    /**
     * name of the mute checkbox
     */
    final private static String muteCn = "masterMute";
    /**
     * name of the volume slider
     */
    final private static String volumeSn = "masterAudioVolume";
    // *************************************************************************
    // constructors

    /**
     * Instantiate an uninitialized tool.
     *
     * @param screenController the controller of the screen that will contain
     * the tool (not null)
     */
    AudioTool(GuiScreenController screenController) {
        super(screenController, "audio");
    }
    // *************************************************************************
    // Tool methods

    /**
     * Enumerate this tool's checkboxes.
     *
     * @return a new list of names (unique id prefixes)
     */
    @Override
    protected List<String> listCheckBoxes() {
        List<String> result = super.listCheckBoxes();
        result.add(muteCn);

        return result;
    }

    /**
     * Enumerate this tool's sliders.
     *
     * @return a new list of names (unique id prefixes)
     */
    @Override
    protected List<String> listSliders() {
        List<String> result = super.listSliders();
        result.add(volumeSn);

        return result;
    }

    /**
     * Update the MVC model based on a check-box event.
     *
     * @param name the name (unique id prefix) of the checkbox
     * @param isChecked the new state of the checkbox (true&rarr;checked,
     * false&rarr;unchecked)
     */
    @Override
    public void onCheckBoxChanged(String name, boolean isChecked) {
        switch (name) {
            case muteCn:
                DemoState demoState = MavDemo2.getDemoState();
                demoState.setMuted(isChecked);
                break;

            default:
                super.onCheckBoxChanged(name, isChecked);
        }
    }

    /**
     * Update the MVC model based on the sliders.
     *
     * @param sliderName the name (unique id prefix) of the slider (not null)
     */
    @Override
    public void onSliderChanged(String sliderName) {
        DemoState demoState = MavDemo2.getDemoState();
        float logVolume = readSlider(volumeSn, volumeSt);
        demoState.setMasterAudioLogVolume(logVolume);
    }

    /**
     * Update this tool prior to rendering. (Invoked once per frame while this
     * tool is displayed.)
     */
    @Override
    protected void toolUpdate() {
        DemoState demoState = MavDemo2.getDemoState();
        boolean isMuted = demoState.isMuted();
        setChecked(muteCn, isMuted);

        setSliderEnabled(volumeSn, !isMuted);
        float logVolume = demoState.masterAudioLogVolume();
        setSlider(volumeSn, volumeSt, logVolume);
    }
}
