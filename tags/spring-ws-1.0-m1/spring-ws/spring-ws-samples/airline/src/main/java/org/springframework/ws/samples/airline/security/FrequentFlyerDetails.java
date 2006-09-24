/*
 * Copyright (c) 2006, Your Corporation. All Rights Reserved.
 * Copyright 2006 the original author or authors.
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

package org.springframework.ws.samples.airline.security;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.userdetails.UserDetails;

import org.springframework.ws.samples.airline.domain.FrequentFlyer;

/**
 * A wrapper around an <code>FrequentFlyer</code> which provides extra functionality needed to implement the
 * <code>UserDetails</code> interface.
 *
 * @author Arjen Poutsma
 */
public class FrequentFlyerDetails implements UserDetails {

    private FrequentFlyer frequentFlyer;

    public static final GrantedAuthority[] GRANTED_AUTHORITIES =
            new GrantedAuthority[]{new GrantedAuthorityImpl("ROLE_FREQUENT_FLYER")};

    public FrequentFlyerDetails(FrequentFlyer frequentFlyer) {
        this.frequentFlyer = frequentFlyer;
    }

    public GrantedAuthority[] getAuthorities() {
        return GRANTED_AUTHORITIES;
    }

    public String getPassword() {
        return frequentFlyer.getPassword();
    }

    public String getUsername() {
        return frequentFlyer.getUsername();
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return true;
    }

    public FrequentFlyer getFrequentFlyer() {
        return frequentFlyer;
    }
}
