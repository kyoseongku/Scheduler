/*
 * Author: Kyoseong Ku <aust1nku@ucla.edu>
 * 
 * Description:
 *    This class defines an Employee object.
 */

import java.util.Scanner;

public class Employee {
  private String nameFile,
                 nameFull,
                 nameDisplay,
                 position,
                 phone,
                 lastSubmission,
                 comment;
  
  private int minHours,
              maxHours;
  
  private byte[][] availCodes;
  
//==============================================================================
  /**
   * Constructor used to create a new employee via GUI
   */
  public Employee(String name, String position, String phone) {
    Scanner nameScan = new Scanner(name);
    String first = nameScan.next();
    String last = nameScan.next();
    nameScan.close();
    
    nameFile = first +"_" +last +".dat";
    nameFull = name;
    nameDisplay = first.substring(0, 1) +". " +last;
    this.position = position;
    this.phone = phone;
    minHours = 0;
    maxHours = 0;
    lastSubmission = "Never";
    comment = "None";
    
    availCodes = new byte[ScheduleMaker.NUM_ROWS][7];
    for(int r = 0; r < ScheduleMaker.NUM_ROWS; r++)
      for(int d = 0; d < 7; d++)
        availCodes[r][d] = ScheduleMaker.UNA;
  }
  
  
  
  /**
   * Constructor used to create an employee via existing file
   */
  public Employee(String name, String position, String phone, int min, int max,
                  String lastSubmission, String comment, byte[][] availCodes) {
    Scanner nameScan = new Scanner(name.substring(0, name.length()-4));
    nameScan.useDelimiter("_");
    String first = nameScan.next();
    String last = nameScan.next();
    nameScan.close();
    
    nameFile = name;
    nameFull = first +" " +last;
    nameDisplay = first.substring(0, 1) +". " +last;
    this.position = position;
    this.phone = phone;
    minHours = min;
    maxHours = max;
    this.lastSubmission = lastSubmission;
    this.comment = comment;
    this.availCodes = availCodes;
  }
  
  
  
  public String getName() {
    return nameFull;
  }
  
  public String getPosition() {
    return position;
  }
  
  public String getPhone() {
    return phone;
  }
  
  public String getComment() {
    return comment;
  }
  
  public String getLastSub() {
    return lastSubmission;
  }
  
  public String fileName() {
    return nameFile;
  }
  
  public String displayName() {
    return nameDisplay;
  }
  
  public int[] requestedHours() {
    int[] pair = {minHours, maxHours};
    return pair;
  }
  
  public String getAssignedHours(int day) {
    return "xx:xx-xx:xx";
  }
  
  public String fileData() {
    String buffer = position +"\n" +phone +"\n" +minHours +"\n" +maxHours +"\n"
                    +lastSubmission +"\n" +comment +"\n";
    
    for(int r = 0; r < ScheduleMaker.NUM_ROWS; r++) {
      for(int d = 0; d < 7; d++)
        buffer += availCodes[r][d];
      buffer += "\n";
    }
    
    return buffer;
  }
  
  public int avail(int r, int d) {
    return availCodes[r][d];
  }
  
  public void edit(String position, String phone) {
    if(!position.equals(""))
      this.position = position;
    if(!phone.equals(""))
      this.phone = phone;
  }
  
  public void edit(int min, int max, String comm, String lastSub, byte[][] c) {
    minHours = min;
    maxHours = max;
    comment = comm;
    lastSubmission = lastSub;
    availCodes = c;
  }
}