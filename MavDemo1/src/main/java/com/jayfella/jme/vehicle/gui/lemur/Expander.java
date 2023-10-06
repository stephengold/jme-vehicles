package com.jayfella.jme.vehicle.gui.lemur;

import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.event.MouseListener;
import java.util.logging.Logger;

/**
 * A MouseListener for a GUI button that expands by 20% when entered.
 *
 * @author Stephen Gold sgold@sonic.net
 */
abstract class Expander implements MouseListener {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(Expander.class.getName());
    // *************************************************************************
    // fields

    /**
     * Spatial to expand
     */
    final private Spatial expandSpatial;
    /**
     * scale factors when expanded
     */
    final private Vector3f expandedScale = new Vector3f();
    /**
     * scale factors when not expanded
     */
    final private Vector3f usualScale = new Vector3f();
    // *************************************************************************
    // constructors

    /**
     * Instantiate an Expander for the specified Spatial.
     *
     * @param buttonSpatial the Spatial to expand (not null, alias created)
     */
    Expander(Spatial buttonSpatial) {
        this.expandSpatial = buttonSpatial;

        Vector3f scale = buttonSpatial.getLocalScale(); // alias
        usualScale.set(scale);
        expandedScale.set(scale).multLocal(1.2f);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Callback invoked when the button is clicked using the LMB. Afterward, the
     * causative MouseButtonEvent is consumed.
     *
     * @param isPressed true&rarr;pressed, false&rarr;released
     */
    abstract void onClick(boolean isPressed);
    // *************************************************************************
    // MouseListener methods

    @Override
    public void mouseButtonEvent(
            MouseButtonEvent event, Spatial s1, Spatial s2) {
        int buttonIndex = event.getButtonIndex();
        if (buttonIndex == 0) { // ignore buttons other than the LMB
            boolean isPressed = event.isPressed();
            onClick(isPressed);
            event.setConsumed();
        }
    }

    @Override
    public void mouseEntered(MouseMotionEvent event, Spatial s1, Spatial s2) {
        expandSpatial.setLocalScale(expandedScale);
    }

    @Override
    public void mouseExited(MouseMotionEvent event, Spatial s1, Spatial s2) {
        expandSpatial.setLocalScale(usualScale);
    }

    @Override
    public void mouseMoved(MouseMotionEvent event, Spatial s1, Spatial s2) {
        // do nothing
    }
}
