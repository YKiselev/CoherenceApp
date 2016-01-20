package org.uze.serialization;

import com.google.common.collect.ImmutableList;
import com.tangosol.io.Serializer;
import com.tangosol.io.WriteBuffer;
import com.tangosol.io.pof.PofBufferWriter;
import com.tangosol.io.pof.PofContext;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Binary;
import com.tangosol.util.BinaryWriteBuffer;
import com.tangosol.util.ExternalizableHelper;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.BeanFactory;
import org.uze.coherence.pof.Foo;
import org.uze.spring.SpringContextHolder;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Uze on 30.08.2014.
 */
@RunWith(Parameterized.class)
public class BinaryFromPortableTest {

    private static PofContext pofContext;

    private final Foo foo;

    @BeforeClass
    public static void setUpClass() throws Exception {
        Locale.setDefault(Locale.ENGLISH);

        CacheFactory.getCacheFactoryBuilder()
            .getConfigurableCacheFactory(BinaryFromPortableTest.class.getClassLoader())
            .getResourceRegistry()
            .registerResource(BeanFactory.class, SpringContextHolder.getApplicationContext());

        final NamedCache test1 = CacheFactory.getCache("Foos");
        final Serializer serializer = test1.getCacheService().getSerializer();
        pofContext = (PofContext) serializer;
    }

    @Parameterized.Parameters
    public static List<Object[]> buildParameters() {
        return new ImmutableList.Builder<Object[]>()
            .add(new Object[]{new Foo(null, 0L, null, false, null)})
            .add(new Object[]{new Foo(1L, 0L, null, false, null)})
            .add(new Object[]{new Foo(1L, 2L, null, false, null)})
            .add(new Object[]{new Foo(1L, 2L, "Name", false, null)})
            .add(new Object[]{new Foo(1L, 2L, "Name", true, null)})
            .add(new Object[]{new Foo(1L, 2L, "Name", true, Boolean.TRUE)})
            .build();
    }

    public BinaryFromPortableTest(Foo foo) {
        this.foo = foo;
    }

    @Test
    public void test() throws Exception {
        final Binary a = ExternalizableHelper.toBinary(foo, pofContext);
        final Binary b = toBinary(foo, pofContext);

        Assert.assertEquals(a, b);

        final Foo af = (Foo)ExternalizableHelper.fromBinary(a, pofContext);
        final Foo bf = (Foo)ExternalizableHelper.fromBinary(b, pofContext);

        Assert.assertEquals(af, bf);
    }

    private static Binary toBinary(Foo foo, PofContext pofContext) throws IOException {
        final BinaryWriteBuffer b = new BinaryWriteBuffer(1);
        final WriteBuffer.BufferOutput bo = b.getBufferOutput();
        final PofBufferWriter.UserTypeWriter writer = new PofBufferWriter.UserTypeWriter(bo, pofContext, 1003, -1);

        bo.writeByte(21);

        writer.writeObject(0, foo.getId());
        writer.writeObject(1, foo.getId2());
        writer.writeObject(2, foo.getName());
        writer.writeObject(3, foo.isFlag1());
        writer.writeObject(4, foo.getFlag2());

        writer.writeRemainder(null);

        return b.toBinary();
    }

}
