/* Copyright 2004, 2005, 2006 Acegi Technology Pty Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap.security.support;

import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.LockedException;
import org.acegisecurity.DisabledException;
import org.acegisecurity.AccountExpiredException;
import org.acegisecurity.CredentialsExpiredException;

/**
 * Generic utility methods for Spring Security
 *
 * @author Tareq Abedrabbo
 * @since 1.5.8
 */
public abstract class AcegiUtils {

    /**
     * Checks the validity of a user's account and credentials.
     * @param user the user to check
     * @throws org.springframework.security.AccountExpiredException if the account has expired
     * @throws org.springframework.security.CredentialsExpiredException if the credentials have expired
     * @throws org.springframework.security.DisabledException if the account is disabled
     * @throws org.springframework.security.LockedException if the account is locked
     */
    public static void checkUserValidity(UserDetails user)
            throws AccountExpiredException, CredentialsExpiredException, DisabledException, LockedException {
        if (!user.isAccountNonLocked()) {
            throw new LockedException("User account is locked");
        }

        if (!user.isEnabled()) {
            throw new DisabledException("User is disabled");
        }

        if (!user.isAccountNonExpired()) {
            throw new AccountExpiredException("User account has expired");
        }

        if (!user.isCredentialsNonExpired()) {
            throw new CredentialsExpiredException("User credentials have expired");
        }
    }
}