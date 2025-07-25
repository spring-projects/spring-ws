/*
 * Copyright 2005-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.xml.sax;

import org.junit.jupiter.api.Test;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;

class SaxUtilsTests {

	@Test
	void testGetSystemId() {

		Resource resource = new FileSystemResource("/path with spaces/file with spaces.txt");
		String systemId = SaxUtils.getSystemId(resource);

		assertThat(systemId).isNotNull();
		assertThat(systemId).endsWith("path%20with%20spaces/file%20with%20spaces.txt");
	}

}
