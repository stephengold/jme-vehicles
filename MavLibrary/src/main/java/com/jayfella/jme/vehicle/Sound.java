package com.jayfella.jme.vehicle;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import com.jme3.math.FastMath;
import com.jme3.scene.Node;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.Loadable;
import jme3utilities.Validate;
import jme3utilities.math.MyMath;

/**
 * A collection of audio nodes used to render a sound at different pitches
 * (fundamental frequencies) and volumes. Each node handles a specific range of
 * pitches. At any given moment, at most one node is playing. Positional audio
 * is used by default.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class Sound implements Loadable {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(Sound.class.getName());
    // *************************************************************************
    // fields

    /**
     * used to load assets, or null if not yet loaded
     */
    private AssetManager assetManager;
    /**
     * AudioNode that's playing, or null if none
     */
    private AudioNode activeNode;
    /**
     * true&rarr;take position/velocity/distance into account when playing,
     * false&rarr;play in "headspace"
     */
    private boolean isPositional = true;
    /**
     * loaded audio nodes, mapped from their fundamental frequencies in cycles
     * per second
     */
    final private Map<Float, AudioNode> pitchToNode = new HashMap<>(5);
    /**
     * paths to assets not yet loaded, mapped from their fundamental frequencies
     * in cycles per second
     */
    final private Map<Float, String> pitchToAssetPath = new HashMap<>(5);
    /**
     * scene-graph node to which this Sound is attached, or null if unattached
     */
    private Node parent;
    // *************************************************************************
    // constructors

    /**
     * A no-arg constructor to avoid javadoc warnings from JDK 18.
     */
    public Sound() {
        // do nothing
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Add an audio asset to this Sound.
     *
     * @param assetPath the asset path, including the extension (not null, not
     * empty)
     * @param recordedPitch the fundamental frequency of the asset (in cycles
     * per second, &gt;0)
     */
    public void addAssetPath(String assetPath, float recordedPitch) {
        Validate.nonEmpty(assetPath, "asset path");
        Validate.positive(recordedPitch, "recorded pitch");

        if (assetManager == null) { // Sound not loaded yet
            pitchToAssetPath.put(recordedPitch, assetPath);

        } else { // Sound already loaded
            AudioNode audioNode = addAudioNode(assetPath, recordedPitch);

            if (parent != null) { // Sound already attached
                parent.attachChild(audioNode);
            }
        }
    }

    /**
     * Attach the audio nodes to the specified scene-graph node.
     *
     * @param parent where to attach (not null, modified)
     */
    public void attachTo(Node parent) {
        assert assetManager != null : "Not loaded.";
        assert this.parent == null : "Already attached.";

        this.parent = parent;
        for (AudioNode node : pitchToNode.values()) {
            parent.attachChild(node);
        }
    }

    /**
     * Detach the audio nodes from the scene graph.
     */
    public void detach() {
        assert this.parent != null : "Not attached.";

        for (AudioNode node : pitchToNode.values()) {
            node.removeFromParent();
        }
        this.parent = null;
    }

    /**
     * Test whether this Sound is positional.
     *
     * @return true if it takes position/velocity/distance into account when
     * playing, false if it plays in "headspace"
     */
    public boolean isPositional() {
        return isPositional;
    }

    /**
     * Configure the audio nodes for silence.
     */
    public void mute() {
        assert assetManager != null : "Not loaded.";

        if (activeNode != null) {
            activeNode.stop();
            this.activeNode = null;
        }
    }

    /**
     * Configure the audio nodes for the specified pitch and volume.
     *
     * @param pitch the desired fundamental frequency (in cycles per second,
     * &gt;0)
     * @param volume the desired relative volume (linear scale, &ge;0)
     */
    public void setPitchAndVolume(float pitch, float volume) {
        assert assetManager != null : "Not loaded.";
        Validate.positive(pitch, "pitch");
        Validate.nonNegative(volume, "volume");

        if (volume == 0f) {
            mute();
            return;
        }

        if (activeNode == null || !canAccuratelySimulate(activeNode, pitch)) {
            // Find the best AudioNode for the desired pitch.
            AudioNode bestNode = findBestNode(pitch);
            if (bestNode != activeNode && activeNode != null) {
                activeNode.stop();
            }
            this.activeNode = bestNode;
        }

        float recordedPitch = recordedPitch(activeNode);
        float playbackSpeed = pitch / recordedPitch;
        float clampedSpeed = FastMath.clamp(playbackSpeed, 0.5f, 2f);
        if (playbackSpeed != clampedSpeed) {
            logger.log(Level.WARNING,
                    "Clamped playback speed: sound={0}, pitch={1} Hz",
                    new Object[]{getClass().getSimpleName(), pitch}
            );
        }
        float oldSpeed = activeNode.getPitch();
        if (clampedSpeed != oldSpeed) {
            activeNode.setPitch(clampedSpeed);
        }

        float oldVolume = activeNode.getVolume();
        if (volume != oldVolume) {
            activeNode.setVolume(volume);
        }

        if (activeNode.getStatus() != AudioSource.Status.Playing) {
            activeNode.play();
        }
    }

    /**
     * Alter whether this Sound is positional.
     *
     * @param newSetting true&rarr;take position/velocity/distance into account
     * when playing, false&rarr;play in "headspace" (default=true)
     */
    public void setPositional(boolean newSetting) {
        this.isPositional = newSetting;

        for (Map.Entry<Float, AudioNode> entry : pitchToNode.entrySet()) {
            AudioNode audioNode = entry.getValue();
            audioNode.setPositional(newSetting);
        }
    }
    // *************************************************************************
    // Loadable methods

    /**
     * Load the assets of this Sound without attaching them to any scene.
     *
     * @param assetManager for loading assets (not null, alias created)
     */
    @Override
    public void load(AssetManager assetManager) {
        Validate.nonNull(assetManager, "asset manager");
        assert this.assetManager == null : "Already loaded.";

        this.assetManager = assetManager;

        for (Map.Entry<Float, String> entry : pitchToAssetPath.entrySet()) {
            String assetPath = entry.getValue();
            float recordedPitch = entry.getKey();
            addAudioNode(assetPath, recordedPitch);
        }

        pitchToAssetPath.clear();
    }
    // *************************************************************************
    // private methods

    /**
     * Create an AudioNode and add it to this Sound.
     *
     * @param assetPath the asset path, including the extension (not null, not
     * empty)
     * @param recordedPitch the fundamental frequency of the asset (in cycles
     * per second, &gt;0)
     * @return the new, non-directional instance
     */
    private AudioNode addAudioNode(String assetPath, float recordedPitch) {
        assert assetManager != null;

        AudioNode result = new AudioNode(
                assetManager, assetPath, AudioData.DataType.Buffer);
        pitchToNode.put(recordedPitch, result);

        result.setDirectional(false);
        result.setLooping(true);
        result.setName(assetPath);
        result.setPositional(isPositional);

        return result;
    }

    /**
     * Test whether the specified AudioNode can accurately simulate the
     * specified pitch.
     *
     * @param node the AudioNode to test (not null)
     * @param pitch the desired pitch (in cycles per second, &gt;0)
     * @return true if possible, otherwise false
     */
    private boolean canAccuratelySimulate(AudioNode node, float pitch) {
        float recorded = recordedPitch(node);
        float minPitch = 0.5f * recorded;
        float maxPitch = 2f * recorded;
        boolean result = MyMath.isBetween(minPitch, pitch, maxPitch);

        return result;
    }

    /**
     * Find the best AudioNode for simulating the specified pitch.
     *
     * @param pitch the desired pitch (in cycles per second, &gt;0)
     * @return a pre-existing instance, or null if none found
     */
    private AudioNode findBestNode(float pitch) {
        AudioNode result = null;
        float bestRatio = 0f;
        for (Map.Entry<Float, AudioNode> entry : pitchToNode.entrySet()) {
            float recorded = entry.getKey();
            float ratio;
            if (pitch > recorded) {
                ratio = recorded / pitch;
            } else {
                ratio = pitch / recorded;
            }
            assert ratio > 0f && ratio <= 1f : ratio;
            if (ratio > bestRatio) {
                bestRatio = ratio;
                result = entry.getValue();
            }
        }

        return result;
    }

    /**
     * Determine the recorded pitch of the specified AudioNode.
     *
     * @param node (not null)
     * @return the fundamental frequency (in cycles per second, &gt;0)
     */
    private float recordedPitch(AudioNode node) {
        for (Map.Entry<Float, AudioNode> entry : pitchToNode.entrySet()) {
            if (entry.getValue() == node) {
                float result = entry.getKey();
                return result;
            }
        }

        throw new IllegalArgumentException("Not part of this Sound: " + node);
    }
}
