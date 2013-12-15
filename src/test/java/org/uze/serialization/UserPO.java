package org.uze.serialization;

import com.tangosol.io.pof.annotation.Portable;
import com.tangosol.io.pof.annotation.PortableProperty;

import java.util.Date;

/**
 * Created by Uze on 12.12.13.
 */
@Portable
public class UserPO implements User {

    @PortableProperty(1)
    private long id;
    @PortableProperty(2)
    private String firstName;
    @PortableProperty(3)
    private String lastName;
    @PortableProperty(4)
    private String middleName;
    @PortableProperty(5)
    private String login;
    @PortableProperty(6)
    private String addressLine1;
    @PortableProperty(7)
    private String addressLine2;
    @PortableProperty(8)
    private String postIndex;
    @PortableProperty(9)
    private String phone1;
    @PortableProperty(10)
    private Date created;
    @PortableProperty(11)
    private Date updated;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getPostIndex() {
        return postIndex;
    }

    public void setPostIndex(String postIndex) {
        this.postIndex = postIndex;
    }

    public String getPhone1() {
        return phone1;
    }

    public void setPhone1(String phone1) {
        this.phone1 = phone1;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public UserPO() {
    }
}
