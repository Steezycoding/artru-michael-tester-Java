package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.testutil.DateUtility;
import com.parkit.parkingsystem.testutil.TimeSlot;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

	private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
	private static ParkingSpotDAO parkingSpotDAO;
	private static TicketDAO ticketDAO;
	private static DataBasePrepareService dataBasePrepareService;

	private static Date actualDate;

	@Mock
	private static InputReaderUtil inputReaderUtil;

	@BeforeAll
	public static void setUp() {
		parkingSpotDAO = new ParkingSpotDAO();
		parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
		ticketDAO = new TicketDAO();
		ticketDAO.dataBaseConfig = dataBaseTestConfig;
		dataBasePrepareService = new DataBasePrepareService();
	}

	@BeforeEach
	public void setUpPerTest() throws Exception {
		// Date : 2024-01-10 13:00:00.000
		actualDate = new Date(1704888000000L);
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
	}

	@AfterEach
	public void tearDown() {
		dataBasePrepareService.clearDataBaseEntries();
	}

	@Test
	public void testParkingACar() {
		when(inputReaderUtil.readSelection()).thenReturn(1);

		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processIncomingVehicle(actualDate);

		Ticket ticket = ticketDAO.getTicket("ABCDEF");

		assertThat(ticket).isNotNull();
		assertThat(ticket.getVehicleRegNumber()).isEqualTo("ABCDEF");
		assertThat(ticket.getPrice()).isEqualTo(0);
		assertThat(ticket.getParkingSpot().getParkingType()).isEqualTo(ParkingType.CAR);
		assertThat(ticket.getInTime().toInstant()).isEqualTo(actualDate.toInstant());
		assertThat(ticket.getOutTime()).isNull();
		assertThat(ticket.getParkingSpot().isAvailable()).isFalse();
	}

	@Test
	public void testParkingLotExit() {
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		Ticket ticket = new Ticket();
		ticket.setVehicleRegNumber("ABCDEF");
		ticket.setInTime(DateUtility.dateModifier(actualDate, TimeSlot.HOUR, -1));
		ticket.setParkingSpot(parkingSpot);
		parkingSpotDAO.updateParking(parkingSpot);
		ticketDAO.saveTicket(ticket);

		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processExitingVehicle(actualDate);

		Ticket updatedTicket = ticketDAO.getTicket("ABCDEF");

		assertThat(updatedTicket.getPrice()).isEqualTo(Fare.CAR_RATE_PER_HOUR);
		assertThat(updatedTicket.getOutTime().toInstant()).isEqualTo(actualDate.toInstant());
		assertThat(updatedTicket.getParkingSpot().isAvailable()).isTrue();
	}

	@Test
	public void testParkingLotExitRecurringUser() {
		Ticket ticketOne = new Ticket();
		ticketOne.setVehicleRegNumber("ABCDEF");
		ticketOne.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, true));
		ticketOne.setInTime(DateUtility.dateModifier(actualDate, TimeSlot.HOUR, -3));
		ticketOne.setOutTime(DateUtility.dateModifier(actualDate, TimeSlot.HOUR, -2));
		ticketOne.setPrice(Fare.CAR_RATE_PER_HOUR);
		ticketDAO.saveTicket(ticketOne);

		Ticket ticketTwo = new Ticket();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		ticketTwo.setVehicleRegNumber("ABCDEF");
		ticketTwo.setParkingSpot(parkingSpot);
		ticketTwo.setInTime(DateUtility.dateModifier(actualDate, TimeSlot.HOUR, -1));
		parkingSpotDAO.updateParking(parkingSpot);
		ticketDAO.saveTicket(ticketTwo);

		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		parkingService.processExitingVehicle(actualDate);

		Ticket lastTicket = ticketDAO.getTicket("ABCDEF");

		assertThat(lastTicket.getPrice()).isEqualTo(Fare.CAR_RATE_PER_HOUR * 0.95);
		assertThat(lastTicket.getOutTime().toInstant()).isEqualTo(actualDate.toInstant());
		assertThat(lastTicket.getParkingSpot().isAvailable()).isTrue();
	}
}
