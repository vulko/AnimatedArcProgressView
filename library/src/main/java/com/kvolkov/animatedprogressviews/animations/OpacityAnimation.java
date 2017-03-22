package com.kvolkov.animatedprogressviews.animations;

/**
 * Opacity animation types.
 *
 * @author Kirill Volkov (https://github.com/vulko).
 *         Copyright (C). All rights reserved.
 */
public class OpacityAnimation {
    public static final int NONE = 0;
    public static final int BLINKING = 1;
    public static final int SHINY = 2;
    public static final int AURA = 3;

    private int mType = 0;

    OpacityAnimation(int i) {
        setType(i);
    }

    /**
     * Set animation type.
     *
     * @param value Should be one of public static values defined here.
     */
    public void setType(final int value) {
        switch (value) {
            case NONE:
            case BLINKING:
            case SHINY:
            case AURA:
                mType = value;
                break;

            default:
                mType = NONE;
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
