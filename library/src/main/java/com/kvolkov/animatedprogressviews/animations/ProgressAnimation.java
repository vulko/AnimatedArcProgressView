package com.kvolkov.animatedprogressviews.animations;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * Progress animation types.
 *
 * @author Kirill Volkov (https://github.com/vulko).
 *         Copyright (C). All rights reserved.
 */
public class ProgressAnimation {

    /**
     * Progress animation types.
     */
    public static final int OPACITY_ANIMATION_TEST_STUB = -1;
    public static final int RACE_CONDITION = 0;
    public static final int SWIRLY = 1;
    public static final int WHIRPOOL = 2;
    public static final int HYPERLOOP = 3;
    public static final int METRONOME_1 = 4;
    public static final int METRONOME_2 = 5;
    public static final int METRONOME_3 = 6;
    public static final int METRONOME_4 = 7;
    public static final int BUTTERFLY_KNIFE = 8;
    public static final int RAINBOW = 9;
    public static final int GOTCHA = 10;

    /**
     * Animation durations in [ms] for animation types. See {@code ProgressAnimation}.
     */
    private static long sProgressAnimationDuration[] = {
            3000,
            6000,
            5000,
            5000,
            1000,
            1000,
            1000,
            1000,
            1000,
            1000,
            1500,
    };

    /**
     * Animation max beta angle initial values. See {@code ProgressAnimation}.
     */
    private static float sPeakBeta[] = {
            180.f,
            180.f,
            180.f,
            90.f,
            60.f,
            60.f,
            60.f,
            60.f,
            270.f,
            360.f,
            360.f,
    };

    /**
     * Animation initial beta angle initial values. See {@code ProgressAnimation}.
     */
    private static float sInitialBeta[] = {
            0.1f,
            0.1f,
            0.1f,
            0.1f,
            0.1f,
            0.1f,
            0.1f,
            0.1f,
            0.1f,
            0.1f,
            0.1f,
    };

    /**
     * Animation initial alpha angle initial values. See {@code ProgressAnimation}.
     */
    private static float sInitialAlpha[] = {
            270.f,
            270.f,
            270.f,
            270.f,
            270.f,
            270.f,
            270.f,
            270.f,
            270.f,
            270.f,
            270.f,
    };

    private int mType = 0;
    private int mAnimatorCount;

    /**
     * Animated values.
     */
    private List<Float> mAlphaAngleList = new ArrayList<>();
    private List<Float> mBetaAngleList = new ArrayList<>();
    private List<ValueAnimator> mAlphaValueAnimatorList = new ArrayList<>();
    private List<ValueAnimator> mBetaValueAnimatorList = new ArrayList<>();

    public ProgressAnimation(int i) {
        setType(i);
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
            case OPACITY_ANIMATION_TEST_STUB:
            case RACE_CONDITION:
            case SWIRLY:
            case WHIRPOOL:
            case HYPERLOOP:
            case METRONOME_1:
            case METRONOME_2:
            case METRONOME_3:
            case METRONOME_4:
            case BUTTERFLY_KNIFE:
            case RAINBOW:
            case GOTCHA:
                mType = value;
                break;

            default:
                mType = OPACITY_ANIMATION_TEST_STUB;
                Log.w(getClass().getName(), "Wrong opacity animation type set! Sticking with default value.");
        }
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
     * Get animation type.
     *
     * @return animation type
     */
    public int getType() {
        return mType;
    }

    /**
     * Call this to explicitly restart progress animation specified by {@code mType}.
     */
    public void restart() {
        stop();

        if (mType == ProgressAnimation.OPACITY_ANIMATION_TEST_STUB) {
            // ignore progress for testing animation effects
            return;
        }

        initAnimators();

        for (int i = 0; i < mAlphaValueAnimatorList.size(); ++i) {
            mAlphaValueAnimatorList.get(i).start();
        }
        for (int i = 0; i < mBetaValueAnimatorList.size(); ++i) {
            mBetaValueAnimatorList.get(i).start();
        }
    }

    /**
     * Call this to explicitly stop progress animation specified by {@code mType}.
     */
    public void stop() {
        Animator animator;
        if (mAlphaValueAnimatorList != null) {
            for (int i = 0; i < mAlphaValueAnimatorList.size(); ++i) {
                animator = mAlphaValueAnimatorList.get(i);
                if (animator != null) {
                    animator.cancel();
                }
            }
            mAlphaValueAnimatorList.clear();
        }
        if (mBetaValueAnimatorList != null) {
            for (int i = 0; i < mBetaValueAnimatorList.size(); ++i) {
                animator = mBetaValueAnimatorList.get(i);
                if (animator != null) {
                    animator.cancel();
                }
            }
            mBetaValueAnimatorList.clear();
        }
    }

    /**
     * Returns initial alpha angle value.
     *
     * @return  A float.
     */
    public float getInitialAlphaValue() {
        return sInitialAlpha[mType];
    }

    /**
     * Returns animated alpha angle value.
     *
     * @param index Index of animated value.
     * @return  A float.
     */
    public float getAlphaAnimatedValue(int index) {
        return mAlphaAngleList.get(index);
    }

    /**
     * Returns animated beta angle value.
     *
     * @param index Index of animated value.
     * @return  A float.
     */
    public float getBetaAnimatedValue(int index) {
        return mBetaAngleList.get(index);
    }

    /**
     * Init arc progress animators.
     */
    protected void initAnimators() {
        mAlphaAngleList.clear();
        mBetaAngleList.clear();
        for (int i = 0; i < mAnimatorCount; ++i) {
            mAlphaAngleList.add(sInitialAlpha[mType]);
            mBetaAngleList.add(sInitialBeta[mType]);
        }

        for (int i = 0; i < mAnimatorCount; ++ i) {
            final ValueAnimator alphaAnimator = new ValueAnimator();
            final ValueAnimator betaAnimator = new ValueAnimator();

            alphaAnimator.setDuration(sProgressAnimationDuration[mType]);
//            alphaAnimator.setStartDelay(ANIMATION_START_DELAY);
            alphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
            alphaAnimator.setRepeatMode(ValueAnimator.RESTART);

            betaAnimator.setDuration(sProgressAnimationDuration[mType]);
//            betaAnimator.setStartDelay(ANIMATION_START_DELAY);
            betaAnimator.setRepeatCount(ValueAnimator.INFINITE);
            betaAnimator.setRepeatMode(ValueAnimator.RESTART);

            switch (mType) {
                case ProgressAnimation.OPACITY_ANIMATION_TEST_STUB:
                case ProgressAnimation.RACE_CONDITION:
                    initRaceConditionAnimators(i, alphaAnimator, betaAnimator);
                    break;

                case ProgressAnimation.SWIRLY:
                    initSwirlyAnimators(i, alphaAnimator, betaAnimator);
                    break;

                case ProgressAnimation.WHIRPOOL:
                    initWhirpoolAnimators(i, alphaAnimator, betaAnimator);
                    break;

                case ProgressAnimation.HYPERLOOP:
                    initHyperloopAnimators(i, alphaAnimator, betaAnimator);
                    break;

                case ProgressAnimation.METRONOME_1:
                case ProgressAnimation.METRONOME_2:
                    initMetronome12Animators(i, alphaAnimator, betaAnimator);
                    break;

                case ProgressAnimation.METRONOME_3:
                case ProgressAnimation.METRONOME_4:
                    initMetronome34Animators(i, alphaAnimator, betaAnimator);
                    break;

                case ProgressAnimation.BUTTERFLY_KNIFE:
                    initButterflyKnifeAnimators(i, alphaAnimator, betaAnimator);
                    break;

                case ProgressAnimation.RAINBOW:
                case ProgressAnimation.GOTCHA:
                    initRainbowOrGotchaAnimators(i, alphaAnimator, betaAnimator);
                    break;
            }

            mAlphaValueAnimatorList.add(alphaAnimator);
            mBetaValueAnimatorList.add(betaAnimator);
        }
    }

    /**
     * Progress Animation helper functions. TODO: move outside of view to some AnimationHelper.
     */
    private void initRaceConditionAnimators(final int index, ValueAnimator alphaAnimator, ValueAnimator betaAnimator) {
        final float factor = 0.05f * (index + 1);

        final float randomAlpha = 360.f;
        alphaAnimator.setFloatValues(sInitialAlpha[mType],
                randomAlpha + sInitialAlpha[mType],
                randomAlpha * 2.f + sInitialAlpha[mType],
                randomAlpha * 3.f + sInitialAlpha[mType]);
        final float alphaDecelerateFactor = (index % 2 == 0) ? 1.f + factor : 1.f - factor;
        alphaAnimator.setInterpolator(new DecelerateInterpolator(alphaDecelerateFactor));
        alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float newAngle = (float) animation.getAnimatedValue();
                if (Math.abs(mAlphaAngleList.get(index) - newAngle) >= 0.5f) {
                    mAlphaAngleList.set(index, newAngle);
                }
            }
        });

        betaAnimator.setFloatValues(sInitialBeta[mType], sPeakBeta[mType], sInitialBeta[mType]);
        final float betaDecelerateFactor = (index % 2 == 0) ? 1.f + factor : 1.f - factor;
        betaAnimator.setInterpolator(new DecelerateInterpolator(betaDecelerateFactor));
        betaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float newAngle = (float) animation.getAnimatedValue();
                if (Math.abs(mBetaAngleList.get(index) - newAngle) >= 0.1f) {
                    mBetaAngleList.set(index, newAngle);
                }
            }
        });
    }

    private void initSwirlyAnimators(final int index, ValueAnimator alphaAnimator, ValueAnimator betaAnimator) {
        alphaAnimator.setRepeatMode(ValueAnimator.REVERSE);
        betaAnimator.setRepeatMode(ValueAnimator.REVERSE);

        final float randomAlpha = 360.f;
        alphaAnimator.setFloatValues(sInitialAlpha[mType],
                randomAlpha + sInitialAlpha[mType],
                randomAlpha * 2.f + sInitialAlpha[mType],
                randomAlpha * 3.f + sInitialAlpha[mType],
                randomAlpha * 3.5f + sInitialAlpha[mType]);
        final float alphaDecelerateFactor = 1.f - 0.05f * index;
        alphaAnimator.setInterpolator(new DecelerateInterpolator(alphaDecelerateFactor));
        alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float newAngle = (float) animation.getAnimatedValue();
                if (Math.abs(mAlphaAngleList.get(index) - newAngle) >= 1.f) {
                    mAlphaAngleList.set(index, newAngle);
                }
            }
        });

        betaAnimator.setFloatValues(sInitialBeta[mType], sPeakBeta[mType], sInitialBeta[mType]);
        final float betaDecelerateFactor = 1.f + 0.05f * index;
        betaAnimator.setInterpolator(new DecelerateInterpolator(betaDecelerateFactor));
        betaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float newAngle = (float) animation.getAnimatedValue();
                if (Math.abs(mBetaAngleList.get(index) - newAngle) >= 0.1f) {
                    mBetaAngleList.set(index, newAngle);
                }
            }
        });
    }

    private void initHyperloopAnimators(final int index, ValueAnimator alphaAnimator, ValueAnimator betaAnimator) {
        alphaAnimator.setRepeatMode(ValueAnimator.RESTART);
        betaAnimator.setRepeatMode(ValueAnimator.REVERSE);

        final float arcCountScaleFactor = 5.f / mAnimatorCount;
        final float randomAlpha = 360.f;
        alphaAnimator.setFloatValues(sInitialAlpha[mType],
                randomAlpha - sInitialAlpha[mType],
                randomAlpha * 2.f + sInitialAlpha[mType],
                randomAlpha * 3.f - sInitialAlpha[mType],
                randomAlpha * 4.f + sInitialAlpha[mType]);
        final float alphaAccelerateFactor = 1.f - arcCountScaleFactor * 0.1f * (index + 1.f) * (index + 1.f);
        alphaAnimator.setInterpolator(new AccelerateInterpolator(alphaAccelerateFactor));
        alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float newAngle = (float) animation.getAnimatedValue();
                if (Math.abs(mAlphaAngleList.get(index) - newAngle) >= 0.5f) {
                    mAlphaAngleList.set(index, newAngle);
                }
            }
        });

        betaAnimator.setFloatValues(sInitialBeta[mType], sPeakBeta[mType], sInitialBeta[mType]);
        final float betaAccelerateFactor = 1.f - arcCountScaleFactor *  0.1f * ( (float) (index + 1) * (index + 1) );
        final float hyperloop = 1.f + 0.01f * (index + 1.f);
        betaAnimator.setInterpolator(new AccelerateInterpolator(betaAccelerateFactor));
        betaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float newAngle = (float) animation.getAnimatedValue();
                if (Math.abs(mBetaAngleList.get(index) - newAngle) >= 0.1f) {
                    mBetaAngleList.set(index, newAngle * hyperloop);
                }
            }
        });
    }

    private void initWhirpoolAnimators(final int index, ValueAnimator alphaAnimator, ValueAnimator betaAnimator) {
        final float randomAlpha = 360.f;
        alphaAnimator.setFloatValues(sInitialAlpha[mType],
                randomAlpha + sInitialAlpha[mType],
                randomAlpha * 2.f + sInitialAlpha[mType],
                randomAlpha * 3.f + sInitialAlpha[mType],
                randomAlpha * 4.f + sInitialAlpha[mType],
                randomAlpha * 5.f + sInitialAlpha[mType],
                randomAlpha * 6.f + sInitialAlpha[mType]);
        final float alphaDecelerateFactor = 1.f + 0.1f * (index + 1);
        alphaAnimator.setInterpolator(new DecelerateInterpolator(alphaDecelerateFactor));
        alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float newAngle = (float) animation.getAnimatedValue();
                if (Math.abs(mAlphaAngleList.get(index) - newAngle) >= 0.5f) {
                    mAlphaAngleList.set(index, newAngle);
                }
            }
        });

        betaAnimator.setFloatValues(sInitialBeta[mType], sPeakBeta[mType], sInitialBeta[mType]);
        final float betaDecelerateFactor = 1.f - 0.05f * (index + 1);
        betaAnimator.setInterpolator(new DecelerateInterpolator(betaDecelerateFactor));
        betaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float newAngle = (float) animation.getAnimatedValue();
                if (Math.abs(mBetaAngleList.get(index) - newAngle) >= 0.1f) {
                    mBetaAngleList.set(index, newAngle);
                }
            }
        });
    }

    private void initMetronome12Animators(final int index, ValueAnimator alphaAnimator, ValueAnimator betaAnimator) {
        alphaAnimator.setRepeatMode(ValueAnimator.REVERSE);
        betaAnimator.setRepeatMode(ValueAnimator.REVERSE);

        final float alphaDecelerateFactor = 1.f + 0.1f * ( index * index );

        if (mType == ProgressAnimation.METRONOME_1) {
            alphaAnimator.setFloatValues(-0.f, 0.f);
            alphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        } else if (mType == ProgressAnimation.METRONOME_2) {
            final float randomAlpha = 5.f;
            alphaAnimator.setFloatValues(-randomAlpha, randomAlpha, -randomAlpha);
            alphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        }
        alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float newAngle = (float) animation.getAnimatedValue();
                if (Math.abs(mAlphaAngleList.get(index) - newAngle) >= 1.f) {
                    mAlphaAngleList.set(index, newAngle);
                }
            }
        });

        betaAnimator.setFloatValues(sPeakBeta[mType], -sPeakBeta[mType]);
        final float betaDecelerateFactor = 1.f + 0.05f * ( index * index );
        betaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        betaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float newAngle = (float) animation.getAnimatedValue();
                if (Math.abs(mBetaAngleList.get(index) - newAngle) >= 0.5f) {
                    mBetaAngleList.set(index, newAngle);
                }
            }
        });
    }

    private void initMetronome34Animators(final int index, ValueAnimator alphaAnimator, ValueAnimator betaAnimator) {
        alphaAnimator.setRepeatMode(ValueAnimator.REVERSE);
        betaAnimator.setRepeatMode(ValueAnimator.REVERSE);
        final float alphaDecelerateFactor = 1.f + 0.05f * ( index * index );

        if (mType == ProgressAnimation.METRONOME_3) {
            final float slownessDegree = 10.f;
            alphaAnimator.setFloatValues(slownessDegree, sInitialBeta[mType], slownessDegree, sInitialBeta[mType], slownessDegree);
            alphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        } else if (mType == ProgressAnimation.METRONOME_4) {
            final float slownessDegree = 20.f;
            alphaAnimator.setFloatValues(sInitialBeta[mType], slownessDegree, sInitialBeta[mType]);
            alphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        }
        alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float newAngle = (float) animation.getAnimatedValue();
                mAlphaAngleList.set(index, newAngle);
            }
        });

        betaAnimator.setFloatValues(sPeakBeta[mType], -sPeakBeta[mType]);
        final float betaDecelerateFactor = 1.f + 0.05f * ( index * index );
        betaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        betaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float newAngle = (float) animation.getAnimatedValue();
                mBetaAngleList.set(index, newAngle);

                // redraw only once at the end of for loop
            }
        });
    }

    private void initButterflyKnifeAnimators(final int index, ValueAnimator alphaAnimator, ValueAnimator betaAnimator) {
        alphaAnimator.setRepeatMode(ValueAnimator.REVERSE);
        betaAnimator.setRepeatMode(ValueAnimator.REVERSE);
        final float alphaDecelerateFactor = 1.f + 0.05f * ( index * index );

        final float slownessDegree = 20.f;
        alphaAnimator.setFloatValues(sInitialBeta[mType], slownessDegree, sInitialBeta[mType]);
        alphaAnimator.setInterpolator(new AccelerateInterpolator(alphaDecelerateFactor));
        alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float newAngle = (float) animation.getAnimatedValue();
                mAlphaAngleList.set(index, newAngle);
            }
        });

        betaAnimator.setFloatValues(sPeakBeta[mType], -sPeakBeta[mType]);
        final float betaDecelerateFactor = 1.f + 0.05f * ( index * index );
        betaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        betaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float newAngle = (float) animation.getAnimatedValue();
                mBetaAngleList.set(index, newAngle);

                // redraw only once at the end of for loop
            }
        });
    }

    private void initRainbowOrGotchaAnimators(final int index, ValueAnimator alphaAnimator, ValueAnimator betaAnimator) {
        alphaAnimator.setRepeatMode(ValueAnimator.REVERSE);
        final float slownessDegree;
        if (getType() == ProgressAnimation.GOTCHA) {
            slownessDegree = 360.f;
        } else {
            slownessDegree = 180.f;
        }
        alphaAnimator.setFloatValues(sInitialBeta[mType], slownessDegree);
        alphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float newAngle = (float) animation.getAnimatedValue();
                mAlphaAngleList.set(index, newAngle);
            }
        });

        betaAnimator.setFloatValues(0.f, sPeakBeta[mType]);
        betaAnimator.setInterpolator(new LinearInterpolator());
        betaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float newAngle = (float) animation.getAnimatedValue();
                mBetaAngleList.set(index, newAngle);

                // redraw only once at the end of for loop
            }
        });
    }


}
