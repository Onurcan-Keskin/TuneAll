package com.onurcankeskin.looneytune;

import android.content.Context;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class TunerModeListeningBlock extends LinearLayout {

    public boolean isActive = true;

    public TunerModeListeningBlock(Context context) {
        super(context);
        init(null, 0);
    }

    public TunerModeListeningBlock(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public TunerModeListeningBlock(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    public void init(AttributeSet attrs, int defStyle) {
        inflate(getContext(), R.layout.view_tuner_mode_listening, this);
    }

    public void startAnimation() {
        Drawable d = ((ImageView) findViewById(R.id.imageViewMain)).getDrawable();
        if(d instanceof AnimatedVectorDrawable) {
            ((AnimatedVectorDrawable) d).start();
            ((AnimatedVectorDrawable) d).registerAnimationCallback(new Animatable2.AnimationCallback() {
                @Override
                public void onAnimationEnd(Drawable drawable) {
                    super.onAnimationEnd(drawable);
                    ((AnimatedVectorDrawable) drawable).start();
                }
            });
        }
    }

    public void stopAnimation() {
        Drawable d = ((ImageView) findViewById(R.id.imageViewMain)).getDrawable();
        if(d instanceof AnimatedVectorDrawable) {
            ((AnimatedVectorDrawable) d).stop();
            ((AnimatedVectorDrawable) d).registerAnimationCallback(null);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        if(isActive) {
            startAnimation();
        }
    }
}
