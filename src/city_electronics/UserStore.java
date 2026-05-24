package city_electronics;

import java.io.*;
import java.util.ArrayList;

public class UserStore {

    public static ArrayList<User> users = new ArrayList<User>();

    // User class
    public static class User {

        String username;
        String password;

        public User(String username, String password) {

            this.username = username;
            this.password = password;
        }
    }

    // File name
    private static final String FILE_NAME = "users.txt";

    // Save user into file
    public static void saveUser(String username, String password) {

        try {

            BufferedWriter bw = new BufferedWriter(
                    new FileWriter(FILE_NAME, true));

            bw.write(username + "," + password);
            bw.newLine();

            bw.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // Load users from file
    public static void loadUsers() {

        users.clear();

        File file = new File(FILE_NAME);

        if (!file.exists()) {
            return;
        }

        try {

            BufferedReader br = new BufferedReader(
                    new FileReader(file));

            String line;

            while ((line = br.readLine()) != null) {

                String[] data = line.split(",");

                if (data.length == 2) {

                    users.add(new User(
                            data[0].trim(),
                            data[1].trim()
                    ));
                }
            }

            br.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}