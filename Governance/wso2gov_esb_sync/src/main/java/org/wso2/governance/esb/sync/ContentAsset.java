package org.wso2.governance.esb.sync;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ContentAsset {
    String name;
    String type;
    String overview_version;
    String asset_content;

    ContentAsset(String name, String type, String version) {
        this.name = name;
        this.type = type;
        this.overview_version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOverview_version() {
        return overview_version;
    }

    public void setOverview_version(String overview_version) {
        this.overview_version = overview_version;
    }

    public String getAsset_content() {
        return asset_content;
    }

    public void setAsset_content(String asset_content) {
        this.asset_content = asset_content;
    }
}
