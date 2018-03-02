package hashcode.algo3;

import hashcode.City;
import hashcode.Ride;
import hashcode.RideAssignment;
import hashcode.Vehicle;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

public class Greedy {

  private static int perRideBonus;
  private static int maxSteps;

  public static RideAssignment solution(City city) {
    perRideBonus = city.getPerRideBonus();
    maxSteps = city.getMaxSteps();

    // Sort ride list by earliest start time ascending
    List<Ride> rides = new ArrayList<>(city.getRideList());
    rides.sort((r1, r2) -> Integer.compare(r1.getEarliestStartTime(), r2.getEarliestStartTime()));

    ArrayDeque<Vehicle> idleVehicles = new ArrayDeque<>(city.getVehicleList());

    PriorityQueue<VehicleTimePair> activeRides = new PriorityQueue<>((p1, p2) -> Integer.compare(p1.finishTime, p2.finishTime));

    RideAssignment rideAssignment = new RideAssignment();

    int step = 0;
    while (step < maxSteps) {
      removeLateRides(rides, step);
      updateActiveRides(activeRides, idleVehicles, step);
      assignRides(rideAssignment, activeRides, idleVehicles, rides, step);

      if (activeRides.size() == 0) {
        break;
      }
      step = activeRides.peek().finishTime;
    }

    return rideAssignment;
  }

  private static void removeLateRides(List<Ride> rides, int step) {
    Iterator<Ride> it = rides.iterator();
    while (it.hasNext()) {
      Ride ride = it.next();
      if (step > ride.getLatestStartTime()) {
        it.remove();
      } else {
        break;
      }
    }
  }

  private static void updateActiveRides(PriorityQueue<VehicleTimePair> activeRides,
      ArrayDeque<Vehicle> idleVehicles, int step) {
    Iterator<VehicleTimePair> it = activeRides.iterator();
    while (it.hasNext()) {
      VehicleTimePair vehicleTimePair = it.next();
      if (step >= vehicleTimePair.finishTime) {
        it.remove();
        idleVehicles.add(vehicleTimePair.vehicle);
      } else {
        break;
      }
    }
  }

  private static void assignRides(RideAssignment rideAssignment,
      PriorityQueue<VehicleTimePair> activeRides,
      ArrayDeque<Vehicle> idleVehicles, List<Ride> rides, int step) {
    Iterator<Vehicle> it = idleVehicles.iterator();
    while (it.hasNext()) {
      Vehicle vehicle = it.next();
      RideScorePair bestRide = new RideScorePair(null, Integer.MIN_VALUE);
      for (Ride ride : rides) {
        if (canPerformRide(ride, vehicle, step)) {
          int score = rideScore(ride, vehicle, step);
          if (score > bestRide.score) {
            bestRide.ride = ride;
            bestRide.score = score;
          }
        }
      }
      if (bestRide.ride != null) {
        it.remove();
        rides.remove(bestRide.ride);
        int finishTime = rideFinishTime(bestRide.ride, vehicle, step);
        activeRides.add(new VehicleTimePair(vehicle, finishTime));

        // Add ride assignment
        rideAssignment.addAssignment(vehicle, bestRide.ride);
        int score = bestRide.ride.getDistance() + rideGetBonus(bestRide.ride, vehicle, step);
        rideAssignment.addScore(score);

        // Update vehicle location
        vehicle.setLocation(bestRide.ride.getFinishLocation());
      }
    }
  }

  private static int rideScore(Ride ride, Vehicle vehicle, int step) {
    int distanceToRideStart = vehicle.getLocation().distanceTo(ride.getStartLocation());
    int timeUntilStart = ride.getEarliestStartTime() - step;

    int bonus = rideGetBonus(ride, vehicle, step);
    int waitTime = Integer.max(timeUntilStart, distanceToRideStart);

    return (int) (Integer.MAX_VALUE * (Math.pow((float) (ride.getDistance() + bonus), 2f/3) / (waitTime + ride.getDistance())));
  }

  private static int rideGetBonus(Ride ride, Vehicle vehicle, int step) {
    int distanceToRideStart = vehicle.getLocation().distanceTo(ride.getStartLocation());
    int timeUntilStart = ride.getEarliestStartTime() - step;

    return distanceToRideStart <= timeUntilStart ? perRideBonus : 0;
  }

  private static boolean canPerformRide(Ride ride, Vehicle vehicle, int step) {
    int distanceToRideStart = vehicle.getLocation().distanceTo(ride.getStartLocation());
    int finishTime = rideFinishTime(ride, vehicle, step);
    int timeToLatestStartTime = ride.getLatestStartTime() - step;

    return distanceToRideStart <= timeToLatestStartTime && finishTime <= maxSteps;
  }

  private static int rideFinishTime(Ride ride, Vehicle vehicle, int step) {
    int distanceToRideStart = vehicle.getLocation().distanceTo(ride.getStartLocation());
    int waitTime = ride.getEarliestStartTime() - step;

    int timeUntilStart = Integer.max(waitTime, distanceToRideStart);

    return timeUntilStart + ride.getDistance() + step;
  }

  private static class VehicleTimePair {

    private Vehicle vehicle;
    private int finishTime;

    public VehicleTimePair(Vehicle vehicle, int finishTime) {
      this.vehicle = vehicle;
      this.finishTime = finishTime;
    }
  }

  private static class RideScorePair {

    private Ride ride;
    private int score;

    public RideScorePair(Ride ride, int score) {
      this.ride = ride;
      this.score = score;
    }
  }
}
