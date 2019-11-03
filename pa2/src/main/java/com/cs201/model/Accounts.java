package com.cs201.model;

        import javax.persistence.*;
        import java.util.List;
        import java.util.Set;

@Entity
@Table(name="Accounts")
public class Accounts {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userID", updatable = false, nullable = false)
    private int userID;

    private String username;
    private String password;

    @OneToMany(mappedBy = "account")
    private List<Junction> junctions;


    public int getUserID(){
        return userID;
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Junction> getJunctions() {
        return junctions;
    }

    public void setJunctions(List<Junction> junctions) {
        this.junctions = junctions;
    }
}
