package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.constants.UserRecurrence;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

public class FareCalculatorServiceTest {

	private static FareCalculatorService fareCalculatorService;
	private Ticket ticket;

	@BeforeAll
	public static void setUp() {
		fareCalculatorService = new FareCalculatorService();
	}

	@BeforeEach
	public void setUpPerTest() {
		ticket = new Ticket();
	}

	@Test
	public void calculateFareCar() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		assertEquals(ticket.getPrice(), Fare.CAR_RATE_PER_HOUR);
	}

	@Test
	public void calculateFareBike() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		assertEquals(ticket.getPrice(), Fare.BIKE_RATE_PER_HOUR);
	}

	@Test
	public void calculateFareUnknownType() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, null, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
	}

	@Test
	public void calculateFareBikeWithFutureInTime() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() + (60 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
	}

	@Test
	public void calculateFareWithHalfHourParkingTime() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (30 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpotBike = new ParkingSpot(1, ParkingType.BIKE, false);
		ParkingSpot parkingSpotCar = new ParkingSpot(2, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);

		ticket.setParkingSpot(parkingSpotBike);
		fareCalculatorService.calculateFare(ticket);
		assertEquals((0.5 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());

		ticket.setParkingSpot(parkingSpotCar);
		fareCalculatorService.calculateFare(ticket);
		assertEquals((0.5 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
	}

	@Test
	public void calculateFareWithLessThanHalfHourParkingTime() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (29 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpotBike = new ParkingSpot(1, ParkingType.BIKE, false);
		ParkingSpot parkingSpotCar = new ParkingSpot(2, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);

		ticket.setParkingSpot(parkingSpotBike);
		fareCalculatorService.calculateFare(ticket);
		assertEquals(0, ticket.getPrice());

		ticket.setParkingSpot(parkingSpotCar);
		fareCalculatorService.calculateFare(ticket);
		assertEquals(0, ticket.getPrice());
	}

	@Test
	public void calculateFareBikeWithLessThanOneHourParkingTime() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		assertEquals((0.75 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
	}

	@Test
	public void calculateFareCarWithLessThanOneHourParkingTime() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		assertEquals((0.75 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
	}

	@Test
	public void calculateFareCarWithMoreThanADayParkingTime() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (24 * 60 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		assertEquals((24 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
	}

	@Test
	public void calculateFareWithDiscount() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (90 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpotBike = new ParkingSpot(1, ParkingType.BIKE, false);
		ParkingSpot parkingSpotCar = new ParkingSpot(2, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpotBike);
		fareCalculatorService.calculateFare(ticket, true);
		assertEquals((1.5 * Fare.BIKE_RATE_PER_HOUR) * UserRecurrence.DISCOUNT_RATE, ticket.getPrice());

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpotCar);
		fareCalculatorService.calculateFare(ticket, true);
		assertEquals((1.5 * Fare.CAR_RATE_PER_HOUR) * UserRecurrence.DISCOUNT_RATE, ticket.getPrice());
	}
}
