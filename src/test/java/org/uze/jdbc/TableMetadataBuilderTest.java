package org.uze.jdbc;

import org.junit.Assert;
import org.junit.Test;

import java.sql.Types;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.*;

/**
 * C - without key columns
 * select C
 * from t
 * where (K) in ((?),(?),(?))
 *
 * batch
 * C - with key columns
 * insert into t (C)
 * select Cv from dual
 * where not exists (select null from t where K1 = ?, K2 = ?)
 *
 * batch
 * C - without key columns
 * update t
 * set C = Cv,...
 * where K = ?
 *
 * * Created by Uze on 18.08.2014.
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

        final Pattern pattern = Pattern.compile("\\s*INSERT\\s+INTO\\s+(.+)\\s*\\((.+)\\)\\s*SELECT\\s*(.+)\\s+FROM DUAL\\s+WHERE\\s*NOT\\s+EXISTS\\s*\\(\\s*SELECT\\s+NULL\\s+FROM\\s+(.+)\\s+WHERE\\s+(.+)\\)",
            Pattern.CASE_INSENSITIVE + Pattern.DOTALL);

        final Matcher m = pattern.matcher(sql);
        Assert.assertTrue(m.matches());

        final String[] tables = m.group(1).split(",");
        Assert.assertThat(tables, arrayWithSize(1));

        final String[] columns = m.group(2).split(",");
        Assert.assertThat(columns, arrayWithSize(7));

        final String[] values = m.group(3).split(",");
        Assert.assertThat(values, arrayWithSize(7));

        final String[] tables2 = m.group(4).split(",");
        Assert.assertThat(tables2, arrayWithSize(1));

        final String[] keys = m.group(5).split("\\s+AND\\s+");
        Assert.assertThat(keys, arrayWithSize(2));
    }

    @Test
    public void testUpdateStatement() throws Exception {
        final String sql = StatementHelper.buildUpdateStatement(MULTI_COLUMN_KEY_TABLE_METADATA);
        Assert.assertNotNull(sql);

        final Pattern pattern = Pattern.compile("\\s*UPDATE\\s+(.+)\\s+SET\\s+(.+)\\s+WHERE\\s+(.+)", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);

        final Matcher m = pattern.matcher(sql);
        Assert.assertTrue(m.matches());

        final String[] tables = m.group(1).split(",");
        Assert.assertThat(tables, arrayWithSize(1));

        final String[] columns = m.group(2).split(",");
        Assert.assertThat(columns, arrayWithSize(7));

        final String[] keys = m.group(3).split("\\s+AND\\s+");
        Assert.assertThat(keys, arrayWithSize(2));
    }

}
