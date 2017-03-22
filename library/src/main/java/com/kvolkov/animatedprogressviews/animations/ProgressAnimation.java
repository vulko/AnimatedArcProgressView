package com.kvolkov.animatedprogressviews.animations;

/**
 * Progress animation types.
 *
 * @author Kirill Volkov (https://github.com/vulko).
 *         Copyright (C). All rights reserved.
 */
public class ProgressAnimation {
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

    private int mType = 0;

    ProgressAnimation(int i) {
        setType(i);
    }

    /**
     * Set animation type.
     *
     * @param value Should be one of public static values defined here.
     */
    public void setType(final int value) {
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
}
