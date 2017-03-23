package com.kvolkov.animatedprogressviews.animations;

import android.animation.ValueAnimator;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * Opacity animation types.
 *
 * @author Kirill Volkov (https://github.com/vulko).
 *         Copyright (C). All rights reserved.
 */
public class OpacityAnimation {

    /**
     * Opacity animation types.
     */
    public static final int NONE = 0;
    public static final int BLINKING = 1;
    public static final int SHINY = 2;
    public static final int AURA = 3;

    /**
     * Animation durations in [ms] for opacity animation types. See {@code OpacityAnimation}.
     */
    private static long sOpacityAnimationDuration[] = {
            0,
            2000,
            1000,
            1000
    };

    /**
     * Opacity animation consts.
     */
    private static int INITIAL_OPACITY = 255;
    private static int TARGET_OPACITY = 50;

    private int mType = 0;
    private int mAnimatorCount;

    /**
     * Animated values.
     */
    private List<Integer> mAlphaOpacityList = new ArrayList<>();
    private List<ValueAnimator> mOpacityValueAnimatorList = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param type  Type of animation.
     */
    public OpacityAnimation(int type) {
        setType(type);
    }

    /**
     * Set animation type.
     *
     * @param value Should be one of public static values defined here.
     */
    public void setType(final int value) {
        if (mType == value) {
            return;
        }

        switch (value) {
            case NONE:
            case BLINKING:
            case SHINY:
            case AURA:
                mType = value;
                break;

            default:
                mType = NONE;
                Log.w(getClass().getName(), "Wrong opacity animation type set! Sticking with default value.");
        }
    }

    /**
     * Get animation type.
     *
     * @return animation type
     */
    public int getType() {
        return mType;
    }

    /**
     * Set number of animators.
     *
     * @param count Number of animators.
     */
    public void setAnimatorsCount(final int count) {
        if (mAnimatorCount == count) {
            return;
        }

        mAnimatorCount = count;
    }

    /**
     * @return animation duration.
     */
    private long getDuration() {
        return sOpacityAnimationDuration[mType];
    }

    /**
     * Init opacity animators.
     */
    private void initAnimators() {
        mAlphaOpacityList.clear();
        for (int i = 0; i < mAnimatorCount; ++i) {
            mAlphaOpacityList.add(INITIAL_OPACITY);
        }

        if (mType != OpacityAnimation.NONE) {

            for (int i = 0; i < mAnimatorCount; ++ i) {
                final ValueAnimator opacityAnimator = new ValueAnimator();

                opacityAnimator.setDuration(getDuration());
//                opacityAnimator.setStartDelay(ANIMATION_START_DELAY);
                opacityAnimator.setRepeatCount(ValueAnimator.INFINITE);
                opacityAnimator.setRepeatMode(ValueAnimator.RESTART);

                switch (mType) {
                    case OpacityAnimation.BLINKING:
                        initBlinkingAnimators(i, opacityAnimator);
                        break;

                    case OpacityAnimation.SHINY:
                        initShinyAnimators(i, opacityAnimator);
                        break;

                    case OpacityAnimation.AURA:
                        initAuraAnimators(i, opacityAnimator);
                        break;
                }

                mOpacityValueAnimatorList.add(opacityAnimator);
            }
        }
    }

    /**
     * Call this to explicitly restart opacity animation specified by {@code mType}.
     */
    public void restart() {
        stop();
        initAnimators();

        for (int i = 0; i < mOpacityValueAnimatorList.size(); ++i) {
            mOpacityValueAnimatorList.get(i).start();
        }
    }

    /**
     * Call this to explicitly stop opacity animation specified by {@code mType}.
     */
    public void stop() {
        ValueAnimator animator;
        if (mOpacityValueAnimatorList != null) {
            for (int i = 0; i < mOpacityValueAnimatorList.size(); ++i) {
                animator = mOpacityValueAnimatorList.get(i);
                if (animator != null) {
                    animator.cancel();
                }
            }
            mOpacityValueAnimatorList.clear();
        }
    }

    /**
     * Returns animated value.
     *
     * @param index Index of animated value.
     * @return  An int.
     */
    public int getAnimatedValue(int index) {
        return mAlphaOpacityList.get(index);
    }

    private void initBlinkingAnimators(final int index, ValueAnimator opacityAnimator) {
        opacityAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        opacityAnimator.setIntValues(INITIAL_OPACITY, TARGET_OPACITY, INITIAL_OPACITY);
        opacityAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final int opacity = (int) animation.getAnimatedValue();
                mAlphaOpacityList.set(index, opacity);
            }
        });
    }

    private void initShinyAnimators(final int index, ValueAnimator opacityAnimator) {
        opacityAnimator.setRepeatMode(ValueAnimator.REVERSE);
        final float opacityDecelerateFactor = 1.f + 0.8f * (index + 1);
        opacityAnimator.setInterpolator(new DecelerateInterpolator(opacityDecelerateFactor));
        opacityAnimator.setIntValues(255, 50, 255);
        opacityAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final int opacity = (int) animation.getAnimatedValue();
                mAlphaOpacityList.set(index, opacity);
            }
        });
    }

    private void initAuraAnimators(final int index, ValueAnimator opacityAnimator) {
        opacityAnimator.setRepeatMode(ValueAnimator.REVERSE);
        final float opacityDecelerateFactor = 1.f + 0.8f * (index + 1);
        opacityAnimator.setInterpolator(new AnticipateInterpolator(opacityDecelerateFactor));
        opacityAnimator.setIntValues(255, 50, 255, 50);
        opacityAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final int opacity = (int) animation.getAnimatedValue();
                mAlphaOpacityList.set(index, opacity);
            }
        });
    }
}
