package com.drrow.pos.model;

import java.util.Date;

public class Setting {
    private String settingKey;
    private String settingValue;
    private String settingGroup;
    private String description;
    private Date updatedAt;

    public Setting() {}

    public Setting(String settingKey, String settingValue, String settingGroup, String description) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.settingGroup = settingGroup;
        this.description = description;
    }

    public String getSettingKey() { return settingKey; }
    public void setSettingKey(String settingKey) { this.settingKey = settingKey; }

    public String getSettingValue() { return settingValue; }
    public void setSettingValue(String settingValue) { this.settingValue = settingValue; }

    public String getSettingGroup() { return settingGroup; }
    public void setSettingGroup(String settingGroup) { this.settingGroup = settingGroup; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
