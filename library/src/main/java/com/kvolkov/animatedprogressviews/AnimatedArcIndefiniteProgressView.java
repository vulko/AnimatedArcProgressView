package com.kvolkov.animatedprogressviews;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an animated arc progress view for displaying indefinite progress animation.
 *
 * @author Kirill Volkov (https://github.com/vulko).
 *         Copyright (C). All rights reserved.
 */
public class AnimatedArcIndefiniteProgressView extends View {

    /**
     * Drawing consts.
     */
    private static final long ANIMATION_START_DELAY = 0;
    private static final int DEFAULT_ARC_COUNT = 5;
    private static final float DEFAULT_ARC_SPACING = 5;
    private static final float DEFAULT_ARC_STROKE_WIDTH = 5;
    private static final int DEFAULT_ARC_COLOR = Color.argb(255, 0, 0, 200);;

    /**
     * Opacity animation types.
     */
// TODO: can't import annotation    @Retention(RetentionPolicy.SOURCE)
    @IntDef({OpacityAnimationType.NONE,
            OpacityAnimationType.BLINKING,
            OpacityAnimationType.SHINY,
            OpacityAnimationType.AURA,
    })
    public @interface OpacityAnimationType {
        int NONE = 0;
        int BLINKING = 1;
        int SHINY = 2;
        int AURA = 3;
    }

    /**
     * Animation durations in [ms] for opacity animation types. See {@link OpacityAnimationType}.
     */
    protected static long sOpacityAnimationDuration[] = {
            0,
            2000,
            1000,
            1000,
    };

    /**
     * Opacity animation consts.
     */
    protected static int INITIAL_OPACITY = 255;
    protected static int TARGET_OPACITY = 50;

    /**
     * Progress animation types.
     */
// TODO: can't import annotation    @Retention(RetentionPolicy.SOURCE)
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

            ProgressAnimationType.OPACITY_ANIMATION_TEST_STUB,
    })
    public @interface ProgressAnimationType {
        int OPACITY_ANIMATION_TEST_STUB = -1;
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

    /**
     * Animation durations in [ms] for animation types. See {@link ProgressAnimationType}.
     */
    protected static long sProgressAnimationDuration[] = {
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
     * Animation max beta angle initial values. See {@link ProgressAnimationType}.
     */
    protected static float sPeakBeta[] = {
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
     * Animation initial beta angle initial values. See {@link ProgressAnimationType}.
     */
    protected static float sInitialBeta[] = {
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
     * Animation initial alpha angle initial values. See {@link ProgressAnimationType}.
     */
    protected static float sInitialAlpha[] = {
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

    protected @AnimatedArcIndefiniteProgressView.OpacityAnimationType
    int mOpacityAnimationType = OpacityAnimationType.NONE;

    @IntRange(from = 1, to = 30)
    protected int mArcCount;
    @FloatRange(from = 0.f, to = 100.f)
    protected float mArcSpacing = DEFAULT_ARC_SPACING;
    @FloatRange(from = 0.f, to = 500.f)
    protected float mArcStwokeWidth = DEFAULT_ARC_STROKE_WIDTH;
    @ColorInt
    protected int mPrimaryColor = DEFAULT_ARC_COLOR;

    protected Paint mArcPaint;
    protected List<Integer> mColorList = new ArrayList<>();
    protected List<Integer> mAlphaOpacityList = new ArrayList<>();
    protected List<RectF> mArcRectList = new ArrayList<>();
    protected List<Float> mAlphaAngleList = new ArrayList<>();
    protected List<Float> mBetaAngleList = new ArrayList<>();

    protected List<ValueAnimator> mAlphaValueAnimatorList = new ArrayList<>();
    protected List<ValueAnimator> mBetaValueAnimatorList = new ArrayList<>();
    protected List<ValueAnimator> mOpacityValueAnimatorList = new ArrayList<>();

    /**
     * Self updating mechanism.
     */
    private boolean mInitialized = false;
    private Handler mHandler = new Handler();
    private Runnable mUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(mUpdateRunnable, 16);
            invalidate();
        }
    };

    public AnimatedArcIndefiniteProgressView(Context context) {
        super(context);
        initView(null);
    }

    public AnimatedArcIndefiniteProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(attrs);
    }

    public AnimatedArcIndefiniteProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AnimatedArcIndefiniteProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(attrs);
    }

    protected void initView(@Nullable AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.AnimatedArcIndefiniteProgressView,
                    0, 0);

            try {
                setArcCount(a.getInteger(R.styleable.AnimatedArcIndefiniteProgressView_arcCount, DEFAULT_ARC_COUNT));
                setArcSpacing(a.getFloat(R.styleable.AnimatedArcIndefiniteProgressView_arcSpacing, DEFAULT_ARC_SPACING));
                setArcStrokeWidth(a.getFloat(R.styleable.AnimatedArcIndefiniteProgressView_arcStrokeWidth, DEFAULT_ARC_STROKE_WIDTH));
                setPrimaryColor(a.getColor(R.styleable.AnimatedArcIndefiniteProgressView_defaultColor, DEFAULT_ARC_COLOR));
                setProgressAnimationType(a.getInteger(R.styleable.AnimatedArcIndefiniteProgressView_progressAnimation, mProgressAnimationType));
            } finally {
                a.recycle();
            }
        }

        if (mArcPaint == null) {
            mArcPaint = new Paint();
        }
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setColor(Color.argb(100, 0, 0, 0));
        mArcPaint.setStrokeCap(Paint.Cap.ROUND);
        mArcPaint.setStrokeWidth(mArcStwokeWidth);
        mArcPaint.setAntiAlias(true);

        mInitialized = true;
    }

    protected void initColorList(@ColorInt int color) {
        mColorList.clear();
        for (int i = 0; i < mArcCount; i++) {
            mColorList.add(color);
        }
    }

    protected void initProgressValueAnimators() {
        mAlphaAngleList.clear();
        mBetaAngleList.clear();
        for (int i = 0; i < mArcCount; ++i) {
            mAlphaAngleList.add(sInitialAlpha[mProgressAnimationType]);
            mBetaAngleList.add(sInitialBeta[mProgressAnimationType]);
        }

        for (int i = 0; i < mArcCount; ++ i) {
            final ValueAnimator alphaAnimator = new ValueAnimator();
            final ValueAnimator betaAnimator = new ValueAnimator();

            alphaAnimator.setDuration(sProgressAnimationDuration[mProgressAnimationType]);
            alphaAnimator.setStartDelay(ANIMATION_START_DELAY);
            alphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
            alphaAnimator.setRepeatMode(ValueAnimator.RESTART);

            betaAnimator.setDuration(sProgressAnimationDuration[mProgressAnimationType]);
            betaAnimator.setStartDelay(ANIMATION_START_DELAY);
            betaAnimator.setRepeatCount(ValueAnimator.INFINITE);
            betaAnimator.setRepeatMode(ValueAnimator.RESTART);

            switch (mProgressAnimationType) {
                case ProgressAnimationType.OPACITY_ANIMATION_TEST_STUB:
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
        for (int i = 0; i < mArcCount; ++i) {
            mAlphaOpacityList.add(INITIAL_OPACITY);
        }

        if (mOpacityAnimationType != OpacityAnimationType.NONE) {

            for (int i = 0; i < mArcCount; ++ i) {
                final ValueAnimator opacityAnimator = new ValueAnimator();

                opacityAnimator.setDuration(sOpacityAnimationDuration[mOpacityAnimationType]);
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

                    case OpacityAnimationType.AURA:
                        initAuraAnimators(i, opacityAnimator);
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
     * @throws IllegalStateException In case of adding more colors, then there are arcs. See {@link #mArcCount}.
     */
    public void addColor(@ColorInt int colorInt) throws IllegalStateException {
        mColorList.add(colorInt);

        if (mColorList.size() > mArcCount) {
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

        if (mProgressAnimationType == ProgressAnimationType.OPACITY_ANIMATION_TEST_STUB) {
            // ignore progress for testing animation effects
            return;
        }

        initProgressValueAnimators();

        for (int i = 0; i < mAlphaValueAnimatorList.size(); ++i) {
            mAlphaValueAnimatorList.get(i).start();
        }
        for (int i = 0; i < mBetaValueAnimatorList.size(); ++i) {
            mBetaValueAnimatorList.get(i).start();
        }
    }

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

    private void startUpdates() {
        mHandler.postDelayed(mUpdateRunnable, 16);
    }

    private void restart() {
        setPrimaryColor(mPrimaryColor);
        startProgressAnimation();
        startOpacityAnimation();
        startUpdates();
    }

    /**
     * Setup primary color for the arcs.
     * Basically initializes {@code mColorList} with provided color.
     *
     * @param color  A {@link ColorInt}.
     */
    protected void setPrimaryColor(@ColorInt int color) {
        mPrimaryColor = color;
        initColorList(color);
    }

    public void setProgressAnimationType(@ProgressAnimationType int animationType) {
        mProgressAnimationType = animationType;

        if (mInitialized) {
            restart();
        }
    }

    public void setOpacityAnimationType(@OpacityAnimationType int animationType) {
        mOpacityAnimationType = animationType;

        if (mInitialized) {
            restart();
        }
    }

    public void setArcCount(@IntRange(from = 1, to = 30) int arcs) {
        if (arcs < 1 || arcs > 30) {
            throw new IllegalArgumentException("Should be in range [1..30]");
        }

        mArcCount = arcs;

        if (mInitialized) {
            restart();
            // remeasure
            requestLayout();
        }
    }

    public void setArcSpacing(@FloatRange(from = 0.f, to = 100.f) float spacing) {
        if (spacing < 0.f || spacing > 100.f) {
            throw new IllegalArgumentException("Should be in range [0.f .. 100.f]");
        }

        mArcSpacing = spacing;

        if (mInitialized) {
            restart();
            // remeasure
            requestLayout();
        }
    }

    public void setArcStrokeWidth(@FloatRange(from = 0.f, to = 500.f) float width) {
        if (width < 0.f || width > 500.f) {
            throw new IllegalArgumentException("Should be in range [0.f .. 1.f]");
        }

        mArcStwokeWidth = width;

        if (mInitialized) {
            restart();
            // remeasure
            requestLayout();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        restart();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(widthSize, heightSize);

        final int prefferedDimension = Math.min(widthSize, heightSize);
        // preffered stroke width to match drawing exactly with the number of arcs and specified padding
        final int arcs = mArcCount + 1; // + 1 since no need to see circle in the middle
        final float leftSpace = prefferedDimension - mArcSpacing * arcs;
        final float prefferedStrokeWidth;
        if (leftSpace >= 0.f) {
            prefferedStrokeWidth = leftSpace / arcs;
        } else {
            prefferedStrokeWidth = mArcSpacing;
        }

        final boolean isWider = widthSize >= heightSize;
        final float sideDiff = isWider ? (widthSize - prefferedDimension) / 2.f
                                       : (heightSize - prefferedDimension) / 2.f;

        //mArcStwokeWidth = prefferedStrokeWidth;
        mArcPaint.setStrokeWidth(mArcStwokeWidth);

        // Init rects for arcs
        mArcRectList.clear();
        final float arcRadiusDiff = mArcSpacing * 2.f;
        final float initialLeft = isWider ? sideDiff : 0.f,
                    initialTop = isWider ? 0.f : sideDiff,
                    initialRight = widthSize - (isWider ? sideDiff : 0.f),
                    initialBottom = heightSize - (isWider ? 0.f : sideDiff);

        for (int i = 0; i < mArcCount; ++i) {
            final float left = initialLeft + arcRadiusDiff * (i + 1),
                        top = initialTop + arcRadiusDiff * (i + 1),
                        right = initialRight - arcRadiusDiff * (i + 1),
                        bottom = initialBottom - arcRadiusDiff * (i + 1);
            mArcRectList.add(new RectF(left, top, right, bottom));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < mArcCount; ++i) {
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
                    canvas.drawArc(mArcRectList.get(i), sInitialAlpha[mProgressAnimationType] + mAlphaAngleList.get(i), mBetaAngleList.get(i), false, mArcPaint);
                    break;

                case ProgressAnimationType.METRONOME_3:
                case ProgressAnimationType.METRONOME_4:
                    canvas.drawArc(mArcRectList.get(i), sInitialAlpha[mProgressAnimationType] - mBetaAngleList.get(i), mAlphaAngleList.get(i), false, mArcPaint);
                    break;

                case ProgressAnimationType.BUTTERFLY_KNIFE:
                    canvas.drawArc(mArcRectList.get(i), sInitialAlpha[mProgressAnimationType] - mBetaAngleList.get(i), mAlphaAngleList.get(i), false, mArcPaint);
                    break;

                case ProgressAnimationType.RAINBOW:
                case ProgressAnimationType.GOTCHA:
                    canvas.drawArc(mArcRectList.get(i), sInitialAlpha[mProgressAnimationType] + mBetaAngleList.get(i), -mAlphaAngleList.get(i), false, mArcPaint);
                    break;

                case ProgressAnimationType.OPACITY_ANIMATION_TEST_STUB:
                    canvas.drawArc(mArcRectList.get(i), 0, 360, false, mArcPaint);
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
        mInitialized = false;
    }

    /**
     * Progress Animation helper functions.
     */
    private void initRaceConditionAnimators(final int index, ValueAnimator alphaAnimator, ValueAnimator betaAnimator) {
        final float factor = 0.05f * (index + 1);

        final float randomAlpha = 360.f;
        alphaAnimator.setFloatValues(sInitialAlpha[mProgressAnimationType],
                randomAlpha + sInitialAlpha[mProgressAnimationType],
                randomAlpha * 2.f + sInitialAlpha[mProgressAnimationType],
                randomAlpha * 3.f + sInitialAlpha[mProgressAnimationType]);
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

        betaAnimator.setFloatValues(sInitialBeta[mProgressAnimationType], sPeakBeta[mProgressAnimationType], sInitialBeta[mProgressAnimationType]);
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
        alphaAnimator.setFloatValues(sInitialAlpha[mProgressAnimationType],
                randomAlpha + sInitialAlpha[mProgressAnimationType],
                randomAlpha * 2.f + sInitialAlpha[mProgressAnimationType],
                randomAlpha * 3.f + sInitialAlpha[mProgressAnimationType],
                randomAlpha * 3.5f + sInitialAlpha[mProgressAnimationType]);
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

        betaAnimator.setFloatValues(sInitialBeta[mProgressAnimationType], sPeakBeta[mProgressAnimationType], sInitialBeta[mProgressAnimationType]);
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
        alphaAnimator.setFloatValues(sInitialAlpha[mProgressAnimationType],
                randomAlpha - sInitialAlpha[mProgressAnimationType],
                randomAlpha * 2.f - sInitialAlpha[mProgressAnimationType],
                randomAlpha * 3.f - sInitialAlpha[mProgressAnimationType],
                randomAlpha * 4.f - sInitialAlpha[mProgressAnimationType]);
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

        betaAnimator.setFloatValues(sInitialBeta[mProgressAnimationType], sPeakBeta[mProgressAnimationType], sInitialBeta[mProgressAnimationType]);
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
        alphaAnimator.setFloatValues(sInitialAlpha[mProgressAnimationType],
                randomAlpha + sInitialAlpha[mProgressAnimationType],
                randomAlpha * 2.f + sInitialAlpha[mProgressAnimationType],
                randomAlpha * 3.f + sInitialAlpha[mProgressAnimationType],
                randomAlpha * 4.f + sInitialAlpha[mProgressAnimationType],
                randomAlpha * 5.f + sInitialAlpha[mProgressAnimationType],
                randomAlpha * 6.f + sInitialAlpha[mProgressAnimationType]);
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

        betaAnimator.setFloatValues(sInitialBeta[mProgressAnimationType], sPeakBeta[mProgressAnimationType], sInitialBeta[mProgressAnimationType]);
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

        betaAnimator.setFloatValues(sPeakBeta[mProgressAnimationType], -sPeakBeta[mProgressAnimationType]);
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
            alphaAnimator.setFloatValues(slownessDegree, sInitialBeta[mProgressAnimationType], slownessDegree, sInitialBeta[mProgressAnimationType], slownessDegree);
            alphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        } else if (mProgressAnimationType == ProgressAnimationType.METRONOME_4) {
            final float slownessDegree = 20.f;
            alphaAnimator.setFloatValues(sInitialBeta[mProgressAnimationType], slownessDegree, sInitialBeta[mProgressAnimationType]);
            alphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        }
        alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float newAngle = (float) animation.getAnimatedValue();
                mAlphaAngleList.set(index, newAngle);
            }
        });

        betaAnimator.setFloatValues(sPeakBeta[mProgressAnimationType], -sPeakBeta[mProgressAnimationType]);
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
        alphaAnimator.setFloatValues(sInitialBeta[mProgressAnimationType], slownessDegree, sInitialBeta[mProgressAnimationType]);
        alphaAnimator.setInterpolator(new AccelerateInterpolator(alphaDecelerateFactor));
        alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float newAngle = (float) animation.getAnimatedValue();
                mAlphaAngleList.set(index, newAngle);
            }
        });

        betaAnimator.setFloatValues(sPeakBeta[mProgressAnimationType], -sPeakBeta[mProgressAnimationType]);
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
        alphaAnimator.setFloatValues(sInitialBeta[mProgressAnimationType], slownessDegree);
        alphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float newAngle = (float) animation.getAnimatedValue();
                mAlphaAngleList.set(index, newAngle);
            }
        });

        betaAnimator.setFloatValues(0.f, sPeakBeta[mProgressAnimationType]);
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