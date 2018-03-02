package hashcode;

public class Ride {

  private final int index;
  private final Location startLocation;
  private final Location finishLocation;
  private final int distance;
  private final int earliestStartTime;
  private final int latestFinishTime;

  public Ride(int i, Location start, Location finish, int earliestStart,
      int latestFinish) {
    this.index = i;
    this.startLocation = start;
    this.finishLocation = finish;
    this.distance = Location.calculateDistance(start, finish);
    this.earliestStartTime = earliestStart;
    this.latestFinishTime = latestFinish;
  }

  public int getIndex() {
    return index;
  }

  public Location getStartLocation() {
    return startLocation;
  }

  public Location getFinishLocation() {
    return finishLocation;
  }

  public int getEarliestStartTime() {
    return earliestStartTime;
  }

  public int getLatestFinishTime() {
    return latestFinishTime;
  }

  public int getDistance() {
    return distance;
  }

  @Override
  public String toString() {
    return "hashcode.Ride{" +
        "index=" + index +
        ", startLocation=" + startLocation +
        ", finishLocation=" + finishLocation +
        ", earliestStartTime=" + earliestStartTime +
        ", latestFinishTime=" + latestFinishTime +
        "}\n";
  }

  public int getLatestStartTime() {
    return getLatestFinishTime() - getDistance();
  }
}
