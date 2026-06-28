package com.livehouse.service;

import com.livehouse.entity.Reservation;

public interface ReservationService {

    boolean addReservation(Reservation reservation);

    boolean checkTodayReservation(Long buildingId, String phone);
}