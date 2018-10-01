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

package com.vrem.wifianalyzer.wifi.channelgraph;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.graphutils.GraphViewAdd;

import org.apache.commons.collections4.IterableUtils;

public class ChannelGraphFragment extends Fragment {
    private SwipeRefreshLayout swipeRefreshLayout;
    private ChannelGraphAdapter channelGraphAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.graph_content, container, false);

        swipeRefreshLayout = view.findViewById(R.id.graphRefresh);
        swipeRefreshLayout.setOnRefreshListener(new ListViewOnRefreshListener());

        LinearLayout linearLayout = view.findViewById(R.id.graphNavigation);
        ChannelGraphNavigation channelGraphNavigation = new ChannelGraphNavigation(linearLayout, getActivity());
        channelGraphAdapter = new ChannelGraphAdapter(channelGraphNavigation);
        addGraphViews(swipeRefreshLayout, channelGraphAdapter);

        MainContext.INSTANCE.getScannerService().register(channelGraphAdapter);

        return view;
    }

    private void addGraphViews(View view, ChannelGraphAdapter channelGraphAdapter) {
        IterableUtils.forEach(channelGraphAdapter.getGraphViews(),
            new GraphViewAdd((ViewGroup) view.findViewById(R.id.graphFlipper)));
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
        MainContext.INSTANCE.getScannerService().unregister(channelGraphAdapter);
        super.onDestroy();
    }

    ChannelGraphAdapter getChannelGraphAdapter() {
        return channelGraphAdapter;
    }

    private class ListViewOnRefreshListener implements SwipeRefreshLayout.OnRefreshListener {
        @Override
        public void onRefresh() {
            refresh();
        }
    }

}
