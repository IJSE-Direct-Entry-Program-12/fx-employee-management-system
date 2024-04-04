package lk.ijse.dep12.ems.controller;

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

    public void initialize() {
        /* Map columns to the domain object */
        tblEmployee.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("nic"));
        tblEmployee.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        tblEmployee.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("address"));
        tblEmployee.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("gender"));

        /* Set up table selection listener */
        tblEmployee.getSelectionModel().selectedItemProperty().addListener((o, previous, current) -> {
            container.setDisable(current == null);
            btnSave.setDisable(current != null);
            btnDelete.setDisable(current == null);
            txtNic.setEditable(current == null);
            txtName.setEditable(current == null);
            txtAddress.setEditable(current == null);
            genderWrapper.setDisable(current != null);

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

        String nic = txtNic.getText().strip();
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

        employeeList.add(new Employee(nic, name, address, gender));
        btnNewEmployee.fire();
    }

    public void btnDeleteOnAction(ActionEvent event) {
        ObservableList<Employee> employeeList = tblEmployee.getItems();
        employeeList.remove(tblEmployee.getSelectionModel().getSelectedItem());
        if (employeeList.isEmpty()) btnNewEmployee.fire();
    }

    public void tblEmployeeOnKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.DELETE) btnDelete.fire();
    }
}
