package com.onurcankeskin.looneytune.TuneInSettings;

import android.content.Context;
import androidx.preference.Preference;
import android.util.AttributeSet;

public abstract class CustomPreference extends Preference {
    public CustomPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public abstract void updatePreferenceView();
}
