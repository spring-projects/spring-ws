/*
 * Copyright 2006 the original author or authors.
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;
import org.springframework.ws.samples.airline.domain.ServiceClass;

public class ServiceClassUserType implements UserType {

    private static final int[] SQL_TYPES = {Types.VARCHAR};

    public boolean isMutable() {
        return false;
    }

    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    public Class returnedClass() {
        return ServiceClass.class;
    }

    public boolean equals(Object x, Object y) {
        return x == y;
    }

    public int hashCode(Object object) throws HibernateException {
        return object.hashCode();
    }

    public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner)
            throws HibernateException, SQLException {
        String name = resultSet.getString(names[0]);
        return resultSet.wasNull() ? null : ServiceClass.getInstance(name);
    }

    public void nullSafeSet(PreparedStatement statement, Object value, int index)
            throws HibernateException, SQLException {
        if (value == null) {
            statement.setNull(index, Types.VARCHAR);
        }
        else {
            statement.setString(index, value.toString());
        }
    }

    public Object deepCopy(Object value) {
        return value;
    }

    public Serializable disassemble(Object object) throws HibernateException {
        return (Serializable) object;
    }

    public Object assemble(Serializable serializable, Object object) throws HibernateException {
        return serializable;
    }

    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }
}