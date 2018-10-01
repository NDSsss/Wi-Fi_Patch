/*
 * WiFiAnalyzer
 * Copyright (C) 2018  VREM Software Development <VREMSoftwareDevelopment@gmail.com>
 *  
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.vrem.wifianalyzer.wifi.filter;

import android.app.Dialog;
import android.support.annotation.NonNull;

import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.filter.adapter.SecurityAdapter;
import com.vrem.wifianalyzer.wifi.model.Security;

import java.util.HashMap;
import java.util.Map;

class SecurityFilter extends EnumFilter<Security, SecurityAdapter> {
    static final Map<Security, Integer> ids = new HashMap<>();

    static {
        ids.put(Security.NONE, R.id.filterSecurityNone);
        ids.put(Security.WPS, R.id.filterSecurityWPS);
        ids.put(Security.WEP, R.id.filterSecurityWEP);
        ids.put(Security.WPA, R.id.filterSecurityWPA);
        ids.put(Security.WPA2, R.id.filterSecurityWPA2);
    }

    SecurityFilter(@NonNull SecurityAdapter securityAdapter, @NonNull Dialog dialog) {
        super(ids, securityAdapter, dialog, R.id.filterSecurity);
    }
}
