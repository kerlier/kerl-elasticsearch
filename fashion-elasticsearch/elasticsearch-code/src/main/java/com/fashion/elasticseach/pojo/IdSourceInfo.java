package com.fashion.elasticseach.pojo;

public class IdSourceInfo {

    private String name;

    private String source;

    private String version;

    private String codeMappedId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public void setCodeMappedId(String codeMappedId) {
        this.codeMappedId = codeMappedId;
    }

    public String getCodeMappedId() {
        return codeMappedId;
    }
}
