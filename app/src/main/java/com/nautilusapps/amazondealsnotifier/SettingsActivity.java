package com.nautilusapps.amazondealsnotifier;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;

public class SettingsActivity extends AppCompatPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        getFragmentManager().beginTransaction().replace(
                android.R.id.content,
                new NotificationPreferenceFragment()).commit();

    }

    private static void updateSummaryAndValue(Preference preference) {

        if (preference.getOnPreferenceChangeListener() == null) {
            preference.setOnPreferenceChangeListener(new PreferenceListener());
        }

        if (preference instanceof ListPreference || preference instanceof RingtonePreference) {

            PreferenceListener.updateSummaryAndSharedPreferences(
                    preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));

        } else if (preference instanceof SwitchPreference) {

            PreferenceListener.updateSummaryAndSharedPreferences(
                    preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getBoolean(preference.getKey(), true));

        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.activity_settings);

            setHasOptionsMenu(true);

            // Fill the summaries:
            updateSummaryAndValue(findPreference(getString(R.string.
                    pref_key_enable_notifications)));
            updateSummaryAndValue(findPreference(getString(R.string.
                    pref_key_notification_sound)));
            updateSummaryAndValue(findPreference(getString(R.string.pref_key_enable_vibration)));
            updateSummaryAndValue(findPreference(getString(R.string.
                    pref_key_update_frequency)));

            // Define the behavior for the button to empty the list:
            Preference emptyItemsListPreference = findPreference(
                    getString(R.string.pref_key_empty_items_list));
            emptyItemsListPreference.setOnPreferenceClickListener(
                    new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(final Preference preference) {

                            AlertDialog.Builder dialog = new AlertDialog.Builder(
                                    preference.getContext()
                            );
                            dialog.setTitle(getString(R.string.title_confirmation_empty_items_list));

                            dialog.setPositiveButton(
                                    getString(R.string.action_yes),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            DBHandler dbHandler = new DBHandler(
                                                    preference.getContext()
                                            );
                                            dbHandler.emptyTable();
                                        }
                                    });

                            dialog.setNegativeButton(
                                    getString(R.string.action_cancel),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });

                            dialog.show();

                            return true;

                        }
                    }
            );

        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {

            int id = item.getItemId();

            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), MainActivity.class));
                return true;
            }

            return super.onOptionsItemSelected(item);

        }

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

}