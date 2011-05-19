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

package org.springframework.ws.samples.airline.domain.openjpa;

import java.sql.Timestamp;

import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.JavaSQLTypes;
import org.apache.openjpa.jdbc.meta.ValueMapping;
import org.apache.openjpa.jdbc.meta.strats.AbstractValueHandler;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ColumnIO;
import org.joda.time.DateTime;

/**
 * @author Arjen Poutsma
 * @since 1.1.0
 */
public class DateTimeValueHandler extends AbstractValueHandler {

    private static final DateTimeValueHandler instance = new DateTimeValueHandler();

    public static DateTimeValueHandler getInstance() {
        return instance;
    }

    public Column[] map(ValueMapping vm, String name, ColumnIO io, boolean adapt) {
        Column col = new Column();
        col.setName(name);
        col.setJavaType(JavaSQLTypes.TIMESTAMP);
        return new Column[]{col};
    }

    public Object toDataStoreValue(ValueMapping valueMapping, Object val, JDBCStore jdbcStore) {
        return val == null ? null : new Timestamp(((DateTime) val).getMillis());
    }

    public Object toObjectValue(ValueMapping valueMapping, Object val) {
        return val == null ? null : new DateTime(val);
    }
}
