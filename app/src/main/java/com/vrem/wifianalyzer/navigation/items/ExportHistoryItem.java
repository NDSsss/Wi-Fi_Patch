package com.vrem.wifianalyzer.navigation.items;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.widget.Toast;

import com.vrem.wifianalyzer.MainActivity;
import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.navigation.NavigationMenu;
import com.vrem.wifianalyzer.wifi.model.WiFiData;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;
import com.vrem.wifianalyzer.wifi.model.WiFiSignal;

import org.apache.commons.collections4.Closure;
import org.apache.commons.collections4.IterableUtils;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExportHistoryItem implements NavigationItem {
    private static final String TIME_STAMP_FORMAT = "yyyy/MM/dd HH:mm:ss";
    private String timestamp;

    @Override
    public void activate(@NonNull MainActivity mainActivity, @NonNull MenuItem menuItem, @NonNull NavigationMenu navigationMenu) {
        String title = getTitle(mainActivity);
        List<WiFiDetail> wiFiDetails = getWiFiDetails();
        if (!dataAvailable(wiFiDetails)) {
            Toast.makeText(mainActivity, R.string.no_data, Toast.LENGTH_LONG).show();
            return;
        }
        timestamp = new SimpleDateFormat(TIME_STAMP_FORMAT).format(new Date());
        String data ="";
        ArrayList<WiFiData> history = getHistory();
        ArrayList<String> historyTime = getHistoryTime();
        if(history.size()==historyTime.size()) {
            for (int i = 0; i < history.size(); i++) {
                List<WiFiDetail> historyDetails = history.get(i).getWiFiDetails();
                data += getData(historyTime.get(i), historyDetails);
                data+=String.valueOf(i)+"---"+"\n";
            }
        }
//        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        boolean isWrote;
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"History.txt");
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(mainActivity.getBaseContext())) {
                    mainActivity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE}, 2909);
                } else {
                    FileWriter writer = new FileWriter(file, true);
                    writer.write(data);
                    writer.flush();
                    writer.close();
                    isWrote = true;
                }
            } else {
                FileWriter writer = new FileWriter(file, true);
                writer.write(data);
                writer.flush();
                writer.close();
                isWrote = true;
            }
            data+=file.getAbsolutePath();
            isWrote = true;
        }catch (Exception e){
            isWrote = false;
        }
        Intent intent = createIntent(title, data);
        Intent chooser = createChooserIntent(intent, title);
        if(isWrote){
            Uri path = Uri.fromFile(file);
            intent.putExtra(Intent.EXTRA_STREAM, path);
//            emailIntent.putExtra(Intent.EXTRA_STREAM, path);
        }else{

        }

// set the type to 'email'
//        emailIntent .setType("vnd.android.cursor.dir/email");
//        String to[] = {"asd@gmail.com"};
//        emailIntent .putExtra(Intent.EXTRA_EMAIL, to);
// the attachment
//        emailIntent .putExtra(Intent.EXTRA_STREAM, path);
// the mail subject
//        emailIntent .putExtra(Intent.EXTRA_SUBJECT, "Subject");
//        startActivity(Intent.createChooser(emailIntent , "Send email..."));



        if (!exportAvailable(mainActivity, chooser)) {
            Toast.makeText(mainActivity, R.string.export_not_available, Toast.LENGTH_LONG).show();
            return;
        }
        try {
            mainActivity.startActivity(chooser);
            MainContext.INSTANCE.getScannerService().clearLogCash();
            MainContext.INSTANCE.getScannerService().clearLogCashTime();
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mainActivity, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean isRegistered() {
        return false;
    }

    private boolean exportAvailable(@NonNull MainActivity mainActivity, @NonNull Intent chooser) {
        return chooser.resolveActivity(mainActivity.getPackageManager()) != null;
    }

    private boolean dataAvailable(@NonNull List<WiFiDetail> wiFiDetails) {
        return !wiFiDetails.isEmpty();
    }

    @NonNull
    String getData(String timestamp, @NonNull List<WiFiDetail> wiFiDetails) {
        final StringBuilder result = new StringBuilder();
        result.append(
                String.format(Locale.ENGLISH,
                        "Time Stamp|SSID|BSSID|Strength|Primary Channel|Primary Frequency|Center Channel|Center Frequency|Width (Range)|Distance|Security%n"));
        IterableUtils.forEach(wiFiDetails, new ExportHistoryItem.WiFiDetailClosure(timestamp, result));
        return result.toString();
    }

    ArrayList<WiFiData> getHistory(){
        return MainContext.INSTANCE.getScannerService().getLogCash();
    }
    ArrayList<String> getHistoryTime(){
        return MainContext.INSTANCE.getScannerService().getLogCashTime();
    }

    @NonNull
    private List<WiFiDetail> getWiFiDetails() {
        return MainContext.INSTANCE.getScannerService().getWiFiData().getWiFiDetails();
    }

    @NonNull
    private String getTitle(@NonNull MainActivity mainActivity) {
        Resources resources = mainActivity.getResources();
        return resources.getString(R.string.action_access_points);
    }

    private Intent createIntent(String title, String data) {
        Intent intent = createSendIntent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, title);
        intent.putExtra(Intent.EXTRA_SUBJECT, title);
        intent.putExtra(Intent.EXTRA_TEXT, data);
        return intent;
    }

    Intent createSendIntent() {
        return new Intent(Intent.ACTION_SEND);
    }

    Intent createChooserIntent(@NonNull Intent intent, @NonNull String title) {
        return Intent.createChooser(intent, title);
    }

    String getTimestamp() {
        return timestamp;
    }

    private class WiFiDetailClosure implements Closure<WiFiDetail> {
        private final StringBuilder result;
        private final String timestamp;

        private WiFiDetailClosure(String timestamp, @NonNull StringBuilder result) {
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
