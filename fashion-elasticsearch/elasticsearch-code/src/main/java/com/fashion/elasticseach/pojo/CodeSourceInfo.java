package com.fashion.elasticseach.pojo;

public class CodeSourceInfo {

    private String code;

    private String source;

    private String version;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return code +"-" + source +"-"+ version ;
    }
}
