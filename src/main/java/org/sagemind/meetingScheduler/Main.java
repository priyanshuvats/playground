package org.sagemind.meetingScheduler;

import java.util.*;
public class Main {

    public static void main(String[] args) {
        AppContex ac = AppContex.getInstance();
        MeetingScheduler ms = ac.getMeetingScheduler();
        for(String arg: args){
            String[] i = arg.split("-");
            ms.bookRoom(new Interva(Integer.parseInt(i[0]), Integer.parseInt(i[1])));
        }
    }

}


class AppContex {

    private static AppContex instance;
    private BookingRepo bookingRepo;
    private IBookingService bookingService;
    private MeetingScheduler meetingScheduler;

    private AppContex(){
        bookingRepo = new BookingRepo();
        bookingService = new BookingService(bookingRepo);
        meetingScheduler = new MeetingScheduler(bookingService);
    }

    public static AppContex getInstance(){
        if(instance != null){
            return instance;
        }
        instance = new AppContex();
        return instance;
    }

    public MeetingScheduler getMeetingScheduler(){
        return meetingScheduler;
    }

}

class MeetingScheduler {
    IBookingService bookingService;

    MeetingScheduler(IBookingService bs){
        this.bookingService = bs;
    }

    public void bookRoom(Interva i) {
        try{
            Booking booking = bookingService.bookRoom(i);
            System.out.println("Successfaully booking id: " + booking.id  + " for interva : " + booking.interva);
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}


class Interva {
    int start;
    int end;
    Interva(int start, int end){
        this.start = start;
        this.end = end;
    }
    public String toString(){
        return start + " to " + end;
    }
}

class Booking implements Comparable<Booking>{
    static int idCount;

    int id;
    Interva interva;

    Booking(Interva i){
        this.id = getAutoIncrementId();
        this.interva = i;
    }

    private static int getAutoIncrementId(){
        return ++idCount;
    }

    public int compareTo(Booking b){
        return this.interva.start - b.interva.start;
    }
}


class RoomUnavailableException extends Exception {
    RoomUnavailableException(String message){
        super(message);
    }
}

class InvalidintervaException extends Exception {
    InvalidintervaException(String message){
        super(message);
    }
}

interface IBookingService {
    public Booking bookRoom(Interva i) throws RoomUnavailableException, InvalidintervaException;
}

class BookingService implements IBookingService {

    BookingRepo bookingRepo;

    public BookingService(BookingRepo bookingRepo){
        this.bookingRepo = bookingRepo;
    }

    public Booking bookRoom(Interva i) throws RoomUnavailableException, InvalidintervaException {
        if(isValidinterva(i)){
            Booking desiredBooking = new Booking(i);
            Booking floor = bookingRepo.getFloorBooking(desiredBooking);
            Booking ceil = bookingRepo.getCeilingBooking(desiredBooking);
            Interva floorInterval = floor==null ? null : floor.interva;
            Interva ceilInterval = ceil==null ? null : ceil.interva;
            if(isOverlapping(i,floorInterval) || isOverlapping(i, ceilInterval)){
                throw new RoomUnavailableException("Room is unavailable for the selected interva : " + i);
            }
            bookingRepo.save(desiredBooking);
            return desiredBooking;
        } else {
            throw new InvalidintervaException("Invalid interva : " + i);
        }
    }

    private boolean isOverlapping(Interva first, Interva second){
        if(first==null || second==null){
            return false;
        }
        if(first.start <= second.start){
            return first.end > second.start;
        }
        return isOverlapping(second, first);
    }

    private boolean isValidinterva(Interva i){
        if(i!=null && i.end > i.start){
            return true;
        }
        return false;
    }
}


class BookingRepo {

    TreeSet<Booking> bookingSet;

    public BookingRepo(){
        bookingSet = new TreeSet<Booking>();
    }

    public void save(Booking b){
        if(b==null){
            return;
        }
        bookingSet.add(b);
    }

    public Booking getFloorBooking(Booking b){
        if(bookingSet.isEmpty()){
            return null;
        }
        return bookingSet.floor(b);
    }

    public Booking getCeilingBooking(Booking b){
        if(bookingSet.isEmpty()){
            return null;
        }
        return bookingSet.ceiling(b);
    }

}


