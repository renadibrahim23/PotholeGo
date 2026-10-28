package com.community.model.lookup;

public class Role {
    // names as stored in the roles table; donors register as Residents
    public static final String ADMIN = "Admin";
    public static final String RESIDENT = "Resident";

    private int id;
    private String name;

    public Role(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
