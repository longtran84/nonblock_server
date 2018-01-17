package com.fintechviet.ad.model;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by tungn on 9/28/2017.
 */
@Entity
@Table(name = "app_ad_installs")
public class AppAdInstalls {
    private long id;
    private Long appAdId;
    private String deviceToken;
    private String platform;
    private Date date;

    @Id
    @Column(name = "id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "appAdId")
    public Long getAppAdId() {
        return appAdId;
    }

    public void setAppAdId(Long appAdId) {
        this.appAdId = appAdId;
    }

    @Basic
    @Column(name = "deviceToken")
    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date", insertable=false)
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppAdInstalls that = (AppAdInstalls) o;

        if (id != that.id) return false;
        if (appAdId != null ? !appAdId.equals(that.appAdId) : that.appAdId != null) return false;
        if (deviceToken != null ? !deviceToken.equals(that.deviceToken) : that.deviceToken != null) return false;
        if (date != null ? !date.equals(that.date) : that.date != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (appAdId != null ? appAdId.hashCode() : 0);
        result = 31 * result + (deviceToken != null ? deviceToken.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        return result;
    }
}
