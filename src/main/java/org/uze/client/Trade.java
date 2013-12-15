package org.uze.client;

import java.util.Date;

/**
 * Created by Юрий on 10.12.13.
 */
public class Trade {

    private Long id;
    private String name;
    private Date date;
    private Counterpart counterpart;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Counterpart getCounterpart() {
        return counterpart;
    }

    public void setCounterpart(Counterpart counterpart) {
        this.counterpart = counterpart;
    }

    @Override
    public String toString() {
        return "Trade{id=" + id + ", name='" + name + "', date=" + date + ", counterpart=" + counterpart + '}';
    }
}
