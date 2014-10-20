/*
 * Author: Kyoseong Ku <aust1nku@ucla.edu>
 * 
 * Description:
 *    This class is the back-end "meat" of the program. It handles the
 *    read/write of employee and business-related files and also creates and
 *    exports the MS Excel file containing the generated work schedule.
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Scanner;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.*;

public class ScheduleMaker {
  // Availability types: unavailable, alternate, preferred
  public static final int UNA = 0,
                          ALT = 1,
                          PRF = 2;
  
  public static final int HOURS_CLOSED = -1,
                          HOURS_24 = -2,
                          NUM_ROWS = 48;
  
  // The opening and closing times of the business
  // openClose[0][i]: the opening time for i-th day
  // openClose[1][i]: the closing time for i-th day
  public String[][] openCloseStr;
  public int[][]    openCloseInt;
  
  // Basically, false only if the program has never been run
  private Boolean hasHoursFile;
  
  public ArrayList<Employee> empList;
  private ArrayList<String>  posList;
  
//==============================================================================
  public ScheduleMaker() {
    empList = new ArrayList<Employee>();
    posList = new ArrayList<String>();
    openCloseStr = new String[2][7];
    openCloseInt = new int[2][7];
    
    // If the directory doesn't exist, create one
    // This directory holds all the employee files
    File empFolder = new File("employees");
    if(!empFolder.exists())
      empFolder.mkdir();
    
    File hoursFile = new File("hours.dat");
    if(hoursFile.exists()) {
      hasHoursFile = true;
      try {
        readHoursFile();
      }
      catch(Exception e) {
        GUI.msgPopup("CRITICAL ERROR! Program has terminated. See err output.");
        System.err.println("@ScheduleMaker/constructor/hours : " +e);
        System.exit(1);
      }
    }
    else
      hasHoursFile = false;
    
    try {
      readEmployees();
    }
    catch(Exception e) {
      GUI.msgPopup("CRITICAL ERROR! Program has terminated. See err output.");
      System.err.println("@ScheduleMaker/constructor/employees : " +e);
      System.exit(1);
    }
  }

//==============================================================================
//  Core class methods
//==============================================================================
  /**
   * Reads the business hours data from file
   */
  private void readHoursFile() throws Exception {
    Scanner hoursReader = new Scanner(new File("hours.dat"));
    for(int i = 0; i < 7; i++) {
      openCloseInt[0][i] = hoursReader.nextInt();
      openCloseInt[1][i] = hoursReader.nextInt();
      openCloseStr[0][i] = timeIntToStr(openCloseInt[0][i]);
      openCloseStr[1][i] = timeIntToStr(openCloseInt[1][i]);
    }
    hoursReader.close();
  }
  
  
  
  /**
   * Reads all of the employees' data from their respective files
   */
  private void readEmployees() throws Exception {
    String   codeRow, name, position, phone, lastSubmission, comment;
    Scanner  empReader;
    int      min, max;
    byte[][] avail;
    
    File empFolder = new File("employees");
    File[] empFiles = empFolder.listFiles();
    String fileName;
    
    for(int i = 0; i < empFiles.length; i++) {
      fileName = "employees/" +empFiles[i].getName();
      empReader = new Scanner(new File(fileName));
      
      name = empFiles[i].getName();
      position = empReader.nextLine();
      phone = empReader.nextLine();
      min = Integer.parseInt(empReader.nextLine());
      max = Integer.parseInt(empReader.nextLine());
      lastSubmission = empReader.nextLine();
      comment = empReader.nextLine();
      
      avail = new byte[NUM_ROWS][7];
      for(int r = 0; r < NUM_ROWS; r++) {
        codeRow = empReader.nextLine();
        for(int d = 0; d < 7; d++)
          avail[r][d] = Byte.parseByte(Character.toString(codeRow.charAt(d)));
      }
      
      empList.add(new Employee(name, position, phone, min, max,
                               lastSubmission, comment, avail));
      
      if(isNewPosition(position))
        posList.add(position);
      
      empReader.close();
    }
  }
  
  
  
  /**
   * Adds a new employee
   */
  public void addEmployee(String name, String position, String phone)
    throws Exception {
    
    Employee emp = new Employee(name, position, phone);
    FileWriter efw = new FileWriter("employees/" +emp.fileName());
    PrintWriter empWriter = new PrintWriter(new BufferedWriter(efw));
    empWriter.print(emp.fileData());
    empWriter.close();
    
    if(isNewPosition(position))
      posList.add(position); 
    empList.add(emp);
  }
  
  
  
  /**
   * Edits an existing employee
   * 
   * In the case the user inputs nothing (empty string, "") in the position
   * and/or the phone field the method Employee::edit method does not update
   * the Employee object's field(s).
   */
  public void editEmployee(Employee emp, String position, String phone)
    throws Exception {
    
    emp.edit(position, phone);
    FileWriter efw = new FileWriter("employees/" +emp.fileName());
    PrintWriter empWriter = new PrintWriter(new BufferedWriter(efw));
    empWriter.print(emp.fileData());
    empWriter.close();
  }
  
  
  
  /**
   * Removes one or more employees
   */
  public void removeEmployee(ArrayList<Employee> rList) throws Exception {
    Employee emp;
    for(int i = 0; i < rList.size(); i++) {
      emp = rList.get(i);
      File empFile = new File("employees/" +emp.fileName());
      empFile.delete();
      empList.remove(emp);
      if(!isHeldPosition(emp.getPosition()))
        posList.remove(emp.getPosition());
    }
  }
  
  
  
  /**
   * Creates or updates the business hours file
   */
  public void writeBusinessHours(String[][] openClose) throws Exception {
    FileWriter fw = new FileWriter("hours.dat");
    PrintWriter writer = new PrintWriter(new BufferedWriter(fw));
    for(int d = 0; d < 7; d++) {
        writer.print(timeStrToInt(openClose[0][d]) +"\n");
        writer.print(timeStrToInt(openClose[1][d]) +"\n");
    }
    writer.close();
    hasHoursFile = true;
    
    // Reread the new times and update the other time-dependent variables
    readHoursFile();
  }
  
  
  
  /**
   * Create and export the MS Excel file that contains the work schedule
   */
  public void export() throws Exception {
    Workbook wb = new XSSFWorkbook();
    CreationHelper helper = wb.getCreationHelper();
    Sheet sheet = wb.createSheet("main");
    ArrayList<Row> rows = new ArrayList<>();
    
    Cell tempCell;
    CellStyle tempCStyle;
    int row = 0;
    
    // Reused fonts
    String defFont = "Arial";
    short defFontSize = 12;
    Font titleFont = wb.createFont();
    titleFont.setFontHeightInPoints((short)(defFontSize*2));
    titleFont.setFontName(defFont);
    titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
    titleFont.setColor(IndexedColors.WHITE.getIndex());
    Font posFont = wb.createFont();
    posFont.setFontHeightInPoints((short)(defFontSize));
    posFont.setFontName(defFont);
    posFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
    posFont.setColor(IndexedColors.WHITE.getIndex());
    Font regFont = wb.createFont();
    regFont.setFontHeightInPoints(defFontSize);
    regFont.setFontName(defFont);
    regFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
    regFont.setColor(IndexedColors.BLACK.getIndex());
    
    // Create the title row
    rows.add(sheet.createRow(row));
    rows.get(row).createCell(0).setCellValue(
      helper.createRichTextString("Weekly Work Schedule"));
    tempCell = rows.get(row).getCell(0);
    tempCStyle = wb.createCellStyle();
    tempCStyle.setAlignment(CellStyle.ALIGN_CENTER);
    tempCStyle.setFont(titleFont);
    tempCStyle.setFillBackgroundColor(IndexedColors.DARK_BLUE.getIndex());
    tempCStyle.setFillPattern(CellStyle.BIG_SPOTS);
    tempCell.setCellStyle(tempCStyle);
    rows.get(row).setHeight((short)(rows.get(row).getHeight()*2));
    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));
    row++;
    
    // Row with column names
    rows.add(sheet.createRow(row));
    rows.get(row).createCell(0).setCellValue(
      helper.createRichTextString("Name"));
    rows.get(row).createCell(1).setCellValue(
      helper.createRichTextString("Phone #"));
    rows.get(row).createCell(2).setCellValue(
      helper.createRichTextString("Mon"));
    rows.get(row).createCell(3).setCellValue(
      helper.createRichTextString("Tue"));
    rows.get(row).createCell(4).setCellValue(
      helper.createRichTextString("Wed"));
    rows.get(row).createCell(5).setCellValue(
      helper.createRichTextString("Thu"));
    rows.get(row).createCell(6).setCellValue(
      helper.createRichTextString("Fri"));
    rows.get(row).createCell(7).setCellValue(
      helper.createRichTextString("Sat"));
    rows.get(row).createCell(8).setCellValue(
      helper.createRichTextString("Sun"));
    tempCStyle = wb.createCellStyle();
    tempCStyle.setFont(regFont);
    tempCStyle.setAlignment(CellStyle.ALIGN_CENTER);
    tempCStyle.setFillBackgroundColor(IndexedColors.PALE_BLUE.getIndex());
    for(int i = 0; i < 9; i++)
      rows.get(row).getCell(i).setCellStyle(tempCStyle);
    row++;
    
    // This will contain all the employees who hold the same position
    ArrayList<Employee> posEmpsList;
    
    // For each position that exists, write each employee's data
    for(int i = 0; i < posList.size(); i++) {
      // Create the row that displays the position title
      rows.add(sheet.createRow(row));
      rows.get(row).createCell(0).setCellValue(
        helper.createRichTextString(posList.get(i)));
      tempCell = rows.get(row).getCell(0);
      tempCStyle = wb.createCellStyle();
      tempCStyle.setAlignment(CellStyle.ALIGN_CENTER);
      tempCStyle.setFont(posFont);
      tempCStyle.setFillBackgroundColor(IndexedColors.PALE_BLUE.getIndex());
      tempCStyle.setFillPattern(CellStyle.FINE_DOTS);
      tempCell.setCellStyle(tempCStyle);
      sheet.addMergedRegion(new CellRangeAddress(row, row, 0, 8));
      row++;
      
      // Retrieve all the employees who hold the ith position
      posEmpsList = getPosEmps(posList.get(i));
      for(int j = 0; j < posEmpsList.size(); j++, row++) {
        // Create the row with employee j's data
        rows.add(sheet.createRow(row));
        rows.get(row).createCell(0).setCellValue(
          helper.createRichTextString(posEmpsList.get(j).displayName()));
        rows.get(row).createCell(1).setCellValue(
          helper.createRichTextString(posEmpsList.get(j).getPhone()));
        tempCStyle = wb.createCellStyle();
        tempCStyle.setFont(regFont);
        tempCStyle.setAlignment(CellStyle.ALIGN_CENTER);
        
        // For each day of the week, write employee j's work hours for day k
        for(int k = 0; k < 7; k++) {
          rows.get(row).createCell(k+2).setCellValue(
            helper.createRichTextString(posEmpsList.get(j).getAssignedHours(k)));
          rows.get(row).getCell(k).setCellStyle(tempCStyle);
        }
        // Cells 0-6 have been stylized in the for loop above.
        // Still need to stylize cells 7 and 8
        rows.get(row).getCell(7).setCellStyle(tempCStyle);
        rows.get(row).getCell(8).setCellStyle(tempCStyle);
      }
    }
    
    // Set column widths
    int defW = sheet.getColumnWidth(0);
    sheet.setColumnWidth(0, (int)(defW*1.9));
    sheet.setColumnWidth(1, (int)(defW*2.0));
    defW = sheet.getColumnWidth(8);
    for(int i = 0; i <= 6; i++)
      sheet.setColumnWidth(8-i, (int)(defW*1.8));
    
    String fileName = "schedule_" +getFileDate() +".xlsx";
    FileOutputStream writer = new FileOutputStream(fileName);
    wb.write(writer);
    writer.close();
  }
  
//==============================================================================
//  Helper methods
//==============================================================================
  public Boolean hoursFileExists() {
    return hasHoursFile;
  }
  
  public Boolean hasNoEmployees() {
    return empList.size() == 0;
  }
  
  
  
  /**
   * Finds an Employee object using the name. Returns null if not found
   */
  public Employee getEmployee(String name) {
    for(int i = 0; i < empList.size(); i++)
      if(empList.get(i).getName().equals(name))
        return empList.get(i);
    return null;
  }
  
  
  
  /**
   * Returns the day's date
   */
  public String getDate() {
    DateFormat dFormat = new SimpleDateFormat("MM/dd/yy");
    Date d = new Date();
    return dFormat.format(d);
  }
  
  
  
  /**
   * Takes an integer representation of time in 24-hour format and converts it
   * into a String representation
   */
  public String timeIntToStr(int time) {
    if(time == HOURS_CLOSED)
      return "Closed";
    if(time == HOURS_24)
      return "24 HR";
    
    String result;
    String end;
    int h = time/100;
    int m = time%100;
    
    if(h < 12)
      end = "AM";
    else
      end = "PM";
    
    if(h > 12)
      h -= 12;
    if(h == 0)
      h = 12;
    
    result = Integer.toString(h);
    result += ":";
    
    if(m == 0)
      result += "00";
    else
      result += Integer.toString(m);
    
    return result + end;
  }
  
  
  
  /**
   * Takes a String representation of time in either 12-hour format and
   * converts it into an integer representation
   */
  private int timeStrToInt(String time) {
    if(time.equals("Closed"))
      return HOURS_CLOSED;
    if(time.equals("24 HR"))
      return HOURS_24;
    
    int hours = 0;
    int minutes;
    
    if(time.charAt(time.length()-2) == 'P')
      hours = 12;
    
    Scanner timeScan = new Scanner(time.substring(0, time.length()-3));
    timeScan.useDelimiter(":");
    
    hours += timeScan.nextInt();
    minutes = timeScan.nextInt();
    
    if(hours == 12 && time.charAt(time.length()-2) == 'A')
      hours = 0;
    
    timeScan.close();
    return hours*100 + minutes;
  }
  
  
  
  /**
   * Checks if the position exists
   */
  private Boolean isNewPosition(String position) {
    for(int i = 0; i < posList.size(); i++) 
      if(position.equals(posList.get(i)))
        return false;
    return true;
  }
  
  
  
  /**
   * Checks if the position is currently held by an employee
   */
  private Boolean isHeldPosition(String position) {
    for(int i = 0; i < empList.size(); i++)
      if(empList.get(i).getPosition().equals(position))
        return true;
    return false;
  }
  
  
  
  /**
   * Takes a date and converts it into a format compatible as a filename
   */
  private String getFileDate() {
    String date = getDate();
    String formatted = "";
    
    for(int i = 0; i < date.length(); i++) {
      if(date.charAt(i) == '/')
        formatted += '-';
      else
        formatted += date.charAt(i);
    }
    
    return formatted;
  }
  
  
  
  /**
   * Returns a list of employees who hold the position
   */
  private ArrayList<Employee> getPosEmps(String position) {
    ArrayList<Employee> list = new ArrayList<>();
    for(int i = 0; i < empList.size(); i++)
      if(empList.get(i).getPosition().equals(position))
        list.add(empList.get(i));
    return list;
  }
}