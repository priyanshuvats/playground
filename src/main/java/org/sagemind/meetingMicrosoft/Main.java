package org.sagemind.meetingMicrosoft;

public class Main {
}


public class MyClass {
    public static void main(String args[]) {
        int x=10;
        int y=25;
        int z=x+y;

        System.out.println("Sum of x+y = " + z);
    }
}

class Interval {
    int start;
    int end;
}

class Calendar {
    List<Interval> occupiedIntervals;
}

class MeetingScheduler {

    public Interval suggestedIntervals(List<Calendar> calendars, int meetingDuration) {
        // for each 1 hour interval
        // for meetingDuration break it into 30 minutes chunk
        // check if it is available in all calendars i.e. not occupied
        // 0-30, 30-60, 60-90.....
        // store start and end as key-value in a map - but have to select a time quantum like 30 min
        // 30-60, 60-90, 90-120.....
        // store start and end as key-value in a map - but have to select a time quantum like 30 min
        // if its not return

        // 1 Calendar
        // check if it is available in 1 calendars i.e. not occupied
        // 0-30, 30-60, 60-90.....
        // store start and end as key-value in a map - but have to select a time quantum like 30 min
        // 30-60, 60-90, 90-120.....
        // store start and end as key-value in a map - but have to select a time quantum like 30 min
        // if its not return

        boolean[] mergedCalendar = createBitMap(calendars); // 48 slots
        int consecuteSlotsNeeded = meetingDuration/30;
        return getTheFirstInterval(mergedCalendar, consecuteSlotsNeeded);
    }

    private boolean[] createBitMap(List<Calendar> calendars) {
        boolean[] mergedCalendar = new boolean[48];
        Arrays.fill(mergedCalendar, true);
        for(Calendar calendar : calendars) {
            for(Interval i: calendar) {
                int startIdx = i.start/30;
                int duration = i.end-i.start;
                int chunks = duration/30;
                while(chunks > 0){
                    mergedCalendar[startIdx+chunks-1] = false;
                    chunks--;
                }
            }
        }
        return mergedCalendar;
    }

    private Interval getTheFirstInterval(boolean[] mergedCalendar, int slots) {
        int f = 0;
        int s = 0;
        //  1 is available
        while(s<mergedCalendar.length){
            if(mergedCalendar[s]){
                if(s-f == slots){
                    return new Interval(f*30, s*30);
                }
                s++;
            } else {
                s++;
                f=s;
            }
        }
    }
    //  0 1
    // 0-30 30-60
}