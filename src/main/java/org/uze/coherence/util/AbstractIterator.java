package org.uze.coherence.util;

import java.util.Iterator;

/**
* Created by Uze on 30.08.2014.
*/
public abstract class AbstractIterator<T> implements Iterator<T> {

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
