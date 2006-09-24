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
package org.springframework.webflow;

import org.springframework.core.style.StylerUtils;

/**
 * Thrown when no transition can be matched given the occurence of an event in
 * the context of a flow execution request.
 * <p>
 * Typically this happens because there is no "handler" transition for the last
 * event that occured.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class NoMatchingTransitionException extends StateException {

	/**
	 * The event that occured that could not be matched to a Transition.
	 */
	private Event event;

	/**
	 * Create a new no matching transition exception.
	 * @param state the state that could not be transitioned out of
	 * @param event the event that occured that could not be matched to a
	 * transition
	 */
	public NoMatchingTransitionException(TransitionableState state, Event event) {
		this(state, event, (Throwable)null);
	}

	/**
	 * Create a new no matching transition exception.
	 * @param state the state that could not be transitioned out of
	 * @param event the event that occured that could not be matched to a
	 * transition
	 * @param cause the underlying cause
	 */
	public NoMatchingTransitionException(TransitionableState state, Event event, Throwable cause) {
		super(state, "No transition found on occurence of event '" + event.getId() + "' in state '" + state.getId()
				+ "' of flow '" + state.getFlow().getId() + "' -- valid transitional criteria are "
				+ StylerUtils.style(state.getTransitionSet().getTransitionCriterias())
				+ " -- likely programmer error, check the set of TransitionCriteria for this state", cause);
		this.event = event;
	}

	/**
	 * Create a new no matching transition exception.
	 * @param state the state that could not be transitioned out of
	 * @param event the event that occured that could not be matched to a
	 * transition
	 * @param message the message
	 */
	public NoMatchingTransitionException(TransitionableState state, Event event, String message) {
		this(state, event, message, null);
	}

	/**
	 * Create a new no matching transition exception.
	 * @param state the state that could not be transitioned out of
	 * @param event the event that occured that could not be matched to a
	 * transition
	 * @param message the message
	 * @param cause the underlying cause
	 */
	public NoMatchingTransitionException(TransitionableState state, Event event, String message, Throwable cause) {
		super(state, message, cause);
		this.event = event;
	}

	/**
	 * Returns the state that could not execute a transition on the occurence of
	 * the event in the context of the current request.
	 */
	public TransitionableState getTransitionableState() {
		return (TransitionableState)getState();
	}

	/**
	 * Returns the event for the current request that did not trigger any
	 * supported transition out of the set state.
	 */
	public Event getEvent() {
		return event;
	}
}