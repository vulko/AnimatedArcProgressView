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
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

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
              AnimationType.METRONOME})
    public @interface AnimationType {
        int RACE_CONDITION = 0;
        int SWIRLY = 1;
        int WHIRPOOL = 2;
        int HYPERLOOP = 3;
        int METRONOME = 4;
    }

    /**
     * Animation durations in [ms] for animation types. See {@link AnimationType}.
     */
    private static long sANIMATION_DURATION[] = {
            3000,
            6000,
            5000,
            5000,
            5000,
            5000
    };

    /**
     * Animation durations in [ms] for animation types. See {@link AnimationType}.
     */
    private static float sPEAK_BETA[] = {
            180.f,
            180.f,
            90.f,
            90.f,
            90.f,
            60.f
    };

    /**
     * Animation durations in [ms] for animation types. See {@link AnimationType}.
     */
    private static float sINITIAL_BETA[] = {
            0.1f,
            0.1f,
            0.1f,
            0.1f,
            0.1f,
            0.1f
    };

    /**
     * Animation durations in [ms] for animation types. See {@link AnimationType}.
     */
    private static float sINITIAL_ALPHA[] = {
            90.f,
            0.f,
            0.f,
            0.f,
            0.f,
            0.f
    };

    /**
     * Drawing consts.
     */
    private static final int ARC_COUNT = 5;
    private static final long ANIMATION_START_DELAY = 0;

    private @AnimationType
    int mAnimationType = AnimationType.RACE_CONDITION;

    private Paint mArcPaint;
    private List<RectF> mArcRectList = new ArrayList<>(ARC_COUNT);
    private List<Float> mAlphaAngleList = new ArrayList<>(ARC_COUNT);
    private List<Float> mBetaAngleList = new ArrayList<>(ARC_COUNT);
    private List<ValueAnimator> mAlphaValueAnimatorList = new ArrayList<>(ARC_COUNT);
    private List<ValueAnimator> mBetaValueAnimatorList = new ArrayList<>(ARC_COUNT);

    private Timer mTimer = null;
    private TimerTask mTimerTask;

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

    private void initView() {
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

    private void initAnimators() {
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

            case AnimationType.METRONOME:
                initMetronomeAnimators();
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
                for (int i = 0; i < mArcRectList.size(); ++i) {
                    canvas.drawArc(mArcRectList.get(i), mAlphaAngleList.get(i), -mBetaAngleList.get(i), false, mArcPaint);
                }
                canvas.restore();
                break;

            case AnimationType.SWIRLY:
            case AnimationType.WHIRPOOL:
            case AnimationType.HYPERLOOP:
                canvas.save();
                for (int i = 0; i < mArcRectList.size(); ++i) {
                    canvas.drawArc(mArcRectList.get(i), mAlphaAngleList.get(i), mBetaAngleList.get(i), false, mArcPaint);
                }
                canvas.restore();
                break;

            case AnimationType.METRONOME:
                canvas.save();
                for (int i = 0; i < mArcRectList.size(); ++i) {
                    canvas.drawArc(mArcRectList.get(i), sINITIAL_ALPHA[mAnimationType], mAlphaAngleList.get(i), false, mArcPaint);
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
        for (int i = 0; i < ARC_COUNT; ++ i) {
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
                        mAlphaAngleList.add(index, newAngle);
                        if (index == 4) invalidate();
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
                        mBetaAngleList.add(index, newAngle);
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
                        mAlphaAngleList.add(index, newAngle);
                        if (index == 4) invalidate();
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
                        mBetaAngleList.add(index, newAngle);
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
                        mAlphaAngleList.add(index, newAngle);
                        if (index == 4) invalidate();
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
                        mBetaAngleList.add(index, newAngle * hyperloop);
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
                        mAlphaAngleList.add(index, newAngle);
                        if (index == 4) invalidate();
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
                        mBetaAngleList.add(index, newAngle);
                    }
                }
            });

            mAlphaValueAnimatorList.add(alphaAnimator);
            mBetaValueAnimatorList.add(betaAnimator);
        }
    }

    private void initMetronomeAnimators() {
        for (int i = 0; i < ARC_COUNT; ++ i) {
            final int index = i;

            final ValueAnimator alphaAnimator = new ValueAnimator();
            final float randomAlpha = 60.f;
            alphaAnimator.setFloatValues(sINITIAL_ALPHA[mAnimationType],
                    sINITIAL_ALPHA[mAnimationType] - randomAlpha,
                    sINITIAL_ALPHA[mAnimationType] + randomAlpha);
            alphaAnimator.setDuration(sANIMATION_DURATION[mAnimationType]);
            alphaAnimator.setStartDelay(ANIMATION_START_DELAY);
            alphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
            alphaAnimator.setRepeatMode(ValueAnimator.REVERSE);
            final float alphaDecelerateFactor = 1.f - 0.05f * ( index * index );
            alphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float newAngle = (float) animation.getAnimatedValue();
                    if (Math.abs(mAlphaAngleList.get(index) - newAngle) >= 1.f) {
                        mAlphaAngleList.add(index, newAngle);
                    }
                }
            });

//            final ValueAnimator betaAnimator = new ValueAnimator();
//            betaAnimator.setFloatValues(sPEAK_BETA[mAnimationType], sINITIAL_BETA[mAnimationType], -sPEAK_BETA[mAnimationType]);
//            betaAnimator.setDuration(sANIMATION_DURATION[mAnimationType]);
//            betaAnimator.setRepeatMode(ValueAnimator.RESTART);
//            betaAnimator.setRepeatCount(ValueAnimator.INFINITE);
//            final float betaDecelerateFactor = 1.f + 0.05f * ( index * index );
//            betaAnimator.setInterpolator(new DecelerateInterpolator(betaDecelerateFactor));
//            betaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                @Override
//                public void onAnimationUpdate(ValueAnimator animation) {
//                    final float newAngle = (float) animation.getAnimatedValue();
//                    if (Math.abs(mBetaAngleList.get(index) - newAngle) >= 0.1f) {
//                        mBetaAngleList.add(index, -mBetaAngleList.get(index));
//                        if (index == 4) invalidate();
//                    }
//                }
//            });

            mAlphaValueAnimatorList.add(alphaAnimator);
//            mBetaValueAnimatorList.add(betaAnimator);
        }
    }



}