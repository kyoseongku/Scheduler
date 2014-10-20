/*
 * Author: Kyoseong Ku <aust1nku@ucla.edu>
 * 
 * Description:
 *    This class consists of the GUI panel that allows users to enter the
 *    slots of time that they're available to work, as well as other information
 *    related to work scheduling.
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;

public class AvailabilityPanel extends JPanel {
  // Default serial version
  private static final long serialVersionUID = 1L;
  
  private int mainPanelWidth;
  
  private ScheduleMaker sm;
  
  private ButtonListener buttonListener;
  private JButton        saveButton,
                         prfButton,
                         altButton,
                         unaButton;
  private JButton[][]    gridButton;
  
  JComboBox<String> namesBox;
  private Employee  editEmp;
  
  private int     availType;
  private Color[] colors;
  private Boolean isPressing;
  
  private JTextField minField,
                     maxField;
  private JTextArea  commentArea;
  
//==============================================================================
  public AvailabilityPanel(ScheduleMaker sm, Dimension dim, Border b) {
    this.sm = sm;
    mainPanelWidth = (int)dim.getWidth()-2*GUI.PANEL_BORDER_SIZE;
    setPreferredSize(dim);
    setBackground(Color.WHITE);
    setBorder(b);
    setLayout(new BorderLayout());
    
    buttonListener = new ButtonListener();
    editEmp = null;
    availType = ScheduleMaker.UNA;
    colors = new Color[3];
    colors[ScheduleMaker.PRF] = Color.GREEN;
    colors[ScheduleMaker.ALT] = Color.ORANGE;
    colors[ScheduleMaker.UNA] = Color.GRAY;
    isPressing = false;
    
    add(setupSelectionPanel(), BorderLayout.CENTER);
    add(setupSavePanel(), BorderLayout.SOUTH);
  }
  
  
  
//==============================================================================
//  Methods that set up the panels
//==============================================================================
  private JPanel setupSavePanel() {
    JPanel panel = new JPanel();
    panel.setBackground(Color.WHITE);
    
    Dimension buttDim = new Dimension(mainPanelWidth-12, GUI.TEXT_WRAP_HEIGHT);
    Border buttBorder = BorderFactory.createMatteBorder(1, 1, 0, 1, Color.GRAY);
    
    saveButton = new JButton("Save");
    saveButton.setBorder(buttBorder);
    saveButton.setPreferredSize(buttDim);
    saveButton.setBackground(Color.WHITE);
    saveButton.addMouseListener(buttonListener);
    
    panel.add(saveButton);
    return panel;
  }
  
  
  
  private JPanel setupSelectionPanel() {
    JPanel panel = new JPanel();
    panel.setBackground(Color.WHITE);
    panel.setLayout(new GridBagLayout());
    
    namesBox = new JComboBox<String>();
    namesBox.setBackground(Color.WHITE);
    namesBox.addItem("Please select an employee");
    for(int i = 0; i < sm.empList.size(); i++)
      namesBox.addItem(sm.empList.get(i).getName());
    
    // Anonymous ActionListener() object that determines what happens when
    // employee is selected from the drop-down menu
    namesBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        int index = namesBox.getSelectedIndex();
        
        // There is no check whether editEmp == null because namesBox
        // was just initialized using sm.emplist
        if(index != 0) {
          editEmp = sm.getEmployee(namesBox.getItemAt(index));
          edit();
        }
      }
    });
    
    panel.add(namesBox);
    return panel;
  }
  
  
  
  private void edit() {
    GUI.removeCenter((BorderLayout)getLayout(), this, "AvailabilityPanel/edit");
    add(setupEditPanel(), BorderLayout.CENTER);
    revalidate();
  }
  
  
  
  private JPanel setupEditPanel() {
    JPanel panel = new JPanel();
    panel.setBackground(Color.WHITE);
    panel.setLayout(new BorderLayout());
    panel.add(setupGridPanel(), BorderLayout.CENTER);
    panel.add(setupSidePanel(), BorderLayout.EAST);
    panel.add(setupCommentPanel(), BorderLayout.SOUTH);
    return panel;
  }
  
  
  
  private JScrollPane setupGridPanel() {
    JPanel panel = new JPanel();
    panel.setBackground(Color.WHITE);
    panel.setLayout(new GridLayout(ScheduleMaker.NUM_ROWS+1, 8, 1, 1));
    
    gridButton = new JButton[ScheduleMaker.NUM_ROWS][7];
    JLabel[] times = new JLabel[ScheduleMaker.NUM_ROWS];
    
    int time = 0;
    for(int r = 0; r < ScheduleMaker.NUM_ROWS; r++) {
      for(int d = 0; d < 7; d++) {
        gridButton[r][d] = new JButton();
        if(businessIsOpen(time, d)) {
          gridButton[r][d].setBackground(colors[editEmp.avail(r, d)]);
          gridButton[r][d].addMouseListener(buttonListener);
        }
        else {
          gridButton[r][d].setBackground(Color.BLACK);
          gridButton[r][d].setText("-");
          gridButton[r][d].setForeground(Color.WHITE);
        }
      }
      
      times[r] = new JLabel(sm.timeIntToStr(time));
      if(time % 100 == 30)
        time += 70;
      else
        time += 30;
    }
    
    JLabel[] days = {new JLabel("Monday"), new JLabel("Tuesday"),
                     new JLabel("Wednesday"), new JLabel("Thursday"),
                     new JLabel("Friday"), new JLabel("Saturday"),
                     new JLabel("Sunday")};
    
    panel.add(GUI.msgPanel(null, null));
    for(int i = 0; i < days.length; i++) {
      days[i].setHorizontalAlignment(JLabel.CENTER);
      panel.add(days[i]);
    }
    
    for(int r = 0; r < ScheduleMaker.NUM_ROWS; r++) {
      panel.add(times[r]);
      for(int d = 0; d < 7; d++)
        panel.add(gridButton[r][d]);
    }
    
    JScrollPane outerPanel = new JScrollPane(panel);
    return outerPanel;
  }
  
  
  
  private JPanel setupSidePanel() {
    JPanel panel = new JPanel();
    panel.setBackground(Color.WHITE);
    panel.setPreferredSize(new Dimension(105, 400));
    
    Dimension dim = new Dimension(100, 30);
    
    int[] reqHours = editEmp.requestedHours();
    minField = new JTextField(Integer.toString(reqHours[0]));
    minField.setPreferredSize(dim);
    maxField = new JTextField(Integer.toString(reqHours[1]));
    maxField.setPreferredSize(dim);
    
    prfButton = new JButton("Preferred");
    prfButton.setForeground(colors[ScheduleMaker.PRF]);
    prfButton.setBackground(Color.WHITE);
    prfButton.addMouseListener(buttonListener);
    prfButton.setPreferredSize(dim);
    altButton = new JButton("Alternate");
    altButton.setForeground(colors[ScheduleMaker.ALT]);
    altButton.setBackground(Color.WHITE);
    altButton.addMouseListener(buttonListener);
    altButton.setPreferredSize(dim);
    unaButton = new JButton("Unavailable");
    unaButton.setForeground(colors[ScheduleMaker.UNA]);
    unaButton.setBackground(Color.WHITE);
    unaButton.addMouseListener(buttonListener);
    unaButton.setPreferredSize(dim);
    JButton clsButton = new JButton("Closed");
    clsButton.setForeground(Color.WHITE);
    clsButton.setBackground(Color.BLACK);
    clsButton.setPreferredSize(dim);
    
    panel.add(new JLabel("Minimum hours:"));
    panel.add(minField);
    panel.add(new JLabel("Maximum hours:"));
    panel.add(maxField);
    panel.add(GUI.msgPanel("", dim));
    panel.add(new JLabel("Select type:"));
    panel.add(prfButton);
    panel.add(altButton);
    panel.add(unaButton);
    panel.add(clsButton);
    panel.add(GUI.msgPanel("", dim));
    panel.add(new JLabel("Last edit date:"));
    panel.add(new JLabel(editEmp.getLastSub()));
    
    return panel;
  }
  
  
  
  private JPanel setupCommentPanel() {
    JPanel panel = new JPanel();
    panel.setBackground(Color.WHITE);
    panel.setPreferredSize(new Dimension(mainPanelWidth, 50));
    
    commentArea = new JTextArea(editEmp.getComment());
    commentArea.setPreferredSize(new Dimension(700, 40));
    commentArea.setLineWrap(true);
    commentArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    
    panel.add(new JLabel("Comment:"));
    panel.add(commentArea);
    return panel;
  }
  
  
  
//==============================================================================
//  Listener
//==============================================================================
  private class ButtonListener implements MouseListener {
    @Override
    public void mouseClicked(MouseEvent event) {
      JButton src = (JButton)event.getSource();
      
      if(src.equals(saveButton)) {
        if(editEmp == null)
          GUI.msgPopup("Select an employee first");
        else {
          String comment = commentArea.getText();
          if(comment.equals(""))
            comment = "None";
          
          editEmp.edit(Integer.parseInt(minField.getText()),
                       Integer.parseInt(maxField.getText()),
                       comment, sm.getDate(), extractCode());
          try {
            sm.editEmployee(editEmp, null, null);
          }
          catch(Exception e) {
            GUI.msgPopup("ERROR @ AvailabilityPanel/ButtonListener/save : " +e);
            return;
          }
          
          GUI.msgPopup("Employee data saved");
        }
      }
      else if(src.equals(prfButton))
        availType = ScheduleMaker.PRF;
      else if(src.equals(altButton))
        availType = ScheduleMaker.ALT;
      else if(src.equals(unaButton))
        availType = ScheduleMaker.UNA;
      else
        src.setBackground(colors[availType]);
    }
     
    @Override
    public void mouseEntered(MouseEvent e) {
      JButton src = (JButton)e.getSource();
      if(src.equals(saveButton) || src.equals(prfButton) ||
         src.equals(altButton) || src.equals(unaButton))
        src.setBackground(GUI.HIGHLIGHT);
      else {
        if(isPressing)
          src.setBackground(colors[availType]);
      }
    }
 
    @Override
    public void mouseExited(MouseEvent e) {
      JButton src = (JButton)e.getSource();
      if(src.equals(saveButton) || src.equals(prfButton) ||
         src.equals(altButton) || src.equals(unaButton))
        src.setBackground(Color.WHITE);
      else {
        if(isPressing)
          src.setBackground(colors[availType]);
      }
    }
   
    @Override
    public void mousePressed(MouseEvent e) {
      isPressing = true;
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
      isPressing = false;
    }
  }
  
  
  
//==============================================================================
//  Private helper methods
//==============================================================================
  /**
   * Extracts the employee's availability data from the colored grid buttons
   * into a 2-d array of numbers representing the availability types.
   */
  private byte[][] extractCode() {
    byte[][] code = new byte[ScheduleMaker.NUM_ROWS][7];
    
    for(int r = 0; r < ScheduleMaker.NUM_ROWS; r++)
      for(int d = 0; d < 7; d++)
        code[r][d] = (byte)colorToCode(gridButton[r][d].getBackground());
    
    return code;
  }
  
  
  
  
  private int colorToCode(Color c) {
    if(c.equals(colors[ScheduleMaker.PRF]))
      return ScheduleMaker.PRF;
    else if(c.equals(colors[ScheduleMaker.ALT]))
      return ScheduleMaker.ALT;
    else
      return ScheduleMaker.UNA;
  }
  
  
  
  private Boolean businessIsOpen(int time, int day) {
    if(sm.openCloseInt[0][day] == ScheduleMaker.HOURS_24)
      return true;
    
    if(sm.openCloseInt[0][day] == ScheduleMaker.HOURS_CLOSED)
      return false;
    
    if(time >= sm.openCloseInt[0][day] && time < sm.openCloseInt[1][day])
      return true;
    else
      return false;
  }
}