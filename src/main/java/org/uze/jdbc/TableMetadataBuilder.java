package org.uze.jdbc;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Created by Uze on 17.08.2014.
 */
public class TableMetadataBuilder {

    static class BuilderColumn {

        private final String name;
        private final int sqlType;
        private final Class clazz;
        private int keyPosition = -1;
        private boolean keyOnly;

        public String getName() {
            return name;
        }

        public int getSqlType() {
            return sqlType;
        }

        public Class getClazz() {
            return clazz;
        }

        public int getKeyPosition() {
            return keyPosition;
        }

        public void setKeyPosition(int keyPosition) {
            this.keyPosition = keyPosition;
        }

        public boolean isKeyOnly() {
            return keyOnly;
        }

        public void setKeyOnly(boolean keyOnly) {
            this.keyOnly = keyOnly;
        }

        public boolean isKey() {
            return keyPosition >= 0;
        }

        BuilderColumn(String name, int sqlType, Class clazz) {
            this.name = name;
            this.sqlType = sqlType;
            this.clazz = clazz;
        }
    }

    private final String tableName;
    private final List<BuilderColumn> columns = new ArrayList<>();
    private BuilderColumn current;

    public TableMetadataBuilder(String tableName) {
        this.tableName = tableName;
    }

    public TableMetadataBuilder column(String name, int sqlType, Class clazz) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(clazz);

        Preconditions.checkArgument(indexOf(name) == -1, "Duplicated column name: " + name);

        current = new BuilderColumn(name, sqlType, clazz);
        columns.add(current);

        return this;
    }

    public TableMetadataBuilder key() {
        return key(0);
    }

    private TableMetadataBuilder setKey(int pos, boolean keyOnly) {
        Objects.requireNonNull(current, "Add column first!");
        if (pos < 0) {
            throw new IllegalArgumentException("Position must be positive or zero: " + pos);
        }

        current.keyPosition = pos;
        current.keyOnly = keyOnly;

        return this;
    }

    public TableMetadataBuilder key(int pos) {
        return setKey(pos, false);
    }

    public TableMetadataBuilder keyOnly() {
        return keyOnly(0);
    }

    public TableMetadataBuilder keyOnly(int pos) {
        return setKey(pos, true);
    }

    public TableMetadata build() {
        final Iterable<String> key = Iterables.transform(Ordering.from(KeyColumnComparator.INSTANCE)
            .sortedCopy(Iterables.filter(columns, new Predicate<BuilderColumn>() {
                @Override
                public boolean apply(@Nullable BuilderColumn input) {
                    return input != null && input.isKey();
                }
            })), new Function<BuilderColumn, String>() {
            @Override
            public String apply(@Nullable BuilderColumn input) {
                return input != null ? input.getName() : null;
            }
        });

        int prevPosition = -1;
        for (String keyColumn : key) {
            final int pos = find(keyColumn).getKeyPosition();
            Preconditions.checkArgument((prevPosition == -1 && pos == 0) || (pos == prevPosition + 1));
            prevPosition = pos;
        }

        return new TableMetadata(tableName, columns, key);
    }

    private int indexOf(String name) {
        for (int i = 0; i < columns.size(); i++) {
            if (name.equals(columns.get(i).getName())) {
                return i;
            }
        }

        return -1;
    }

    private BuilderColumn find(String name) {
        final int index = indexOf(name);

        if (index >= 0) {
            return columns.get(index);
        }

        throw new IllegalArgumentException("Column not found: " + name);
    }

    enum KeyColumnComparator implements Comparator<BuilderColumn> {

        INSTANCE;

        @Override
        public int compare(BuilderColumn o1, BuilderColumn o2) {
            return Integer.compare(o1.getKeyPosition(), o2.getKeyPosition());
        }
    }
}
