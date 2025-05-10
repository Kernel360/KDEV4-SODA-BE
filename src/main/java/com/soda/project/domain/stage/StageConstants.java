package com.soda.project.domain.stage;

public final class StageConstants {
    public static final float ORDER_INCREMENT = 1000.0f;
    public static final float INITIAL_ORDER = 1000.0f;
    public static final int MAX_STAGES_PER_PROJECT = 10;
    public static final int MAX_STAGE_NAME_LENGTH = 100;

    private StageConstants() {
        throw new AssertionError("Utility class");
    }
}