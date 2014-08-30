package org.uze.pof;

import com.tangosol.io.ReadBuffer;
import com.tangosol.io.WriteBuffer;
import com.tangosol.io.pof.PofContext;
import com.tangosol.io.pof.PofSerializer;

import java.io.IOException;

/**
 * Created by Uze on 28.08.2014.
 */
public class MyPofContext implements PofContext {

    private final PofContext ctx;

    public MyPofContext(PofContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public PofSerializer getPofSerializer(int i) {
        return ctx.getPofSerializer(i);
    }

    @Override
    public int getUserTypeIdentifier(Object o) {
        return ctx.getUserTypeIdentifier(o);
    }

    @Override
    public int getUserTypeIdentifier(Class aClass) {
        return ctx.getUserTypeIdentifier(aClass);
    }

    @Override
    public int getUserTypeIdentifier(String s) {
        return ctx.getUserTypeIdentifier(s);
    }

    @Override
    public String getClassName(int i) {
        return ctx.getClassName(i);
    }

    @Override
    public Class getClass(int i) {
        return ctx.getClass(i);
    }

    @Override
    public boolean isUserType(Object o) {
        return ctx.isUserType(o);
    }

    @Override
    public boolean isUserType(Class aClass) {
        return ctx.isUserType(aClass);
    }

    @Override
    public boolean isUserType(String s) {
        return ctx.isUserType(s);
    }

    @Override
    public void serialize(WriteBuffer.BufferOutput bufferOutput, Object o) throws IOException {
        ctx.serialize(bufferOutput, o);
    }

    @Override
    public Object deserialize(ReadBuffer.BufferInput bufferInput) throws IOException {
        return ctx.deserialize(bufferInput);
    }
}
