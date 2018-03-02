package hashcode;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Parser {

  private Parser() {

  }


  public static City parseCity(String fileName) {
    try {
      Scanner scanner = new Scanner(new FileReader(fileName));
      int rows = scanner.nextInt();
      int cols = scanner.nextInt();
      int numVehicles = scanner.nextInt();
      int numRides = scanner.nextInt();
      int perRideBonus = scanner.nextInt();
      int maxSteps = scanner.nextInt();
      ArrayList<Ride> rides = parseRides(scanner, numRides);

      return new City(rows, cols, perRideBonus, maxSteps, numVehicles, rides);
    } catch (FileNotFoundException e) {
      throw new RuntimeException("parseCity error");
    }
  }

  private static ArrayList<Ride> parseRides(Scanner scanner, int numRides) {
    ArrayList<Ride> rides = new ArrayList<>();
    for (int i = 0; i < numRides; i++) {
      int c1 = scanner.nextInt();
      int r1 = scanner.nextInt();
      int c2 = scanner.nextInt();
      int r2 = scanner.nextInt();
      Location startLocation = new Location(r1, c1);
      Location finishLocation = new Location(r2, c2);

      int earliestStartTime = scanner.nextInt();
      int latestFinishTime = scanner.nextInt();
      Ride ride = new Ride(i, startLocation, finishLocation,
          earliestStartTime, latestFinishTime);
      rides.add(ride);
    }
    return rides;
  }

}
