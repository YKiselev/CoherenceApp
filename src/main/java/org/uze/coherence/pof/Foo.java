package org.uze.coherence.pof;

import com.tangosol.io.pof.annotation.Portable;
import com.tangosol.io.pof.annotation.PortableProperty;

/**
 * Created by Uze on 30.08.2014.
 */
@Portable
public class Foo {

    @PortableProperty(0)
    private Long id;
    @PortableProperty(1)
    private long id2;
    @PortableProperty(2)
    private String name;
    @PortableProperty(3)
    private boolean flag1;
    @PortableProperty(4)
    private Boolean flag2;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getId2() {
        return id2;
    }

    public void setId2(long id2) {
        this.id2 = id2;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFlag1() {
        return flag1;
    }

    public void setFlag1(boolean flag1) {
        this.flag1 = flag1;
    }

    public Boolean getFlag2() {
        return flag2;
    }

    public void setFlag2(Boolean flag2) {
        this.flag2 = flag2;
    }

    public Foo() {
    }

    public Foo(Long id, long id2, String name, boolean flag1, Boolean flag2) {
        this.id = id;
        this.id2 = id2;
        this.name = name;
        this.flag1 = flag1;
        this.flag2 = flag2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Foo)) return false;

        Foo foo = (Foo) o;

        if (flag1 != foo.flag1) return false;
        if (id2 != foo.id2) return false;
        if (flag2 != null ? !flag2.equals(foo.flag2) : foo.flag2 != null) return false;
        if (id != null ? !id.equals(foo.id) : foo.id != null) return false;
        if (name != null ? !name.equals(foo.name) : foo.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (int) (id2 ^ (id2 >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (flag1 ? 1 : 0);
        result = 31 * result + (flag2 != null ? flag2.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Foo{" +
            "id=" + id +
            ", id2=" + id2 +
            ", name='" + name + '\'' +
            ", flag1=" + flag1 +
            ", flag2=" + flag2 +
            '}';
    }
}
