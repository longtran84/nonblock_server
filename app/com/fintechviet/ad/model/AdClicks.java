package com.fintechviet.ad.model;


import com.fintechviet.user.model.User;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by tungn on 9/6/2017.
 */
@Entity
@Table(name = "ad_clicks")
public class AdClicks {
    private long id;
    private User user;
    private Ad ad;
    private Date date;

    @Id
    @Column(name = "id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @ManyToOne
    @JoinColumn(name = "uid")
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @ManyToOne
    @JoinColumn(name = "adId")
    public Ad getAd() {
        return ad;
    }

    public void setAd(Ad ad) {
        this.ad = ad;
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

        AdClicks adClicks = (AdClicks) o;

        if (id != adClicks.id) return false;
        if (date != null ? !date.equals(adClicks.date) : adClicks.date != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (date != null ? date.hashCode() : 0);
        return result;
    }
}
