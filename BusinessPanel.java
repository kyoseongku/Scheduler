/*
 * Author: Kyoseong Ku <aust1nku@ucla.edu>
 * 
 * Description:
 *    This class consists of the GUI panel that allows users to perform
 *    functions on the business information, such as the editing of store hours
 *    and setting up scheduling constraints.
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

public class BusinessPanel extends JPanel {
  // Default serial version
  private static final long serialVersionUID = 1L;
  
  private int mainPanelWidth;
  
  private ScheduleMaker sm;
  
  private OptionListener oListener;
  private JButton        hoursButton,
                         constraintButton,
                         saveButton,
                         selectedButton;
  
  private JComboBox<String>[][] hoursBox;

//==============================================================================
  public BusinessPanel(ScheduleMaker sm, Dimension dim, Border b) {
    this.sm = sm;
    mainPanelWidth = (int)dim.getWidth()-2*GUI.PANEL_BORDER_SIZE;
    setPreferredSize(dim);
    setBackground(Color.WHITE);
    setBorder(b);
    setLayout(new BorderLayout());
    
    oListener = new OptionListener();
    add(setupOptionsPanel(), BorderLayout.NORTH);
    add(setupSavePanel(), BorderLayout.SOUTH);
    
    String msg;
    if(sm.hoursFileExists()) {
      msg = "Please select an option from above.";
      add(GUI.msgPanel(msg, null), BorderLayout.CENTER);
    }
    else {
      selectedButton = hoursButton;
      hoursButton.setBackground(GUI.HIGHLIGHT);
      add(setupHoursPanel(), BorderLayout.CENTER);
      
      msg = "This is your first time running this program. "
          +"Please set your business hours first.";
      GUI.msgPopup(msg);
    }
  }
  
  
  
//==============================================================================
//  Methods that set up the panels
//==============================================================================
  private JPanel setupOptionsPanel() {
    JPanel panel = new JPanel();
    panel.setBackground(Color.WHITE);
    
    int numButtons = 2;
    int buttonW = (mainPanelWidth/numButtons)-(4*numButtons);
    Dimension buttonDim = new Dimension(buttonW, GUI.TEXT_WRAP_HEIGHT);
    Border buttBorder = BorderFactory.createMatteBorder(0, 1, 1, 1, Color.GRAY);

    // When adding new buttons or deleting existing ones, make sure to update
    // the numButtons variable above.
    hoursButton = new JButton("Edit business hours");
    hoursButton.setBorder(buttBorder);
    hoursButton.setPreferredSize(buttonDim);
    hoursButton.setBackground(Color.WHITE);
    hoursButton.addMouseListener(oListener);
    constraintButton = new JButton("Edit scheduling constraints");
    constraintButton.setBorder(buttBorder);
    constraintButton.setPreferredSize(buttonDim);
    constraintButton.setBackground(Color.WHITE);
    constraintButton.addMouseListener(oListener);
    
    panel.add(hoursButton);
    panel.add(constraintButton);
    return panel;
  }
  
  
  
  private JPanel setupSavePanel() {
    JPanel panel = new JPanel();
    panel.setBackground(Color.WHITE);
    
    Dimension buttDim = new Dimension(mainPanelWidth-12, GUI.TEXT_WRAP_HEIGHT);
    Border buttBorder = BorderFactory.createMatteBorder(1, 1, 0, 1, Color.GRAY);
    
    saveButton = new JButton("Save");
    saveButton.setBorder(buttBorder);
    saveButton.setPreferredSize(buttDim);
    saveButton.setBackground(Color.WHITE);
    saveButton.addMouseListener(oListener);
    
    panel.add(saveButton);
    return panel;
  }
  
  
  // Warning suppressed for hoursBox initialization
  @SuppressWarnings("unchecked")
  private JPanel setupHoursPanel() {
    JPanel panel = new JPanel();
    panel.setBackground(Color.WHITE);
    panel.setLayout(new GridLayout(8, 3, 10, 10));
    
    String[] hours = getTimeRange(0, 2330);
    hoursBox = (JComboBox<String>[][]) new JComboBox[2][7];
    HoursBoxListener hbl = new HoursBoxListener();
    
    for(int t = 0; t < 2; t++)
      for(int d = 0; d < 7; d++) {
        hoursBox[t][d] = new JComboBox<String>();
        hoursBox[t][d].setBackground(Color.WHITE);
        hoursBox[t][d].addItem("Closed");
        hoursBox[t][d].addItem("24 HR");
        for(int i = 0; i < hours.length; i++)
          hoursBox[t][d].addItem(hours[i]);
      }
    
    // Set each combo box to appropriate time
    int index;
    if(sm.hoursFileExists()) {
      for(int t = 0; t < 2; t++)
        for(int d = 0; d < 7; d++) {
          if(sm.openCloseInt[t][d] == ScheduleMaker.HOURS_CLOSED)
            hoursBox[t][d].setSelectedIndex(0);
          else if(sm.openCloseInt[t][d] == ScheduleMaker.HOURS_24)
            hoursBox[t][d].setSelectedIndex(1);
          else {
            index = (sm.openCloseInt[t][d]/100)*2;
            
            // The index for x:30 is 1 after x:00
            if(sm.openCloseInt[t][d] % 100 == 30)
              index++;
            
            // To skip the two indices for "Closed" and "24 HR" options
            index += 2;
            
            hoursBox[t][d].setSelectedIndex(index);
          }
        }
    }
    
    for(int d = 0; d < 7; d++) {
      hoursBox[0][d].addActionListener(hbl);
      hoursBox[1][d].addActionListener(hbl);
    }
    
    String days[] = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
                     "Saturday", "Sunday"};
    
    // A blank msgPanel in the top-left corner
    panel.add(GUI.msgPanel(null, null));
    panel.add(new JLabel("Opening time"));
    panel.add(new JLabel("Closing time"));
    
    for(int d = 0; d < 7; d++) {
      panel.add(new JLabel(days[d]));
      for(int t = 0; t < 2; t++)
        panel.add(hoursBox[t][d]);
    }
    
    Dimension tbPadding = new Dimension(100, 100);
    Dimension sPadding = new Dimension(225, 225);
    
    JPanel outerPanel = new JPanel();
    outerPanel.setLayout(new BorderLayout());
    outerPanel.add(GUI.msgPanel(null, tbPadding), BorderLayout.NORTH);
    outerPanel.add(GUI.msgPanel(null, tbPadding), BorderLayout.SOUTH);
    outerPanel.add(GUI.msgPanel(null, sPadding), BorderLayout.WEST);
    outerPanel.add(GUI.msgPanel(null, sPadding), BorderLayout.EAST);
    outerPanel.add(panel, BorderLayout.CENTER);
    
    return outerPanel;
  }
  
  
  
  private JPanel setupConstraintsPanel() {
    JPanel panel = new JPanel();
    panel.setBackground(Color.WHITE);
    panel.add(GUI.msgPanel(null, new Dimension(200, 200)));
    
    String msg1 = "This option's purpose is to load any existing contraints "
                 +"from a file onto the display, then allow users to create, "
                 +"edit, or remove constraints.";
    panel.add(new JLabel(msg1));
    
    String msg2 = "A constraint is something like limiting the automatic "
                  +"scheduler to assign a maximum of 2 supervisors on a "
                  +"Sunday, for example.";
    panel.add(new JLabel(msg2));
    
    return panel;
  }
  
//==============================================================================
//  Methods for each option
//==============================================================================
  private void editBusinessHours() {
    GUI.removeCenter((BorderLayout)getLayout(), this,
      "BusinessPanel/editBusinessHours");
    add(setupHoursPanel(), BorderLayout.CENTER);
    revalidate();
  }
  
  
  
  private void editConstraints() {
    GUI.removeCenter((BorderLayout)getLayout(), this,
      "BusinessPanel/editConstraints");
    add(setupConstraintsPanel(), BorderLayout.CENTER);
    revalidate();
  }
  
  
  
  private void saveHours() {
    String hours[][] = new String[2][7];
    int index;
    
    for(int d = 0; d < 7; d++)
      for(int t = 0; t < 2; t++) {
        index = hoursBox[t][d].getSelectedIndex();
        hours[t][d] = hoursBox[t][d].getItemAt(index);
      }
    
    try {
      sm.writeBusinessHours(hours);
    }
    catch(Exception e) {
      GUI.msgPopup("ERROR @ BusinessPanel/saveHours() : " +e);
      return;
    }
    
    GUI.msgPopup("Business hours saved");
  }
  
  
  
  private void saveConstraints() {
    GUI.msgPopup("Constraint functionality not implemented");
  }

//==============================================================================
//  Listeners
//==============================================================================
  private class OptionListener implements MouseListener {
    @Override
    public void mouseClicked(MouseEvent e) {
      JButton src = (JButton)e.getSource();
      
      if(!src.equals(saveButton)) {
        if(selectedButton != null)
          selectedButton.setBackground(Color.WHITE);
        src.setBackground(GUI.HIGHLIGHT);
        selectedButton = src;
      }
      
      if(src.equals(hoursButton))
        editBusinessHours();
      else if(src.equals(constraintButton))
        editConstraints();
      else if(src.equals(saveButton) && selectedButton == null)
        GUI.msgPopup("Nothing to save");
      else if(src.equals(saveButton) && selectedButton.equals(hoursButton))
        saveHours();
      else if(src.equals(saveButton) && selectedButton.equals(constraintButton))
        saveConstraints();
      else;
    }
  
    @Override
    public void mouseEntered(MouseEvent e) {
      JButton src = (JButton)e.getSource();
      if(!src.equals(selectedButton))
        src.setBackground(GUI.HIGHLIGHT);
    }
  
    @Override
    public void mouseExited(MouseEvent e) {
      JButton src = (JButton)e.getSource();
      if(!src.equals(selectedButton))
        src.setBackground(Color.WHITE);
    }
    
    @Override
    public void mousePressed(MouseEvent e){}
    @Override
    public void mouseReleased(MouseEvent e){}
  }
  
  
  
  private class HoursBoxListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      @SuppressWarnings("unchecked")
      JComboBox<String> b = (JComboBox<String>)e.getSource();
      
      if(b.getSelectedIndex() == 0 || b.getSelectedIndex() == 1)
        for(int d = 0; d < 7; d++) {
          if(b.equals(hoursBox[0][d]))
            hoursBox[1][d].setSelectedIndex(b.getSelectedIndex());
          else if(b.equals(hoursBox[1][d]))
            hoursBox[0][d].setSelectedIndex(b.getSelectedIndex());
          else;
        }
    }
  }
//==============================================================================
//  Private methods
//==============================================================================
  /**
   * Converts the input time, which uses the 24-hour clock format, into a String
   * object that uses the 12-hour format
   */
  private String from24to12(int time) {
    int hour = time / 100;
    int min = time % 100;
    Boolean pm = false;

    if(hour >= 12)
      pm = true;
    if(hour > 12)
      hour -= 12;
    if(hour == 0)
      hour = 12;

    String output = hour +":";
    if(min == 0)
      output += "00";
    else
      output += min;
    
    if(pm)
      output += " PM";
    else
      output += " AM";

    return output;
  }
  
  
  
  /**
   * Returns a String[] of times in 12-hour format in 30-minute intervals.
   * Minimum input value is 0 and maximum input value is 2330.
   * The returned array wraps around, meaning you can start the range at 2000
   * and end at 430. It will return times from 8:00 PM to 4:30 AM.
   * Correct usage is assumed.
   * 
   * @param start  Time in 24-hour format in the format xx00 or xx30 
   * @param end    Time in 24-hour format in the format xx00 or xx30
   * @return       Times in String[] from {@code start} to {@code end}
   */
  private String[] getTimeRange(int start, int end) {
    // Commented out because correct usage is assumed
    //if(start < 0 || start > 2330 ||
    //   end < 0 || end > 2330 ||
    //   (start%100 != 0 && start%100 != 30) ||
    //   (end%100 != 0 && end%100 != 30))
    //  return null;
    
    ArrayList<String> timeList = new ArrayList<String>();
    int time = start;
    
    timeList.add(from24to12(time));
    while(time >= 0) {
      if(time % 100 == 0)
        time += 30;
      else
        time += 70;
      
      if(time == 2400)
        time = 0;
      
      timeList.add(from24to12(time));
      
      // To quit the loop
      if(time == end)
        time = -1;
    }
    
    return timeList.toArray(new String[timeList.size()]);
  }
}