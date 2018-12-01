package org.uze.coherence.model;

import java.io.Serializable;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class Key implements Serializable {

    private final long id;

    private final long group;

    public long id() {
        return id;
    }

    public long group() {
        return group;
    }

    public Key(long id, long group) {
        this.id = id;
        this.group = group;
    }
}
