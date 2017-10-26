package org.uze.coherence.model;

import com.tangosol.util.Versionable;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class Item implements Serializable, Versionable<Long> {

    private static final long serialVersionUID = 1598581650977142105L;

    private String id;

    private int payload;

    private long version;

    public Item() {
    }

    public Item(String id, int payload, long version) {
        this.id = id;
        this.payload = payload;
        this.version = version;
    }

    public Item(String id, int payload) {
        this(id, payload, 0);
    }

    public String id() {
        return id;
    }

    public int payload() {
        return payload;
    }

    public long version() {
        return version;
    }

    public void payload(int payload) {
        this.payload = payload;
    }

    @Override
    public Long getVersionIndicator() {
        return version;
    }

    @Override
    public void incrementVersion() {
        version++;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id='" + id + '\'' +
                ", payload='" + payload + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(id, item.id) &&
                Objects.equals(payload, item.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, payload);
    }
}
