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

package com.vrem.wifianalyzer.wifi.scanner;

import android.app.Application;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.navigation.items.ExportHistoryItem;
import com.vrem.wifianalyzer.settings.Settings;
import com.vrem.wifianalyzer.wifi.model.WiFiData;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;
import com.vrem.wifianalyzer.wifi.model.WiFiSignal;

import org.apache.commons.collections4.Closure;
import org.apache.commons.collections4.IterableUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

class Scanner implements ScannerService {
    private static final String TIME_STAMP_FORMAT = "yyyy/MM/dd HH:mm:ss";
    private final List<UpdateNotifier> updateNotifiers;
    private final WifiManager wifiManager;
    private final Settings settings;
    private Transformer transformer;
    private WiFiData wiFiData;
    private Cache cache;
    private PeriodicScan periodicScan;
    private ArrayList<WiFiData> logCash;
    private ArrayList<String> logCashTimeStamp;
    private File file;
    private File filePath;

    Scanner(@NonNull WifiManager wifiManager, @NonNull Handler handler, @NonNull Settings settings) {
        this.updateNotifiers = new ArrayList<>();
        this.wifiManager = wifiManager;
        this.settings = settings;
        this.wiFiData = WiFiData.EMPTY;
        this.setTransformer(new Transformer());
        this.setCache(new Cache());
        this.periodicScan = new PeriodicScan(this, handler, settings);
        logCash = new ArrayList<>();
        logCashTimeStamp = new ArrayList<>();
        filePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath()+"/wifiAnalyzer");
        file = new File(filePath,"history"+String.valueOf(new Date().getTime())+".txt");
    }

    @Override
    public void update() {
        enableWiFi();
        scanResults();
        wiFiData = transformer.transformToWiFiData(cache.getScanResults(), wiFiInfo(), wifiConfiguration());
        if(logCash.size()<10) {
            logCash.add(wiFiData);
        } else {
            writeToFile(logCash);
            logCash.clear();
        }
        logCashTimeStamp.add(new SimpleDateFormat(TIME_STAMP_FORMAT).format(new Date()));
        IterableUtils.forEach(updateNotifiers, new UpdateClosure());
    }

    private void writeToFile(ArrayList<WiFiData> cash){
        try {
            String data ="";
            ArrayList<WiFiData> history = cash;
            ArrayList<String> historyTime = getHistoryTime();
            if(history.size()==historyTime.size()) {
                for (int i = 0; i < history.size(); i++) {
                    List<WiFiDetail> historyDetails = history.get(i).getWiFiDetails();
                    data += getData(historyTime.get(i), historyDetails);
                    data+=String.valueOf(i)+"---"+"\n";
                }
            }
            if(!filePath.exists()) {
                filePath.mkdir();
            }
            FileWriter writer = new FileWriter(file, true);
            BufferedWriter bufferWriter = new BufferedWriter(writer);
            bufferWriter.write(data);
            bufferWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    @NonNull
    String getData(String timestamp, @NonNull List<WiFiDetail> wiFiDetails) {
        final StringBuilder result = new StringBuilder();
        result.append(
                String.format(Locale.ENGLISH,
                        "Time Stamp|SSID|BSSID|Strength|Primary Channel|Primary Frequency|Center Channel|Center Frequency|Width (Range)|Distance|Security%n"));
        IterableUtils.forEach(wiFiDetails, new WiFiDetailClosure(timestamp, result));
        return result.toString();
    }

    ArrayList<String> getHistoryTime(){
        return MainContext.INSTANCE.getScannerService().getLogCashTime();
    }

    @Override
    @NonNull
    public WiFiData getWiFiData() {
        return wiFiData;
    }

    @Override
    public void register(@NonNull UpdateNotifier updateNotifier) {
        updateNotifiers.add(updateNotifier);
    }

    @Override
    public void unregister(@NonNull UpdateNotifier updateNotifier) {
        updateNotifiers.remove(updateNotifier);
    }

    @Override
    public void pause() {
        //periodicScan.stop();
    }

    @Override
    public boolean isRunning() {
        return periodicScan.isRunning();
    }

    @Override
    public void resume() {
       // periodicScan.start();
    }

    @Override
    public void setWiFiOnExit() {
        if (settings.isWiFiOffOnExit()) {
            try {
                wifiManager.setWifiEnabled(false);
            } catch (Exception e) {
                // critical error: do not die
            }
        }
    }

    @NonNull
    PeriodicScan getPeriodicScan() {
        return periodicScan;
    }

    void setPeriodicScan(@NonNull PeriodicScan periodicScan) {
        this.periodicScan = periodicScan;
    }

    void setCache(@NonNull Cache cache) {
        this.cache = cache;
    }

    void setTransformer(@NonNull Transformer transformer) {
        this.transformer = transformer;
    }

    @NonNull
    List<UpdateNotifier> getUpdateNotifiers() {
        return updateNotifiers;
    }

    private void enableWiFi() {
        try {
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }
        } catch (Exception e) {
            // critical error: do not die
        }
    }

    private void scanResults() {
        try {
            if (wifiManager.startScan()) {
                List<ScanResult> scanResults = wifiManager.getScanResults();
                cache.add(scanResults);
            }
        } catch (Exception e) {
            // critical error: do not die
        }
    }

    private WifiInfo wiFiInfo() {
        try {
            return wifiManager.getConnectionInfo();
        } catch (Exception e) {
            // critical error: do not die
            return null;
        }
    }

    private List<WifiConfiguration> wifiConfiguration() {
        try {
            return wifiManager.getConfiguredNetworks();
        } catch (Exception e) {
            // critical error: do not die
            return new ArrayList<>();
        }
    }

    @Override
    public void clearLogCash() {
        logCash.clear();
    }

    public ArrayList<WiFiData> getLogCash(){
        return logCash;
    }
    @Override
    public void clearLogCashTime() {
        logCashTimeStamp.clear();
    }

    public ArrayList<String> getLogCashTime(){
        return logCashTimeStamp;
    }

    private class UpdateClosure implements Closure<UpdateNotifier> {
        @Override
        public void execute(UpdateNotifier updateNotifier) {
            updateNotifier.update(wiFiData);
        }
    }

    public class WiFiDetailClosure implements Closure<WiFiDetail> {
        private final StringBuilder result;
        private final String timestamp;

        public WiFiDetailClosure(String timestamp, @NonNull StringBuilder result) {
            this.result = result;
            this.timestamp = timestamp;
        }

        @Override
        public void execute(WiFiDetail wiFiDetail) {
            WiFiSignal wiFiSignal = wiFiDetail.getWiFiSignal();
            result.append(String.format(Locale.ENGLISH, "%s|%s|%s|%ddBm|%d|%d%s|%d|%d%s|%d%s (%d - %d)|%s|%s%n|%s",
                    timestamp,
                    wiFiDetail.getSSID(),
                    wiFiDetail.getBSSID(),
                    wiFiSignal.getLevel(),
                    wiFiSignal.getPrimaryWiFiChannel().getChannel(),
                    wiFiSignal.getPrimaryFrequency(),
                    WiFiSignal.FREQUENCY_UNITS,
                    wiFiSignal.getCenterWiFiChannel().getChannel(),
                    wiFiSignal.getCenterFrequency(),
                    WiFiSignal.FREQUENCY_UNITS,
                    wiFiSignal.getWiFiWidth().getFrequencyWidth(),
                    WiFiSignal.FREQUENCY_UNITS,
                    wiFiSignal.getFrequencyStart(),
                    wiFiSignal.getFrequencyEnd(),
                    wiFiSignal.getDistance(),
                    wiFiDetail.getCapabilities(),
                    "qqqqq"));
        }
    }
}
