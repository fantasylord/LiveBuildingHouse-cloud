package com.livehouse.service.impl;

import com.livehouse.entity.HouseReserve;
import com.livehouse.entity.Reservation;
import com.livehouse.service.HouseReserveService;
import com.livehouse.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final HouseReserveService houseReserveService;

    @Override
    public boolean addReservation(Reservation reservation) {
        try {
            HouseReserve houseReserve = new HouseReserve();
            houseReserve.setReserveCode("RSV" + System.currentTimeMillis());
            houseReserve.setCustomerName(reservation.getCustomerName());
            houseReserve.setPhone(reservation.getCustomerPhone());
            houseReserve.setBuildingId(reservation.getHouseId());
            houseReserve.setUnitId(reservation.getUnitId());
            
            LocalDate reserveDate = reservation.getReserveDate();
            if (reserveDate != null) {
                houseReserve.setVisitTime(LocalDateTime.of(reserveDate, LocalTime.NOON));
            }
            
            houseReserve.setVisitType(reservation.getVisitType());
            houseReserve.setVisitStatus(0);
            houseReserve.setRemark(reservation.getRemark());
            
            return houseReserveService.save(houseReserve);
        } catch (Exception e) {
            log.error("添加预约失败", e);
            return false;
        }
    }

    @Override
    public boolean checkTodayReservation(Long buildingId, String phone) {
        try {
            return houseReserveService.lambdaQuery()
                    .eq(HouseReserve::getBuildingId, buildingId)
                    .eq(HouseReserve::getPhone, phone)
                    .eq(HouseReserve::getDeleted, 0)
                    .exists();
        } catch (Exception e) {
            log.error("检查预约失败", e);
            return false;
        }
    }
}
