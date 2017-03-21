package com.kvolkov.animatedprogressviews.annotations;

import java.lang.annotation.Documented;

/**
 * Progress animation types.
 *
 * @author Kirill Volkov (https://github.com/vulko).
 *         Copyright (C). All rights reserved.
 */
@Documented
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
