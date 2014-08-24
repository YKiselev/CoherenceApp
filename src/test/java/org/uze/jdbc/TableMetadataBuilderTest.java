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
 * <p/>
 * batch
 * C - with key columns
 * insert into t (C)
 * select Cv from dual
 * where not exists (select null from t where K1 = ?, K2 = ?)
 * <p/>
 * batch
 * C - without key columns
 * update t
 * set C = Cv,...
 * where K = ?
 * <p/>
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

    private final StatementBuilder statementBuilder = new OracleStatementBuilder();

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
        final String sql = statementBuilder.buildSelectStatement(MULTI_COLUMN_KEY_TABLE_METADATA, 20);
        Assert.assertNotNull(sql);

        final Pattern pattern = Pattern.compile("\\s*SELECT\\s+(.+)\\s+FROM\\s+(.+)\\s+WHERE\\s*\\((.+)\\)\\s*IN\\s*\\((.+)\\)",
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

    @Test
    public void testSimpleKeySelectStatement() throws Exception {
        final String sql = statementBuilder.buildSelectStatement(SINGLE_COLUMN_KEY_TABLE_METADATA, 7);
        Assert.assertNotNull(sql);

        final Pattern pattern = Pattern.compile("\\s*SELECT\\s+(.+)\\s+FROM\\s+(.+)\\s+WHERE\\s*(.+)\\s*IN\\s*\\((.+)\\)",
            Pattern.CASE_INSENSITIVE + Pattern.DOTALL);

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
        Assert.assertThat(columns, arrayWithSize(7));

        final String[] values = m.group(3).split(",");
        Assert.assertThat(values, arrayWithSize(7));
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
        Assert.assertThat(columns, arrayWithSize(7));

        final String[] keys = m.group(3).split("\\s+AND\\s+");
        Assert.assertThat(keys, arrayWithSize(2));
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
        Assert.assertThat(using, arrayWithSize(7));

        final String alias = m.group(3);
        Assert.assertEquals("S", alias);

        final String[] on = m.group(4).split("\\s+AND\\s+");
        Assert.assertThat(on, arrayWithSize(2));

        final String[] update = m.group(5).split(",");
        Assert.assertThat(update, arrayWithSize(5));

        final String[] columns = m.group(6).split(",");
        Assert.assertThat(columns, arrayWithSize(7));

        final String[] values = m.group(7).split(",");
        Assert.assertThat(values, arrayWithSize(7));
    }
}
