/*
 * Author: Kyoseong Ku <aust1nku@ucla.edu>
 * 
 * Description:
 *    This class consists of the GUI panel that allows users to perform
 *    functions on employees (add, edit, remove).
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;

public class EmployeePanel extends JPanel {
  // Default serial version
  private static final long serialVersionUID = 1L;
  
  private int mainPanelWidth;
  
  private ScheduleMaker sm;
  
  private OptionListener oListener;
  
  private JButton addButton,
                  editButton,
                  removeButton,
                  saveButton,
                  selectedButton;
  
  private JTextField nameField,
                     positionField,
                     phoneField,
                     editPositionField,
                     editPhoneField;
  
  private JComboBox<String> namesBox;
  
  private Employee            editEmp;
  private ArrayList<Employee> removeList;

//==============================================================================
  public EmployeePanel(ScheduleMaker sm, Dimension dim, Border b) {
    this.sm = sm;
    mainPanelWidth = (int)dim.getWidth()-2*GUI.PANEL_BORDER_SIZE;
    setPreferredSize(dim);
    setBackground(Color.WHITE);
    setBorder(b);
    setLayout(new BorderLayout());
    
    editEmp = null;
    removeList = new ArrayList<Employee>();
    
    oListener = new OptionListener();
    add(setupOptionsPanel(), BorderLayout.NORTH);
    add(setupSavePanel(), BorderLayout.SOUTH);
    add(GUI.msgPanel("Select an option from above", null), BorderLayout.CENTER);
  }
  
  
  
//==============================================================================
//  Methods that set up the panels
//==============================================================================
  private JPanel setupOptionsPanel() {
    JPanel panel = new JPanel();
    panel.setBackground(Color.WHITE);
    
    int numButtons = 3;
    int buttonW = (mainPanelWidth/numButtons)-(4*numButtons);
    Dimension buttonDim = new Dimension(buttonW, GUI.TEXT_WRAP_HEIGHT);
    Border buttBorder = BorderFactory.createMatteBorder(0, 1, 1, 1, Color.GRAY);

    // When adding new buttons or deleting existing ones, make sure to update
    // the numButtons variable above;
    addButton = new JButton("Add employee");
    addButton.setBorder(buttBorder);
    addButton.setPreferredSize(buttonDim);
    addButton.setBackground(Color.WHITE);
    addButton.addMouseListener(oListener);
    editButton = new JButton("Edit employee");
    editButton.setBorder(buttBorder);
    editButton.setPreferredSize(buttonDim);
    editButton.setBackground(Color.WHITE);
    editButton.addMouseListener(oListener);
    removeButton = new JButton("Remove employee");
    removeButton.setBorder(buttBorder);
    removeButton.setPreferredSize(buttonDim);
    removeButton.setBackground(Color.WHITE);
    removeButton.addMouseListener(oListener);
    
    
    panel.add(addButton);
    panel.add(editButton);
    panel.add(removeButton);
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
  
  
  
  private JPanel setupAddPanel() {
    JPanel panel = new JPanel();
    panel.setBackground(Color.WHITE);
    panel.setLayout(new GridLayout(3, 2, 50, 50));
    
    nameField = new JTextField();
    positionField = new JTextField();
    phoneField = new JTextField();
    
    panel.add(new JLabel("Name (First Last)"));
    panel.add(nameField);
    panel.add(new JLabel("Position"));
    panel.add(positionField);
    panel.add(new JLabel("Phone number (###-###-####)"));
    panel.add(phoneField);
    
    Dimension tbPadding = new Dimension(150, 150);
    Dimension sPadding = new Dimension(200, 200);
    
    JPanel outerPanel = new JPanel();
    outerPanel.setLayout(new BorderLayout());
    outerPanel.add(GUI.msgPanel(null, tbPadding), BorderLayout.NORTH);
    outerPanel.add(GUI.msgPanel(null, tbPadding), BorderLayout.SOUTH);
    outerPanel.add(GUI.msgPanel(null, sPadding), BorderLayout.WEST);
    outerPanel.add(GUI.msgPanel(null, sPadding), BorderLayout.EAST);
    outerPanel.add(panel, BorderLayout.CENTER);
    
    return outerPanel;
  }
  
  
  
  private JPanel setupEditPanel() {
    JPanel panel = new JPanel();
    panel.setBackground(Color.WHITE);
    panel.setLayout(new GridLayout(2, 2, 50, 130));
    
    namesBox = new JComboBox<String>();
    editPositionField = new JTextField();
    editPhoneField = new JTextField();
    
    namesBox.setBackground(Color.WHITE);
    namesBox.addItem("Please select an employee");
    for(int i = 0; i < sm.empList.size(); i++)
      namesBox.addItem(sm.empList.get(i).getName());
    
    // Anonymous listener class that fills in the text fields with current info
    namesBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        int index = namesBox.getSelectedIndex();
        
        if(index == 0) {
          editEmp = null;
          editPositionField.setText("");
          editPhoneField.setText("");
        }
        else {
          // There is no check whether editEmp == null because namesBox
          // was just initialized using sm.emplist
          editEmp = sm.getEmployee(namesBox.getItemAt(index));
          
          if(editEmp != null) {
            editPositionField.setText(editEmp.getPosition());
            editPhoneField.setText(editEmp.getPhone());
          }
        }
      }
    });
    
    panel.add(new JLabel("Position"));
    panel.add(editPositionField);
    panel.add(new JLabel("Phone number (###-###-####)"));
    panel.add(editPhoneField);
    
    Dimension tbPadding = new Dimension(150, 150);
    Dimension sPadding = new Dimension(200, 200);
    
    JPanel namesPanel = GUI.msgPanel(null, tbPadding);
    namesPanel.add(namesBox);
    
    JPanel outerPanel = new JPanel();
    outerPanel.setLayout(new BorderLayout());
    outerPanel.add(namesPanel, BorderLayout.NORTH);
    outerPanel.add(GUI.msgPanel(null, tbPadding), BorderLayout.SOUTH);
    outerPanel.add(GUI.msgPanel(null, sPadding), BorderLayout.WEST);
    outerPanel.add(GUI.msgPanel(null, sPadding), BorderLayout.EAST);
    outerPanel.add(panel, BorderLayout.CENTER);
    
    return outerPanel;
  }
  
  
  
  private JScrollPane setupRemovePanel() {
    if(sm.empList.size() == 0) {
      String msg = "There are no employees to remove";
      return new JScrollPane(GUI.msgPanel(msg, new Dimension(200, 200)));
    }
    else {
      JPanel panel = new JPanel();
      panel.setBackground(Color.WHITE);
      panel.setLayout(new GridLayout(5, 2, 10, 10));
      panel.setPreferredSize(new Dimension(150, 50));
      
      CheckListener cl = new CheckListener();
      for(int i = 0; i < sm.empList.size(); i++) {
        JCheckBox temp = new JCheckBox(sm.empList.get(i).getName());
        temp.addItemListener(cl);
        temp.setBackground(Color.WHITE);
        panel.add(temp);
      }
      
      JScrollPane outerPanel = new JScrollPane(panel);
      return outerPanel;
    }
  }
  
//==============================================================================
//  Methods for each option
//==============================================================================
  private void addEmployee() {
    GUI.removeCenter((BorderLayout)getLayout(), this,
                     "EmployeePanel/addEmployee");
    add(setupAddPanel(), BorderLayout.CENTER);
    revalidate();
  }
  
  private void editEmployee() {
    GUI.removeCenter((BorderLayout)getLayout(), this,
                     "EmployeePanel/editEmployee");
    add(setupEditPanel(), BorderLayout.CENTER);
    revalidate();
  }
  
  private void removeEmployee() {
    GUI.removeCenter((BorderLayout)getLayout(), this,
                     "EmployeePanel/removeEmployee");
    add(setupRemovePanel(), BorderLayout.CENTER);
    revalidate();
  }
  
  private void save() {
    String msg;
    
    if(selectedButton.equals(addButton)) {
      try {
        sm.addEmployee(nameField.getText(),
                       positionField.getText(),
                       phoneField.getText());
      }
      catch(Exception e) {
        GUI.msgPopup("ERROR @ EmployeePanel/save(add) : " +e);
        return;
      }
      
      msg = "Employee has been added";
      nameField.setText("");
      positionField.setText("");
      phoneField.setText("");
    }
    else if(selectedButton.equals(editButton)) {
      int index = namesBox.getSelectedIndex();
      String name = namesBox.getItemAt(index);
      
      if(index == 0 || editEmp == null)
        msg = "Please select an employee first.";
      else {
        try {
          sm.editEmployee(editEmp,
                          editPositionField.getText(),
                          editPhoneField.getText());
        }
        catch(Exception e) {
          GUI.msgPopup("ERROR @ EmployeePanel/save(edit) : " +e);
          return;
        }
        msg = "Employee " +name +" has been edited";
      }
    }
    else if(selectedButton.equals(removeButton)) {
      if(removeList.size() == 0)
        msg = "Please check the employee(s) you want to remove.";
      else {
        try {
          sm.removeEmployee(removeList);
        }
        catch(Exception e) {
          GUI.msgPopup("ERROR @ EmployeePanel/save(remove) : " +e);
          return;
        }
        
        if(removeList.size() == 1)
          msg = "Employee has been removed";
        else
          msg = "Employees have been removed";
        
        for(int i = removeList.size(); i > 0; i--)
          removeList.remove(removeList.size()-1);
        
        // This refreshes the panel
        removeEmployee();
      }
    }
    else
      msg = null;
    
    GUI.msgPopup(msg);
  }

//==============================================================================
//  Listeners
//==============================================================================
  // For the JButtons
  private class OptionListener implements MouseListener {
    @Override
    public void mouseClicked(MouseEvent e) {
      JButton src = (JButton)e.getSource();
      
      // Toggle highlight of selected button
      if(!src.equals(saveButton)) {
        if(selectedButton != null)
          selectedButton.setBackground(Color.WHITE);
        src.setBackground(GUI.HIGHLIGHT);
        selectedButton = src;
      }
      
      if(src.equals(addButton))
        addEmployee();
      else if(src.equals(editButton))
        editEmployee();
      else if(src.equals(removeButton))
        removeEmployee();
      else if(src.equals(saveButton) && selectedButton == null)
        GUI.msgPopup("Nothing to save");
      else if(src.equals(saveButton))
        save();
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
  
  
  
  // For the checkboxes for employee removal
  private class CheckListener implements ItemListener {
    @Override
    public void itemStateChanged(ItemEvent e) {
      JCheckBox item;
      Employee emp;
      // There is no check whether emp == null because namesBox would
      // contain the updated list of employees
      if(e.getStateChange() == ItemEvent.SELECTED) {
        item = (JCheckBox)e.getItem();
        emp = sm.getEmployee(item.getText());
        removeList.add(emp);
      }
      else {
        item = (JCheckBox)e.getItem();
        emp = sm.getEmployee(item.getText());
        removeList.remove(emp);
      }
    }
  }
}