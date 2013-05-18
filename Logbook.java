package HAS_Tools;
// Kindlet imports
import com.amazon.kindle.kindlet.AbstractKindlet;
import com.amazon.kindle.kindlet.KindletContext;
import com.amazon.kindle.kindlet.ui.KindletUIResources.*;
import com.amazon.kindle.kindlet.ui.KMenu;
import com.amazon.kindle.kindlet.ui.KMenuItem;
import com.amazon.kindle.kindlet.input.Gestures;
import com.amazon.kindle.kindlet.ui.KOptionPane;
// Utilities
import java.util.Calendar;
import java.util.ArrayList;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
// Ui Imports
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ActionMap;
import javax.swing.Action;
import javax.swing.AbstractAction;
//import java.awt.event.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Point;
import java.awt.Container;
import java.awt.Font;
import java.awt.Color;

public class Logbook extends AbstractKindlet {
	private KindletContext ctx;
	public void create(KindletContext context) {
		this.ctx = context;
	}
	private String week_toString(int day) {
		switch(day){
			case Calendar.MONDAY : return "Mon";
			case Calendar.TUESDAY : return "Tue";
			case Calendar.WEDNESDAY : return "Wed";
			case Calendar.THURSDAY : return "Thu";
			case Calendar.FRIDAY : return "Fri";
			case Calendar.SATURDAY : return "Sat";
			case Calendar.SUNDAY : return "Sun";
			default : return "";
		}
	}
	private String month_toString(int month) {
		switch(month) {
			case Calendar.JANUARY : return  "JAN";
			case Calendar.FEBRUARY : return  "FEB";
			case Calendar.MARCH : return  "MAR";
			case Calendar.APRIL : return  "APR";
			case Calendar.MAY : return  "MAY";
			case Calendar.JUNE : return  "JUN";
			case Calendar.JULY : return  "JUL";
			case Calendar.AUGUST : return  "AUG";
			case Calendar.SEPTEMBER : return  "SEP";
			case Calendar.OCTOBER : return  "OCT";
			case Calendar.NOVEMBER : return  "NOV";
			case Calendar.DECEMBER : return  "DEC";
			default : return "";
		}
	}

	public String date;
	public String filename = "/mnt/us/developer/APP Logbook/work/";
	public FlightModel fmodel;

	public void start() {
		// Get date, filename info
		Calendar today = Calendar.getInstance();
		date = new StringBuffer()
			.append("  ")
			.append( week_toString(today.get(Calendar.DAY_OF_WEEK)))
			.append(", ")
			.append(month_toString(today.get(Calendar.MONTH)))
			.append(" ")
			.append(Integer.toString(today.get(Calendar.DAY_OF_MONTH)))
			.toString();
		filename = new StringBuffer(filename)
			.append(Integer.toString(today.get(Calendar.YEAR)))
			.append("-")
			.append(Integer.toString(today.get(Calendar.MONTH)+1))
			.append("-")
			.append(Integer.toString(today.get(Calendar.DAY_OF_MONTH)))
			.append(".csv")
			.toString();
		// init table/data model
		fmodel = new FlightModel(filename);
		//fmodel.save(filename.concat(".test"));
		
		try {
			// Set Fonts to Use
			Font menu_font = ctx.getUIResources().getFont(KFontFamilyName.MONOSPACE,14);
			Font table_font = ctx.getUIResources().getFont(KFontFamilyName.MONOSPACE,10);
			// Setup menu
			KMenu menu = new KMenu();
			menu.add("Save",new MenuListener(fmodel,ctx,'S'));
			menu.add("Sync",new MenuListener(fmodel,ctx,'Y'));
			menu.add("Set Date",new MenuListener(fmodel,ctx,'D'));
			menu.add("Quit",new MenuListener(fmodel,ctx,'D'));
			ctx.setMenu(menu);
			// Setup UI
			JLabel day_info = new JLabel(date.concat("   Flights: ")
					.concat(Integer.toString(fmodel.getFlightCount())));
			day_info.setFont(menu_font);
			day_info.setBorder(new LineBorder(Color.black));
			JButton tandem = new JButton("Add Tandem");
			tandem.setFont(menu_font);
			tandem.addActionListener(new ButtonListener(fmodel,ctx.getRootContainer()));
			JButton solo = new JButton("Add Solo");
			solo.setFont(menu_font);
			solo.addActionListener(new ButtonListener(fmodel,ctx.getRootContainer()));

			// Main Flight Log Table
			JPanel logs = new JPanel(new BorderLayout());
			JTable cells = new JTable(fmodel);
			//cells.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
			cells.setSelectionMode(DefaultListSelectionModel.SINGLE_INTERVAL_SELECTION);
			cells.setRowSelectionAllowed(true);
			cells.setColumnSelectionAllowed(false);
			cells.setCellSelectionEnabled(false);
			cells.setFont(table_font);
			cells.setRowHeight(29);
			// Set up Column sizes
			cells.getColumnModel().getColumn(0).setPreferredWidth(40); // #
			cells.getColumnModel().getColumn(1).setPreferredWidth(316); // Name
			cells.getColumnModel().getColumn(2).setPreferredWidth(130); // Notes
			cells.getColumnModel().getColumn(3).setPreferredWidth(50); // Alt
			cells.getColumnModel().getColumn(4).setPreferredWidth(50); // Work
			// Set up list Selection Edit Events
			// Set up paging and editing event triggers
			cells.addMouseListener(new CellListener(cells,ctx.getRootContainer()));

			cells.getTableHeader().setBorder(new LineBorder(Color.black));
			logs.add(cells.getTableHeader(),BorderLayout.NORTH);
			logs.add(cells,BorderLayout.CENTER);

			JPanel bot = new JPanel(new GridLayout(1,2));
			bot.add(tandem);
			bot.add(solo);
			bot.setFont(menu_font);

			ctx.getRootContainer().setLayout(new BorderLayout());
			ctx.getRootContainer().add(day_info,BorderLayout.NORTH);
			ctx.getRootContainer().add(logs,BorderLayout.CENTER);
			ctx.getRootContainer().add(bot,BorderLayout.SOUTH);
		} catch (Throwable t) {
			t.printStackTrace();
		}

	}
}

class ButtonListener implements ActionListener {
	public Container root;
	private FlightModel fmodel;
	public ButtonListener(FlightModel fmodel,Container root) {
		this.fmodel = fmodel;
		this.root = root;
	}
	public void actionPerformed(ActionEvent e) {
		String name, notes, alt, work;
		int flight_num = this.fmodel.getFlightCount();
		name = KOptionPane.showInputDialog(this.root,
		"Enter The Pilots name:",
		"Adding Flight #".concat(Integer.toString(flight_num)),
		KOptionPane.PLAIN_MESSAGE,
		KOptionPane.CANCEL_SAVE_OPTIONS,
		"");
		// Notes
		notes = KOptionPane.showInputDialog(this.root,
		"Enter The Additional Info:",
		"Adding Flight #".concat(Integer.toString(flight_num)),
		KOptionPane.PLAIN_MESSAGE,
		KOptionPane.CANCEL_SAVE_OPTIONS,
		"");
		// Alt
		alt = KOptionPane.showInputDialog(this.root,
		"Enter The Altitude:\n 0 - weaklink break\n 1 - Pat\n 2 - 2500\n 3 - nile\n 4 - 10k",
		"Adding Flight #".concat(Integer.toString(flight_num)),
		KOptionPane.PLAIN_MESSAGE,
		KOptionPane.CANCEL_SAVE_OPTIONS,
		"");
		// Work Logs
		work = KOptionPane.showInputDialog(this.root,
		"Enter The Work Logs:\n Tug,Crew,Tandem",
		"Adding Flight #".concat(Integer.toString(flight_num)),
		KOptionPane.PLAIN_MESSAGE,
		KOptionPane.CANCEL_SAVE_OPTIONS,
		"");
		// Create new log
		fmodel.addFlight(name,notes,Integer.parseInt(alt),work);
	}
}

class CellListener implements MouseListener {
	public JTable cells;
	public Container root;
	public CellListener(JTable cells, Container root) {
		this.cells = cells;
		this.root = root;
	}
	public void mouseClicked(MouseEvent e) {
		switch(e.getButton()) {
			case Gestures.BUTTON_FLICK_NORTH :
			case Gestures.BUTTON_FLICK_WEST :
				((FlightModel)cells.getModel()).next_page();
				break;
			case Gestures.BUTTON_FLICK_SOUTH :
			case Gestures.BUTTON_FLICK_EAST :
				((FlightModel)cells.getModel()).prev_page();
				break;
			case Gestures.BUTTON_HOLD : // Enter Edit Mode
				Point p = e.getPoint();
				int row = cells.rowAtPoint(p);
				int col = cells.columnAtPoint(p);
				int flight_num = Integer.parseInt((String)cells.getValueAt(row,0));
				String output;
				switch(col) {
					case 1 : // Name
						output = KOptionPane.showInputDialog(root,
						"Modify The Pilots name:",
						"Editing Flight #".concat(Integer.toString(flight_num)),
						KOptionPane.PLAIN_MESSAGE,
						KOptionPane.CANCEL_SAVE_OPTIONS,
						(String)cells.getValueAt(row,col));
						break;
					case 2 : // Notes
						output = KOptionPane.showInputDialog(root,
						"Modify The Additional Info:",
						"Editing Flight #".concat(Integer.toString(flight_num)),
						KOptionPane.PLAIN_MESSAGE,
						KOptionPane.CANCEL_SAVE_OPTIONS,
						(String)cells.getValueAt(row,col));
						break;
					case 3 : // Altitude
						output = KOptionPane.showInputDialog(root,
						"Modify The Altitude:",
						"Editing Flight #".concat(Integer.toString(flight_num)),
						KOptionPane.PLAIN_MESSAGE,
						KOptionPane.CANCEL_SAVE_OPTIONS,
						(String)cells.getValueAt(row,col));
						break;
					case 4 : // Work Logs
						output = KOptionPane.showInputDialog(root,
						"Modify The Work Logs:",
						"Editing Flight #".concat(Integer.toString(flight_num)),
						KOptionPane.PLAIN_MESSAGE,
						KOptionPane.CANCEL_SAVE_OPTIONS,
						(String)cells.getValueAt(row,col));
						break;
					default : output = null;
				}
				if(output!=null)cells.setValueAt(output,row,col);
				break;
			case Gestures.BUTTON_GROW :
			case Gestures.BUTTON_SHRINK :
			case Gestures.BUTTON_TAP :
		}
	}
	public void mouseReleased(MouseEvent e) { }
	public void mousePressed(MouseEvent e) { }
	public void mouseExited(MouseEvent e) { }
	public void mouseEntered(MouseEvent e) { }
}

class MenuListener implements ActionListener {
	public KindletContext ctx;
	public Container root;
	public FlightModel fmodel;
	public int func;
	public MenuListener(FlightModel fmodel,KindletContext ctx,int func) {
		this.fmodel = fmodel;
		this.func = func;
		this.ctx = ctx;
		this.root = ctx.getRootContainer();
	}
	public void actionPerformed(ActionEvent e) {
		switch(func) {
			case 'S' :
				fmodel.save();
				KOptionPane.showMessageDialog(root,"Data Saved");
				break;
			case 'Y' :
				KOptionPane.showMessageDialog(root,"Sync Not Yet Implemented");
				break;
			case 'D' :
				KOptionPane.showMessageDialog(root,"Set Date not Yet Implemented");
				break;
			case 'Q' :
				KOptionPane.showMessageDialog(root,"Quiting Logbook");
				//ctx.destroy();
				break;
		}
	}
}

class FlightModel extends AbstractTableModel {
	private String[] col_names = {"#","Pilot","Notes","Alt","Wrk"};
	public ArrayList fdata;
	public String filename;
	public int page;
	public int page_size = 20;

	public FlightModel(String filename) {
		this.page = 0;
		this.filename = filename;
		this.fdata = new ArrayList(30);
		try{ 
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String line;
			while((line = in.readLine()) != null) {
				this.fdata.add(new Flight(line));
			}
			in.close();
		} catch(IOException e) {
			System.err.println(e);
		}
	}
	public void save() { this.save(this.filename); }
	public void save(String filename) {
		// Create String to Record
		StringBuffer save = new StringBuffer();
		int index = 0;
		while(index < fdata.size()) {
			save.append(this.fdata.get(index++).toString()).append('\n');
		}
		// Write to file
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
			out.write(save.toString());
			out.close();
		} catch(IOException e) {
			System.err.println(e);
		}
	}
	public int getColumnCount() { return col_names.length; }
	public int getRowCount() { return fdata.size()-(this.page)*20; }
	public int getFlightCount() { return fdata.size(); }
	public String getColumnName(int col) { return col_names[col]; }
	public Object getValueAt(int row, int col) {
		switch(col) {
			case 0 : return Integer.toString(row+1+(this.page*this.page_size));
			case 1 : return ((Flight)fdata.get(row+this.page*this.page_size)).name;
			case 2 : return ((Flight)fdata.get(row+this.page*this.page_size)).notes;
			case 3 : return ((Flight)fdata.get(row+this.page*this.page_size)).getAltstr();
			case 4 : return new String(((Flight)fdata.get(row+this.page*this.page_size)).work);
			default: return "E";
		}
	}
	public Class getColumnClass(int col) { return "".getClass(); }
	public void prev_page() {
		if(this.page>0) {
			this.page--;
			fireTableDataChanged();
		}
	}
	public void next_page() {
		if(this.fdata.size() > (this.page+1) * this.page_size) {
			this.page++;
			fireTableDataChanged();
		}
	}
	public void setValueAt(Object value,int row,int col) {
		((Flight)this.fdata.get(row+this.page*this.page_size)).setCol(col,(String)value);
		fireTableCellUpdated(row-(this.page*this.page_size),col);
	}
	public void addFlight(String name, String notes, int alt, String work) {
		this.fdata.add(new Flight(name,notes,alt,work));
		int size = this.fdata.size();
		this.page = size/this.page_size;
		if(size%this.page_size==0) {
			fireTableDataChanged();
		} else fireTableRowsInserted(this.fdata.size(),this.fdata.size());
	}
}

class Flight {
	public String name;
	public String notes;
	public int alt;
	public char[] work = new char[3];

	public Flight(String name, String Notes, int alt, String wrk) {
		this.name = name;
		this.notes = notes;
		this.alt = alt; // 0,1,2,3,4
		this.work[0] = wrk.charAt(0);
		this.work[1] = wrk.charAt(1);
		this.work[2] = wrk.charAt(2);
	}
	public Flight( String data ) {
		int prev_i = 0, next_i = data.indexOf(',');
		this.name = data.substring(prev_i,next_i); // Name
		prev_i = next_i + 1; next_i = data.indexOf(',',prev_i);
		this.alt = Flight.alt_parse(data.substring(prev_i,next_i)); // Tandem Alt
		prev_i = next_i + 1; next_i = data.indexOf(',',prev_i);
		if(this.alt == -1) this.alt = Flight.alt_parse(data.substring(prev_i,next_i)); // Solo Alt
		prev_i = next_i + 1; next_i = data.indexOf(',',prev_i);
		if((this.work[0] = data.charAt(prev_i))==',') { // Tug Pilot
			this.work[0] = ' '; prev_i++;
		} else prev_i += 2;
		if((this.work[2] = data.charAt(prev_i))==',') { // Tandem Pilot
			this.work[2] = ' '; prev_i++;
		} else prev_i += 2;
		if((this.work[1] = data.charAt(prev_i))==',') { // Crew
			this.work[1] = ' '; prev_i++;
		} else prev_i += 2;
		this.notes = data.substring(data.lastIndexOf(',')+1);
	}
	public void setCol(int col,String val) {
		switch(col){
			case 1 : // Name
				this.name = val;
				break;
			case 2 : // Notes
				this.notes = val;
				break;
			case 3 : // Alt
				this.alt = Integer.parseInt(val);
				break;
			case 4 : // Work
				this.work[0] = val.charAt(0);
				this.work[1] = val.charAt(1);
				this.work[2] = val.charAt(2);
				break;
		}
	}
	public String toString() {
		String alt_str = "";
		switch(this.alt) {
			case 0 : alt_str = "0"; break;
			case 1 : alt_str = "0.5"; break;
			case 2 : alt_str = "1"; break;
			case 3 : alt_str = "2"; break;
			case 4 : alt_str = "4"; break;
		}
		StringBuffer str = new StringBuffer();
		str.append(this.name).append(','); // Name
		if(this.work[2]!=' ') {
			str.append(alt_str).append(',').append(','); // Tandem Alt #
		} else {
			str.append(',').append(alt_str).append(','); // Solo Alt #
		}
		str.append(this.work[0]).append(','); // Tug Pilot
		if(this.work[2]!=' ') { str.append(this.work[2]); } // Tandem Pilot
		str.append(',');
		if(this.work[1]!=' ') { str.append(this.work[1]); }// Crew
		str.append(',');
		str.append(this.notes); // Rental/Notes
		return str.toString();
	}
	public String getAltstr(){
		switch(this.alt) {
			case 0 : return "X";
			case 1 : return "Pat";
			case 2 : return "25";
			case 3 : return "1m";
			case 4 : return "10k";
		}
		return "Err";
	}
	private static int alt_parse(String alt) {
		if(alt.length()==0) { return -1; };
		if(alt.length()>1) { return 1; };
		switch(alt.charAt(0)) {
			case '0' : return 0;
			case '1' : return 2;
			case '2' : return 3;
			case '4' : return 4;
		}
		return -1;
	}
}
/* --ToDo--
 * menu - Other Options
 * add new pages
 * edit pages
 * name lookup
 * sync system - email/dropbox
 * create - destroy - start - stop
 */
