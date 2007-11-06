/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.samples.airline.domain.hibernate;

import java.io.Serializable;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.LiteralType;
import org.hibernate.usertype.UserType;
import org.joda.time.LocalDate;

public class LocalDateUserType implements UserType, LiteralType {

    public int[] sqlTypes() {
        return new int[]{Types.DATE};
    }

    public Class returnedClass() {
        return LocalDate.class;
    }

    public boolean isMutable() {
        return false;
    }

    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) value;
    }

    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }

    public boolean equals(Object x, Object y) throws HibernateException {
        return x == y || !(x == null || y == null) && x.equals(y);
    }

    public int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
        Date sqlDate = rs.getDate(names[0]);
        if (rs.wasNull()) {
            return null;
        }
        return new LocalDate(sqlDate.getTime());
    }

    public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, Types.DATE);
        }
        else {
            LocalDate localDate = (LocalDate) value;
            Date sqlDate = new Date(localDate.toDateMidnight().getMillis());
            st.setDate(index, sqlDate);
        }
    }

    public String objectToSQLString(Object value, Dialect dialect) throws Exception {
        LocalDate localDate = (LocalDate) value;
        return localDate.toString();
    }
}
