import java.awt.EventQueue;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.gson.Gson;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.FormSpecs;
import transfer.FTPManager;

public class AutoGuiTest extends JFrame {

	private JPanel contentPane;
	private JTextField txtCustominput;
	private JTextField txtParameters;
	private static JComboBox<String> comboBox;

	private final String TEAM_NUMBER = "4761"; // ALWAYS 4 CHARACTERS

	private static final boolean COMP_MODE = true; // Auto load values

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AutoGuiTest frame = new AutoGuiTest();
					frame.setSize(600, 600);
					frame.setVisible(true);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public AutoGuiTest() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 576, 411);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("21dlu:grow"),
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(17dlu;default)"),
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
				new RowSpec[] {
						FormSpecs.RELATED_GAP_ROWSPEC,
						FormSpecs.DEFAULT_ROWSPEC,
						FormSpecs.RELATED_GAP_ROWSPEC,
						RowSpec.decode("default:grow"),
						FormSpecs.RELATED_GAP_ROWSPEC,
						FormSpecs.DEFAULT_ROWSPEC,
						FormSpecs.RELATED_GAP_ROWSPEC,
						FormSpecs.DEFAULT_ROWSPEC,
						FormSpecs.RELATED_GAP_ROWSPEC,
						FormSpecs.DEFAULT_ROWSPEC,
						FormSpecs.RELATED_GAP_ROWSPEC,
						FormSpecs.DEFAULT_ROWSPEC,
						FormSpecs.RELATED_GAP_ROWSPEC,
						FormSpecs.DEFAULT_ROWSPEC,}));

		comboBox = new JComboBox<String>();
		comboBox.addItem("Custom");
		contentPane.add(comboBox, "2, 2, fill, default");

		DefaultListModel<String> listModel = new DefaultListModel<String>();

		JList<String> list = new JList<String>(listModel);
		list.setToolTipText("Current Sequence\r\n");
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		contentPane.add(list, "6, 4, fill, fill");


		final String customText = "Custom";

		txtParameters = new JTextField();
		txtParameters.setText("Parameters");
		contentPane.add(txtParameters, "2, 10, fill, default");
		txtParameters.setColumns(10);


		txtCustominput = new JTextField();
		txtCustominput.setText("CustomInput");
		contentPane.add(txtCustominput, "2, 12, fill, default");
		txtCustominput.setColumns(10);

		JButton btnAddSeq = new JButton("Add Seq");
		btnAddSeq.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String selectedItem = (String)comboBox.getSelectedItem();

				if (selectedItem.equals(customText)) {
					comboBox.addItem(txtCustominput.getText());
					listModel.addElement("SEQ {" + txtParameters.getText() + "} " + txtCustominput.getText());
				} else {
					listModel.addElement("SEQ {" + txtParameters.getText() + "} " + selectedItem);
				}
			}
		});

		contentPane.add(btnAddSeq, "2, 6");

		JButton btnAdd = new JButton("Add Par");
		btnAdd.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String selectedItem = (String)comboBox.getSelectedItem();

				if (selectedItem.equals(customText)) {
					comboBox.addItem(txtCustominput.getText());
					listModel.addElement("PAR {" + txtParameters.getText() + "} " + txtCustominput.getText());
				} else {
					listModel.addElement("PAR {" + txtParameters.getText() + "} " + selectedItem);
				}
			}
		});

		contentPane.add(btnAdd, "2, 8");


		JButton btnDelete = new JButton("Delete");
		btnDelete.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int index = list.getSelectedIndex();

				if (index != -1) {
					listModel.remove(index);
				}
			}
		});

		contentPane.add(btnDelete, "6, 12");

		JButton btnSaveValues = new JButton("Save Values");
		btnSaveValues.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				StringBuilder csvVal = new StringBuilder();

				ComboBoxModel<String> model = comboBox.getModel();

				for (int i=0; i<model.getSize(); i++) {
					String element = model.getElementAt(i);

					if (!element.trim().equals(customText)) {
						csvVal.append(element).append(",");
					}
				}

				String csvStr;
				if (csvVal.lastIndexOf(",") == csvVal.length()-1) {
					csvStr = csvVal.substring(0, csvVal.length()-1);
				} else {
					csvStr = csvVal.toString();
				}

				JFileChooser c = new JFileChooser();

				c.addChoosableFileFilter(new FileNameExtensionFilter("*.csv", ".csv"));

				int val = c.showSaveDialog(contentPane);

				if (val == JFileChooser.APPROVE_OPTION) {
					String name = c.getSelectedFile().toString();

					if (!name.endsWith(".csv")) {
						name += ".csv";
					}

					try (FileWriter fw = new FileWriter(name)){
						fw.write(csvStr);
					} catch (IOException ee) {
						ee.printStackTrace();
					}
				}

			}
		});

		contentPane.add(btnSaveValues, "2, 14");

		JButton btnSend = new JButton("Send");
		btnSend.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ListModel<String> lm = list.getModel();

				ArrayList<ArrayList<ArrayList<ArrayList<String>>>> data = new ArrayList<>();

				ArrayList<ArrayList<String>> commandSet = new ArrayList<>();

				boolean wasLastSeq = false;
				boolean wasLastPar = false;
				for (int i=0; i<lm.getSize(); i++) {
					//{{{"SEQ"}}, {{"DriveStraight", "5"}, {"DriveHorizontal", "3"}, {"TurnAngle", "90"}}},
					//                {{{"PAR"}}, {{"DriveStraight", "5"}, {"DriveHorizontal", "5"}}}

					String element = (lm.getElementAt(i)).trim(); // SEQ {1,2} Command

					String[] parts = element.split(" ");

					String type = parts[0];

					String args = parts[1];
					int i1 = args.indexOf("{");
					int i2 = args.indexOf("}");
					args = args.substring(i1+1,i2);

					String command = parts[2];

					if ((!wasLastSeq && type.equals("SEQ"))|| (!wasLastPar && type.equals("PAR"))) {
						data.add(new ArrayList<>());

						if (type.equals("SEQ")) {
							wasLastSeq = true;
							wasLastPar = false;

							ArrayList<ArrayList<ArrayList<String>>> workingSet = data.get(data.size()-1); // Get last element in list (the one that was just added)

							ArrayList<ArrayList<String>> temp = new ArrayList<>();
							ArrayList<String> temp2 = new ArrayList<>();
							temp2.add("SEQ");
							temp.add(temp2);
							workingSet.add(temp);

							workingSet.add(new ArrayList<>());

							commandSet = workingSet.get(workingSet.size()-1);

						} else if (type.equals("PAR")) {
							wasLastPar = true;
							wasLastSeq = false;

							ArrayList<ArrayList<ArrayList<String>>> workingSet = data.get(data.size()-1); // Get last element in list (the one that was just added)

							ArrayList<ArrayList<String>> temp = new ArrayList<>();
							ArrayList<String> temp2 = new ArrayList<>();
							temp2.add("PAR");
							temp.add(temp2);
							workingSet.add(temp);

							workingSet.add(new ArrayList<>());

							commandSet = workingSet.get(workingSet.size()-1);
						}
					}

					ArrayList<String> commandArr = new ArrayList<>();

					commandArr.add(command);
					commandArr.add(args);

					commandSet.add(commandArr);
				}

				String json = new Gson().toJson(data);

				String name = "AutoCommand.json";

				try (PrintWriter out = new PrintWriter(name)) {
					out.println(json);
				} catch (FileNotFoundException ee) {
					ee.printStackTrace();
				}

				FTPManager ftpManager = new FTPManager("lvuser", "roboRIO-" + TEAM_NUMBER + "-frc.local");

				try {
					ftpManager.sendFile(name, "AutoCommand");
				} catch (JSchException | SftpException e1) {
					e1.printStackTrace();
				}

				System.out.println(json);
			}
		});

		JButton btnLoad = new JButton("Load");

		btnLoad.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser c = new JFileChooser();

				c.addChoosableFileFilter(new FileNameExtensionFilter("*.csv", ".csv"));

				int val = c.showOpenDialog(contentPane);

				if (val == JFileChooser.APPROVE_OPTION) {
					String name = c.getSelectedFile().toString();

					load(name);
				}
			}
		});

		contentPane.add(btnLoad, "4, 14");
		contentPane.add(btnSend, "6, 14");


		if (COMP_MODE) {
			load("Compo.csv");
		}
	}

	public static void load(String name) {
		if (!name.endsWith(".csv")) {
			name += ".csv";
		}
		try {
			FileInputStream fio = new FileInputStream(name);

			int size;

			byte[] data;

			String config = "";

			while((size = fio.available()) > 0) { // I don't know if this works
				data = new byte[size];
				fio.read(data);
				config += new String(data);
			}

			fio.close();

			comboBox.removeAllItems();

			comboBox.addItem("Custom");

			String[] elements = config.split(",");

			for (String s : elements) {
				comboBox.addItem(s.trim());
			}

		} catch (IOException e1) {
			System.out.println("File may not exist!");
			e1.printStackTrace();
		}
	}

}
