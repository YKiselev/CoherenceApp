package org.uze.stores;

/**
 * Created by Uze on 16.08.2014.
 */
public class Test1Store extends BaseJdbcBinaryEntryStore {

    public Test1Store() {
        System.out.println(getClass().getName() + ".ctor()");
    }
}
