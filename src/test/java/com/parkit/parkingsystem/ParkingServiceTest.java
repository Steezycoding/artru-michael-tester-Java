package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.constants.UserRecurrence;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @Captor
    private ArgumentCaptor<Ticket> ticketCaptor;

    @BeforeEach
    public void setUpPerTest() {
        try {
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            System.setOut(new PrintStream(outputStreamCaptor));
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @AfterEach
    public void tearDown() {
        System.setOut(standardOut);
    }
    
    @Nested
    class IncomingVehicle {
        @BeforeEach
        public void setUp() {
            try {
                when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            } catch (Exception e) {
                e.printStackTrace();
                throw  new RuntimeException("Failed to set up INCOMING vehicle test mock objects");
            }
        }

        @Test
        public void processIncomingCarTest(){
            when(inputReaderUtil.readSelection()).thenReturn(1);
            when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(2);

            parkingService.processIncomingVehicle();

            verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
        }

        @Test
        public void processIncomingBikeTest(){
            when(inputReaderUtil.readSelection()).thenReturn(2);
            when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(4);

            parkingService.processIncomingVehicle();

            verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
        }

        @Test
        public void processNormalUserIncomingVehicleTest() {
            when(inputReaderUtil.readSelection()).thenReturn(1);
            when(ticketDAO.getTicketsCount(anyString())).thenReturn(UserRecurrence.MIN_TICKET_COUNT - 1);
            when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(2);

            parkingService.processIncomingVehicle();

            verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
            assertFalse(outputStreamCaptor.toString().trim().contains("Happy to see you again! As a recurring user, you will get a 5% discount."));
        }

        @Test
        public void processRecurrentUserIncomingVehicleTest() {
            when(inputReaderUtil.readSelection()).thenReturn(1);
            when(ticketDAO.getTicketsCount(anyString())).thenReturn(UserRecurrence.MIN_TICKET_COUNT);
            when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(2);

            parkingService.processIncomingVehicle();

            verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
            assertTrue(outputStreamCaptor.toString().trim().contains("Happy to see you again! As a recurring user, you will get a 5% discount."));
        }
    }

    @Nested
    class ExitingVehicle {
        @BeforeEach
        public void setUp() {
            try {
                ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
                Ticket ticket = new Ticket();
                ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
                ticket.setParkingSpot(parkingSpot);
                ticket.setVehicleRegNumber("ABCDEF");

                when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
                when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            } catch (Exception e) {
                e.printStackTrace();
                throw  new RuntimeException("Failed to set up EXITING vehicle test mock objects");
            }
        }

        @Test
        public void processExitingVehicleUnableUpdateTicketTest() {
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

            parkingService.processExitingVehicle();

            assertTrue(outputStreamCaptor.toString().trim().contains("Unable to update ticket information. Error occurred"));
            verifyNoInteractions(parkingSpotDAO);
        }

        @Test
        public void processNormalUserExitingVehicleTest(){
            try {
                when(ticketDAO.getTicketsCount(anyString())).thenReturn(UserRecurrence.MIN_TICKET_COUNT - 1);
                when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

                parkingService.processExitingVehicle();

                verify(ticketDAO).updateTicket(ticketCaptor.capture());
                Ticket ticket = ticketCaptor.getValue();
                assertThat(ticket.getPrice(), is(Fare.CAR_RATE_PER_HOUR));
                verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
            } catch (Exception e) {
                e.printStackTrace();
                throw  new RuntimeException("Failed to exiting vehicule");
            }
        }

        @Test
        public void processRecurrentUserExitingVehicleTest(){
            try {
                when(ticketDAO.getTicketsCount(anyString())).thenReturn(UserRecurrence.MIN_TICKET_COUNT);
                when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

                parkingService.processExitingVehicle();

                verify(ticketDAO).updateTicket(ticketCaptor.capture());
                Ticket ticket = ticketCaptor.getValue();
                assertThat(ticket.getPrice(), is(Fare.CAR_RATE_PER_HOUR * UserRecurrence.DISCOUNT_RATE));
                verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
            } catch (Exception e) {
                e.printStackTrace();
                throw  new RuntimeException("Failed to exiting vehicule");
            }
        }
    }
}
