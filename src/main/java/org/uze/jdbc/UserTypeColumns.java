package org.uze.jdbc;

import com.google.common.collect.ImmutableList;

/**
 * Created by Uze on 31.08.2014.
 */
public interface UserTypeColumns {
    int getUserTypeId();

    ImmutableList<String> getNames();

    int getSize();

    String getName(int index);

    Column getColumn(String name);

    Column getColumn(int index);
}
