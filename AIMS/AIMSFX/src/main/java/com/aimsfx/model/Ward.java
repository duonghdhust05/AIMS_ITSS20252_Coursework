package com.aimsfx.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Ward {
    
    @JsonProperty("code")
    private Integer code;
    
    @JsonProperty("codename")
    private String codename;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("division_type")
    private String divisionType;
    
    @JsonProperty("province_code")
    private Integer provinceCode;
    
    public Ward() {}
    
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
    
    public Integer getProvinceCode() {
        return provinceCode;
    }
    
    public void setProvinceCode(Integer provinceCode) {
        this.provinceCode = provinceCode;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
