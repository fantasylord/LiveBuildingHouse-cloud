package com.livehouse.entity;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Reservation {

    private Long id;

    private String customerName;

    private String customerPhone;

    private Long houseId;

    private Long unitId;

    private LocalDate reserveDate;

    private String reserveTimeSlot;

    private Integer visitType;

    private Integer visitStatus;

    private String remark;
}