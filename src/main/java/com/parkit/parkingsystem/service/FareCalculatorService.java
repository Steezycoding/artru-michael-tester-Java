package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.UserRecurrence;
import com.parkit.parkingsystem.model.Ticket;

import java.time.Duration;
import java.util.Date;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }

    public void calculateFare(Ticket ticket, boolean isRecurrent) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        Date inHour = ticket.getInTime();
        Date outHour = ticket.getOutTime();
        Duration duration = Duration.between(inHour.toInstant(), outHour.toInstant());
        double time;
        double price;

        if (duration.toMinutes() <= 30) {
            time = 0;
        } else {
            time = (double) duration.toMinutes() / 60;
        }

        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                price = time * Fare.CAR_RATE_PER_HOUR;
                break;
            }
            case BIKE: {
                price = time * Fare.BIKE_RATE_PER_HOUR;
                break;
            }
            default: throw new IllegalArgumentException("Unknown Parking Type");
        }

        double finalPrice = isRecurrent ? price * UserRecurrence.DISCOUNT_RATE : price;
        ticket.setPrice(finalPrice);
    }
}