package com.fashion.elasticseach.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

public class CodeMappedState {

    @JsonIgnore
    private String esId;

    private IdSourceInfo currentCode;

    private List<IdSourceInfo> relatedCodes;

    public CodeMappedState(){
        this.relatedCodes = new ArrayList<>();
    }

    public IdSourceInfo getCurrentCode() {
        return currentCode;
    }

    public void setCurrentCode(IdSourceInfo currentCode) {
        this.currentCode = currentCode;
    }

    public List<IdSourceInfo> getRelatedCodes() {
        return relatedCodes;
    }

    public void setRelatedCodes(List<IdSourceInfo> relatedCodes) {
        this.relatedCodes = relatedCodes;
    }

    public String getEsId() {
        return esId;
    }

    public void setEsId(String esId) {
        this.esId = esId;
    }
}
