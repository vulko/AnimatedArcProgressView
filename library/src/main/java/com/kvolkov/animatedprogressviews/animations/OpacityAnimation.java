package com.kvolkov.animatedprogressviews.animations;

import android.animation.ValueAnimator;
import android.support.annotation.IntRange;
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
    public static final int RIPPLE = 4;

    /**
     * Animation durations in [ms] for opacity animation types. See {@code OpacityAnimation}.
     */
    private static long sOpacityAnimationDuration[] = {
            0,
            2000,
            1000,
            1000,
            1000,
    };

    /**
     * Opacity values to animate between.
     */
    private int mInitialOpacity = 255;
    private int mTargetOpacity = 150;

    private int mType = 0;
    private int mAnimatorCount;

    /**
     * Animated values.
     */
    private List<Integer> mAlphaOpacityList = new ArrayList<>();
    private List<ValueAnimator> mOpacityValueAnimatorList = new ArrayList<>();
    private ValueAnimator mRippleEffectAnimator;

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
            case RIPPLE:
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
     * @return animation type.
     */
    public int getType() {
        return mType;
    }

    /**
     * Setup opacity values to animate between.
     *
     * @param initialOpacity    An int in range [0..255].
     * @param targetOpacity     An int in range [0..255].
     */
    public void setOpacityValues(@IntRange(from = 0, to = 255) final int initialOpacity,
                                 @IntRange(from = 0, to = 255) final int targetOpacity) {
        mInitialOpacity = initialOpacity;
        mTargetOpacity = targetOpacity;
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
     * Call this to explicitly restart opacity animation specified by {@code mType}.
     */
    public void restart() {
        stop();
        initAnimators();

        for (int i = 0; i < mOpacityValueAnimatorList.size(); ++i) {
            mOpacityValueAnimatorList.get(i).start();
        }

        if (mRippleEffectAnimator != null) {
            mRippleEffectAnimator.start();
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

        if (mRippleEffectAnimator != null) {
            mRippleEffectAnimator.cancel();
            mRippleEffectAnimator = null;
        }
    }

    /**
     * Returns animated value.
     *
     * @param index Index of animated value.
     * @return  An int.
     */
    public int getAnimatedValue(int index) {
        switch (mType) {
            case NONE:
            case BLINKING:
            case SHINY:
            case AURA:
                return mAlphaOpacityList.get(index);

            case RIPPLE:
                return mAlphaOpacityList.get(index);

            default:
                Log.e(getClass().getName(), "Unknown OpacityAnimationType! Animated value returned -1, should be in range [0..255]!");
                return -1;
        }
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
            mAlphaOpacityList.add(mInitialOpacity);
        }

        for (int i = 0; i < mAnimatorCount; ++ i) {
            final ValueAnimator opacityAnimator = new ValueAnimator();

            opacityAnimator.setDuration(getDuration());
//                opacityAnimator.setStartDelay(ANIMATION_START_DELAY);
            opacityAnimator.setRepeatCount(ValueAnimator.INFINITE);
            opacityAnimator.setRepeatMode(ValueAnimator.RESTART);

            switch (mType) {
                case OpacityAnimation.NONE:
                    // no need to init anything
                    return;

                case OpacityAnimation.BLINKING:
                    initBlinkingAnimators(i, opacityAnimator);
                    break;

                case OpacityAnimation.SHINY:
                    initShinyAnimators(i, opacityAnimator);
                    break;

                case OpacityAnimation.AURA:
                    initAuraAnimators(i, opacityAnimator);
                    break;

                case OpacityAnimation.RIPPLE:
                    initRippleAnimators(i, opacityAnimator);
                    // no need to go through loop in case of this animation
                    return;
            }

            mOpacityValueAnimatorList.add(opacityAnimator);
        }
    }

    private void initBlinkingAnimators(final int index, ValueAnimator opacityAnimator) {
        opacityAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        opacityAnimator.setIntValues(mInitialOpacity, mTargetOpacity, mInitialOpacity);
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

    private void initRippleAnimators(final int index, ValueAnimator opacityAnimator) {
        final float opacityRange = (float) mInitialOpacity - mTargetOpacity;
        final float funcXRange = mAnimatorCount / 2.f;
        final float pow = mAnimatorCount / 20.f;
        if (mRippleEffectAnimator == null) {
            mRippleEffectAnimator = new ValueAnimator();
        }
        mRippleEffectAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mRippleEffectAnimator.setRepeatMode(ValueAnimator.RESTART);
        mRippleEffectAnimator.setFloatValues((float) mAnimatorCount + funcXRange, -funcXRange);
        mRippleEffectAnimator.setDuration(getDuration());
        mRippleEffectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mRippleEffectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float rippleRadius = (float) animation.getAnimatedValue();
                for (int i = 0; i < mAlphaOpacityList.size(); ++i) {
                    final float xVal = (float) i - rippleRadius;
                    final float opacityArcFactor = (float) (1.f - Math.pow(xVal, pow) / Math.pow(funcXRange, pow));
                    int rippleOpacityValue = mTargetOpacity + Math.round(opacityRange * opacityArcFactor);
                    if (rippleOpacityValue > 255) {
                        rippleOpacityValue = 255;
                    } else if (rippleOpacityValue < mTargetOpacity) {
                        rippleOpacityValue = mTargetOpacity;
                    }
                    mAlphaOpacityList.set(i, rippleOpacityValue);
                }
            }
        });
    }

}
