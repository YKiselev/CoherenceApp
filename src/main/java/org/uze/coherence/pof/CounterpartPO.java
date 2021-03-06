package org.uze.coherence.pof;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import java.io.IOException;

/**
 * Created by Юрий on 10.12.13.
 */
public class CounterpartPO implements PortableObject {

    public static final int POF_ID = 0;
    public static final int POF_NAME = 1;
    private Long id;
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void readExternal(PofReader pofReader) throws IOException {
        System.out.println(getClass().getName()+".readExternal()");
        id = pofReader.readLong(POF_ID);
        name = pofReader.readString(POF_NAME);
    }

    @Override
    public void writeExternal(PofWriter pofWriter) throws IOException {
        System.out.println(getClass().getName()+".writeExternal()");
        pofWriter.writeLong(POF_ID, id != null ? id : 0L);
        pofWriter.writeString(POF_NAME, name);
    }

    @Override
    public String toString() {
        return "CounterpartPO{id=" + id + ", name='" + name + "'}";
    }
}
