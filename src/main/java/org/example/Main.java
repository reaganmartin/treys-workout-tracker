package org.example;

import java.io.IOException;
import java.util.HashMap;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws IOException {
        //Git change
        System.out.println("Trey's Workout Tracker Starting");

        WorkoutTracker tracker = new WorkoutTracker();
        HashMap<String, Float> currentLifts = tracker.getLiftsFromFile();
        if (currentLifts.isEmpty()) {
            System.out.println("No lifts have been entered yet or have been reset.");
            tracker.inputLifts();
        }
        tracker.validateInput();
        tracker.writeLiftsToFile();
        tracker.reportWorkout();
        tracker.writeLiftsToFile();
    }
}