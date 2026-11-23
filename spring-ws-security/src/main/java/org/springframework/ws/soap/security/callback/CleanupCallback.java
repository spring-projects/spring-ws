/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.security.callback;

import java.io.Serial;
import java.io.Serializable;

import javax.security.auth.callback.Callback;

/**
 * Underlying security services instantiate and pass a {@code CleanupCallback} to the
 * {@code handle} method of a {@code CallbackHandler} to clean up security state.
 *
 * @author Arjen Poutsma
 * @since 1.0.4
 */
public class CleanupCallback implements Callback, Serializable {

	@Serial
	private static final long serialVersionUID = 4744181820980888237L;

}
