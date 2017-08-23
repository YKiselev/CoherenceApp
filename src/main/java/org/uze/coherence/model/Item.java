package org.uze.coherence.model;

import java.io.Serializable;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class Item implements Serializable {

    private static final long serialVersionUID = 1598581650977142105L;

    private String id;

    private String payload;

    public Item() {
    }

    public Item(String id, String payload) {
        this.id = id;
        this.payload = payload;
    }

    public String id() {
        return id;
    }

    public String payload() {
        return payload;
    }
}
