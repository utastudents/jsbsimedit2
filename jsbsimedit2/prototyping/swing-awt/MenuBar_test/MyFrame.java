package MenuBar_test;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.*;
import javax.swing.*;


public class MyFrame extends JFrame implements ActionListener {

	JMenuBar menuBar;
	JMenu fileMenu;
	JMenu editMenu;
	JMenuItem newItem;
	JMenuItem aboutItem;
	ImageIcon Lancer;
	JToolBar toolBar;
	JButton addB;
	JButton deleteB;
	JTextField textF;
	JTabbedPane Tpane;
	JPanel pane_A;
	JLabel labelA;
	JPanel pane_B;
	JLabel labelB;
	ImageIcon PlusIcon;
	ImageIcon MinusIcon;
	
	
	MyFrame(){
		try {
            // Set the look and feel to the system's default
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
       } catch (Exception e) {
            e.printStackTrace();
       }
		
		PlusIcon = new ImageIcon(getClass().getResource("PlusSign.png"));
		MinusIcon = new ImageIcon(getClass().getResource("MinusSign.png"));
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(500,400);
		//this.setLayout(null);
		menuBar = new JMenuBar();
		toolBar = new JToolBar();
		addB = new JButton(PlusIcon);
		deleteB = new JButton(MinusIcon);
		addB.setToolTipText("Click to add");
		deleteB.setToolTipText("Click to remove");
		textF = new JTextField();
		Tpane = new JTabbedPane(JTabbedPane.TOP);
		pane_A = new JPanel();
		labelA = new JLabel("Main");
		pane_B = new JPanel();
		labelB = new JLabel();
		
		pane_A.setLayout(new BorderLayout());
		pane_A.add(toolBar, BorderLayout.NORTH);
		Tpane.add("Pane A", pane_A);
		
		
		Tpane.add("Panel B", pane_B);
		toolBar.setBackground(Color.darkGray);
		toolBar.add(addB);
		//toolBar.add(textF);
		toolBar.add(deleteB);
		toolBar.setFloatable(false);
		
		fileMenu = new JMenu("File");
		editMenu = new JMenu("Edit");
		
		newItem = new JMenuItem("New");
		aboutItem = new JMenuItem("About");
		
		fileMenu.add(newItem);
		fileMenu.add(aboutItem);
		
		
		newItem.addActionListener(this);
		aboutItem.addActionListener(this);
		
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		
		
		this.setJMenuBar(menuBar);
		this.setContentPane(Tpane);
		this.setVisible(true);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == newItem) {
			System.out.println("*beep boop* you saved a file");
		}
		if(e.getSource() == aboutItem) {
			System.out.println("IT'S NOT READY YET");
		}
		
		
	}
	
}
