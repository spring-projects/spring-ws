/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.binding.method;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.springframework.beans.BeanUtils;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.ObjectUtils;

/**
 * A class method signature.
 * 
 * @author Keith Donald
 */
public class ClassMethodKey implements Serializable {

	/**
	 * The class the method is a member of.
	 */
	private Class type;

	/**
	 * The name of the method.
	 */
	private String methodName;

	/**
	 * The method's parameter types.
	 */
	private Class[] parameterTypes;

	/**
	 * A cached handle to the resolved method (may be null).
	 */
	private transient Method method;

	/**
	 * Create a new named argument definition.
	 * 
	 * @param name the name
	 */
	public ClassMethodKey(Class type, String methodName, Class[] parameterTypes) {
		this.type = type;
		this.methodName = methodName;
		this.parameterTypes = parameterTypes;
	}

	public Class getType() {
		return type;
	}

	public String getMethodName() {
		return methodName;
	}

	public Class[] getParameterTypes() {
		return parameterTypes;
	}

	public Method getMethod() throws InvalidMethodSignatureException {
		if (method == null) {
			method = resolveMethod();
		}
		return method;
	}

	protected Method resolveMethod() throws InvalidMethodSignatureException {
		try {
			return type.getMethod(getMethodName(), getParameterTypes());
		}
		catch (NoSuchMethodException e) {
			Method method = findMethodConsiderAssignableParameterTypes();
			if (method != null) {
				return method;
			}
			else {
				throw new InvalidMethodSignatureException(this, e);
			}
		}
	}

	protected Method findMethodConsiderAssignableParameterTypes() {
		Method[] candidateMethods = getType().getMethods();
		for (int i = 0; i < candidateMethods.length; i++) {
			if (candidateMethods[i].getName().equals(getMethodName())) {
				// Check if the method has the correct number of parameters.
				Class[] candidateParameterTypes = candidateMethods[i].getParameterTypes();
				if (candidateParameterTypes.length == getParameterTypes().length) {
					int numberOfCorrectArguments = 0;
					for (int j = 0; j < candidateParameterTypes.length; j++) {
						// Check if the candidate type is assignable to the sig
						// parameter type.
						Class candidateType = candidateParameterTypes[j];
						Class parameterType = parameterTypes[j];
						if (parameterType != null) {
							if (BeanUtils.isAssignable(candidateType, parameterType)) {
								numberOfCorrectArguments++;
							}
						}
						else {
							// just match on a null param type (effectively
							// 'any')
							numberOfCorrectArguments++;
						}
					}
					if (numberOfCorrectArguments == parameterTypes.length) {
						return candidateMethods[i];
					}
				}
			}
		}
		return null;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof ClassMethodKey)) {
			return false;
		}
		ClassMethodKey other = (ClassMethodKey)obj;
		return type.equals(other.type) && methodName.equals(other.methodName)
				&& argumentTypesEqual(other.parameterTypes);
	}

	private boolean argumentTypesEqual(Class[] other) {
		if (parameterTypes == other) {
			return true;
		}
		if (parameterTypes.length != other.length) {
			return false;
		}
		for (int i = 0; i < this.parameterTypes.length; i++) {
			if (!ObjectUtils.nullSafeEquals(parameterTypes[i], other[i])) {
				return false;
			}
		}
		return true;
	}

	public int hashCode() {
		return type.hashCode() + methodName.hashCode() + argumentTypesHash();
	}

	private int argumentTypesHash() {
		if (parameterTypes == null) {
			return 0;
		}
		int hash = 0;
		for (int i = 0; i < parameterTypes.length; i++) {
			Class parameterType = parameterTypes[i];
			if (parameterType != null) {
				hash += parameterTypes[i].hashCode();
			}
		}
		return hash;
	}

	public String toString() {
		return new ToStringCreator(this).append("type", type).append("methodName", methodName).append("parameterTypes",
				parameterTypes).toString();
	}
}