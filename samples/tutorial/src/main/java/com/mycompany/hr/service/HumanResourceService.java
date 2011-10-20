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

package com.mycompany.hr.service;

import java.util.Date;

/**
 * This interface defined the contract for a HR business service. It is used by the {@link
 * com.mycompany.hr.ws.HolidayEndpoint}.
 *
 * @author Arjen Poutsma
 */
public interface HumanResourceService {

    /**
     * Books a holiday.
     *
     * @param startDate the start date of the holiday
     * @param endDate   the end date of the holiday
     * @param name      the name of the person taking the holiday
     */
    void bookHoliday(Date startDate, Date endDate, String name);

}
