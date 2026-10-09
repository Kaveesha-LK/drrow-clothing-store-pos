package com.drrow.pos.model;

public class Colour {
    private int colourId;
    private String colourName;
    private String hexCode;
    private boolean active = true;

    public Colour() {}

    public Colour(int colourId, String colourName, String hexCode, boolean active) {
        this.colourId = colourId;
        this.colourName = colourName;
        this.hexCode = hexCode;
        this.active = active;
    }

    public int getColourId() { return colourId; }
    public void setColourId(int colourId) { this.colourId = colourId; }

    public String getColourName() { return colourName; }
    public void setColourName(String colourName) { this.colourName = colourName; }

    public String getHexCode() { return hexCode; }
    public void setHexCode(String hexCode) { this.hexCode = hexCode; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return colourName;
    }
}
