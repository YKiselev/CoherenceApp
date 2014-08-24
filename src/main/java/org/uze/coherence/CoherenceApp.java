package org.uze.coherence;

import com.google.common.base.Charsets;
import com.tangosol.io.ByteArrayWriteBuffer;
import com.tangosol.io.Serializer;
import com.tangosol.io.pof.PofBufferWriter;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.internal.SessionOptimisticPut;
import com.tangosol.util.Binary;
import com.tangosol.util.BinaryWriteBuffer;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.uze.caches.CacheAccess;
import org.uze.client.Counterpart;
import org.uze.client.Trade;
import org.uze.pof.CounterpartPO;
import org.uze.spring.SpringContextHolder;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * User: Uze
 * Date: 09.12.13
 * Time: 23:15
 */
public class CoherenceApp {

    private static final String USAGE = "Usage:" +
            "\n\tcache <name>" +
            "\n\tput key value" +
            "\n\tget key" +
            "\n\tremove key" +
            "\n\tlist" +
            "\n\tbye" +
            "\n\n Don't forget to set right coherence config via -Dtangosol.coherence.cacheconfig=<cache-config.xml>!";
    private static boolean exitFlag;
    private static CacheAccess cache;

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.ENGLISH);

//        final JdbcTemplate template = SpringContextHolder.getBean("ch-app.jdbcTemplate", JdbcTemplate.class);
//
//        template.query("select * from table2 where (id,sub_id) in ((?,?),(?,?))", new PreparedStatementSetter() {
//            @Override
//            public void setValues(PreparedStatement ps) throws SQLException {
//                ps.setLong(1, 1L);
//                ps.setString(2, "Some name");
//                ps.setLong(3, 2L);
//                ps.setString(4, "Name");
//            }
//        }, new RowCallbackHandler() {
//            @Override
//            public void processRow(ResultSet rs) throws SQLException {
//                System.out.println(rs.getString("ID"));
//            }
//        });
//
//        System.exit(0);

        CacheFactory.getCacheFactoryBuilder()
            .getConfigurableCacheFactory(CoherenceApp.class.getClassLoader())
            .getResourceRegistry()
            .registerResource(BeanFactory.class, SpringContextHolder.getApplicationContext());

        final NamedCache test1 = CacheFactory.getCache("Test1");

        Map m1 = test1.getAll(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L));
        CounterpartPO c = new CounterpartPO();
        c.setId(1L);
        c.setName("New counterpart #1");
        test1.put(123L, c);

        //////////////////////
        //PofBufferWriter writer = new PofBufferWriter(buffer, (()test1.getCacheService().getSerializer();
        /////////////////
        final Counterpart cp2 =new Counterpart();

        cp2.setId(1L);
        cp2.setName("New user #2");

        final Counterpart oldcp = CacheAccess.COUNTERPARTS.put(cp2.getId(), cp2);
        System.out.println("Old cp: "+oldcp);
        ///////////////

        // check raw cache items
        final NamedCache tmp = CacheFactory.getCache("Counterparts");
        Object raw1 = tmp.get(1L);
        if (raw1 == null) {
            fillDatabase();
        } else {
            Object raw2 = tmp.get(1L);
            System.out.println("obj1 equals obj2 ? " + (raw1.equals(raw2)) + ", obj1 == obj2 ? " + (raw1 == raw2));
        }

        System.out.println(USAGE);

        final StreamTokenizer tokenizer = new StreamTokenizer(new InputStreamReader(System.in));
        tokenizer.slashSlashComments(false);
        tokenizer.eolIsSignificant(true);
        tokenizer.slashStarComments(false);
        int i;
        final List<String> tokens = new ArrayList<String>();
        while (!exitFlag && ((i = tokenizer.nextToken()) != StreamTokenizer.TT_EOF)) {
            switch (i) {
                case StreamTokenizer.TT_EOL:
                    // parse
                    try {
                        processCommandLine(tokens.toArray(new String[tokens.size()]));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    tokens.clear();
                    break;

                case StreamTokenizer.TT_NUMBER:
                    tokens.add(Long.toString((long) tokenizer.nval));
                    break;

                case StreamTokenizer.TT_WORD:
                case '"':
                case '\'':
                    tokens.add(tokenizer.sval);
                    break;

                default:
                    System.out.print("Unknown token: " + i);
            }
        }
    }

    private static void fillDatabase() {
        System.out.println("Populating database with default objects");

        Counterpart cpart = new Counterpart();

        cpart.setId(1L);
        cpart.setName("John Smith");

        CacheAccess.COUNTERPARTS.put(cpart.getId(), cpart);

        Trade trade = new Trade();

        trade.setId(1L);
        trade.setName("Trade #1");
        trade.setCounterpart(cpart);
        trade.setDate(new Date());

        CacheAccess.TRADES.put(trade.getId(), trade);

        System.out.println("Database population complete");
    }

    private static void processCommandLine(String[] args) {
        if (args.length == 0) {
            return;
        }

        String command = null, key = null, value = null;
        if (args.length > 0) {
            command = args[0];
        }
        if (args.length > 1) {
            key = args[1];
        }
        if (args.length > 2) {
            value = args[2];
        }

        if ("cache".equals(command)) {
            cache = CacheAccess.getCache(key);
            return;
        } else if ("bye".equals(command)) {
            exitFlag = true;
            return;
        } else if (cache == null) {
            System.out.println("Select cache first!");
            return;
        }

        if ("put".equals(command)) {
            if (key != null && value != null) {
                Object first = cache.get(1L);
                if (first instanceof Counterpart) {
                    Counterpart cp = (Counterpart) first;

                    cp.setId(Long.parseLong(key));
                    cp.setName(value);

                    cache.put(cp.getId(), cp);
                } else if (first instanceof Trade) {
                    Trade trade = (Trade) first;

                    trade.setId(Long.parseLong(key));
                    trade.setName(value);
                    trade.setDate(new Date());

                    cache.put(trade.getId(), trade);
                } else {
                    System.out.println("Unsupported operation for this cache!");
                }
            } else {
                System.out.println("Need a key and value!");
            }
        } else if ("get".equals(command)) {
            if (key != null) {
                System.out.println(key + "=" + cache.get(Long.parseLong(key)));
            } else {
                System.out.println("Need a key!");
            }
        } else if ("remove".equals(command)) {
            if (key != null) {
                System.out.println(cache.remove(Long.parseLong(key)));
            } else {
                System.out.println("Need a key!");
            }
        } else if ("list".equals(command)) {
            for (Object key2 : cache.keySet()) {
                System.out.println(key2 + "=" + cache.get(Long.parseLong(key2.toString())));
            }
        } else {
            System.out.println("Unknown command: " + command);
        }
    }
}
