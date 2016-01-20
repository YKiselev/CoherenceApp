package org.uze.coherence.jdbc;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Created by Uze on 17.08.2014.
 */
public class TableMetadata {

    public static final int MIN_USER_TYPE_ID = 1000;

    private final String tableName;
    private final UserTypeColumns key;
    private final UserTypeColumns value;
    private final ImmutableMap<String, Column> columnMap;

    public String getTableName() {
        return tableName;
    }

    public UserTypeColumns getKey() {
        return key;
    }

    public UserTypeColumns getValue() {
        return value;
    }

    public ImmutableCollection<Column> getColumns() {
        return columnMap.values();
    }

    public ImmutableSet<String> getAllColumns() {
        return columnMap.keySet();
    }

    public Column getColumn(String name) {
        return columnMap.get(name);
    }

    private TableMetadata(String tableName, Iterable<Builder.BuilderColumn> columns, Iterable<String> keyColumnNames, int valueUserTypeId, int keyUserTypeId) {
        Objects.requireNonNull(tableName);
        Objects.requireNonNull(columns);
        Objects.requireNonNull(keyColumnNames);

        Preconditions.checkArgument(valueUserTypeId >= MIN_USER_TYPE_ID, "Illegal user type id: " + valueUserTypeId);

        final ImmutableList.Builder<String> listBuilder = ImmutableList.builder();
        final ImmutableMap.Builder<String, Column> mapBuilder = ImmutableMap.builder();
        for (Builder.BuilderColumn c : columns) {
            if (!c.isKeyOnly()) {
                listBuilder.add(c.getName());
            }
            mapBuilder.put(c.getName(), new Column(c.getSqlType(), c.getClazz(), c.isKeyOnly()));
        }

        this.tableName = tableName;
        this.columnMap = mapBuilder.build();
        this.key = new UserTypeColumnsImpl(keyUserTypeId, ImmutableList.copyOf(keyColumnNames));
        this.value = new UserTypeColumnsImpl(valueUserTypeId, listBuilder.build());
    }

    private class UserTypeColumnsImpl implements UserTypeColumns {

        private final int userTypeId;
        private final ImmutableList<String> names;

        @Override
        public int getUserTypeId() {
            return userTypeId;
        }

        @Override
        public ImmutableList<String> getNames() {
            return names;
        }

        @Override
        public int getSize() {
            return names.size();
        }

        @Override
        public Column getColumn(String name) {
            return columnMap.get(name);
        }

        @Override
        public Column getColumn(int index) {
            return columnMap.get(names.get(index));
        }

        @Override
        public String getName(int index) {
            return names.get(index);
        }

        public UserTypeColumnsImpl(int userTypeId, ImmutableList<String> names) {
            Preconditions.checkArgument(names.size() == 1 || userTypeId >= MIN_USER_TYPE_ID, "Illegal user type id: " + userTypeId);

            this.userTypeId = userTypeId;
            this.names = names;
        }
    }

    /**
     *
     */
    public static class Builder {

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
        private int keyUserTypeId = -1;
        private int valueUserTypeId = -1;

        public Builder(String tableName) {
            this.tableName = tableName;
        }

        public Builder column(String name, int sqlType, Class clazz) {
            Objects.requireNonNull(name);
            Objects.requireNonNull(clazz);

            Preconditions.checkArgument(indexOf(name) == -1, "Duplicated column name: " + name);

            current = new BuilderColumn(name, sqlType, clazz);
            columns.add(current);

            return this;
        }

        public Builder key() {
            return key(0);
        }

        private Builder setKey(int pos, boolean keyOnly) {
            Objects.requireNonNull(current, "Add column first!");
            if (pos < 0) {
                throw new IllegalArgumentException("Position must be positive or zero: " + pos);
            }

            current.keyPosition = pos;
            current.keyOnly = keyOnly;

            return this;
        }

        public Builder key(int pos) {
            return setKey(pos, false);
        }

        public Builder keyOnly() {
            return keyOnly(0);
        }

        public Builder keyOnly(int pos) {
            return setKey(pos, true);
        }

        public Builder withUserTypeId(int userTypeId) {
            this.valueUserTypeId = userTypeId;
            return this;
        }

        public Builder withKeyUserTypeId(int userTypeId) {
            this.keyUserTypeId = userTypeId;
            return this;
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

            return new TableMetadata(tableName, columns, key, valueUserTypeId, keyUserTypeId);
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
}
