package org.hifly.bikedump.domain;


public class TrackSelected {

    private String filename;
    private String name;

    public TrackSelected(String filename) {
        this.filename = filename;
    }

    public TrackSelected(String filename, String name) {
        this.filename = filename;
        this.name = name;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrackSelected)) return false;

        TrackSelected that = (TrackSelected) o;

        return filename.equals(that.filename);

    }

    @Override
    public int hashCode() {
        return filename.hashCode();
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
