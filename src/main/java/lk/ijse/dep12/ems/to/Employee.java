package lk.ijse.dep12.ems.to;

public class Employee {
    private String nic;
    private String name;
    private String address;
    private String gender;

    public Employee(String nic, String name, String address, String gender) {
        this.nic = nic;
        this.name = name;
        this.address = address;
        this.gender = gender;
    }

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "nic='" + nic + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", gender='" + gender + '\'' +
                '}';
    }
}
