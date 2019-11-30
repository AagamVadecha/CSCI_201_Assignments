package com.company;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class configFileProperties {
    private String hostname;
    private String port;
    private String connection;
    private String DBUsername;
    private String DBPassword;
    private String secretWordFile;

    public configFileProperties(InputStream is) throws IOException {
        Properties configFile = new Properties();
        if (is != null) {
            configFile.load(is);
        }

        System.out.println("\nReading config file...");
        hostname = configFile.getProperty("ServerHostname");
        port = configFile.getProperty("ServerPort");
        connection = configFile.getProperty("DBConnection");
        DBUsername = configFile.getProperty("DBUsername");
        DBPassword = configFile.getProperty("DBPassword");
        secretWordFile = configFile.getProperty("SecretWordFile");
    }
    public String getHostname() {
        return hostname;
    }


    public String getPort() {
        return port;
    }

    public String getConnection() {
        return connection;
    }


    public String getDBUsername() {
        return DBUsername;
    }

    public String getDBPassword() {
        return DBPassword;
    }


    public String getSecretWordFile() {
        return secretWordFile;
    }
}
