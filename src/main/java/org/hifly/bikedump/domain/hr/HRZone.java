package org.hifly.bikedump.domain.hr;


public enum HRZone {
    ZONE1("Active Recovery", 80),
    ZONE2("Aerobic", 88),
    ZONE3("Threshold", 94),
    ZONE4("Lactate", 101),
    ZONE5("V02 Max", 102),
    ZONE6("Anaerobic capacity", null);

    private String description;
    private Integer limit;

    HRZone(String description, Integer limit) {
        this.description = description;
        this.limit = limit;
    }


    public String getDescription() {
        return description;
    }

    public Integer getLimit() {
        return limit;
    }
}
