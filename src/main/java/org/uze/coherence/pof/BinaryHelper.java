package org.uze.coherence.pof;

import com.tangosol.io.WriteBuffer;
import com.tangosol.io.pof.PofBufferWriter;
import com.tangosol.io.pof.PofContext;
import com.tangosol.util.Binary;
import com.tangosol.util.BinaryWriteBuffer;

import java.io.IOException;

/**
 * Created by Uze on 30.08.2014.
 */
public class BinaryHelper {

    public static final int DEF_CAPACITY = 64;
    public static final int FMT_USER_TYPE = 21;

    public static Binary toBinary(ValueExtractor extractor, PofContext pofContext) throws IOException {
        final BinaryWriteBuffer b = new BinaryWriteBuffer(DEF_CAPACITY);
        final WriteBuffer.BufferOutput bo = b.getBufferOutput();
        final PofBufferWriter.UserTypeWriter writer = new PofBufferWriter.UserTypeWriter(bo, pofContext, extractor.getUserTypeId(), -1);

        bo.writeByte(FMT_USER_TYPE);

        int i = 0;
        for (Object value : extractor.getValues()) {
            writer.writeObject(i, value);
            i++;
        }

        writer.writeRemainder(null);

        return b.toBinary();
    }

    public static interface ValueExtractor {

        int getUserTypeId();

        Iterable<Object> getValues();
    }
}
