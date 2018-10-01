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

package com.vrem.wifianalyzer.wifi.predicate;

import android.support.annotation.NonNull;

import com.vrem.wifianalyzer.wifi.band.WiFiBand;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;

import org.apache.commons.collections4.Predicate;

public class WiFiBandPredicate implements Predicate<WiFiDetail> {
    private final WiFiBand wiFiBand;

    public WiFiBandPredicate(@NonNull WiFiBand wiFiBand) {
        this.wiFiBand = wiFiBand;
    }

    @Override
    public boolean evaluate(WiFiDetail object) {
        return object.getWiFiSignal().getWiFiBand().equals(wiFiBand);
    }

}
