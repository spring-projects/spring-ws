/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.support;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Random;

import org.springframework.util.Assert;

/**
 * Utility class that finds free BSD ports for use in testing scenario's.
 *
 * @author Ben Hale
 * @author Arjen Poutsma
 */
public abstract class FreePortScanner {

	private static final int MIN_SAFE_PORT = 1024;

	private static final int MAX_PORT = 65535;

	private static final Random random = new Random();

	/**
	 * Returns the number of a free port in the default range.
	 */
	public static int getFreePort() {
		return getFreePort(MIN_SAFE_PORT, MAX_PORT);
	}

	/**
	 * Returns the number of a free port in the given range.
	 */
	public static int getFreePort(int minPort, int maxPort) {

		Assert.isTrue(minPort > 0, "'minPort' must be larger than 0");
		Assert.isTrue(maxPort > minPort, "'maxPort' must be larger than minPort");
		int portRange = maxPort - minPort;
		int candidatePort;
		int searchCounter = 0;

		do {
			if (++searchCounter > portRange) {
				throw new IllegalStateException(
						String.format("There were no ports available in the range %d to %d", minPort, maxPort));
			}
			candidatePort = getRandomPort(minPort, portRange);
		}
		while (!isPortAvailable(candidatePort));

		return candidatePort;
	}

	private static int getRandomPort(int minPort, int portRange) {
		return minPort + random.nextInt(portRange);
	}

	private static boolean isPortAvailable(int port) {

		try (ServerSocket serverSocket = new ServerSocket()) {

			try {
				InetSocketAddress sa = new InetSocketAddress(port);
				serverSocket.bind(sa);
				return true;
			}
			catch (IOException e) {
				return false;
			}

		}
		catch (IOException ex) {
			throw new IllegalStateException("Unable to create ServerSocket.", ex);
		}
	}

}
