package hashcode.algo1;

import hashcode.City;
import hashcode.Parser;
import hashcode.Ride;
import hashcode.RideAssignment;
import hashcode.Vehicle;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

public class Greedy {

  private static class RidePair {
    Ride ride;
    Vehicle vehicle;
    int finishTime;

    public RidePair(Ride ride, Vehicle vehicle, int finishTime) {
      this.ride = ride;
      this.vehicle = vehicle;
      this.finishTime = finishTime;
    }
  }

  public static RideAssignment randomGreedySolution(City city) {
    RideAssignment bestRideAssignment = null;

    for (int i = 0; i < 10; i++) {
      RideAssignment rideAssignment = greedySolution(city);
      if (bestRideAssignment == null
          || rideAssignment.getScore() > bestRideAssignment.getScore()) {
        bestRideAssignment = rideAssignment;
      }
    }

    return bestRideAssignment;
  }

  public static RideAssignment greedySolution(City city) {
    RideAssignment rideAssignment = new RideAssignment();

    ArrayList<Ride> sortedRidesByStartTime = new ArrayList<Ride>(city.getRideList());
    sortedRidesByStartTime.sort((r1, r2) -> Integer.compare(r1.getEarliestStartTime(), r2.getEarliestStartTime()));

    ArrayDeque<Vehicle> freeVehicles = new ArrayDeque<>(city.getVehicleList());
    PriorityQueue<RidePair> busyVehicles = new PriorityQueue<>(
        (p1, p2) -> Integer.compare(p1.finishTime, p2.finishTime));

    long step = 0;
    while (step < city.getMaxSteps()) {
      Iterator<RidePair> busyIt = busyVehicles.iterator();
      while (busyIt.hasNext()) {
        RidePair ridePair = busyIt.next();

        if (step >= ridePair.finishTime) {
          busyIt.remove();
          freeVehicles.add(ridePair.vehicle);
          //System.out.printf("step %d: vehicle %d ride %d removed from busy\n", step, ridePair.vehicle.getIndex(), ridePair.ride.getIndex());
        } else {
          break;
        }
      }

      int curRideIndex = 0;
      for (int i = 0; i < sortedRidesByStartTime.size(); i++) {
        Ride ride = sortedRidesByStartTime.get(i);
        curRideIndex = i;
        if (step <= ride.getEarliestStartTime()) {
          break;
        }
      }

      int maxVehicles = freeVehicles.size();
      float coldness = 2;

      Iterator<Vehicle> freeIt = freeVehicles.iterator();
      while (freeIt.hasNext()) {
        Vehicle vehicle = freeIt.next();

        // Choose best ride for vehicle
        int bestScore = Integer.MIN_VALUE;
        Ride bestRide = null;
        for (int i = curRideIndex; i < sortedRidesByStartTime.size(); i++) {
          Ride ride = sortedRidesByStartTime.get(i);
          if (canPerformRide(city, (int) step, ride, vehicle)) {
            int rideScore = (int) (rideScoreMaxScore(city, (int) step, ride, vehicle) * (1f + ((0.5f - Math.random()) / coldness)));
            if (rideScore > bestScore) {
              bestScore = rideScore;
              bestRide = ride;
            }
          }
        }
        if (bestRide != null) {
          int rideFinish = rideFinishTime((int) step, bestRide, vehicle);
          freeIt.remove();
          sortedRidesByStartTime.remove(bestRide);
          busyVehicles.add(new RidePair(bestRide, vehicle, rideFinish));
          rideAssignment.addAssignment(vehicle, bestRide);
          rideAssignment.addScore(bestRide.getDistance() + (getsBonus((int) step, bestRide, vehicle) ? city.getPerRideBonus() : 0));
          //System.out.printf("step %d: vehicle %d ride %d added to busy finish at %d\n", step, vehicle.getIndex(), bestRide.getIndex(), rideFinish);
        }
        coldness += 10f / maxVehicles;
      }

      if (busyVehicles.size() <= 0) {
        break;
      }
      step = busyVehicles.peek().finishTime;
    }

    return rideAssignment;
  }

  private static int rideScoreMaxScore(City city, int step, Ride ride, Vehicle vehicle) {
    int distanceToRideStart = vehicle.getLocation().distanceTo(ride.getStartLocation());
    int timeUntilStart = ride.getEarliestStartTime() - step;

    int bonus = distanceToRideStart < timeUntilStart ? city.getPerRideBonus() : 0;
    int waitTime = Integer.max(timeUntilStart, distanceToRideStart);

    int rideScore = ride.getDistance() + bonus - waitTime;
    return rideScore;
  }

  private static float rideScoreMaxEfficiency(City city, int step, Ride ride, Vehicle vehicle) {
    int distanceToRideStart = vehicle.getLocation().distanceTo(ride.getStartLocation());
    int timeUntilStart = ride.getEarliestStartTime() - step;

    int bonus = distanceToRideStart < timeUntilStart ? city.getPerRideBonus() : 0;
    int waitTime = Integer.max(timeUntilStart, distanceToRideStart);

    int rideScore = ride.getDistance() + bonus;
    return (float) rideScore / (ride.getDistance() + waitTime);
  }

  private static boolean getsBonus(int step, Ride ride, Vehicle vehicle) {
    int distanceToRideStart = vehicle.getLocation().distanceTo(ride.getStartLocation());
    int timeUntilStart = ride.getEarliestStartTime() - step;

    return distanceToRideStart < timeUntilStart;
  }

  private static boolean canPerformRide(City city, int step, Ride ride, Vehicle vehicle) {
    int distanceToRideStart = vehicle.getLocation().distanceTo(ride.getStartLocation());
    int latestStartTime = ride.getLatestFinishTime() - ride.getDistance();

    int timeUntilStart = ride.getEarliestStartTime() - step;
    int waitTime = Integer.max(timeUntilStart, distanceToRideStart);

    int finishTime = waitTime + ride.getDistance();

    return distanceToRideStart < latestStartTime && finishTime < city.getMaxSteps();
  }

  private static int rideFinishTime(int step, Ride ride, Vehicle vehicle) {
    int distanceToRideStart = vehicle.getLocation().distanceTo(ride.getStartLocation());
    int timeUntilStart = ride.getEarliestStartTime() - step;

    int waitTime = Integer.max(timeUntilStart, distanceToRideStart);

    return waitTime + ride.getDistance() + step;
  }

}
