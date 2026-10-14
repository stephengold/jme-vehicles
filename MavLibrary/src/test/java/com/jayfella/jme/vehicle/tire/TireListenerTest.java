package com.jayfella.jme.vehicle.tire;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/** Regression tests for optional tire-change listeners. */
public class TireListenerTest {
    private static TireSettings settings() {
        return new TireSettings(1.3f, 15.2f, -1.6f, 2f, 0.000055f);
    }

    private static void changeAll(TireSettings settings) {
        settings.setSlipAngleCoefficientC(1.4f);
        settings.setSlipAngleCoefficientB(16f);
        settings.setSlipAngleCoefficientE(-1.5f);
        settings.setLoadCoefficientKA(3f);
        settings.setLoadCoefficientKB(0.00006f);
        assertEquals(1.4f, settings.getSlipAngleCoefficientC(), 0f);
        assertEquals(16f, settings.getSlipAngleCoefficientB(), 0f);
        assertEquals(-1.5f, settings.getSlipAngleCoefficientE(), 0f);
        assertEquals(3f, settings.getLoadCoefficientKA(), 0f);
        assertEquals(0.00006f, settings.getLoadCoefficientKB(), 0f);
    }

    @Test
    public void coefficientsAcceptAnAbsentOrRemovedListener() {
        TireSettings settings = settings();
        changeAll(settings);
        final int[] notifications = {0};
        settings.setChangeListener(new TireSettings.ChangeListener() {
            @Override
            public void valueChanged() {
                ++notifications[0];
            }
        });
        changeAll(settings);
        assertEquals(5, notifications[0]);
        settings.setChangeListener(null);
        changeAll(settings);
        assertEquals(5, notifications[0]);
    }

    @Test
    public void maximumLoadAcceptsAnAbsentOrRemovedListener() {
        TireSettings settings = settings();
        PacejkaTireModel model = new PacejkaTireModel(
                "test", settings, settings, settings, 10000f);
        model.setMaxLoad(11000f);
        assertEquals(11000f, model.getMaxLoad(), 0f);
        final int[] notifications = {0};
        model.setChangeListener(new TireSettings.ChangeListener() {
            @Override
            public void valueChanged() {
                ++notifications[0];
            }
        });
        model.setMaxLoad(12000f);
        assertEquals(1, notifications[0]);
        model.setChangeListener(null);
        model.setMaxLoad(13000f);
        assertEquals(13000f, model.getMaxLoad(), 0f);
        assertEquals(1, notifications[0]);
    }
}
