package lk.ijse.dep12.ems.controller;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import lk.ijse.dep12.ems.to.Employee;

import java.io.*;
import java.util.Optional;

public class MainViewController {

    public Button btnDelete;
    public Button btnNewEmployee;
    public Button btnSave;
    public GridPane container;
    public ToggleGroup grpGender;
    public RadioButton rdFemale;
    public RadioButton rdMale;
    public AnchorPane root;
    public TableView<Employee> tblEmployee;
    public TextField txtAddress;
    public TextField txtName;
    public TextField txtNic;
    public HBox genderWrapper;
    private final File DB_FILE = new File(System.getProperty("user.home"), ".ems.db");

    public void initialize() {
        /* Map columns to the domain object */
        tblEmployee.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("nic"));
        tblEmployee.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        tblEmployee.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("address"));
        tblEmployee.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("gender"));

        /* Set up the table selection listener */
        tblEmployee.getSelectionModel().selectedItemProperty().addListener((o, previous, current) -> {
            container.setDisable(current == null);
            btnSave.setDisable(current != null);
            btnDelete.setDisable(current == null);
            txtNic.setEditable(current == null);
            txtName.setEditable(current == null);
            txtAddress.setEditable(current == null);
            genderWrapper.setDisable(current != null);

            /* Display currently selected employee details */
            if (current != null) {
                txtNic.setText(current.getNic());
                txtName.setText(current.getName());
                txtAddress.setText(current.getAddress());
                grpGender.selectToggle(current.getGender().equals("MALE") ? rdMale : rdFemale);
            }
        });

        /* Set up mnemonics */
        container.lookupAll(".label").forEach(node -> {
            Label lbl = (Label) node;
            lbl.setLabelFor(container.lookup(lbl.getAccessibleText()));
        });

        container.setDisable(true);
        loadEmployeeDetails();
    }

    private void loadEmployeeDetails(){
        try {
            if (!DB_FILE.exists()) {
                DB_FILE.createNewFile();
                return;
            }

            try (BufferedReader br = new BufferedReader(new FileReader(DB_FILE))) {
                while (true) {
                    String nic = br.readLine();
                    if (nic == null) break;
                    String name = br.readLine();
                    String address = br.readLine();
                    String gender = br.readLine();
                    String newLine = br.readLine();

                    if (name == null || address == null || gender == null || newLine == null ||
                            !isValidNic(nic) || !isValidName(name) ||
                            address.length() < 3 || !(gender.equals("MALE") || gender.equals("FEMALE")) ||
                            !newLine.equals("")) {
                        Alert alert = new Alert(Alert.AlertType.ERROR,
                                "Corrupted database found. Do you want to reinitialize the database?",
                                ButtonType.YES, ButtonType.NO);
                        alert.setHeaderText("Database Error");
                        Optional<ButtonType> buttonType = alert.showAndWait();
                        if (buttonType.get() == ButtonType.YES) {
                            DB_FILE.delete();
                            DB_FILE.createNewFile();
                            return;
                        } else {
                            Platform.exit();
                            return;
                        }
                    } else {
                        ObservableList<Employee> employeeList = tblEmployee.getItems();
                        employeeList.add(new Employee(nic, name, address, gender));
                    }
                }
            }
        }catch (IOException exception){
            Alert alert = new Alert(Alert.AlertType.ERROR, "Something went wrong. Try again. If the problem persists, contact DEP-12");
            alert.setHeaderText("Loading Error");
            alert.showAndWait();
            exception.printStackTrace();
            Platform.exit();
        }
    }

    public void btnNewEmployeeOnAction(ActionEvent event) {
        /* Clear everything */
        tblEmployee.getSelectionModel().clearSelection();
        txtNic.clear();
        txtName.clear();
        txtAddress.clear();
        grpGender.selectToggle(null);

        container.setDisable(false);
        txtNic.requestFocus();
    }

    private boolean isValidNic(String nic) {
        if (nic.length() != 10 || !nic.toUpperCase().endsWith("V")) return false;
        for (char c : nic.substring(0, 9).toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    private boolean isValidName(String name) {
        for (char c : name.toCharArray()) {
            if (!(Character.isSpaceChar(c) || Character.isLetter(c))) return false;
        }
        return true;
    }

    private boolean validateData() {
        boolean valid = true;
        container.lookupAll(".error").forEach(node -> node.getStyleClass().remove("error"));

        if (grpGender.getSelectedToggle() == null) {
            genderWrapper.getStyleClass().add("error");
            rdMale.requestFocus();
            valid = false;
        }

        String address = txtAddress.getText();
        if (address.isBlank() || address.length() < 3) {
            txtAddress.getStyleClass().add("error");
            txtAddress.requestFocus();
            valid = false;
        }

        String name = txtName.getText();
        if (name.isBlank() || !isValidName(name)) {
            txtName.getStyleClass().add("error");
            txtName.requestFocus();
            valid = false;
        }

        String nic = txtNic.getText();
        if (nic.isBlank() || !isValidNic(nic)) {
            txtNic.getStyleClass().add("error");
            txtNic.requestFocus();
            valid = false;
        }

        return valid;
    }

    public void btnSaveOnAction(ActionEvent event) {
        ObservableList<Employee> employeeList = tblEmployee.getItems();

        /* Data Validation */
        if (!validateData()) {
            return;
        }

        String nic = txtNic.getText().strip().toUpperCase();
        String name = txtName.getText().strip();
        String address = txtAddress.getText().strip();
        String gender = ((RadioButton) grpGender.getSelectedToggle()).getText();

        /* Business Validation */
        for (Employee employee : employeeList) {
            if (employee.getNic().equals(nic)){
                Alert alert = new Alert(Alert.AlertType.ERROR, "NIC is already associated with another employee");
                alert.setHeaderText("Duplicate NIC Error");
                alert.showAndWait();
                txtNic.getStyleClass().add("error");
                txtNic.requestFocus();
                return;
            }
        }

        Employee employee = new Employee(nic, name, address, gender);
        if (saveEmployee(employee)){
            employeeList.add(employee);
            btnNewEmployee.fire();
        }else{
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Failed to save the employee, something went wrong, try again.");
            alert.setHeaderText("Save Failed");
            alert.show();
        }
    }

    private boolean saveEmployee(Employee employee){
        // File dbFile = new File(System.getProperty("user.home"), ".ems.db");
        try(var bw = new BufferedWriter(new FileWriter(DB_FILE, true))){
            DB_FILE.createNewFile();
            bw.write(employee.getNic() + "\n");
            bw.write(employee.getName() + "\n");
            bw.write(employee.getAddress() + "\n");
            bw.write(employee.getGender() + "\n\n");
            return true;
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
    }

    public void btnDeleteOnAction(ActionEvent event) {
        ObservableList<Employee> employeeList = tblEmployee.getItems();

        if (deleteEmployee(tblEmployee.getSelectionModel().getSelectedItem())){
            employeeList.remove(tblEmployee.getSelectionModel().getSelectedItem());
            if (employeeList.isEmpty()) btnNewEmployee.fire();
        }else{
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to delete the employee, try again!");
            alert.setHeaderText("Delete Failed");
            alert.show();
        }
    }

    private boolean deleteEmployee(Employee deleteEmployee){
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(DB_FILE))){
            ObservableList<Employee> employeeList = tblEmployee.getItems();
            for (Employee employee : employeeList) {
                if (deleteEmployee == employee) continue;

                bw.write(employee.getNic() + "\n");
                bw.write(employee.getName() + "\n");
                bw.write(employee.getAddress() + "\n");
                bw.write(employee.getGender() + "\n\n");
            }
            return true;
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
    }

    public void tblEmployeeOnKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.DELETE) btnDelete.fire();
    }
}
