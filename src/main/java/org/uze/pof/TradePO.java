package org.uze.pof;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import java.io.IOException;
import java.util.Date;

/**
 * Created by Юрий on 10.12.13.
 */
public class TradePO implements PortableObject {

    public static final int POF_ID = 0;
    public static final int POF_NAME = 1;
    public static final int POF_DATE = 2;
    public static final int POF_CPART_ID = 3;

    private Long id;
    private String name;
    private Date date;
    private Long counterpartId;

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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getCounterpartId() {
        return counterpartId;
    }

    public void setCounterpartId(Long counterpartId) {
        this.counterpartId = counterpartId;
    }

    @Override
    public void readExternal(PofReader pofReader) throws IOException {
        System.out.println(getClass().getName()+".readExternal()");
        id = pofReader.readLong(POF_ID);
        name = pofReader.readString(POF_NAME);
        date = pofReader.readDate(POF_DATE);
        counterpartId = pofReader.readLong(POF_CPART_ID);
    }

    @Override
    public void writeExternal(PofWriter pofWriter) throws IOException {
        System.out.println(getClass().getName()+".writeExternal()");
        pofWriter.writeLong(POF_ID, id != null ? id : 0L);
        pofWriter.writeString(POF_NAME, name);
        pofWriter.writeDate(POF_DATE, date);
        pofWriter.writeLong(POF_CPART_ID, counterpartId);
    }

    @Override
    public String toString() {
        return "TradePO{id=" + id + ", name='" + name + "', date=" + date + ", counterpartId=" + counterpartId + '}';
    }
}
