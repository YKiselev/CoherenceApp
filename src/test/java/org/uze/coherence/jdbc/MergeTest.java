package org.uze.coherence.jdbc;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.Types;
import java.util.*;

/**
 * Created by Uze on 23.08.2014.
 */
@RunWith(Parameterized.class)
@TestExecutionListeners(listeners = {
    DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations = {"/spring/coherence-app-context.xml"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MergeTest {

    enum TestKind {
        SINGLE_INSERT, DUMB_BATCH, MERGE
    }

    static class Data {
        private long time;
        private int counter;

        public long getTime() {
            return time;
        }

        public void add(long delta) {
            time += delta;
        }

        public int getCounter() {
            return counter;
        }

        public void incCounter() {
            counter++;
        }
    }

    public static final int TEST_RUN_COUNT = 3;
    public static final int TEST_DATA_SIZE = 1000;
    @Autowired
    private JdbcTemplate template;
    @Autowired
    private PlatformTransactionManager txManager;
    private final List<Item> items;
    private long t0;
    private TestKind testKind = TestKind.MERGE;
    private static final Map<TestKind, Data> averageTimes = new HashMap<>();

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        final TestKind[] kinds = TestKind.values();
        long counter = 0;
        final SecureRandom random = new SecureRandom();
        final List<Object[]> result = new ArrayList<>();
        for (int i = 0; i < TEST_RUN_COUNT; i++) {
            final List<Item> items = new ArrayList<>(TEST_DATA_SIZE);
            for (int k = 0; k < TEST_DATA_SIZE; k++) {
                items.add(new Item(++counter, new BigInteger(130, random).toString(32)));
            }
            result.add(new Object[]{items, kinds[(int) (counter % kinds.length)]});
        }
        return result;
    }

    public MergeTest(List<Item> items, TestKind testKind) {
        this.items = items;
        this.testKind = testKind;
    }

    private void addTime(TestKind kind, long delta) {
        Data data = averageTimes.get(kind);
        if (data == null) {
            data = new Data();
            averageTimes.put(kind, data);
        }
        data.add(delta);
        data.incCounter();
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        Locale.setDefault(Locale.ENGLISH);
    }

    @Before
    public void setUp() throws Exception {
        final TestContextManager manager = new TestContextManager(getClass());
        manager.prepareTestInstance(this);

        t0 = System.nanoTime();
    }

    @After
    public void tearDown() throws Exception {
        addTime(testKind, System.nanoTime() - t0);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println("Single batch size: " + TEST_DATA_SIZE);

        for (Map.Entry<TestKind, Data> entry : averageTimes.entrySet()) {
            final Data data = entry.getValue();
            final long dt = data.getTime() / 1000000L;
            System.out.println("Test " + entry.getKey() + " run " + data.getCounter() + " times with total time (ms): " + dt);
            System.out.println("Average time (ms): " + dt / data.getCounter());
        }
    }

    @Test
    public void test() throws Exception {
        new TransactionTemplate(txManager).execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus transactionStatus) {
                switch (testKind) {
                    case SINGLE_INSERT:
                        mergeOneByOne(items);
                        break;

                    case DUMB_BATCH:
                        mergeWithDumbBatch(items);
                        break;

                    case MERGE:
                        mergeWithMerge(items);
                        break;


                    default:
                        mergeOneByOne(items);
                        mergeWithDumbBatch(items);
                        mergeWithMerge(items);
                        break;
                }
                return null;
            }
        });
    }

    private void mergeOneByOne(Iterable<Item> items) {
        final JdbcOperations operations = template;//. .getJdbcOperations();

        for (Item current : items) {
            int count = operations.update("update Test1 set NAME=? where ID=?",
                new Object[]{current.getName(), current.getId()},
                new int[]{Types.VARCHAR, Types.BIGINT});

            if (count == 0) {
                count = operations.update("insert into Test1 (ID, NAME)values(?,?)",
                    new Object[]{current.getId(), current.getName()},
                    new int[]{Types.BIGINT, Types.VARCHAR});
            }
        }
    }

    private void mergeWithDumbBatch(List<Item> items) {
        final JdbcOperations operations = template;//.getJdbcOperations();
        final List<Object[]> batchArgs = new ArrayList<>(items.size());
        final List<Object[]> batchArgs2 = new ArrayList<>(items.size());
        for (Item item : items) {
            batchArgs.add(new Object[]{item.getId(), item.getName(), item.getId()});
            batchArgs2.add(new Object[]{item.getName(), item.getId()});
        }

        operations.batchUpdate("update Test1 set NAME=? where ID=?", batchArgs2, new int[]{
            Types.VARCHAR, Types.BIGINT
        });
        operations.batchUpdate("insert into Test1 (ID, NAME) select ?,? from dual where not exists (select null from Test1 where ID = ?)",
            batchArgs, new int[]{Types.BIGINT, Types.VARCHAR, Types.BIGINT});
    }

    private void mergeWithMerge(List<Item> items) {
        final JdbcOperations operations = template;//.getJdbcOperations();
        final List<Object[]> batchArgs = new ArrayList<>(items.size());
        for (Item item : items) {
            batchArgs.add(new Object[]{item.getId(), item.getName()});
        }

        operations.batchUpdate("merge into Test1 t1 using (select ? as ID,? as NAME from dual) t2 on (t2.ID = t1.ID)" +
                " when matched then update set NAME=t2.NAME" +
                " when not matched then insert (ID, NAME) values (t2.ID,t2.NAME)",
            batchArgs, new int[]{Types.BIGINT, Types.VARCHAR});
    }

    /**
     * Test data class
     */
    public static class Item {
        private long id;
        private String name;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Item() {
        }

        public Item(long id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return "Item{" +
                "getId=" + id +
                ", name='" + name + '\'' +
                '}';
        }
    }
}
