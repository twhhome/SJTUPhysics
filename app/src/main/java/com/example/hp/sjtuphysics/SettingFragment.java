package com.example.hp.sjtuphysics;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

public class SettingFragment extends PreferenceFragment
        implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    private PrefManager prefManager;

    private FragmentInteraction listener;

    private SwitchPreference autoSignIn;
    private Preference logout;
    private Preference email;
    private Preference github;

    public interface FragmentInteraction {
        void logout();
    }

    public static SettingFragment newInstance() {
        return new SettingFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof FragmentInteraction) {
            listener = (FragmentInteraction)activity;
        } else{
            throw new IllegalArgumentException("activity must implements FragmentInteraction");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefManager = new PrefManager(this.getActivity());

        addPreferencesFromResource(R.xml.preference_about);
        autoSignIn = (SwitchPreference) findPreference("autoSignIn");
        autoSignIn.setChecked(prefManager.isAutoLogin());
        autoSignIn.setOnPreferenceChangeListener(this);
        logout = findPreference("logout");
        logout.setOnPreferenceClickListener(this);
        email = findPreference("email");
        email.setOnPreferenceClickListener(this);
        github = findPreference("github");
        github.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        final String key = preference.getKey();
        switch (key) {
            case "logout":
                showMessageOKCancel("退出登录？", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prefManager.setAutoLoginin(false);
                        listener.logout();
                    }
                }, null);
                return true;
            case "email":
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + email.getSummary()));
                startActivity(intent);
                return true;
            case "github":
                Intent githubIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("" + github.getSummary()));
                startActivity(githubIntent);
                return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object object) {
        final String key = preference.getKey();
        switch (key) {
            case "autoSignIn":
                boolean value = (boolean)object;
                prefManager.setAutoLoginin(value);
                break;
        }
        return true;
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener cancelListener) {
        new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton("确定", okListener)
                .setNegativeButton("取消", cancelListener)
                .setCancelable(true)
                .create()
                .show();
    }
}
