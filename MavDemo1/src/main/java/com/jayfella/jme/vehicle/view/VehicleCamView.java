package com.jayfella.jme.vehicle.view;

public enum VehicleCamView {

    ChaseCam,
    DashCam;

    public VehicleCamView next() {
        int currentOrdinal = ordinal();

        if (currentOrdinal == VehicleCamView.values().length - 1) {
            return values()[0];
        } else {
            return values()[currentOrdinal + 1];
        }
    }

    public VehicleCamView previous() {
        int currentOrdinal = ordinal();

        if (currentOrdinal == 0) {
            return values()[VehicleCamView.values().length - 1];
        } else {
            return values()[currentOrdinal - 1];
        }
    }
}
