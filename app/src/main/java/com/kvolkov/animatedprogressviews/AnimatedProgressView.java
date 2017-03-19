package com.kvolkov.animatedprogressviews;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an animated progress view.
 *
 * @author Kirill Volkov (https://github.com/vulko).
 *         Copyright (C). All rights reserved.
 */
public class AnimatedProgressView extends View {

    /**
     * Drawing consts.
     */
    protected static final int ARC_COUNT = 5;
    protected static final long ANIMATION_START_DELAY = 0;

    /**
     * Opacity animation consts.
     */
    protected static int INITIAL_OPACITY = 255;
    protected static int TARGET_OPACITY = 50;

    @IntDef({ProgressAnimationType.RACE_CONDITION,
            ProgressAnimationType.SWIRLY,
            ProgressAnimationType.WHIRPOOL,
            ProgressAnimationType.HYPERLOOP,
            ProgressAnimationType.METRONOME_1,
            ProgressAnimationType.METRONOME_2,
            ProgressAnimationType.METRONOME_3,
            ProgressAnimationType.METRONOME_4,
            ProgressAnimationType.BUTTERFLY_KNIFE,
            ProgressAnimationType.RAINBOW,
            ProgressAnimationType.GOTCHA,
    })
    public @interface ProgressAnimationType {
        int RACE_CONDITION = 0;
        int SWIRLY = 1;
        int WHIRPOOL = 2;
        int HYPERLOOP = 3;
        int METRONOME_1 = 4;
        int METRONOME_2 = 5;
        int METRONOME_3 = 6;
        int METRONOME_4 = 7;
        int BUTTERFLY_KNIFE = 8;
        int RAINBOW = 9;
        int GOTCHA = 10;
    }

    @IntDef({OpacityAnimationType.NONE,
            OpacityAnimationType.BLINKING,
            OpacityAnimationType.SHINY,
    })
    public @interface OpacityAnimationType {
        int NONE = 0;
        int BLINKING = 1;
        int SHINY = 2;
    }

    /**
     * Animation durations in [ms] for animation types. See {@link ProgressAnimationType}.
     */
    protected static long sANIMATION_DURATION[] = {
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
            3000,
    };

    /**
     * Animation durations in [ms] for animation types. See {@link ProgressAnimationType}.
     */
    protected static float sPEAK_BETA[] = {
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
     * Animation durations in [ms] for animation types. See {@link ProgressAnimationType}.
     */
    protected static float sINITIAL_BETA[] = {
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
     * Animation durations in [ms] for animation types. See {@link ProgressAnimationType}.
     */
    protected static float sINITIAL_ALPHA[] = {
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

    protected @ProgressAnimationType
    int mProgressAnimationType = ProgressAnimationType.RACE_CONDITION;

    protected @AnimatedProgressView.OpacityAnimationType
    int mOpacityAnimationType = OpacityAnimationType.NONE;

    protected Paint mArcPaint;
    protected List<Integer> mColorList = new ArrayList<>();
    protected List<Integer> mAlphaOpacityList = new ArrayList<>();
    protected List<RectF> mArcRectList = new ArrayList<>();
    protected List<Float> mAlphaAngleList = new ArrayList<>();
    protected List<Float> mBetaAngleList = new ArrayList<>();

    protected List<ValueAnimator> mAlphaValueAnimatorList = new ArrayList<>();
    protected List<ValueAnimator> mBetaValueAnimatorList = new ArrayList<>();
    protected List<ValueAnimator> mOpacityValueAnimatorList = new ArrayList<>();

    protected Handler mHandler = new Handler();

    public AnimatedProgressView(Context context) {
        super(context);

        initView();
    }

    public AnimatedProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initView();
    }

    public AnimatedProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AnimatedProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        initView();
    }

    protected void initView() {
        if (mArcPaint == null) {
            mArcPaint = new Paint();
        }
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setColor(Color.argb(100, 0, 0, 0));
        mArcPaint.setStrokeCap(Paint.Cap.ROUND);
        mArcPaint.setStrokeWidth(30);
        mArcPaint.setAntiAlias(true);

        // TODO: dirty init colors.
        mColorList.add(Color.argb(255, 0, 0, 200));
        mColorList.add(Color.argb(255, 0, 0, 200));
        mColorList.add(Color.argb(255, 0, 0, 200));
        mColorList.add(Color.argb(255, 0, 0, 200));
        mColorList.add(Color.argb(255, 0, 0, 200));
//        mColorList.add(Color.argb(255, 0, 200, 0));
//        mColorList.add(Color.argb(255, 200, 0, 0));
//        mColorList.add(Color.argb(255, 0, 200, 0));
//        mColorList.add(Color.argb(255, 200, 0, 0));
    }

    protected void initProgressValueAnimators() {
        mAlphaAngleList.clear();
        mBetaAngleList.clear();
        for (int i = 0; i < ARC_COUNT; ++i) {
            mAlphaAngleList.add(sINITIAL_ALPHA[mProgressAnimationType]);
            mBetaAngleList.add(sINITIAL_BETA[mProgressAnimationType]);
        }

        for (int i = 0; i < ARC_COUNT; ++ i) {
            final ValueAnimator alphaAnimator = new ValueAnimator();
            final ValueAnimator betaAnimator = new ValueAnimator();

            alphaAnimator.setDuration(sANIMATION_DURATION[mProgressAnimationType]);
            alphaAnimator.setStartDelay(ANIMATION_START_DELAY);
            alphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
            alphaAnimator.setRepeatMode(ValueAnimator.RESTART);

            betaAnimator.setDuration(sANIMATION_DURATION[mProgressAnimationType]);
            betaAnimator.setStartDelay(ANIMATION_START_DELAY);
            betaAnimator.setRepeatCount(ValueAnimator.INFINITE);
            betaAnimator.setRepeatMode(ValueAnimator.RESTART);

            switch (mProgressAnimationType) {
                case ProgressAnimationType.RACE_CONDITION:
                    initRaceConditionAnimators(i, alphaAnimator, betaAnimator);
                    break;

                case ProgressAnimationType.SWIRLY:
                    initSwirlyAnimators(i, alphaAnimator, betaAnimator);
                    break;

                case ProgressAnimationType.WHIRPOOL:
                    initWhirpoolAnimators(i, alphaAnimator, betaAnimator);
                    break;

                case ProgressAnimationType.HYPERLOOP:
                    initHyperloopAnimators(i, alphaAnimator, betaAnimator);
                    break;

                case ProgressAnimationType.METRONOME_1:
                case ProgressAnimationType.METRONOME_2:
                    initMetronome12Animators(i, alphaAnimator, betaAnimator);
                    break;

                case ProgressAnimationType.METRONOME_3:
                case ProgressAnimationType.METRONOME_4:
                    initMetronome34Animators(i, alphaAnimator, betaAnimator);
                    break;

                case ProgressAnimationType.BUTTERFLY_KNIFE:
                    initButterflyKnifeAnimators(i, alphaAnimator, betaAnimator);
                    break;

                case ProgressAnimationType.RAINBOW:
                case ProgressAnimationType.GOTCHA:
                    initRainbowOrGotchaAnimators(i, alphaAnimator, betaAnimator);
                    break;
            }

            mAlphaValueAnimatorList.add(alphaAnimator);
            mBetaValueAnimatorList.add(betaAnimator);
        }
    }

    protected void initOpacityAnimators() {
        mAlphaOpacityList.clear();
        for (int i = 0; i < ARC_COUNT; ++i) {
            mAlphaOpacityList.add(INITIAL_OPACITY);
        }

        if (mOpacityAnimationType != OpacityAnimationType.NONE) {

            for (int i = 0; i < ARC_COUNT; ++ i) {
                final ValueAnimator opacityAnimator = new ValueAnimator();

                opacityAnimator.setDuration(sANIMATION_DURATION[mProgressAnimationType]);
                opacityAnimator.setStartDelay(ANIMATION_START_DELAY);
                opacityAnimator.setRepeatCount(ValueAnimator.INFINITE);
                opacityAnimator.setRepeatMode(ValueAnimator.RESTART);

                switch (mOpacityAnimationType) {
                    case OpacityAnimationType.BLINKING:
                        initBlinkingAnimators(i, opacityAnimator);
                        break;

                    case OpacityAnimationType.SHINY:
                        initShinyAnimators(i, opacityAnimator);
                        break;
                }

                mOpacityValueAnimatorList.add(opacityAnimator);
            }
        }
    }

    /**
     * Add color to color list.
     *
     * @param colorInt  A {@link ColorInt}.
     *
     * @throws IllegalStateException In case of adding more colors, then there are arcs. See {@link #ARC_COUNT}.
     */
    public void addColor(@ColorInt int colorInt) throws IllegalStateException {
        mColorList.add(colorInt);

        if (mColorList.size() > ARC_COUNT) {
            throw new IllegalStateException("can't add more colors as there are arcs!");
        }
    }

    public void startOpacityAnimation() {
        stopOpacityAnimation();
        initOpacityAnimators();

        for (int i = 0; i < mOpacityValueAnimatorList.size(); ++i) {
            mOpacityValueAnimatorList.get(i).start();
        }
    }

    public void stopOpacityAnimation() {
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

    public void startProgressAnimation() {
        stopAnimation();
        initProgressValueAnimators();

        for (int i = 0; i < mAlphaValueAnimatorList.size(); ++i) {
            mAlphaValueAnimatorList.get(i).start();
        }
        for (int i = 0; i < mBetaValueAnimatorList.size(); ++i) {
            mBetaValueAnimatorList.get(i).start();
        }

        mHandler.postDelayed(mUpdateRunnable, 16);
    }

    // TODO REmove
    private Runnable mUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(mUpdateRunnable, 16);
            invalidate();
        }
    };
    // TODO REmove

    public void stopAnimation() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mUpdateRunnable);
        }

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

    public void setAnimationType(@ProgressAnimationType int animationType) {
        mProgressAnimationType = animationType;

        startProgressAnimation();
        startOpacityAnimation();
    }

    public void setOpacityAnimationType(@OpacityAnimationType int animationType) {
        mOpacityAnimationType = animationType;

        startProgressAnimation();
        startOpacityAnimation();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(widthSize, heightSize);

        final int prefferedDimension = Math.min(widthSize, heightSize);

        mArcRectList.clear();
        for (int i = 0; i < ARC_COUNT; ++i) {
            final float factor = (i + 1) * 40.f;
            mArcRectList.add(new RectF(0.f + factor, 0.f + factor, prefferedDimension - factor, prefferedDimension - factor));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < ARC_COUNT; ++i) {
            canvas.save();

            mArcPaint.setColor(mColorList.get(i));
            mArcPaint.setAlpha(mAlphaOpacityList.get(i));
            switch (mProgressAnimationType) {
                case ProgressAnimationType.RACE_CONDITION:
                    canvas.drawArc(mArcRectList.get(i), mAlphaAngleList.get(i), mBetaAngleList.get(i), false, mArcPaint);
                    break;

                case ProgressAnimationType.SWIRLY:
                case ProgressAnimationType.WHIRPOOL:
                case ProgressAnimationType.HYPERLOOP:
                    canvas.drawArc(mArcRectList.get(i), mAlphaAngleList.get(i), mBetaAngleList.get(i), false, mArcPaint);
                    break;

                case ProgressAnimationType.METRONOME_1:
                case ProgressAnimationType.METRONOME_2:
                    canvas.drawArc(mArcRectList.get(i), sINITIAL_ALPHA[mProgressAnimationType] + mAlphaAngleList.get(i), mBetaAngleList.get(i), false, mArcPaint);
                    break;

                case ProgressAnimationType.METRONOME_3:
                case ProgressAnimationType.METRONOME_4:
                    canvas.drawArc(mArcRectList.get(i), sINITIAL_ALPHA[mProgressAnimationType] - mBetaAngleList.get(i), mAlphaAngleList.get(i), false, mArcPaint);
                    break;

                case ProgressAnimationType.BUTTERFLY_KNIFE:
                    canvas.drawArc(mArcRectList.get(i), sINITIAL_ALPHA[mProgressAnimationType] - mBetaAngleList.get(i), mAlphaAngleList.get(i), false, mArcPaint);
                    break;

                case ProgressAnimationType.RAINBOW:
                case ProgressAnimationType.GOTCHA:
                    canvas.drawArc(mArcRectList.get(i), sINITIAL_ALPHA[mProgressAnimationType] + mBetaAngleList.get(i), -mAlphaAngleList.get(i), false, mArcPaint);
                    break;
            }

            canvas.restore();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        // TODO: manage mem stub
        stopAnimation();
        stopOpacityAnimation();
    }

    /**
     * Progress Animation helper functions.
     */
    private void initRaceConditionAnimators(final int index, ValueAnimator alphaAnimator, ValueAnimator betaAnimator) {
        final float factor = 0.05f * (index + 1);

        final float randomAlpha = 360.f;
        alphaAnimator.setFloatValues(sINITIAL_ALPHA[mProgressAnimationType],
                randomAlpha + sINITIAL_ALPHA[mProgressAnimationType],
                randomAlpha * 2.f + sINITIAL_ALPHA[mProgressAnimationType],
                randomAlpha * 3.f + sINITIAL_ALPHA[mProgressAnimationType]);
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

        betaAnimator.setFloatValues(sINITIAL_BETA[mProgressAnimationType], sPEAK_BETA[mProgressAnimationType], sINITIAL_BETA[mProgressAnimationType]);
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
        alphaAnimator.setFloatValues(sINITIAL_ALPHA[mProgressAnimationType],
                randomAlpha + sINITIAL_ALPHA[mProgressAnimationType],
                randomAlpha * 2.f + sINITIAL_ALPHA[mProgressAnimationType],
                randomAlpha * 3.f + sINITIAL_ALPHA[mProgressAnimationType],
                randomAlpha * 3.5f + sINITIAL_ALPHA[mProgressAnimationType]);
        final float alphaDecelerateFactor = 1.f - 0.05f * ( index * index );
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

        betaAnimator.setFloatValues(sINITIAL_BETA[mProgressAnimationType], sPEAK_BETA[mProgressAnimationType], sINITIAL_BETA[mProgressAnimationType]);
        final float betaDecelerateFactor = 1.f + 0.05f * ( index * index );
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
        alphaAnimator.setRepeatMode(ValueAnimator.REVERSE);
        betaAnimator.setRepeatMode(ValueAnimator.REVERSE);

        final float randomAlpha = 360.f;
        alphaAnimator.setFloatValues(sINITIAL_ALPHA[mProgressAnimationType],
                randomAlpha - sINITIAL_ALPHA[mProgressAnimationType],
                randomAlpha * 2.f - sINITIAL_ALPHA[mProgressAnimationType],
                randomAlpha * 3.f - sINITIAL_ALPHA[mProgressAnimationType],
                randomAlpha * 4.f - sINITIAL_ALPHA[mProgressAnimationType]);
        final float alphaAccelerateFactor = 1.f - 0.1f * ( (float) (index + 1) * (index + 1) );
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

        betaAnimator.setFloatValues(sINITIAL_BETA[mProgressAnimationType], sPEAK_BETA[mProgressAnimationType], sINITIAL_BETA[mProgressAnimationType]);
        final float betaAccelerateFactor = 1.f - 0.1f * ( (float) (index + 1) * (index + 1) );
        final float hyperloop = 1.f + 0.01f * ( (float) (index + 1) * (index + 1) );
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
        alphaAnimator.setFloatValues(sINITIAL_ALPHA[mProgressAnimationType],
                randomAlpha + sINITIAL_ALPHA[mProgressAnimationType],
                randomAlpha * 2.f + sINITIAL_ALPHA[mProgressAnimationType],
                randomAlpha * 3.f + sINITIAL_ALPHA[mProgressAnimationType],
                randomAlpha * 4.f + sINITIAL_ALPHA[mProgressAnimationType],
                randomAlpha * 5.f + sINITIAL_ALPHA[mProgressAnimationType],
                randomAlpha * 6.f + sINITIAL_ALPHA[mProgressAnimationType]);
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

        betaAnimator.setFloatValues(sINITIAL_BETA[mProgressAnimationType], sPEAK_BETA[mProgressAnimationType], sINITIAL_BETA[mProgressAnimationType]);
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

        if (mProgressAnimationType == ProgressAnimationType.METRONOME_1) {
            alphaAnimator.setFloatValues(-0.f, 0.f);
            alphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        } else if (mProgressAnimationType == ProgressAnimationType.METRONOME_2) {
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

        betaAnimator.setFloatValues(sPEAK_BETA[mProgressAnimationType], -sPEAK_BETA[mProgressAnimationType]);
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

        if (mProgressAnimationType == ProgressAnimationType.METRONOME_3) {
            final float slownessDegree = 10.f;
            alphaAnimator.setFloatValues(slownessDegree, sINITIAL_BETA[mProgressAnimationType], slownessDegree, sINITIAL_BETA[mProgressAnimationType], slownessDegree);
            alphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        } else if (mProgressAnimationType == ProgressAnimationType.METRONOME_4) {
            final float slownessDegree = 20.f;
            alphaAnimator.setFloatValues(sINITIAL_BETA[mProgressAnimationType], slownessDegree, sINITIAL_BETA[mProgressAnimationType]);
            alphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        }
        alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float newAngle = (float) animation.getAnimatedValue();
                mAlphaAngleList.set(index, newAngle);
            }
        });

        betaAnimator.setFloatValues(sPEAK_BETA[mProgressAnimationType], -sPEAK_BETA[mProgressAnimationType]);
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
        alphaAnimator.setFloatValues(sINITIAL_BETA[mProgressAnimationType], slownessDegree, sINITIAL_BETA[mProgressAnimationType]);
        alphaAnimator.setInterpolator(new AccelerateInterpolator(alphaDecelerateFactor));
        alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float newAngle = (float) animation.getAnimatedValue();
                mAlphaAngleList.set(index, newAngle);
            }
        });

        betaAnimator.setFloatValues(sPEAK_BETA[mProgressAnimationType], -sPEAK_BETA[mProgressAnimationType]);
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
        if (mProgressAnimationType == ProgressAnimationType.GOTCHA) {
            slownessDegree = 360.f;
        } else {
            slownessDegree = 180.f;
        }
        alphaAnimator.setFloatValues(sINITIAL_BETA[mProgressAnimationType], slownessDegree);
        alphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float newAngle = (float) animation.getAnimatedValue();
                mAlphaAngleList.set(index, newAngle);
            }
        });

        betaAnimator.setFloatValues(0.f, sPEAK_BETA[mProgressAnimationType]);
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

}