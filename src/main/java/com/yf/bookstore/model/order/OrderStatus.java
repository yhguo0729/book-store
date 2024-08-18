package com.yf.bookstore.model.order;

public enum OrderStatus {
    CREATED(0), // 创建
    CANCELLED(1), // 已取消
    COMPLETED(2), // 已完成
    PROCESSING(3); //处理中

    private final int value;

    OrderStatus(int value) {
        this.value = value;
    }

    public static OrderStatus fromValue(int value) {
        for (OrderStatus status : values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid OrderStatus value: " + value);
    }

    public int getValue() {
        return value;
    }
}
