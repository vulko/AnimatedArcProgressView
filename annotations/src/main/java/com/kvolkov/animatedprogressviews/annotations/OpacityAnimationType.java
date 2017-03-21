package com.kvolkov.animatedprogressviews.annotations;

import java.lang.annotation.Documented;

/**
 * Opacity animation types.
 *
 * @author Kirill Volkov (https://github.com/vulko).
 *         Copyright (C). All rights reserved.
 */
@Documented
public @interface OpacityAnimationType {
    int NONE = 0;
    int BLINKING = 1;
    int SHINY = 2;
    int AURA = 3;
}
