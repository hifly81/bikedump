package org.hifly.bikedump.gui.graph;

public final class ClimbRange {
    public final double startKm;
    public final double endKm;

    public ClimbRange(double startKm, double endKm) {
        this.startKm = startKm;
        this.endKm = endKm;
    }

    public boolean contains(double km) {
        return km >= startKm && km <= endKm;
    }
}