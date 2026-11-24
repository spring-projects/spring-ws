/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.server.endpoint.mapping;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.EndpointInvocationChain;
import org.springframework.ws.server.endpoint.mapping.AbstractAnnotationMethodEndpointMapping;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.server.SoapEndpointInvocationChain;
import org.springframework.ws.soap.server.SoapEndpointMapping;
import org.springframework.ws.soap.server.endpoint.annotation.SoapAction;
import org.springframework.ws.soap.server.endpoint.annotation.SoapActions;
import org.springframework.ws.soap.support.SoapUtils;

/**
 * Implementation of the {@link org.springframework.ws.server.EndpointMapping} interface
 * that uses the {@link SoapAction} annotation to map methods to the request SOAPAction
 * header.
 * <p>
 * Endpoints typically have the following form:
 *
 * <pre><code class='java'>
 * &#64;Endpoint
 * public class MyEndpoint{
 *	  &#64;SoapAction("http://springframework.org/spring-ws/SoapAction")
 *	  public Source doSomethingWithRequest() {
 *		 ...
 *	  }
 * }</code></pre>
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public class SoapActionAnnotationMethodEndpointMapping extends AbstractAnnotationMethodEndpointMapping<String>
		implements SoapEndpointMapping {

	private String @Nullable [] actorsOrRoles;

	private boolean isUltimateReceiver = true;

	@Override
	public final void setActorOrRole(String actorOrRole) {
		Assert.notNull(actorOrRole, "actorOrRole must not be null");
		this.actorsOrRoles = new String[] { actorOrRole };
	}

	@Override
	public final void setActorsOrRoles(String[] actorsOrRoles) {
		Assert.notEmpty(actorsOrRoles, "actorsOrRoles must not be empty");
		this.actorsOrRoles = actorsOrRoles;
	}

	@Override
	public final void setUltimateReceiver(boolean ultimateReceiver) {
		this.isUltimateReceiver = ultimateReceiver;
	}

	/**
	 * Creates a new {@code SoapEndpointInvocationChain} based on the given endpoint, and
	 * the set interceptors, and actors/roles.
	 * @param endpoint the endpoint
	 * @param interceptors the endpoint interceptors
	 * @return the created invocation chain
	 * @see #setInterceptors(org.springframework.ws.server.EndpointInterceptor[])
	 * @see #setActorsOrRoles(String[])
	 */
	@Override
	protected final EndpointInvocationChain createEndpointInvocationChain(MessageContext messageContext,
			Object endpoint, EndpointInterceptor[] interceptors) {
		return new SoapEndpointInvocationChain(endpoint, interceptors, this.actorsOrRoles, this.isUltimateReceiver);
	}

	@Override
	protected @Nullable String getLookupKeyForMessage(MessageContext messageContext) throws Exception {
		if (messageContext.getRequest() instanceof SoapMessage request) {
			return SoapUtils.unescapeAction(request.getSoapAction());
		}
		else {
			return null;
		}
	}

	@Override
	protected @Nullable String getLookupKeyForMethod(Method method) {
		SoapAction soapAction = AnnotationUtils.findAnnotation(method, SoapAction.class);
		return (soapAction != null) ? soapAction.value() : null;
	}

	@Override
	protected List<String> getLookupKeysForMethod(Method method) {
		List<String> result = new ArrayList<>();

		SoapActions soapActions = AnnotationUtils.findAnnotation(method, SoapActions.class);
		if (soapActions != null) {
			for (SoapAction soapAction : soapActions.value()) {
				result.add(soapAction.value());
			}
		}
		else {
			SoapAction soapAction = AnnotationUtils.findAnnotation(method, SoapAction.class);
			if (soapAction != null) {
				result.add(soapAction.value());
			}
		}
		return result;
	}

}
