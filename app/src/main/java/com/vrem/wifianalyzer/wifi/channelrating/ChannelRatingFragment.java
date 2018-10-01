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

package com.vrem.wifianalyzer.wifi.channelrating;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;

public class ChannelRatingFragment extends Fragment {
    private SwipeRefreshLayout swipeRefreshLayout;
    private ChannelRatingAdapter channelRatingAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.channel_rating_content, container, false);

        swipeRefreshLayout = view.findViewById(R.id.channelRatingRefresh);
        swipeRefreshLayout.setOnRefreshListener(new ListViewOnRefreshListener());

        TextView bestChannels = view.findViewById(R.id.channelRatingBestChannels);
        ListView listView = view.findViewById(R.id.channelRatingView);

        channelRatingAdapter = new ChannelRatingAdapter(getActivity(), bestChannels);
        listView.setAdapter(channelRatingAdapter);

        MainContext.INSTANCE.getScannerService().register(channelRatingAdapter);

        return view;
    }

    private void refresh() {
        swipeRefreshLayout.setRefreshing(true);
        MainContext.INSTANCE.getScannerService().update();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public void onDestroy() {
        MainContext.INSTANCE.getScannerService().unregister(channelRatingAdapter);
        super.onDestroy();
    }

    ChannelRatingAdapter getChannelRatingAdapter() {
        return channelRatingAdapter;
    }

    private class ListViewOnRefreshListener implements SwipeRefreshLayout.OnRefreshListener {
        @Override
        public void onRefresh() {
            refresh();
        }
    }
}
