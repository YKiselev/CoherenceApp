package org.uze.jdbc;

import org.junit.Assert;
import org.junit.Test;

import java.sql.Types;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.*;

/**
 * Created by Uze on 18.08.2014.
 */
public class TableMetadataBuilderTest {

    public static final TableMetadata MULTI_COLUMN_KEY_TABLE_METADATA = new TableMetadataBuilder("TABLE1")
        .column("COB", Types.VARCHAR, String.class)
        .keyOnly(1)
        .column("ID", Types.INTEGER, int.class)
        .keyOnly(0)
        .column("NAME", Types.VARCHAR, String.class)
        .column("DESC", Types.VARCHAR, String.class)
        .column("CREATED_DATE", Types.TIMESTAMP, Date.class)
        .column("UPDATED_DATE", Types.TIMESTAMP, Date.class)
        .column("GROUP_ID", Types.NUMERIC, Long.class)
        .build();
    public static final TableMetadata SINGLE_COLUMN_KEY_TABLE_METADATA = new TableMetadataBuilder("TABLE1")
        .column("ID", Types.INTEGER, int.class)
        .key(0)
        .column("NAME", Types.VARCHAR, String.class)
        .column("DESC", Types.VARCHAR, String.class)
        .column("CREATED_DATE", Types.TIMESTAMP, Date.class)
        .column("UPDATED_DATE", Types.TIMESTAMP, Date.class)
        .column("GROUP_ID", Types.NUMERIC, Long.class)
        .build();

    @Test
    public void testKey() throws Exception {
        final TableMetadata md = new TableMetadataBuilder("TABLE1")
            .column("COB", Types.VARCHAR, String.class)
            .keyOnly(1)
            .column("ID", Types.INTEGER, int.class)
            .keyOnly(0)
            .build();

        Assert.assertNotNull(md);
        Assert.assertThat(md.getKeyColumns(), hasSize(2));
        Assert.assertThat(md.getKeyColumns(), hasItems("ID", "COB"));
//        Assert.assertThat(md.getColumns(), hasEntry(is("ID"), equalTo(new TableMetadata.Column(Types.INTEGER, int.class))));
//        Assert.assertThat(md.getColumns(), Matchers.<String, TableMetadata.Column>hasEntry(is("ID"), allOf(
//                hasProperty("sqlType", is(Types.INTEGER)),
//                hasProperty("clazz", isA(int.class))
//        )));
    }

    @Test
    public void testMultyKeySelectStatement() throws Exception {
        final String sql = StatementHelper.buildSelectStatement(MULTI_COLUMN_KEY_TABLE_METADATA, 20);
        Assert.assertNotNull(sql);

        final Pattern pattern = Pattern.compile("\\s*SELECT\\s+(.+)\\s+FROM\\s+(.+)\\s+WHERE\\s*\\((.+)\\)\\s*IN\\s*\\((.+)\\)", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);

        final Matcher m = pattern.matcher(sql);
        Assert.assertTrue(m.matches());

        final String[] columns = m.group(1).split(",");
        Assert.assertThat(columns, arrayWithSize(7));

        final String[] tables = m.group(2).split(",");
        Assert.assertThat(tables, arrayWithSize(1));

        final String[] keys = m.group(3).split(",");
        Assert.assertThat(keys, arrayWithSize(2));

        final String[] values = m.group(4).split("(?<=\\))\\s*,\\s*(?=\\()");
        Assert.assertThat(values, arrayWithSize(20));
    }

    @Test
    public void testSimpleKeySelectStatement() throws Exception {
        final String sql = StatementHelper.buildSelectStatement(SINGLE_COLUMN_KEY_TABLE_METADATA, 7);
        Assert.assertNotNull(sql);

        final Pattern pattern = Pattern.compile("\\s*SELECT\\s+(.+)\\s+FROM\\s+(.+)\\s+WHERE\\s*(.+)\\s*IN\\s*\\((.+)\\)", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);

        final Matcher m = pattern.matcher(sql);
        Assert.assertTrue(m.matches());

        final String[] columns = m.group(1).split(",");
        Assert.assertThat(columns, arrayWithSize(6));

        final String[] tables = m.group(2).split(",");
        Assert.assertThat(tables, arrayWithSize(1));

        final String[] keys = m.group(3).split(",");
        Assert.assertThat(keys, arrayWithSize(1));

        final String[] values = m.group(4).split("\\s*,\\s*");
        Assert.assertThat(values, arrayWithSize(7));
    }

    @Test
    public void testMultyKeyInsertStatement() throws Exception {
        final String sql = StatementHelper.buildInsertStatement(MULTI_COLUMN_KEY_TABLE_METADATA);
        Assert.assertNotNull(sql);

        final Pattern pattern = Pattern.compile("\\s*INSERT\\s+INTO\\s+(.+)\\s*\\((.+)\\)\\s*SELECT\\s*(.+)\\s+FROM DUALWHERE\\s*\\((.+)\\)\\s*IN\\s*\\((.+)\\)",
            Pattern.CASE_INSENSITIVE + Pattern.DOTALL);

        final Matcher m = pattern.matcher(sql);
        Assert.assertTrue(m.matches());

        final String[] columns = m.group(1).split(",");
        Assert.assertThat(columns, arrayWithSize(7));

        final String[] tables = m.group(2).split(",");
        Assert.assertThat(tables, arrayWithSize(1));

        final String[] keys = m.group(3).split(",");
        Assert.assertThat(keys, arrayWithSize(2));

        final String[] values = m.group(4).split("(?<=\\))\\s*,\\s*(?=\\()");
        Assert.assertThat(values, arrayWithSize(20));
    }
}
