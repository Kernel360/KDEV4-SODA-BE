package com.soda.member.domain.member;

public enum MemberStatus {
    AVAILABLE("업무 가능", true),
    BUSY("바쁨", false),
    AWAY("자리 비움", true),
    ON_VACATION("휴가중", false);

    private final String description;
    private final boolean isWorkable;

    MemberStatus(String description, boolean isWorkable) {
        this.description = description;
        this.isWorkable = isWorkable;
    }
    public String getDescription() {return description;}

    public boolean isWorkable() { return isWorkable; }
}
