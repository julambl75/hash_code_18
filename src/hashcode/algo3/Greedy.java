package hashcode.algo3;

import hashcode.City;
import hashcode.Location;
import hashcode.Ride;
import hashcode.RideAssignment;
import hashcode.Vehicle;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

public class Greedy {

  private static int numC;
  private static int numR;
  private static int stepBuckets = 10;
  private static int buckets = 10;
  private static int perRideBonus;
  private static int maxSteps;
  private static List<Vehicle> vehicles;
  private static int numRides;

  // Precomputed
  private static Location center;
  private static int iqr;
  private static float[][][] rideDensity;

  public static RideAssignment randomSolution(City city) {
    RideAssignment bestRideAssignment = null;

    for (int i = 0; i < 10; i++) {
      RideAssignment rideAssignment = solution(city);
      System.out.println(rideAssignment.getScore());
      if (bestRideAssignment == null
          || rideAssignment.getScore() > bestRideAssignment.getScore()) {
        bestRideAssignment = rideAssignment;
      }
    }

    return bestRideAssignment;
  }

  public static RideAssignment solution(City city) {
    numC = city.getCols();
    numR = city.getRows();
    perRideBonus = city.getPerRideBonus();
    maxSteps = city.getMaxSteps();
    vehicles = city.getVehicleList();
    numRides = city.getRideList().size();

    precalculate(city);

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

  private static void precalculate(City city) {
    List<Ride> rideList = city.getRideList();
    long sumC = 0;
    long sumR = 0;
    for (Ride ride : rideList) {
      sumC += ride.getStartLocation().getC();
      sumC += ride.getFinishLocation().getR();
      sumR += ride.getStartLocation().getC();
      sumR += ride.getFinishLocation().getR();
    }
    int avgC = (int) (sumC / (rideList.size() * 2));
    int avgR = (int) (sumR / (rideList.size() * 2));
    //System.out.println("c " + avgC);
    //System.out.println("r " + avgR);

    // Sort ride list by c ascending
    List<Ride> csorted = new ArrayList<>(rideList);
    csorted.sort((r1, r2) -> Integer.compare(r1.getStartLocation().getC(), r2.getStartLocation().getC()));

    // Sort ride list by r ascending
    List<Ride> rsorted = new ArrayList<>(rideList);
    rsorted.sort((r1, r2) -> Integer.compare(r1.getStartLocation().getR(), r2.getStartLocation().getR()));

    //System.out.println("cm " + csorted.get(csorted.size() / 2).getStartLocation().getC());
    //System.out.println("rm " + rsorted.get(rsorted.size() / 2).getStartLocation().getR());

    //System.out.println("iqr " + (rsorted.get((rsorted.size() * 3) / 4).getStartLocation().getR() - rsorted.get(rsorted.size() / 4).getStartLocation().getR()));
    int cm = csorted.get(csorted.size() / 2).getStartLocation().getC();
    int rm = csorted.get(csorted.size() / 2).getStartLocation().getR();
    center = new Location(cm, rm);
    iqr = rsorted.get((rsorted.size() * 3) / 4).getStartLocation().getR() - rsorted.get(rsorted.size() / 4).getStartLocation().getR();


    int[][][] rideDensityCount = new int[stepBuckets][buckets][buckets];
    int[] maxCount = new int[stepBuckets];
    for (Ride ride : rideList) {
      Location startLocation = ride.getStartLocation();
      int sb = (int) (((float) ride.getLatestStartTime() / maxSteps) * stepBuckets);
      int cb = (int) (((float) startLocation.getC() / city.getCols()) * buckets);
      int rb = (int) (((float) startLocation.getR() / city.getRows()) * buckets);
      rideDensityCount[sb][cb][rb]++;
      if (rideDensityCount[sb][cb][rb] > maxCount[sb]) {
        maxCount[sb] = rideDensityCount[sb][cb][rb];
      }
    }

    rideDensity = new float[stepBuckets][buckets][buckets];
    for (int s = 0; s < stepBuckets; s++) {
      for (int c = 0; c < buckets; c++) {
        for (int r = 0; r < buckets; r++) {
          rideDensity[s][c][r] = (float) rideDensityCount[s][c][r] / maxCount[s];
        }
      }
    }
  }

  private static Location vehicleMedian() {
    List<Vehicle> csorted = new ArrayList<>(vehicles);
    csorted.sort((v1, v2) -> Integer.compare(v1.getLocation().getC(), v2.getLocation().getC()));

    List<Vehicle> rsorted = new ArrayList<>(vehicles);
    rsorted.sort((v1, v2) -> Integer.compare(v1.getLocation().getC(), v2.getLocation().getC()));

    int mc = csorted.get(csorted.size() / 2).getLocation().getC();
    int mr = rsorted.get(rsorted.size() / 2).getLocation().getR();
    return new Location(mc, mr);
  }

  private static float getRideDensity(int step, Location location) {
    int sb = (int) ((float) step / maxSteps * stepBuckets);
    int cb = (int) (((float) location.getC() / numC) * buckets);
    int rb = (int) (((float) location.getR() / numR) * buckets);
    return rideDensity[sb][cb][rb];
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

    Location vehicleMedian = vehicleMedian();
    List<Vehicle> temp = new ArrayList<>(idleVehicles);
    temp.sort((v1, v2) -> Integer.compare(v1.getLocation().distanceTo(vehicleMedian), v2.getLocation().distanceTo(vehicleMedian)));
    idleVehicles.clear();
    for (Vehicle vehicle : temp) {
      idleVehicles.add(vehicle);
    }

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

    float rd = getRideDensity(step, ride.getFinishLocation());

    float rand = 1 + (float) (0.5f - Math.random()) / 10f;

    return (int) (rand * Integer.MAX_VALUE * ((Math.pow((float) (ride.getDistance() + bonus), 2f/3)) /
        (waitTime + ride.getDistance() + Math.sqrt(ride.getFinishLocation().distanceTo(center)))));
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
