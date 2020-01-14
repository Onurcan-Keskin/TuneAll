package com.onurcankeskin.looneytune.TuneInSettings;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.onurcankeskin.looneytune.R;

public class PreferenceSwitch extends CustomPreference implements Preference.OnPreferenceClickListener {

    /*  Couldn't get `android:title` and `android:summary` to
        work and actually set the text of the android:id/title
        and android:id/summary so had to use custom app:* attributes.
     */

    protected String mKey;
    protected String mTitle;
    protected String mSummary;
    private boolean defaultValue;

    private ImageSwitcher checkBoxView;



    public PreferenceSwitch(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(context, attrs);
    }

    public PreferenceSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutResource(R.layout.pref_switch);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PreferenceSwitch);

        int titleResId = ta.getResourceId(R.styleable.PreferenceSwitch_title, 0);
        if (titleResId != 0) {
            mTitle = context.getResources().getString(titleResId);
        }

        int summaryResId = ta.getResourceId(R.styleable.PreferenceSwitch_summary, 0);
        if (summaryResId != 0) {
            mSummary = context.getResources().getString(summaryResId);
        }

        ta.recycle();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        defaultValue = a.getBoolean(index, false);
        return a.getBoolean(index, false);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        TextView titleView = ((TextView) holder.findViewById(R.id.title));
        TextView summaryView = ((TextView) holder.findViewById(R.id.summary));
        titleView.setText(mTitle);
        summaryView.setText(mSummary);

        setOnPreferenceClickListener(this);

        checkBoxView = (ImageSwitcher) holder.findViewById(R.id.pref_checkbox);
        checkBoxView.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imageVew = new ImageView(getContext());
                return imageVew;
            }
        });

        Animation inAnim = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        inAnim.setDuration(100);
        Animation outAnim = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
        outAnim.setDuration(100);
        checkBoxView.setInAnimation(inAnim);
        checkBoxView.setOutAnimation(outAnim);

        updatePreferenceView();
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    @Override
    public void updatePreferenceView() {
        if (getPersistedBoolean(defaultValue)) {
            checkBoxView.setImageDrawable(getContext().getDrawable(R.drawable.ic_check_checked));
        } else {
            checkBoxView.setImageDrawable(getContext().getDrawable(R.drawable.ic_check_unchecked));
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        persistBoolean(!getPersistedBoolean(defaultValue));
        updatePreferenceView();
        return true;
    }
}
