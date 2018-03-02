package hashcode;

import java.util.ArrayList;
import java.util.List;

public class City {

  private final int rows;
  private final int cols;
  private final int perRideBonus;
  private final int maxSteps;
  private final List<Vehicle> vehicleList;
  private final ArrayList<Ride> rideList;

  public City(int rows, int cols, int perRideBonus, int maxSteps,
      int numVehicles, ArrayList<Ride> rideList) {
    this.rows = rows;
    this.cols = cols;
    this.perRideBonus = perRideBonus;
    this.maxSteps = maxSteps;
    this.vehicleList = initVehicleList(numVehicles);
    this.rideList = rideList;
  }

  private List<Vehicle> initVehicleList(int numVehicles) {
    List<Vehicle> vehicles = new ArrayList<>();
    for (int i = 0; i < numVehicles; i++) {
      vehicles.add(new Vehicle(new Location(0, 0), i));
    }
    return vehicles;
  }

  public int getRows() {
    return rows;
  }

  public int getCols() {
    return cols;
  }

  public int getPerRideBonus() {
    return perRideBonus;
  }

  public List<Vehicle> getVehicleList() {
    return vehicleList;
  }

  public List<Ride> getRideList() {
    return rideList;
  }

  public int getMaxSteps() {
    return maxSteps;
  }

  @Override
  public String toString() {
    return "\nhashcode.City{" +
        "\nrows=" + rows +
        "\ncols=" + cols +
        "\nperRideBonus=" + perRideBonus +
        "\nvehicleList=" + vehicleList +
        "\nrideList=" + rideList +
        "\nmaxSteps=" + maxSteps +
        "\n}";
  }
}
