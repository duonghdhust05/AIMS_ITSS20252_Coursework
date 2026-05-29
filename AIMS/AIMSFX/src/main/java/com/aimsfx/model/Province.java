package com.aimsfx.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Province {
    
    @JsonProperty("code")
    private Integer code;
    
    @JsonProperty("codename")
    private String codename;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("division_type")
    private String divisionType;
    
    @JsonProperty("phone_code")
    private Integer phoneCode;
    
    @JsonProperty("wards")
    private List<Ward> wards;
    
    public Province() {}
    
    public Integer getCode() {
        return code;
    }
    
    public void setCode(Integer code) {
        this.code = code;
    }
    
    public String getCodename() {
        return codename;
    }
    
    public void setCodename(String codename) {
        this.codename = codename;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDivisionType() {
        return divisionType;
    }
    
    public void setDivisionType(String divisionType) {
        this.divisionType = divisionType;
    }
    
    public Integer getPhoneCode() {
        return phoneCode;
    }
    
    public void setPhoneCode(Integer phoneCode) {
        this.phoneCode = phoneCode;
    }
    
    public List<Ward> getWards() {
        return wards;
    }
    
    public void setWards(List<Ward> wards) {
        this.wards = wards;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
