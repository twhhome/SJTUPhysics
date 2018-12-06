package com.example.hp.sjtuphysics;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class DownloadsFragment extends Fragment {

    private Bundle files = new Bundle();

    private RecyclerView DownloadsList;

    private FragmentInteraction listener;

    public interface FragmentInteraction {
        void longClick(int index);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof FragmentInteraction) {
            listener = (FragmentInteraction) activity;
        } else {
            throw new IllegalArgumentException("activity must implements FragmentInteraction");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View DownloadsView = inflater.inflate(R.layout.fragment_downloads, container, false);
        files = getArguments().getBundle("files");

        DownloadsList = DownloadsView.findViewById(R.id.DownloadsList);
        DownloadsList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        final DownloadsAdapter downloadsAdapter = new DownloadsAdapter(getActivity(), getActivity(), files);
        downloadsAdapter.setOnItemLongClickListener(new DownloadsAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, int index) {
                //Toast.makeText(getActivity(), String.valueOf(index), Toast.LENGTH_SHORT).show();
                listener.longClick(index);
            }
        });
        DownloadsList.setAdapter(downloadsAdapter);

        return DownloadsView;
    }
}
