/*
 * Author: Kyoseong Ku <aust1nku@ucla.edu>
 * 
 * Description:
 *    This class starts the graphic user interface which handles everything.
 */

import javax.swing.JFrame;

public class Driver {
  public static void main(String[] args) {
    GUI frame = new GUI();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setResizable(false);
    frame.setVisible(true);
  }
}