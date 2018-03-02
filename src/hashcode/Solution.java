package hashcode;

import hashcode.algo1.Greedy;
import hashcode.algo2.TraverseRides;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;


public class Solution {

  private static final String INPUT_EXT = ".in";
  private static final String OUTPUT_EXT = ".out";
  private static int scoreSum = 0;

  public static void main(String[] args) {
    List<String> filenames = new ArrayList<>();
    filenames.add("input_data/a_example");
    filenames.add("input_data/b_should_be_easy");
    filenames.add("input_data/c_no_hurry");
    filenames.add("input_data/d_metropolis");
    filenames.add("input_data/e_high_bonus");

    for (String filename : filenames) {
      generateSolution(filename);
    }

    System.out.printf("Total score: %d\n", scoreSum);
  }

  private static void generateSolution(String filename) {
    City city = Parser.parseCity(filename + INPUT_EXT);
    RideAssignment assignment =  hashcode.algo3.Greedy.solution(city);

    String solution = assignment.getSolution();

    int score = assignment.getScore();
    scoreSum += score;
    System.out.printf("%s - score %d\n", filename, score);

    try {
      PrintWriter printWriter = new PrintWriter(filename + OUTPUT_EXT);
      printWriter.println(solution);
      printWriter.close();
    } catch (FileNotFoundException e) {
      throw new RuntimeException("generateSolution error");
    }
  }

}
