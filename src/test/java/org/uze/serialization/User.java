package org.uze.serialization;

import java.util.Date;

/**
 * Created by Uze on 12.12.13.
 */
public interface User {

    long getId();

    void setId(long id);

    String getFirstName();

    void setFirstName(String firstName);

    String getLastName();

    void setLastName(String lastName);

    String getMiddleName();

    void setMiddleName(String middleName);

    String getLogin();

    void setLogin(String login);

    String getAddressLine1();

    void setAddressLine1(String addressLine1);

    String getAddressLine2();

    void setAddressLine2(String addressLine2);

    String getPostIndex();

    void setPostIndex(String postIndex);

    String getPhone1();

    void setPhone1(String phone1);

    Date getCreated();

    void setCreated(Date created);

    Date getUpdated();

    void setUpdated(Date updated);
}
