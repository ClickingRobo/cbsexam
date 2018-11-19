package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
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
                        rs.getString("email"));

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
                        rs.getString("salt"),
                        rs.getString("token"));


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


  //able to delete your own user through token implementation - so that you can only delete your own user
  public static boolean deleteUser(String token) {

    Log.writeLog(UserController.class.getName(), null, "Deleting an existing user from DB", 0);

    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    //Verifies token through the method "verifyToken and takes the token (String) as parameter
    DecodedJWT decodedJWT = verifyToken(token);

    //deletes user with the signed claim (ID) from the token --> your own userID
    String sqlDelete = "DELETE FROM user WHERE id = " + decodedJWT.getClaim("userid").asInt();

    int rs = dbCon.insert(sqlDelete);

    //returns 1 if executed correctly (the insert method is build like that)
    if (rs > 0){
        return true;
    } else {
        return false;
    }
  }


  //Make some logic so that you can't change to an existing email address and hash password whilst creating after a new password
  public static boolean updateUser(String token, User user) {

    String salt = "";

    Log.writeLog(UserController.class.getName(), null, "Updating an existing user from DB", 0);

    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    DecodedJWT decodedJWT = verifyToken(token);

    try {
      String sqlGetSalt = "SELECT salt FROM user WHERE id =\'" + decodedJWT.getClaim("userid").asInt() +"'";
      ResultSet rs = dbCon.query(sqlGetSalt);
      if (rs.next()){
        salt = rs.getString("salt");
      } else {
        System.out.println("could not return salt based on userid from claim in token");
      }
    } catch (SQLException e){
      e.printStackTrace();
    }

    String sql = "UPDATE user SET " +
                 "first_name = COALESCE(\'" + user.getFirstname() + "\', first_name), " +
                 "last_name = COALESCE(\'" + user.getLastname() + "\', last_name), " +
                 "password = COALESCE(\'" + Hashing.sha(user.getPassword(), salt) + "\', password), " +
                 "email = COALESCE(\'" + user.getEmail() + "\', email) " +
                 "WHERE id =" + decodedJWT.getClaim("userid").asInt() + "";


    int rs = dbCon.insert(sql);

    if (rs > 0) {
      return true;
    } else {
      return false;
    }
  }


  public static String userAuthentication (User user) {

    String salt = "";
    int userID = 0;
    long date = System.currentTimeMillis();
    String newAccessToken = "";

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
      String sql = "SELECT id FROM user WHERE email= \'" + user.getEmail() + "\' AND password = \'"
              + Hashing.sha(user.getPassword(), salt) + "\'";
      ResultSet rs = dbCon.query(sql);
      if (rs.next()) {
        userID = rs.getInt("id");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }


    //generates token via JWT. Has issue and expiriation date along with a claim on userID.
    try{
      //https://github.com/auth0/java-jwt
      //Assigns a token for the user using JWT
      Algorithm algorithm = Algorithm.HMAC256("secret");
      newAccessToken = JWT.create()
              .withIssuer("auth0")
              .withIssuedAt(new Date(date))
              //Token is available for about 28 hours
              .withExpiresAt(new Date(date+100000000))
              .withClaim("userid", userID)
              .sign(algorithm);

    } catch(JWTCreationException exception){
      exception.printStackTrace();
    }

    //Updates token for user if AccessToken has been initiated
    if(newAccessToken != null){

          String sql = "UPDATE user SET " + "token = \'" + newAccessToken + "\' " +
                  "WHERE id = \'" + userID + "\'";
          dbCon.insert(sql);

          return newAccessToken;

    }

    return null;
  }


  //Method able to decode token
  public static DecodedJWT verifyToken (String token){

    //declare
    DecodedJWT jwt;

    try{
      Algorithm algorithm = Algorithm.HMAC256("secret");
      JWTVerifier verifier = JWT.require(algorithm)
              .withIssuer("auth0")
              .build();
      jwt = verifier.verify(token);

      return jwt;

    } catch (JWTVerificationException exception){

    }
   return null;
  }

}