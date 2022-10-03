package org.hifly.bikedump.domain;

import java.io.Serializable;

public class ProfileSetting implements Serializable {
   
    private static final long serialVersionUID = 6L;
    
    private String unitSystem;

    public void setUnitSystem(String unitSystem) {
        this.unitSystem = unitSystem;
    }



}
