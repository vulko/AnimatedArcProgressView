package com.kvolkov.animatedprogressviews;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This is an animated progress view.
 *
 * @author Kirill Volkov (https://github.com/vulko).
 *         Copyright (C). All rights reserved.
 */
public class AnimatedProgressView extends View {

    @IntDef({ AnimationType.RACE_CONDITION,
              AnimationType.SWIRLY,
              AnimationType.WHIRPOOL,
              AnimationType.HYPERLOOP,
            AnimationType.METRONOME_1,
            AnimationType.METRONOME_2,
            AnimationType.METRONOME_3,
            AnimationType.METRONOME_4,
            AnimationType.BUTTERFLY_KNIFE,
            AnimationType.RAINBOW,
    })
    public @interface AnimationType {
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
    }

    /**
     * Animation durations in [ms] for animation types. See {@link AnimationType}.
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
    };

    /**
     * Animation durations in [ms] for animation types. See {@link AnimationType}.
     */
    protected static float sPEAK_BETA[] = {
            180.f,
            180.f,
            90.f,
            90.f,
            60.f,
            60.f,
            60.f,
            60.f,
            270.f,
            360.f,
    };

    /**
     * Animation durations in [ms] for animation types. See {@link AnimationType}.
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
    };

    /**
     * Animation durations in [ms] for animation types. See {@link AnimationType}.
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
    };

    /**
     * Drawing consts.
     */
    protected static final int ARC_COUNT = 5;
    protected static final long ANIMATION_START_DELAY = 0;

    protected @AnimationType
    int mAnimationType = AnimationType.RACE_CONDITION;

    protected Paint mArcPaint;
    protected List<Integer> mColorList = new ArrayList<>();
    protected List<RectF> mArcRectList = new ArrayList<>();
    protected List<Float> mAlphaAngleList = new ArrayList<>();
    protected List<Float> mBetaAngleList = new ArrayList<>();
    protected List<ValueAnimator> mAlphaValueAnimatorList = new ArrayList<>();
    protected List<ValueAnimator> mBetaValueAnimatorList = new ArrayList<>();

    protected Timer mTimer = null;
    protected TimerTask mTimerTask;

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

        initAnimators();
    }

    protected void initAnimators() {
        mAlphaAngleList.clear();
        mBetaAngleList.clear();
        for (int i = 0; i < ARC_COUNT; ++i) {
            mAlphaAngleList.add(sINITIAL_ALPHA[mAnimationType]);
            mBetaAngleList.add(sINITIAL_BETA[mAnimationType]);
        }

        switch (mAnimationType) {
            case AnimationType.RACE_CONDITION:
                initRaceConditionAnimators();
                break;

            case AnimationType.SWIRLY:
                initSwirlyAnimators();
                break;

            case AnimationType.WHIRPOOL:
                initWhirpoolAnimators();
                break;

            case AnimationType.HYPERLOOP:
                initHyperloopAnimators();
                break;

            case AnimationType.METRONOME_1:
            case AnimationType.METRONOME_2:
                initMetronome12Animators();
                break;

            case AnimationType.METRONOME_3:
            case AnimationType.METRONOME_4:
                initMetronome34Animators();
                break;

            case AnimationType.BUTTERFLY_KNIFE:
                initButterflyKnifeAnimators();
                break;

            case AnimationType.RAINBOW:
                initRainbowAnimators();
                break;
        }
    }

    public void startAnimation() {
        stopAnimation();
        initAnimators();

        for (int i = 0; i < mAlphaValueAnimatorList.size(); ++i) {
            mAlphaValueAnimatorList.get(i).start();
        }
        for (int i = 0; i < mBetaValueAnimatorList.size(); ++i) {
            mBetaValueAnimatorList.get(i).start();
        }

        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    ((Activity)getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            invalidate();
                        }
                    });
                }
            };

            if (mTimer == null) {
                mTimer = new Timer();
            }
            mTimer.schedule(mTimerTask, 40);
        }
    }

    public void stopAnimation() {
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        if (mTimer != null) {
            mTimer.purge();
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

    public void setAnimationType(@AnimationType int animationType) {
        mAnimationType = animationType;

        // TODO: cancel old ones first
        startAnimation();
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

        switch (mAnimationType) {
            case AnimationType.RACE_CONDITION:
                canvas.save();
                for (int i = 0; i < ARC_COUNT; ++i) {
                    canvas.drawArc(mArcRectList.get(i), mAlphaAngleList.get(i), mBetaAngleList.get(i), false, mArcPaint);
                }
                canvas.restore();
                break;

            case AnimationType.SWIRLY:
            case AnimationType.WHIRPOOL:
            case AnimationType.HYPERLOOP:
                canvas.save();
                for (int i = 0; i < ARC_COUNT; ++i) {
                    canvas.drawArc(mArcRectList.get(i), mAlphaAngleList.get(i), mBetaAngleList.get(i), false, mArcPaint);
                }
                canvas.restore();
                break;

            case AnimationType.METRONOME_1:
            case AnimationType.METRONOME_2:
                canvas.save();
                for (int i = 0; i < ARC_COUNT; ++i) {
                    canvas.drawArc(mArcRectList.get(i), sINITIAL_ALPHA[mAnimationType] + mAlphaAngleList.get(i), mBetaAngleList.get(i), false, mArcPaint);
                }
                canvas.restore();
                break;

            case AnimationType.METRONOME_3:
            case AnimationType.METRONOME_4:
                canvas.save();
                for (int i = 0; i < ARC_COUNT; ++i) {
                    canvas.drawArc(mArcRectList.get(i), sINITIAL_ALPHA[mAnimationType] - mBetaAngleList.get(i), mAlphaAngleList.get(i), false, mArcPaint);
                }
                canvas.restore();
                break;

            case AnimationType.BUTTERFLY_KNIFE:
                canvas.save();
                for (int i = 0; i < ARC_COUNT; ++i) {
                    canvas.drawArc(mArcRectList.get(i), sINITIAL_ALPHA[mAnimationType] - mBetaAngleList.get(i), mAlphaAngleList.get(i), false, mArcPaint);
                }
                canvas.restore();
                break;

            case AnimationType.RAINBOW:
                canvas.save();
                for (int i = 0; i < ARC_COUNT; ++i) {
                    canvas.drawArc(mArcRectList.get(i), sINITIAL_ALPHA[mAnimationType] + mBetaAngleList.get(i), -mAlphaAngleList.get(i), false, mArcPaint);
                }
                canvas.restore();
                break;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        // TODO: manage mem stub
        if (mAlphaValueAnimatorList != null) {
            for (int i = 0; i < mAlphaValueAnimatorList.size(); ++i) {
                ValueAnimator animator = mAlphaValueAnimatorList.get(i);
                if (animator != null) {
                    animator.cancel();
                }
            }
            mAlphaValueAnimatorList = null;
        }
    }

    private void initRaceConditionAnimators() {
        for (int i = 0; i < ARC_COUNT; ++i) {
            final int index = i;
            final float factor = 0.05f * (i + 1);

            final ValueAnimator alphaAnimator = new ValueAnimator();
            final float randomAlpha = 360.f;
            alphaAnimator.setFloatValues(sINITIAL_ALPHA[mAnimationType],
                    randomAlpha + sINITIAL_ALPHA[mAnimationType],
                    randomAlpha * 2.f + sINITIAL_ALPHA[mAnimationType],
                    randomAlpha * 3.f + sINITIAL_ALPHA[mAnimationType]);
            alphaAnimator.setDuration(sANIMATION_DURATION[mAnimationType]);
            alphaAnimator.setStartDelay(ANIMATION_START_DELAY);
            alphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
            alphaAnimator.setRepeatMode(ValueAnimator.RESTART);
            final float alphaDecelerateFactor = (i % 2 == 0) ? 1.f + factor : 1.f - factor;
            alphaAnimator.setInterpolator(new DecelerateInterpolator(alphaDecelerateFactor));
            alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float newAngle = (float) animation.getAnimatedValue();
                    if (Math.abs(mAlphaAngleList.get(index) - newAngle) >= 0.5f) {
                        mAlphaAngleList.set(index, newAngle);
                        if (index == (ARC_COUNT - 1)) invalidate();
                    }
                }
            });

            final ValueAnimator betaAnimator = new ValueAnimator();
            betaAnimator.setFloatValues(sINITIAL_BETA[mAnimationType], sPEAK_BETA[mAnimationType], sINITIAL_BETA[mAnimationType]);
            betaAnimator.setDuration(sANIMATION_DURATION[mAnimationType]);
            betaAnimator.setRepeatMode(ValueAnimator.RESTART);
            betaAnimator.setRepeatCount(ValueAnimator.INFINITE);
            final float betaDecelerateFactor = (i % 2 == 0) ? 1.f + factor : 1.f - factor;
            betaAnimator.setInterpolator(new DecelerateInterpolator(betaDecelerateFactor));
            betaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float newAngle = (float) animation.getAnimatedValue();
                    if (Math.abs(mBetaAngleList.get(index) - newAngle) >= 0.1f) {
                        mBetaAngleList.set(index, newAngle);
                        if (index == (ARC_COUNT - 1)) invalidate();
                    }
                }
            });

            mAlphaValueAnimatorList.add(alphaAnimator);
            mBetaValueAnimatorList.add(betaAnimator);
        }
    }

    private void initSwirlyAnimators() {
        for (int i = 0; i < ARC_COUNT; ++ i) {
            final int index = i;

            final ValueAnimator alphaAnimator = new ValueAnimator();
            final float randomAlpha = 360.f;
            alphaAnimator.setFloatValues(sINITIAL_ALPHA[mAnimationType],
                    randomAlpha + sINITIAL_ALPHA[mAnimationType],
                    randomAlpha * 2.f + sINITIAL_ALPHA[mAnimationType],
                    randomAlpha * 3.f + sINITIAL_ALPHA[mAnimationType],
                    randomAlpha * 3.5f + sINITIAL_ALPHA[mAnimationType]);
            alphaAnimator.setDuration(sANIMATION_DURATION[mAnimationType]);
            alphaAnimator.setStartDelay(ANIMATION_START_DELAY);
            alphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
            alphaAnimator.setRepeatMode(ValueAnimator.REVERSE);
            final float alphaDecelerateFactor = 1.f - 0.05f * ( index * index );
            alphaAnimator.setInterpolator(new DecelerateInterpolator(alphaDecelerateFactor));
            alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float newAngle = (float) animation.getAnimatedValue();
                    if (Math.abs(mAlphaAngleList.get(index) - newAngle) >= 1.f) {
                        mAlphaAngleList.set(index, newAngle);
                        if (index == (ARC_COUNT - 1)) invalidate();
                    }
                }
            });

            final ValueAnimator betaAnimator = new ValueAnimator();
            betaAnimator.setFloatValues(sINITIAL_BETA[mAnimationType], sPEAK_BETA[mAnimationType], sINITIAL_BETA[mAnimationType]);
            betaAnimator.setDuration(sANIMATION_DURATION[mAnimationType]);
            betaAnimator.setRepeatMode(ValueAnimator.RESTART);
            betaAnimator.setRepeatCount(ValueAnimator.INFINITE);
            final float betaDecelerateFactor = 1.f + 0.05f * ( index * index );
            betaAnimator.setInterpolator(new DecelerateInterpolator(betaDecelerateFactor));
            betaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float newAngle = (float) animation.getAnimatedValue();
                    if (Math.abs(mBetaAngleList.get(index) - newAngle) >= 0.1f) {
                        mBetaAngleList.set(index, newAngle);
                        if (index == (ARC_COUNT - 1)) invalidate();
                    }
                }
            });

            mAlphaValueAnimatorList.add(alphaAnimator);
            mBetaValueAnimatorList.add(betaAnimator);
        }
    }

    private void initHyperloopAnimators() {
        for (int i = 0; i < ARC_COUNT; ++ i) {
            final int index = i;

            final ValueAnimator alphaAnimator = new ValueAnimator();
            final float randomAlpha = 360.f;
            alphaAnimator.setFloatValues(sINITIAL_ALPHA[mAnimationType],
                    randomAlpha - sINITIAL_ALPHA[mAnimationType],
                    randomAlpha * 2.f - sINITIAL_ALPHA[mAnimationType],
                    randomAlpha * 3.f - sINITIAL_ALPHA[mAnimationType],
                    randomAlpha * 4.f - sINITIAL_ALPHA[mAnimationType]);
            alphaAnimator.setDuration(sANIMATION_DURATION[mAnimationType]);
            alphaAnimator.setStartDelay(ANIMATION_START_DELAY);
            alphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
            alphaAnimator.setRepeatMode(ValueAnimator.RESTART);
            final float alphaDecelerateFactor = 1.f - 0.1f * ( (float) (i + 1) * (i + 1) );
            alphaAnimator.setInterpolator(new AccelerateInterpolator(alphaDecelerateFactor));
            alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float newAngle = (float) animation.getAnimatedValue();
                    if (Math.abs(mAlphaAngleList.get(index) - newAngle) >= 0.5f) {
                        mAlphaAngleList.set(index, newAngle);
                        if (index == (ARC_COUNT - 1)) invalidate();
                    }
                }
            });

            final ValueAnimator betaAnimator = new ValueAnimator();
            betaAnimator.setFloatValues(sINITIAL_BETA[mAnimationType], sPEAK_BETA[mAnimationType], sINITIAL_BETA[mAnimationType]);
            betaAnimator.setDuration(sANIMATION_DURATION[mAnimationType]);
            betaAnimator.setRepeatMode(ValueAnimator.RESTART);
            betaAnimator.setRepeatCount(ValueAnimator.INFINITE);
            final float betaDecelerateFactor = 1.f - 0.1f * ( (float) (i + 1) * (i + 1) );
            final float hyperloop = 1.f + 0.01f * ( (float) (i + 1) * (i + 1) );
            betaAnimator.setInterpolator(new AccelerateInterpolator(betaDecelerateFactor));
            betaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float newAngle = (float) animation.getAnimatedValue();
                    if (Math.abs(mBetaAngleList.get(index) - newAngle) >= 0.1f) {
                        mBetaAngleList.set(index, newAngle * hyperloop);
                        if (index == (ARC_COUNT - 1)) invalidate();
                    }
                }
            });

            mAlphaValueAnimatorList.add(alphaAnimator);
            mBetaValueAnimatorList.add(betaAnimator);
        }
    }

    private void initWhirpoolAnimators() {
        for (int i = 0; i < ARC_COUNT; ++ i) {
            final int index = i;
            final ValueAnimator alphaAnimator = new ValueAnimator();
            final float randomAlpha = 360.f;
            alphaAnimator.setFloatValues(sINITIAL_ALPHA[mAnimationType],
                    randomAlpha + sINITIAL_ALPHA[mAnimationType],
                    randomAlpha * 2.f + sINITIAL_ALPHA[mAnimationType],
                    randomAlpha * 3.f + sINITIAL_ALPHA[mAnimationType]);
            alphaAnimator.setDuration(sANIMATION_DURATION[mAnimationType]);
            alphaAnimator.setStartDelay(ANIMATION_START_DELAY);
            alphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
            alphaAnimator.setRepeatMode(ValueAnimator.RESTART);
            final float alphaDecelerateFactor = 1.f + 0.1f * (i + 1);
            alphaAnimator.setInterpolator(new DecelerateInterpolator(alphaDecelerateFactor));
            alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float newAngle = (float) animation.getAnimatedValue();
                    if (Math.abs(mAlphaAngleList.get(index) - newAngle) >= 0.5f) {
                        mAlphaAngleList.set(index, newAngle);
                        if (index == (ARC_COUNT - 1)) invalidate();
                    }
                }
            });

            final ValueAnimator betaAnimator = new ValueAnimator();
            betaAnimator.setFloatValues(sINITIAL_BETA[mAnimationType], sPEAK_BETA[mAnimationType], sINITIAL_BETA[mAnimationType]);
            betaAnimator.setDuration(sANIMATION_DURATION[mAnimationType]);
            betaAnimator.setRepeatMode(ValueAnimator.RESTART);
            betaAnimator.setRepeatCount(ValueAnimator.INFINITE);
            final float betaDecelerateFactor = 1.f - 0.05f * (i + 1);
            betaAnimator.setInterpolator(new DecelerateInterpolator(betaDecelerateFactor));
            betaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float newAngle = (float) animation.getAnimatedValue();
                    if (Math.abs(mBetaAngleList.get(index) - newAngle) >= 0.1f) {
                        mBetaAngleList.set(index, newAngle);
                        if (index == (ARC_COUNT - 1)) invalidate();
                    }
                }
            });

            mAlphaValueAnimatorList.add(alphaAnimator);
            mBetaValueAnimatorList.add(betaAnimator);
        }
    }

    private void initMetronome12Animators() {
        for (int i = 0; i < ARC_COUNT; ++ i) {
            final int index = i;
            final float alphaDecelerateFactor = 1.f + 0.1f * ( index * index );

            final ValueAnimator alphaAnimator = new ValueAnimator();
            if (mAnimationType == AnimationType.METRONOME_1) {
                alphaAnimator.setFloatValues(-0.f, 0.f);
                alphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            } else if (mAnimationType == AnimationType.METRONOME_2) {
                final float randomAlpha = 5.f;
                alphaAnimator.setFloatValues(-randomAlpha, randomAlpha, -randomAlpha);
                alphaAnimator.setInterpolator(new FastOutSlowInInterpolator());
            }
            alphaAnimator.setDuration(sANIMATION_DURATION[mAnimationType]);
            alphaAnimator.setStartDelay(ANIMATION_START_DELAY);
            alphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
            alphaAnimator.setRepeatMode(ValueAnimator.REVERSE);
            alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float newAngle = (float) animation.getAnimatedValue();
                    if (Math.abs(mAlphaAngleList.get(index) - newAngle) >= 1.f) {
                        mAlphaAngleList.set(index, newAngle);
                        if (index == (ARC_COUNT - 1)) invalidate();
                    }
                }
            });

            final ValueAnimator betaAnimator = new ValueAnimator();
            betaAnimator.setFloatValues(sPEAK_BETA[mAnimationType], -sPEAK_BETA[mAnimationType]);
            betaAnimator.setDuration(sANIMATION_DURATION[mAnimationType]);
            betaAnimator.setRepeatMode(ValueAnimator.REVERSE);
            betaAnimator.setRepeatCount(ValueAnimator.INFINITE);
            final float betaDecelerateFactor = 1.f + 0.05f * ( index * index );
            betaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            betaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float newAngle = (float) animation.getAnimatedValue();
                    if (Math.abs(mBetaAngleList.get(index) - newAngle) >= 0.5f) {
                        mBetaAngleList.set(index, newAngle);
                        if (index == (ARC_COUNT - 1)) invalidate();
                    }
                }
            });

            mAlphaValueAnimatorList.add(alphaAnimator);
            mBetaValueAnimatorList.add(betaAnimator);
        }
    }

    private void initMetronome34Animators() {
        for (int i = 0; i < ARC_COUNT; ++ i) {
            final int index = i;
            final float alphaDecelerateFactor = 1.f + 0.05f * ( index * index );

            final ValueAnimator alphaAnimator = new ValueAnimator();
            if (mAnimationType == AnimationType.METRONOME_3) {
                final float slownessDegree = 10.f;
                alphaAnimator.setFloatValues(slownessDegree, sINITIAL_BETA[mAnimationType], slownessDegree, sINITIAL_BETA[mAnimationType], slownessDegree);
                alphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            } else if (mAnimationType == AnimationType.METRONOME_4) {
                final float slownessDegree = 20.f;
                alphaAnimator.setFloatValues(sINITIAL_BETA[mAnimationType], slownessDegree, sINITIAL_BETA[mAnimationType]);
                alphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            }
            alphaAnimator.setDuration(sANIMATION_DURATION[mAnimationType]);
            alphaAnimator.setStartDelay(ANIMATION_START_DELAY);
            alphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
            alphaAnimator.setRepeatMode(ValueAnimator.REVERSE);
            alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float newAngle = (float) animation.getAnimatedValue();
                    mAlphaAngleList.set(index, newAngle);
                    if (index == (ARC_COUNT - 1)) invalidate();
                }
            });

            final ValueAnimator betaAnimator = new ValueAnimator();
            betaAnimator.setFloatValues(sPEAK_BETA[mAnimationType], -sPEAK_BETA[mAnimationType]);
            betaAnimator.setDuration(sANIMATION_DURATION[mAnimationType]);
            betaAnimator.setRepeatMode(ValueAnimator.REVERSE);
            betaAnimator.setRepeatCount(ValueAnimator.INFINITE);
            final float betaDecelerateFactor = 1.f + 0.05f * ( index * index );
            betaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            betaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float newAngle = (float) animation.getAnimatedValue();
                    mBetaAngleList.set(index, newAngle);

                    // redraw only once at the end of for loop
                    if (index == (ARC_COUNT - 1)) invalidate();
                }
            });

            mAlphaValueAnimatorList.add(alphaAnimator);
            mBetaValueAnimatorList.add(betaAnimator);
        }
    }

    private void initButterflyKnifeAnimators() {
        for (int i = 0; i < ARC_COUNT; ++ i) {
            final int index = i;
            final float alphaDecelerateFactor = 1.f + 0.05f * ( index * index );

            final ValueAnimator alphaAnimator = new ValueAnimator();
            final float slownessDegree = 20.f;
            alphaAnimator.setFloatValues(sINITIAL_BETA[mAnimationType], slownessDegree, sINITIAL_BETA[mAnimationType]);
            alphaAnimator.setInterpolator(new AccelerateInterpolator(alphaDecelerateFactor));
            alphaAnimator.setDuration(sANIMATION_DURATION[mAnimationType]);
            alphaAnimator.setStartDelay(ANIMATION_START_DELAY);
            alphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
            alphaAnimator.setRepeatMode(ValueAnimator.REVERSE);
            alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float newAngle = (float) animation.getAnimatedValue();
                    mAlphaAngleList.set(index, newAngle);
                    if (index == (ARC_COUNT - 1)) invalidate();
                }
            });

            final ValueAnimator betaAnimator = new ValueAnimator();
            betaAnimator.setFloatValues(sPEAK_BETA[mAnimationType], -sPEAK_BETA[mAnimationType]);
            betaAnimator.setDuration(sANIMATION_DURATION[mAnimationType]);
            betaAnimator.setRepeatMode(ValueAnimator.REVERSE);
            betaAnimator.setRepeatCount(ValueAnimator.INFINITE);
            final float betaDecelerateFactor = 1.f + 0.05f * ( index * index );
            betaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            betaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float newAngle = (float) animation.getAnimatedValue();
                    mBetaAngleList.set(index, newAngle);

                    // redraw only once at the end of for loop
                    if (index == (ARC_COUNT - 1)) invalidate();
                }
            });

            mAlphaValueAnimatorList.add(alphaAnimator);
            mBetaValueAnimatorList.add(betaAnimator);
        }
    }

    private void initRainbowAnimators() {
        for (int i = 0; i < ARC_COUNT; ++ i) {
            final int index = i;
            final int reversedIndex = ARC_COUNT - index - 1;
            final float alphaDecelerateFactor = 1.f + 0.1f * ( index * index );

            final ValueAnimator alphaAnimator = new ValueAnimator();
            final float slownessDegree = 180.f;
            alphaAnimator.setFloatValues(sINITIAL_BETA[mAnimationType], slownessDegree);
            alphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            alphaAnimator.setDuration(sANIMATION_DURATION[mAnimationType]);
            alphaAnimator.setStartDelay(ANIMATION_START_DELAY);
            alphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
            alphaAnimator.setRepeatMode(ValueAnimator.REVERSE);
            alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float newAngle = (float) animation.getAnimatedValue();
                    mAlphaAngleList.set(index, newAngle);
                    if (index == (ARC_COUNT - 1)) invalidate();
                }
            });

            final ValueAnimator betaAnimator = new ValueAnimator();
            betaAnimator.setFloatValues(0.f, sPEAK_BETA[mAnimationType]);
            betaAnimator.setDuration(sANIMATION_DURATION[mAnimationType]);
            betaAnimator.setRepeatMode(ValueAnimator.RESTART);
            betaAnimator.setRepeatCount(ValueAnimator.INFINITE);
            final float betaDecelerateFactor = 1.f + 0.05f * ( index * index );
            betaAnimator.setInterpolator(new LinearInterpolator());
            betaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float newAngle = (float) animation.getAnimatedValue();
                    mBetaAngleList.set(index, newAngle);

                    // redraw only once at the end of for loop
                    if (index == (ARC_COUNT - 1)) invalidate();
                }
            });

            mAlphaValueAnimatorList.add(alphaAnimator);
            mBetaValueAnimatorList.add(betaAnimator);
        }
    }

}