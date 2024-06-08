package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;
import java.io.File;

public class WorkoutTracker {


    private final ArrayList<String> lifts;
    private final ArrayList<String> bools;
    private final File liftsFile;
    private HashMap<String, Float> liftsMap;
    private HashMap<String, Float> currentWorkoutMap;
    private Scanner scanner;

    public WorkoutTracker() {
        this.lifts = new ArrayList<>(Arrays.asList("Bench", "Row", "Overhead Press", "Chinups", "Squat", "Deadlift"));
        this.bools = new ArrayList<>(Arrays.asList("squatDay", "benchRowDay"));
        this.liftsFile = new File("lifts.json");
        this.liftsMap = getLiftsFromFile();
        this.currentWorkoutMap = getCurrentWorkout();
        this.scanner = new Scanner(System.in);
    }

    public HashMap<String, Float> getLiftsFromFile() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(liftsFile,
                    new TypeReference<>() {
                    });
        } catch (IOException e) {
            return new HashMap<>();
        }
    }

    public void writeLiftsToFile() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(liftsFile, liftsMap);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save lifts to file.");
        }
    }

    public void inputLifts() {
        System.out.println("Input your current weight for the following lifts or press enter if already correct:");
        for (String lift : lifts) {
            while (true) {
                Float currentLift = liftsMap.getOrDefault(lift, null);
                System.out.println(lift + String.format(": %s", currentLift));
                String response = scanner.nextLine();
                try {
                    Float weight = Float.parseFloat(response);
                    if (weight < 45F && !lift.equals("Chinups")) {
                        System.out.println("If you are using a real barbell, weights cannot be less than 45 pounds.");
                    } else {
                        liftsMap.put(lift, weight);
                        break;
                    }
                } catch (NumberFormatException e) {
                    if (response.isEmpty() && !Objects.isNull(currentLift)) {
                        break;
                    } else if (response.isEmpty()) {
                        System.out.println("This weight has not been set. Input a number to set to current weight.");
                    } else {
                        System.out.println("Number not detected. Please input a number.");
                    }
                }
            }
        }
        for (String bool : bools) {
            boolean squatDay = bool.equals("squatDay");
            String message = squatDay ? "Is it squat day? Y/N" : "Is it bench/row day? Y/N";
            if (squatDay) {
                getSquatResponse(bool, message);
            } else {
                getWorkoutOrder(bool, message);
            }
        }
    }

    public boolean validateInput() {
        System.out.println("Current lifts:" + getCurrentLifts());
        System.out.println("Next scheduled workout:" + getCurrentWorkout());
        System.out.println("Is this correct? Type N to reset or Y to continue");
        boolean notValidated = true;
        while (notValidated) {
            String response = scanner.next();
            scanner.nextLine();
            if (response.equals("N")) {
                inputLifts();
                notValidated = validateInput();
            } else if (response.equals("Y")) {
                break;
            } else {
                System.out.println("Input Y to continue or N to reset lifts.");
            }
        }
        return false;
    }

    public void getSquatResponse(String bool, String message) {
        System.out.println(message);
        while (true) {
            if (scanner.hasNext()) {
                String response = scanner.next();
                if (response.equals("Y")) {
                    while (true) {
                        System.out.println("Is it the first workout of the week? Y/N");
                        if (scanner.hasNext()) {
                            String squatResponse = scanner.next();
                            if (squatResponse.equals("Y")) {
                                liftsMap.put(bool, 0.0F);
                                break;
                            } else if (squatResponse.equals("N")) {
                                liftsMap.put(bool, 2F);
                                break;
                            } else {
                                System.out.println("Respond with Y or N.");
                            }
                        }
                    }
                    break;
                } else if (response.equals("N")) {
                    liftsMap.put(bool, 1F);
                    break;
                } else {
                    System.out.println("Respond with Y or N.");
                }
            }
        }
    }

    public void getWorkoutOrder(String bool, String message) {
        System.out.println(message);
        while (true) {
            if (scanner.hasNext()) {
                String response = scanner.next();
                if (response.equals("Y")) {
                    liftsMap.put(bool, 1F);
                    break;
                } else if (response.equals("N")) {
                    liftsMap.put(bool, (float) 0);
                    break;
                } else {
                    System.out.println("Respond with Y or N.");
                }
            }
        }
    }

    public HashMap<String, Float> getCurrentWorkout() {
        Float squatDay = liftsMap.get("squatDay");
        Float benchRowDay = liftsMap.get("benchRowDay");
        HashMap<String, Float> currentWorkout = new HashMap<>();
        if (squatDay == 0.0F || squatDay == 2F) {
            currentWorkout.put("Squat", liftsMap.get("Squat"));
        } else {
            currentWorkout.put("Deadlift", liftsMap.get("Deadlift"));
        }
        if (benchRowDay == 1F) {
            currentWorkout.put("Bench", liftsMap.get("Bench"));
            currentWorkout.put("Row", liftsMap.get("Row"));
        } else {
            currentWorkout.put("Chinups", liftsMap.get("Chinups"));
            currentWorkout.put("Overhead Press", liftsMap.get("Overhead Press"));
        }
        this.currentWorkoutMap = currentWorkout;
        return currentWorkout;
    }

    public void reportWorkout() {
        System.out.println("Have you completed the scheduled workout? Y/N");
        System.out.println(String.format("Scheduled workout: %s", currentWorkoutMap));
        while (true) {
            String response = scanner.next();
            if (response.equals("Y")) {
                reportLifts();
                alternateSquatAndBenchRowDay();
                break;
            } else if (response.equals("N")) {
                System.out.println("Here is how to setup the bar for the scheduled workout.");
                setUpBar();
                System.out.println("Come back to report your lifts when you've completed the workout.");
                break;
            } else {
                System.out.println("Input Y if you've completed the workout or N if you haven't.");
            }
        }
    }

    public void reportLifts() {
        for (String lift : currentWorkoutMap.keySet()) {
            while (true) {
                System.out.println(String.format("Did you complete at least 5 reps for all sets for lift: %s? Y/N", lift));
                String response = scanner.next();
                if (response.equals("Y")) {
                    addWeight(lift);
                    break;
                } else if (response.equals("N")) {
                    System.out.println(String.format("Failed set for lift: %s. Deloading weight by 10 percent.", lift));
                    Float deloadedWeight = roundWeight(liftsMap.get(lift) * 0.9F);
                    liftsMap.put(lift, deloadedWeight);
                    break;
                } else {
                    System.out.println("Input Y or N");
                }
            }
        }
    }

    public void addWeight(String lift) {
        while (true) {
            System.out.println(String.format("Did you complete 10 or more reps on your last set for lift: %s? Y/N", lift));
            String response = scanner.next();

            if (response.equals("Y")) {
                float increment = (lift.equals("Squat") || lift.equals("Deadlift")) ? 10F : 5F;
                liftsMap.put(lift, roundWeight(liftsMap.get(lift) + increment));
                break;
            } else if (response.equals("N")) {
                float increment = (lift.equals("Squat") || lift.equals("Deadlift")) ? 5F : 2.5F;
                liftsMap.put(lift, roundWeight(liftsMap.get(lift) + increment));
                break;
            } else {
                System.out.println("Input Y or N.");
            }
        }
    }

    public void alternateSquatAndBenchRowDay() {
        Float nextSquatDay = (liftsMap.get("squatDay") + 1F) % 3;
        liftsMap.put("squatDay", nextSquatDay);

        Float benchRowDay = (liftsMap.get("benchRowDay") == 0.0F ? 1F : 0.0F);
        liftsMap.put("benchRowDay", benchRowDay);
    }

    public void setUpBar() {
        ArrayList<String> weightsInOrder = new ArrayList<>(Arrays.asList("45", "35", "25", "10", "5", "2.5", "1.25"));
        for (String lift : currentWorkoutMap.keySet()) {
            HashMap<String, Integer> platesMap = new HashMap<>();
            Float total = currentWorkoutMap.get(lift);
            System.out.println(String.format("Total weight for %s: %s", lift, total));
            //Subtract 45 from total to account for the bar
            total = total - 45F;
            //Subtract 90 for each 45 pound plate. This can only happen once for Trey.
            if (total >= 90F) {
                total = total - 90F;
                platesMap.put("45", 1 + platesMap.getOrDefault("45", 0));
            }
            //Subtract 70 for each 35 pound plate. This can only happen once for Trey.
            if (total >= 70F) {
                total = total - 70F;
                platesMap.put("35", 1 + platesMap.getOrDefault("35", 0));
            }
            //Subtract 50 for each 25 pound plate. This can only happen once for Trey.
            if (total >= 50F) {
                total = total - 50F;
                platesMap.put("25", 1 + platesMap.getOrDefault("25",0));
            }
            //Subtract 20 for each 10 pound plate. This can only happen once for Trey.
            if (total >= 20F) {
                total = total - 20F;
                platesMap.put("10", 1 + platesMap.getOrDefault("10", 0));
            }
            //Subtract 10 for each 5 pound plate. This can happen a total of two times.
            for (int i = 0; i < 2; i++) {
                if (total >= 10F) {
                    total = total - 10F;
                    platesMap.put("5", 1 + platesMap.getOrDefault("5", 0));
                }
            }
            //Subtract 5 for each 2.5 pound plate. This can only happen once for Trey.
            if (total >= 5F) {
                total = total - 5F;
                platesMap.put("2.5", 1 + platesMap.getOrDefault("2.5", 0));
            }
            //Subtract 2.5 for reach 1.25 pound microplate. This can theoretically happen many times but in practice
            //should only be once.
            if (total >= 2.5F) {
                platesMap.put("1.25", 1 + platesMap.getOrDefault("1.25", 0));
            }
            for (String weight : weightsInOrder) {
                if (!Objects.isNull(platesMap.get(weight))) {
                    System.out.println(String.format("Use %d %s-pound plate(s) on each side.",
                            platesMap.get(weight),
                            weight));
                }
            }
        }
    }

    public Float roundWeight(Float weight) {
        return Math.round(weight / 2.5F) * 2.5F;
    }

    public HashMap<String, Float> getCurrentLifts() {
        return this.liftsMap;
    }
}
