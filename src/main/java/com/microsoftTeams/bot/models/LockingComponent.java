package com.microsoftTeams.bot.models;

import com.microsoftTeams.bot.helpers.UserInfo;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "lockingComponent")
@SuppressWarnings("unused")
public class LockingComponent {

    @Id
    private String id;
    private String component;
    private UserInfo userInfo;

    public LockingComponent(String component) {
        this.component = component;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
}
