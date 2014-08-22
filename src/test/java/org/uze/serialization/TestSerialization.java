package org.uze.serialization;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Uze on 12.12.13.
 */
@Ignore
public class TestSerialization {

    private static final int COUNT = 50000;
    private static final String ASCII = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String POF_1 = "POF-1";
    public static final String POF_2 = "POF-2";
    public static final String JSRL = "JSRL";

    @BeforeClass
    public static void setUpClass() throws Exception {
        CacheFactory.getCache(POF_1);
        CacheFactory.getCache(POF_2);
        CacheFactory.getCache(JSRL);
    }

    public boolean sameObjectInstanceReturned() throws Exception {
        NamedCache cache = CacheFactory.getCache(POF_2);

        final long id = -5L;

        User user = new UserPO2();

        user.setId(id);
        user.setFirstName("John");
        user.setLastName("Smith");

        cache.put(user.getId(), user);

        User user1 = (User) cache.get(id);

        User user2 = (User) cache.get(id);

        boolean result = (user1 == user2);
        cache.remove(id);

        return result;
    }

    @Test
    public void testAll() throws Exception {
//        final int length = 256 * 1024;
//        StringBuilder sb = new StringBuilder(length);
//
//        long t0 = System.nanoTime();
//        for (int i = 0; i < length; i++) {
//            sb.append(getNext());
//        }
//        long t1 = System.nanoTime();
//
//        System.out.println(sb.toString());
//        System.out.println("Time: " + (t1 - t0) / 1000000 + " ms");

        boolean liveFlag = sameObjectInstanceReturned();
        if (!liveFlag) {
            System.out.println("All objects are deserialized on cache.get()");
        } else {
            System.out.println("Warning: In memory cache with live objects");
        }

        for (int i = 1; i < 5; i++) {
            System.out.println("====================== Iteration #" + i + " ===================");
            testPOFAttributes();
            testPOF();
            testJSRL();
        }
    }

    public void testPOFAttributes() throws Exception {
        // init users
        final Map<Long, User> users = new HashMap<Long, User>(COUNT);
        for (long i = 0; i < COUNT; i++) {
            User user = new UserPO();

            populate(user);
            user.setId(i);

            users.put(i, user);
        }

        doTest(users, POF_1, "Test: POF with Annotations");
    }

    public void testPOF() throws Exception {
        // init users
        final Map<Long, User> users = new HashMap<Long, User>(COUNT);
        for (long i = 0; i < COUNT; i++) {
            User user = new UserPO2();

            populate(user);
            user.setId(i);

            users.put(i, user);
        }

        doTest(users, POF_2, "Test: POF with hand-written methods");
    }

    public void testJSRL() throws Exception {
        // init users
        final Map<Long, User> users = new HashMap<Long, User>(COUNT);
        for (long i = 0; i < COUNT; i++) {
            User user = new UserJSRL();

            populate(user);
            user.setId(i);

            users.put(i, user);
        }

        doTest(users, JSRL, "Test: Java Serializable objects");
    }

    public void doTest(Map<Long, User> users, String cacheName, String title) throws Exception {
        final NamedCache cache = CacheFactory.getCache(cacheName);
        try {
            long t0 = System.nanoTime();

            // fill cache
            cache.putAll(users);

            long t1 = System.nanoTime();

            long updateTime = 0;

            // update cache
            for (Map.Entry<Long, User> entry : users.entrySet()) {
                User user = entry.getValue();

                populate(user);

                long ut0 = System.nanoTime();
                cache.put(entry.getKey(), user);
                long ut1 = System.nanoTime();

                updateTime += (ut1 - ut0);
            }

            long t2 = System.nanoTime();

            // read cache
            for (Map.Entry<Long, User> entry : users.entrySet()) {
                User user = (User) cache.get(entry.getKey());
            }

            long t3 = System.nanoTime();

            System.out.println(title + " (" + COUNT + " iterations)");
            System.out.println("Total time (ms): " + (t3 - t0) / 1000000);
            System.out.println("putAll time (ms): " + (t1 - t0) / 1000000);
            System.out.println("put time (ms): " + updateTime / 1000000);
            System.out.println("get time (ms): " + (t3 - t2) / 1000000);
            System.out.println("--------------------------");
        } finally {
            cache.release();
        }
    }

    private char getNext() {
        int idx = (int) (Math.random() * ASCII.length());
        return ASCII.charAt(idx);
    }

    private String genString(int length) {
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            sb.append(getNext());
        }

        return sb.toString();
    }

    private String genNumString(int length) {
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            sb.append(((int) getNext()) % 10);
        }

        return sb.toString();
    }

    private void populate(User user) {
        user.setFirstName(genString(9));
        user.setLastName(genString(12));
        user.setMiddleName(genString(8));

        user.setLogin(genString(8));
        user.setPostIndex(genNumString(6));
        user.setAddressLine1(genString(24));
        user.setAddressLine2(genString(18));

        user.setPhone1(genNumString(11));

        if (user.getCreated() == null) {
            user.setCreated(new Date());
        }
        user.setUpdated(new Date());
    }
}
