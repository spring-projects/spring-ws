/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.webflow.execution.repository.support;

import org.springframework.util.Assert;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.execution.repository.FlowExecutionKey;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.util.RandomGuidUidGenerator;
import org.springframework.webflow.util.UidGenerator;

/**
 * Convenient implementation that encapsulates the assembly of a <i>default</i>
 * flow execution repository factory and delegates to it at runtime.
 * <ul>
 * Specifically, <i>default</i> means this delegating repository factory:
 * <ul>
 * <li>Sets a {@link SharedMapFlowExecutionRepositoryFactory} to manage flow
 * execution repository implementations statefully in the
 * {@link ExternalContext#getSessionMap()}, typically backed by the HTTP
 * session.
 * <li>Configures it with a simple repository creator to create instances of
 * {@link DefaultFlowExecutionRepository} when requested for placement in the
 * shared session map.
 * </ul>
 * This class inherits from {@link FlowExecutionRepositoryServices} to allow for
 * direct configuration of services needed by the repositories created by this
 * factory.
 * 
 * @author Keith Donald
 */
public class DefaultFlowExecutionRepositoryFactory extends DelegatingFlowExecutionRepositoryFactory {

	/**
	 * Creates a new simple flow execution repository factory.
	 * @param flowLocator the locator for loading flow definitions for which
	 * flow executions are created from
	 */
	public DefaultFlowExecutionRepositoryFactory(FlowLocator flowLocator) {
		super(flowLocator);
		setRepositoryFactory(new SharedMapFlowExecutionRepositoryFactory(
				new DefaultFlowExecutionRepositoryCreator(this)));
	}

	/**
	 * Sets the uid generation strategy used to generate unique continuation
	 * identifiers for {@link FlowExecutionKey flow execution keys}.
	 */
	public void setContinuationIdGenerator(UidGenerator continuationIdGenerator) {
		getRepositoryCreator().setContinuationIdGenerator(continuationIdGenerator);
	}

	/**
	 * Sets a flag indicating if a new {@link FlowExecutionKey} should always be
	 * generated before each put call. By setting this to false a FlowExecution
	 * can remain identified by the same key throughout its life.
	 * @param alwaysGenerateNewNextKey the generate flag
	 */
	public void setAlwaysGenerateNewNextKey(boolean alwaysGenerateNewNextKey) {
		getRepositoryCreator().setAlwaysGenerateNewNextKey(alwaysGenerateNewNextKey);
	}

	/**
	 * Helper that returns the configured repository creator used by this
	 * factory.
	 */
	protected DefaultFlowExecutionRepositoryCreator getRepositoryCreator() {
		SharedMapFlowExecutionRepositoryFactory factory = (SharedMapFlowExecutionRepositoryFactory)getRepositoryFactory();
		return (DefaultFlowExecutionRepositoryCreator)factory.getRepositoryCreator();
	}

	/**
	 * A creational strategy returning {@link SimpleFlowExecutionRepository}
	 * instances.
	 * 
	 * @author Keith Donald
	 */
	private static class DefaultFlowExecutionRepositoryCreator extends AbstractFlowExecutionRepositoryCreator {

		/**
		 * The continuation uid generation strategy to use.
		 */
		private transient UidGenerator continuationIdGenerator = new RandomGuidUidGenerator();

		/**
		 * Flag to indicate whether or not a new flow execution key should
		 * always be generated before each put call. Default is true.
		 */
		private boolean alwaysGenerateNewNextKey = true;

		public DefaultFlowExecutionRepositoryCreator(FlowExecutionRepositoryServices repositoryServices) {
			super(repositoryServices);
		}

		/**
		 * Sets the continuation uid strategto use generate unique continuation
		 * identifiers for {@link FlowExecutionKey flow execution keys}.
		 */
		public void setContinuationIdGenerator(UidGenerator continuationIdGenerator) {
			Assert.notNull(continuationIdGenerator, "The continuation id generator is required");
			this.continuationIdGenerator = continuationIdGenerator;
		}

		/**
		 * Sets a flag indicating if a new {@link FlowExecutionKey} should
		 * always be generated before each put call. By setting this to false a
		 * FlowExecution can remain identified by the same key throughout its
		 * life.
		 * @param alwaysGenerateNewNextKey the generate flag
		 */
		public void setAlwaysGenerateNewNextKey(boolean alwaysGenerateNewNextKey) {
			this.alwaysGenerateNewNextKey = alwaysGenerateNewNextKey;
		}

		public FlowExecutionRepository createRepository() {
			DefaultFlowExecutionRepository repository = new DefaultFlowExecutionRepository(getRepositoryServices());
			repository.setContinuationIdGenerator(continuationIdGenerator);
			repository.setAlwaysGenerateNewNextKey(alwaysGenerateNewNextKey);
			return repository;
		}

		public FlowExecutionRepository rehydrateRepository(FlowExecutionRepository repository) {
			DefaultFlowExecutionRepository defaultRepository = (DefaultFlowExecutionRepository)repository;
			defaultRepository.setRepositoryServices(getRepositoryServices());
			defaultRepository.setContinuationIdGenerator(continuationIdGenerator);
			return repository;
		}
	}
}