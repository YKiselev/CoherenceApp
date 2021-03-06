package org.uze.coherence.jdbc;

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
public class StatementBuilderTest {

    public static final TableMetadata MULTI_COLUMN_KEY_TABLE_METADATA = new TableMetadata.Builder("TABLE1")
        .column("COB", Types.VARCHAR, String.class)
        .keyOnly(1)
        .column("ID", Types.INTEGER, int.class)
        .keyOnly(0)
        .column("NAME", Types.VARCHAR, String.class)
        .column("DESC", Types.VARCHAR, String.class)
        .column("CREATED_DATE", Types.TIMESTAMP, Date.class)
        .column("UPDATED_DATE", Types.TIMESTAMP, Date.class)
        .column("GROUP_ID", Types.NUMERIC, Long.class)
        .withUserTypeId(1005)
        .withKeyUserTypeId(1006)
        .build();
    public static final TableMetadata SINGLE_COLUMN_KEY_TABLE_METADATA = new TableMetadata.Builder("TABLE1")
        .column("ID", Types.INTEGER, int.class)
        .key(0)
        .column("NAME", Types.VARCHAR, String.class)
        .column("DESC", Types.VARCHAR, String.class)
        .column("CREATED_DATE", Types.TIMESTAMP, Date.class)
        .column("UPDATED_DATE", Types.TIMESTAMP, Date.class)
        .column("GROUP_ID", Types.NUMERIC, Long.class)
        .withUserTypeId(1004)
        .build();

    private final StatementBuilder statementBuilder = new OracleStatementBuilder();

    @Test
    public void testKey() throws Exception {
        final TableMetadata md = new TableMetadata.Builder("TABLE1")
            .column("COB", Types.VARCHAR, String.class)
            .keyOnly(1)
            .column("ID", Types.INTEGER, int.class)
            .keyOnly(0)
            .withUserTypeId(1001)
            .withKeyUserTypeId(1002)
            .build();

        Assert.assertNotNull(md);
        Assert.assertThat(md.getKey().getSize(), is(2));
        Assert.assertThat(md.getKey().getNames(), hasItems("ID", "COB"));
    }

    @Test
    public void testMultiKeySelectStatement() throws Exception {
        final String sql = statementBuilder.buildSelectStatement(MULTI_COLUMN_KEY_TABLE_METADATA, 20);
        Assert.assertNotNull(sql);

        final Pattern pattern = Pattern.compile("\\s*SELECT\\s+(.+)\\s+FROM\\s+(.+)\\s+WHERE\\s*\\((.+)\\)\\s*IN\\s*\\((.+)\\)",
            Pattern.CASE_INSENSITIVE + Pattern.DOTALL);

        final Matcher m = pattern.matcher(sql);
        Assert.assertTrue(m.matches());

        final String[] columns = m.group(1).split(",");
        Assert.assertThat(columns, arrayWithSize(MULTI_COLUMN_KEY_TABLE_METADATA.getValue().getSize()));

        final String[] tables = m.group(2).split(",");
        Assert.assertThat(tables, arrayWithSize(1));

        final String[] keys = m.group(3).split(",");
        Assert.assertThat(keys, arrayWithSize(MULTI_COLUMN_KEY_TABLE_METADATA.getKey().getSize()));

        final String[] values = m.group(4).split("(?<=\\))\\s*,\\s*(?=\\()");
        Assert.assertThat(values, arrayWithSize(20));
    }

    @Test
    public void testSimpleKeySelectStatement() throws Exception {
        final String sql = statementBuilder.buildSelectStatement(SINGLE_COLUMN_KEY_TABLE_METADATA, 7);
        Assert.assertNotNull(sql);

        final Pattern pattern = Pattern.compile("\\s*SELECT\\s+(.+)\\s+FROM\\s+(.+)\\s+WHERE\\s*(.+)\\s*IN\\s*\\((.+)\\)",
            Pattern.CASE_INSENSITIVE + Pattern.DOTALL);

        final Matcher m = pattern.matcher(sql);
        Assert.assertTrue(m.matches());

        final String[] columns = m.group(1).split(",");
        Assert.assertThat(columns, arrayWithSize(SINGLE_COLUMN_KEY_TABLE_METADATA.getValue().getSize()));

        final String[] tables = m.group(2).split(",");
        Assert.assertThat(tables, arrayWithSize(1));

        final String[] keys = m.group(3).split(",");
        Assert.assertThat(keys, arrayWithSize(SINGLE_COLUMN_KEY_TABLE_METADATA.getKey().getSize()));

        final String[] values = m.group(4).split("\\s*,\\s*");
        Assert.assertThat(values, arrayWithSize(7));
    }

    @Test
    public void testInsertStatement() throws Exception {
        final String sql = statementBuilder.buildInsertStatement(MULTI_COLUMN_KEY_TABLE_METADATA);
        Assert.assertNotNull(sql);

        final Pattern pattern = Pattern.compile("\\s*INSERT\\s+INTO\\s+(.+)\\s*\\((.+)\\)\\s*VALUES\\s*\\((.+)\\)",
            Pattern.CASE_INSENSITIVE + Pattern.DOTALL);

        final Matcher m = pattern.matcher(sql);
        Assert.assertTrue(m.matches());

        final String[] tables = m.group(1).split(",");
        Assert.assertThat(tables, arrayWithSize(1));

        final String[] columns = m.group(2).split(",");
        Assert.assertThat(columns, arrayWithSize(MULTI_COLUMN_KEY_TABLE_METADATA.getAllColumns().size()));

        final String[] values = m.group(3).split(",");
        Assert.assertThat(values, arrayWithSize(MULTI_COLUMN_KEY_TABLE_METADATA.getAllColumns().size()));
    }

    @Test
    public void testUpdateStatement() throws Exception {
        final String sql = statementBuilder.buildUpdateStatement(MULTI_COLUMN_KEY_TABLE_METADATA);
        Assert.assertNotNull(sql);

        final Pattern pattern = Pattern.compile("\\s*UPDATE\\s+(.+)\\s+SET\\s+(.+)\\s+WHERE\\s+(.+)",
            Pattern.CASE_INSENSITIVE + Pattern.DOTALL);

        final Matcher m = pattern.matcher(sql);
        Assert.assertTrue(m.matches());

        final String[] tables = m.group(1).split(",");
        Assert.assertThat(tables, arrayWithSize(1));

        final String[] columns = m.group(2).split(",");
        Assert.assertThat(columns, arrayWithSize(MULTI_COLUMN_KEY_TABLE_METADATA.getValue().getSize()));

        final String[] keys = m.group(3).split("\\s+AND\\s+");
        Assert.assertThat(keys, arrayWithSize(MULTI_COLUMN_KEY_TABLE_METADATA.getKey().getSize()));
    }

    @Test
    public void testMergeStatement() throws Exception {
        final String sql = statementBuilder.buildMergeStatement(MULTI_COLUMN_KEY_TABLE_METADATA);
        Assert.assertNotNull(sql);

        final Pattern pattern = Pattern.compile("\\s*MERGE\\s+INTO\\s+(.+)\\s+USING\\s*\\(\\s*SELECT\\s+(.+)\\s+FROM\\s+DUAL\\)\\s+(\\w)\\s*ON\\s*\\((.+)\\)\\s+" +
                "WHEN\\s+MATCHED\\s+THEN\\s+UPDATE\\s+SET\\s+(.+)\\s+" +
                "WHEN\\s+NOT\\s+MATCHED\\s+THEN\\s+INSERT\\s*\\((.+)\\)\\s*VALUES\\s*\\((.+)\\)",
            Pattern.CASE_INSENSITIVE + Pattern.DOTALL);

        final Matcher m = pattern.matcher(sql);
        Assert.assertTrue(m.matches());

        final String[] tables = m.group(1).split(",");
        Assert.assertThat(tables, arrayWithSize(1));

        final String[] using = m.group(2).split(",");
        Assert.assertThat(using, arrayWithSize(MULTI_COLUMN_KEY_TABLE_METADATA.getAllColumns().size()));

        final String alias = m.group(3);
        Assert.assertEquals("S", alias);

        final String[] on = m.group(4).split("\\s+AND\\s+");
        Assert.assertThat(on, arrayWithSize(MULTI_COLUMN_KEY_TABLE_METADATA.getKey().getSize()));

        final String[] update = m.group(5).split(",");
        Assert.assertThat(update, arrayWithSize(MULTI_COLUMN_KEY_TABLE_METADATA.getValue().getSize()));

        final String[] columns = m.group(6).split(",");
        Assert.assertThat(columns, arrayWithSize(MULTI_COLUMN_KEY_TABLE_METADATA.getAllColumns().size()));

        final String[] values = m.group(7).split(",");
        Assert.assertThat(values, arrayWithSize(MULTI_COLUMN_KEY_TABLE_METADATA.getAllColumns().size()));
    }

    @Test
    public void testDeleteStatement() throws Exception {
        final String sql = statementBuilder.buildDeleteStatement(MULTI_COLUMN_KEY_TABLE_METADATA, 15);
        Assert.assertNotNull(sql);

        final Pattern pattern = Pattern.compile("\\s*DELETE\\s+FROM\\s+(.+)\\s+WHERE\\s*\\((.+)\\)\\s*IN\\s*\\((.+)\\)",
            Pattern.CASE_INSENSITIVE + Pattern.DOTALL);

        final Matcher m = pattern.matcher(sql);
        Assert.assertTrue(m.matches());

        final String[] tables = m.group(1).split(",");
        Assert.assertThat(tables, arrayWithSize(1));

        final String[] keys = m.group(2).split(",");
        Assert.assertThat(keys, arrayWithSize(MULTI_COLUMN_KEY_TABLE_METADATA.getKey().getSize()));

        final String[] values = m.group(3).split("(?<=\\))\\s*,\\s*(?=\\()");
        Assert.assertThat(values, arrayWithSize(15));
    }

}
