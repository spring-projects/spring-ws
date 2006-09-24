/*
 * Copyright 2005 the original author or authors.
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
package org.springframework.ws.samples.airline.dao.hibernate;

import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;
import org.springframework.ws.samples.airline.domain.Customer;

public class HibernateCustomerDaoTest extends AbstractTransactionalDataSourceSpringContextTests {

    private HibernateCustomerDao dao;

    public void setDao(HibernateCustomerDao dao) {
        this.dao = dao;
    }

    public void testInsertAndGet() {
        Customer customer = new Customer();
        customer.setFirstName("firstName");
        customer.setLastName("lastName");
        dao.insertCustomer(customer);
        Long id = customer.getId();
        Customer other = dao.getCustomer(id.longValue());
        assertNotNull("Invalid customer", other);
        assertEquals("Invalid name", "firstName", other.getFirstName());
        assertEquals("Invalid name", "lastName", other.getLastName());
    }

    protected String[] getConfigLocations() {
        return new String[]{
                "classpath:org/springframework/ws/samples/airline/dao/hibernate/applicationContext-hibernate.xml"};
    }

}
