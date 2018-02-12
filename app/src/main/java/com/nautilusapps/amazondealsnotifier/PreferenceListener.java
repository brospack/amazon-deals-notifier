package com.nautilusapps.amazondealsnotifier;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.text.TextUtils;

public class PreferenceListener implements Preference.OnPreferenceChangeListener {

    /**
     * Called when a preference is changed. Updates the summary of the preference and its value in
     * {@code SharedPreferences}.
     */
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        Context context = preference.getContext();

        updateSummaryAndSharedPreferences(preference, newValue);

        // If the user changes the update frequency reschedules the next update:
        if (preference
                .getKey()
                .equals(context.getString(R.string.pref_key_update_frequency))) {
            UpdateService.scheduleUpdate(
                    context,
                    MainActivity.getUpdateFrequency(context));
        }

        return true;

    }

    public static void updateSummaryAndSharedPreferences(Preference preference, Object newValue) {

        String newValueString = newValue.toString();
        Context context = preference.getContext();

        SharedPreferences sharedPreferences = context
                .getSharedPreferences(
                        preference.getContext().getString(R.string.sharedpref_file_name),
                        Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (preference instanceof ListPreference) {

            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(newValueString);

            preference.setSummary(index >= 0
                    ? listPreference.getEntries()[index]
                    : null);

            // Update the value in SharedPreferences:
            editor.putString(preference.getKey(), newValueString);

        } else if (preference instanceof RingtonePreference) {

            if (TextUtils.isEmpty(newValueString)) {
                preference.setSummary(R.string.action_silent);
                editor.putString(preference.getKey(), "silent");
            } else {
                // Try to get the notification sound:
                Ringtone ringtone = RingtoneManager.getRingtone(
                        preference.getContext(),
                        Uri.parse(newValueString));

                if (ringtone == null) {
                    preference.setSummary(null);
                    editor.putString(preference.getKey(), "invalid");
                } else {
                    String title = ringtone.getTitle(preference.getContext());
                    preference.setSummary(title);
                    editor.putString(preference.getKey(), newValueString);
                }
            }

        } else if (preference instanceof SwitchPreference) {

            editor.putBoolean(preference.getKey(), Boolean.valueOf(newValueString));

        } else {
            preference.setSummary(newValueString);
        }

        // Commit the new value in SharedPreferences:
        editor.apply();

    }

}