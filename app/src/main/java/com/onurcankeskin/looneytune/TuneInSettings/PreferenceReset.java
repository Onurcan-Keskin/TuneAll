package com.onurcankeskin.looneytune.TuneInSettings;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.preference.PreferenceViewHolder;

import com.onurcankeskin.looneytune.DesignUtils.Button;
import com.onurcankeskin.looneytune.R;

public class PreferenceReset extends CustomPreference {

    public OnPreferencesResetListener onPreferencesResetListener;

    public PreferenceReset(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PreferenceReset(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutResource(R.layout.pref_reset);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        Button button = ((Button) holder.findViewById(R.id.pref_reset_button));

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onPreferencesResetListener != null) {
                    onPreferencesResetListener.onEvent();
                }
            }
        });
    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    @Override
    public void updatePreferenceView() { }

    public void setOnPreferencesResetListener(OnPreferencesResetListener onPreferencesResetListener) {
        this.onPreferencesResetListener = onPreferencesResetListener;
    }

    public interface OnPreferencesResetListener {
        void onEvent();
    }
}