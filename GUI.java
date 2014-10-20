/*
 * Author: Kyoseong Ku <aust1nku@ucla.edu>
 * 
 * Description:
 *    This class is the primary GUI of the program. It generates pop-up windows
 *    with a user-specified message, generates blank panels with a
 *    user-specified message, and houses the different GUI panels.
 */

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class GUI extends JFrame {
  // Default serial version
  private static final long serialVersionUID = 1L;

  public static final Dimension FRAME_DIM = new Dimension(1000, 600),
                                OPTIONS_DIM = new Dimension(175, 600),
                                MAIN_DIM = new Dimension(825, 600);
  
  public static final int PANEL_BORDER_SIZE = 2,
                          TEXT_WRAP_HEIGHT = 30;
  
  public static final Border PANEL_BORDER = BorderFactory.createMatteBorder(
                                              PANEL_BORDER_SIZE,
                                              PANEL_BORDER_SIZE,
                                              PANEL_BORDER_SIZE,
                                              PANEL_BORDER_SIZE,
                                              Color.BLACK);
  
  public static final Color HIGHLIGHT = new Color(245, 245, 245);
  
  private ScheduleMaker sm;
  
  private JButton availButton,
                  schButton,
                  empButton,
                  busButton,
                  selectedButton;
  
//==============================================================================
  public GUI() {
    super("ScheduleMe");
    sm = new ScheduleMaker();
    setPreferredSize(FRAME_DIM);
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(setupOptionsPanel(), BorderLayout.WEST);
    
    if(sm.hoursFileExists()) {
      String msg = "Welcome to ScheduleMe";
      getContentPane().add(msgPanel(msg, null), BorderLayout.CENTER);
    }
    else {
      // Enter the business hours setup panel if the business hours file
      // doesn't exist (this is the first time the program is being run)
      busButton.setBackground(HIGHLIGHT);
      selectedButton = busButton;
      business();
    }
  }
  
  
  
//==============================================================================
//  Public methods
//==============================================================================
  /**
   * Generates a simple pop-up message displaying the specified message string
   */
  public static void msgPopup(String msg) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.pack();
    f.setResizable(false);
    f.setVisible(false);
    f.setAlwaysOnTop(true);
    
    if(msg == null)
      JOptionPane.showMessageDialog(f, "NULL");
    else
      JOptionPane.showMessageDialog(f, msg);
  }
  
  
  
  /**
   * Generates a simple white panel with the provided message and dimensions
   */
  public static JPanel msgPanel(String msg, Dimension dim) {
    JPanel p = new JPanel();
    p.setBackground(Color.WHITE);
    p.setLayout(new GridBagLayout());
    
    if(dim != null)
      p.setPreferredSize(dim);
    if(msg != null)
      p.add(new JLabel(msg));
    
    return p;
  }
  
  
  
  /**
   * Removes the JPanel in BorderLayout.CENTER
   */
  public static void removeCenter(BorderLayout bl, JPanel p, String location) {
    try {
      p.remove(bl.getLayoutComponent(BorderLayout.CENTER));
    }
    catch(NullPointerException e) {
      // The try block attempts to remove any existing panels, and execution
      // ends up here if there are no existing panels due to a null pointer.
      // In such a case, do nothing
    }
    catch(Exception e) {
      msgPopup("CRITICAL ERROR! Program has terminated. See err output.");
      System.err.println("@" +location +" : " +e);
      System.exit(1);
    }
  }

//==============================================================================
//  Methods for each primary option on the left side of the GUI
//==============================================================================
  private void availability() {
    removeCenter();
    AvailabilityPanel ap = new AvailabilityPanel(sm, MAIN_DIM, PANEL_BORDER);
    getContentPane().add(ap, BorderLayout.CENTER);
    getContentPane().revalidate();
  }
  
  private void generate() {
    try {
      sm.export();
    }
    catch(Exception e) {
      GUI.msgPopup("ERROR @ GUI/generate() : " +e);
      return;
    }
    msgPopup("Excel file has been generated");
  }
  
  private void employee() {
    removeCenter();
    EmployeePanel ep = new EmployeePanel(sm, MAIN_DIM, PANEL_BORDER);
    getContentPane().add(ep, BorderLayout.CENTER);
    getContentPane().revalidate();
  }
  
  private void business() {
    removeCenter();
    BusinessPanel bp = new BusinessPanel(sm, MAIN_DIM, PANEL_BORDER);
    getContentPane().add(bp, BorderLayout.CENTER);
    getContentPane().revalidate();
  }
  
//==============================================================================
//  Listener
//==============================================================================
  private class OptionListener implements MouseListener {
    @Override
    public void mouseClicked(MouseEvent e) {
      JButton src = (JButton)e.getSource();
      String msg;
      
      if(!sm.hoursFileExists() && !src.equals(busButton)) {
        msg = "Business hours have not been set. "
              +"Please set the business hours first by using the "
              +"\"Business settings\" button.";
      }
      else if(sm.hasNoEmployees() &&
                (src.equals(availButton) || src.equals(schButton))) {
        msg = "There are no employees. Please add an employee first "
              +"by using the \"Employee settings\" button.";
      }
      else
        msg = null;
      
      if(msg != null)
        msgPopup(msg);
      else {
        if(!src.equals(schButton)) {
          if(selectedButton != null)
            selectedButton.setBackground(Color.WHITE);
          src.setBackground(HIGHLIGHT);
          selectedButton = src;
        }
        
        if(src.equals(availButton))
          availability();
        else if(src.equals(schButton))
          generate();
        else if(src.equals(empButton))
          employee();
        else if(src.equals(busButton))
          business();
        else;
      }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
      JButton src = (JButton)e.getSource();
      if(!src.equals(selectedButton))
        src.setBackground(HIGHLIGHT);
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

  
  
//==============================================================================
//  Private helper methods
//==============================================================================
  private JPanel setupOptionsPanel() {
    JPanel panel = new JPanel();
    panel.setPreferredSize(OPTIONS_DIM);
    panel.setBackground(Color.WHITE);
    panel.setBorder(PANEL_BORDER);
    
    Border buttBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY);
    int panelWidth = (int)(OPTIONS_DIM.getWidth()-2*PANEL_BORDER_SIZE);
    Dimension buttonDim = new Dimension(panelWidth, TEXT_WRAP_HEIGHT);
    OptionListener oListener = new OptionListener();
    
    availButton = new JButton("Enter availability");
    availButton.setBorder(buttBorder);
    availButton.setPreferredSize(buttonDim);
    availButton.setBackground(Color.WHITE);
    availButton.addMouseListener(oListener);
    schButton = new JButton("Generate schedule");
    schButton.setBorder(buttBorder);
    schButton.setPreferredSize(buttonDim);
    schButton.setBackground(Color.WHITE);
    schButton.addMouseListener(oListener);
    empButton = new JButton("Employee settings");
    empButton.setBorder(buttBorder);
    empButton.setPreferredSize(buttonDim);
    empButton.setBackground(Color.WHITE);
    empButton.addMouseListener(oListener);
    busButton = new JButton("Business settings");
    busButton.setBorder(buttBorder);
    busButton.setPreferredSize(buttonDim);
    busButton.setBackground(Color.WHITE);
    busButton.addMouseListener(oListener);
    
    panel.add(availButton);
    panel.add(schButton);
    panel.add(empButton);
    panel.add(busButton);
    return panel;
  }
  
  
  
  /**
   * Removes the JPanel in BorderLayout.CENTER for the GUI class
   */
  private void removeCenter() {
    try {
      BorderLayout bl = (BorderLayout)getContentPane().getLayout();
      getContentPane().remove(bl.getLayoutComponent(BorderLayout.CENTER));
    }
    catch(NullPointerException e) {
      // The try block attempts to remove any existing panels, and execution
      // ends up here if there are no existing panels due to a null pointer.
      // In such a case, do nothing
    }
    catch(Exception e) {
      msgPopup("CRITICAL ERROR! Program has terminated. See err output.");
      System.err.println("@GUI/removeCenter : " +e);
      System.exit(1);
    }
  }
}