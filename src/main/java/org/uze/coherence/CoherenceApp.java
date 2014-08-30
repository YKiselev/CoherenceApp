package org.uze.coherence;

import com.tangosol.io.Serializer;
import com.tangosol.io.WriteBuffer;
import com.tangosol.io.pof.PofBufferWriter;
import com.tangosol.io.pof.PofContext;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Binary;
import com.tangosol.util.BinaryWriteBuffer;
import com.tangosol.util.ExternalizableHelper;
import org.springframework.beans.factory.BeanFactory;
import org.uze.caches.CacheAccess;
import org.uze.client.Counterpart;
import org.uze.client.Trade;
import org.uze.pof.CounterpartPO;
import org.uze.pof.MyPofContext;
import org.uze.spring.SpringContextHolder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
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

        CacheFactory.getCacheFactoryBuilder()
            .getConfigurableCacheFactory(CoherenceApp.class.getClassLoader())
            .getResourceRegistry()
            .registerResource(BeanFactory.class, SpringContextHolder.getApplicationContext());

        final NamedCache test1 = CacheFactory.getCache("Test1");
        final Serializer serializer = test1.getCacheService().getSerializer();
        CounterpartPO c1 = new CounterpartPO();
        c1.setId(123L);
        c1.setName("John Doe");

        final PofContext pofContext = new MyPofContext((PofContext) serializer);

        final Binary b1 = ExternalizableHelper.toBinary(c1, pofContext);
        final Binary b12 = toBinary(c1, pofContext);

        c1.setName(null);
        final Binary b2 = ExternalizableHelper.toBinary(c1, pofContext);
        final Binary b22 = toBinary(c1, pofContext);

        c1.setId(null);
        c1.setName("John Doe");
        final Binary b3 = ExternalizableHelper.toBinary(c1, pofContext);
        final Binary b32 = toBinary(c1, pofContext);

        c1.setId(null);
        c1.setName(null);
        final Binary b4 = ExternalizableHelper.toBinary(c1, pofContext);
        final Binary b42 = toBinary(c1, pofContext);

        System.out.println(b1 + " == " + b12 + " ? " + b1.equals(b12));
        System.out.println(b2 + " == " + b22 + " ? " + b2.equals(b22));
        System.out.println(b3 + " == " + b32 + " ? " + b3.equals(b32));
        System.out.println(b4 + " == " + b42 + " ? " + b4.equals(b42));

        CounterpartPO p1 = (CounterpartPO)ExternalizableHelper.fromBinary(b1, pofContext);
        CounterpartPO p12 = (CounterpartPO)ExternalizableHelper.fromBinary(b12, pofContext);

        CounterpartPO p2 = (CounterpartPO)ExternalizableHelper.fromBinary(b2, pofContext);
        CounterpartPO p22 = (CounterpartPO)ExternalizableHelper.fromBinary(b22, pofContext);

        CounterpartPO p3 = (CounterpartPO)ExternalizableHelper.fromBinary(b3, pofContext);
        CounterpartPO p32 = (CounterpartPO)ExternalizableHelper.fromBinary(b32, pofContext);

        CounterpartPO p4 = (CounterpartPO)ExternalizableHelper.fromBinary(b4, pofContext);
        CounterpartPO p42 = (CounterpartPO)ExternalizableHelper.fromBinary(b42, pofContext);

        System.exit(0);

        final Map m1 = test1.getAll(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L));
        System.out.println("Result map: " + m1);

        System.exit(0);

//        CounterpartPO c = new CounterpartPO();
//        c.setId(1L);
//        c.setName("New counterpart #1");
//        test1.put(123L, c);

        /////////////////
//        final Counterpart cp2 = new Counterpart();
//
//        cp2.setId(1L);
//        cp2.setName("New user #2");
//
//        final Counterpart oldcp = CacheAccess.COUNTERPARTS.put(cp2.getId(), cp2);
//        System.out.println("Old cp: " + oldcp);
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

    private static Binary toBinary(CounterpartPO cp, PofContext pofContext) throws IOException {
        final BinaryWriteBuffer b = new BinaryWriteBuffer(1);
        final WriteBuffer.BufferOutput bo = b.getBufferOutput();

        //bo.writeByte(21);
        //pofContext.serialize(bo, cp);

        //final PofWriter writer = new PofBufferWriter(bo, pofContext);//, 1002, -1);

        //bo.writeByte(21);
        //writer.writeObject(-1, cp);

        final PofBufferWriter.UserTypeWriter writer = new PofBufferWriter.UserTypeWriter(bo, pofContext, 1002, -1);

        bo.writeByte(21);
        //writer.writeObject(0, cp.getId());
        //writer.writeString(1, cp.getName());

        writer.writeObject(0, cp.getId() != null ? cp.getId() : 0L);
        if (cp.getName() != null) {
            writer.writeObject(1, cp.getName());
        }

//        if (cp.getId() == null) {
//            writer.writeObject(0, cp.getId());
//        } else {
//            writer.writeLong(0, cp.getId());
//        }
//        writer.writeString(1, cp.getName());

        writer.writeRemainder(null);

        return b.toBinary();
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
