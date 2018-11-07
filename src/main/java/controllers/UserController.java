package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import model.User;
import utils.Hashing;
import utils.Log;

public class UserController {

  private static DatabaseController dbCon;

  public UserController() {
    dbCon = new DatabaseController();
  }

  public static User getUser(int id) {

    // Check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build the query for DB
    String sql = "SELECT * FROM user where id=" + id;

    // Actually do the query
    ResultSet rs = dbCon.query(sql);
    User user = null;

    try {
      // Get first object, since we only have one
      if (rs.next()) {
        user =
                new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getLong("created_at"),
                        rs.getString("salt"));

        // return the create object
        return user;
      } else {
        System.out.println("No user found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return null
    return user;
  }

  /**
   * Get all users in database
   *
   * @return
   */
  public static ArrayList<User> getUsers() {

    // Check for DB connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build SQL
    String sql = "SELECT * FROM user";

    // Do the query and initialyze an empty list for use if we don't get results
    ResultSet rs = dbCon.query(sql);
    ArrayList<User> users = new ArrayList<User>();

    try {
      // Loop through DB Data
      while (rs.next()) {
        User user =
                new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getLong("created_at"),
                        rs.getString("salt"));

        // Add element to list
        users.add(user);
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return the list of users
    return users;
  }

  public static User createUser(User user) {

    Hashing hashing = new Hashing();

    // Write in log that we've reach this step
    Log.writeLog(UserController.class.getName(), user, "Actually creating a user in DB", 0);

    // Set creation time for user.
    user.setCreatedTime(System.currentTimeMillis() / 1000L);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    //sets the salt from the server to the specific client
    user.setSalt(hashing.combiningSalts());

    //hashing password with sha algorithm
    user.setPassword(Hashing.sha(user.getPassword(), user.getSalt()));

    // Insert the user in the DB
    // TODO: Hash the user password before saving it (FIXED).
    //Creates object to access method from the class since it's not static

    int userID = dbCon.insert(
            "INSERT INTO user(first_name, last_name, password, email, created_at, salt) VALUES('"
                    + user.getFirstname()
                    + "', '"
                    + user.getLastname()
                    + "', '"
                    + user.getPassword()
                    + "', '"
                    + user.getEmail()
                    + "', '"
                    + user.getCreatedTime()
                    + "', '"
                    + user.getSalt()
                    + "')");


    if (userID != 0) {
      //Update the userid of the user before returning
      user.setId(userID);
    } else {
      // Return null if user has not been inserted into database
      return null;
    }

    // Return user
    return user;
  }


  public static User deleteUser(User user) {

    Log.writeLog(UserController.class.getName(), user, "Deleting an existing user from DB", 0);

    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    String sqlDelete = "DELETE * FROM user WHERE id = " + user.id;

    dbCon.insert(sqlDelete);

    return user;
  }


  //Make some logic so that you can't change to an existing email adress and hash password whilst creating after a new password
  public static User updateUser(User user) {

    Log.writeLog(UserController.class.getName(), user, "Updating an existing user from DB", 0);

    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    //Apply some logic so that 2x users cannot have the same email adress!
    String sqlUpdate = "UPDATE * FROM user SET firstname = ?, lastname = ? WHERE id = " + user.id;

    dbCon.insert(sqlUpdate);

    return user;
  }


  public static User userAuthentication (User user) {

    String salt = "";

    Log.writeLog(UserController.class.getName(), user, "Authenticating the user/log in", 0);

    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    //Obtain salt from database
    try{
      String sqlGetSalt = "SELECT salt FROM user WHERE email=\'" + user.getEmail() + "\'";
      ResultSet rs = dbCon.query(sqlGetSalt);
      if (rs.next()){
        salt = rs.getString("salt");
      } else {
        System.out.println("could not return salt based on email");
        return null;
      }
    } catch (SQLException error){
      error.printStackTrace();
    }


    //Authenticate user-login via email and password
    try {
      String sql = "SELECT created_at FROM user WHERE email= \'" + user.getEmail() + "\' AND password = \'"
              + Hashing.sha(user.getPassword(), salt) + "\'";
      ResultSet rs = dbCon.query(sql);
      if (rs.next()) {
        user =
                new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"));

      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return user;
  }
}