package org.uze.serialization;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import java.io.IOException;
import java.util.Date;

/**
 * Created by Uze on 12.12.13.
 */
public class UserPO2 implements PortableObject, User {

    public static final int POF_ID = 1;
    public static final int POF_FIRST_NAME = 2;
    public static final int POF_LAST_NAME = 3;
    public static final int POF_MIDDLE_NAME = 4;
    public static final int POF_LOGIN = 5;
    public static final int POF_ADDRESS_LINE1 = 6;
    public static final int POF_ADDRESS_LINE2 = 7;
    public static final int POF_POST_INDEX = 8;
    public static final int POF_PHONE1 = 9;
    public static final int POF_CREATED = 10;
    public static final int POF_UPDATED = 11;
    private long id;
    private String firstName;
    private String lastName;
    private String middleName;
    private String login;
    private String addressLine1;
    private String addressLine2;
    private String postIndex;
    private String phone1;
    private Date created;
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

    public UserPO2() {
    }

    @Override
    public void readExternal(PofReader pofReader) throws IOException {
        id = pofReader.readLong(POF_ID);
        firstName = pofReader.readString(POF_FIRST_NAME);
        lastName = pofReader.readString(POF_LAST_NAME);
        middleName = pofReader.readString(POF_MIDDLE_NAME);
        login = pofReader.readString(POF_LOGIN);
        addressLine1 = pofReader.readString(POF_ADDRESS_LINE1);
        addressLine2 = pofReader.readString(POF_ADDRESS_LINE2);
        postIndex = pofReader.readString(POF_POST_INDEX);
        phone1 = pofReader.readString(POF_PHONE1);
        created = pofReader.readDate(POF_CREATED);
        updated = pofReader.readDate(POF_UPDATED);
    }

    @Override
    public void writeExternal(PofWriter pofWriter) throws IOException {
        pofWriter.writeLong(POF_ID, id);
        pofWriter.writeString(POF_FIRST_NAME, firstName);
        pofWriter.writeString(POF_LAST_NAME, lastName);
        pofWriter.writeString(POF_MIDDLE_NAME, middleName);
        pofWriter.writeString(POF_LOGIN, login);
        pofWriter.writeString(POF_ADDRESS_LINE1, addressLine1);
        pofWriter.writeString(POF_ADDRESS_LINE2, addressLine2);
        pofWriter.writeString(POF_POST_INDEX, postIndex);
        pofWriter.writeString(POF_PHONE1, phone1);
        pofWriter.writeDate(POF_CREATED, created);
        pofWriter.writeDate(POF_UPDATED, updated);
    }
}
