package controllers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import model.*;
import utils.Log;

public class OrderController {

  private static DatabaseController dbCon;
  private static Connection connection = null;

  public OrderController() {
    dbCon = new DatabaseController();
  }

  public static Order getOrder(int id) {

    // check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build SQL string to query
    String sql = "SELECT * FROM orders where id=" + id;

    // Do the query in the database and create an empty object for the results
    ResultSet rs = dbCon.query(sql);
    Order order = null;

    try {
      if (rs.next()) {

        // Perhaps we could optimize things a bit here and get rid of nested queries.
        User user = UserController.getUser(rs.getInt("user_id"));
        ArrayList<LineItem> lineItems = LineItemController.getLineItemsForOrder(rs.getInt("id"));
        Address billingAddress = AddressController.getAddress(rs.getInt("billing_address_id"));
        Address shippingAddress = AddressController.getAddress(rs.getInt("shipping_address_id"));

        // Create an object instance of order from the database dataa
        order =
            new Order(
                rs.getInt("id"),
                user,
                lineItems,
                billingAddress,
                shippingAddress,
                rs.getFloat("order_total"),
                rs.getLong("created_at"),
                rs.getLong("updated_at"));

        // Returns the build order
        return order;
      } else {
        System.out.println("No order found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Returns null
    return order;
  }

  /**
   * Get all orders in database
   *
   * @return
   */
  public static ArrayList<Order> getOrders() {

    //SOURCE of information: https://docs.oracle.com/javase/8/docs/api/java/util/HashMap.html
    //Creates a hashmap for key Integer and value Order
    Map<Integer, Order> hashmap = new HashMap<>();

    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    String sql = "SELECT *, orders.id as order_id, billing_address.name as billing_address_name, shipping_address.name as " +
            "shipping_address_name, user.id as user_id, billing_address.id as billing_address_id, " +
            "shipping_address.id as shipping_address_id, " +
            "product.id as product_id, line_item.id as line_item_id " +
            "FROM orders " +
            "INNER JOIN user on user.id = orders.user_id " +
            "INNER JOIN address as billing_address ON orders.billing_address_id = billing_address.id " +
            "INNER JOIN address as shipping_address ON orders.shipping_address_id = shipping_address.id " +
            "INNER JOIN line_item ON orders.id = line_item.order_id " +
            "INNER JOIN product ON line_item.product_id = product.id";

    ResultSet rs = dbCon.query(sql);
    ArrayList<Order> orders = new ArrayList<Order>();

    try {
      while(rs.next()) {

        int orderid = rs.getInt("order_id");

        // Perhaps we could optimize things a bit here and get rid of nested queries.
        User user = new User(
                rs.getInt("user_id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("password"),
                rs.getString("email"));


        Address billingAddress =
                new Address(
                        rs.getInt("billing_address_id"),
                        rs.getString("billing_address_name"),
                        rs.getString("street_address"),
                        rs.getString("city"),
                        rs.getString("zipcode")
                );

        Address shippingAddress =
                new Address(
                        rs.getInt("shipping_address_id"),
                        rs.getString("shipping_address_name"),
                        rs.getString("street_address"),
                        rs.getString("city"),
                        rs.getString("zipcode")
                );

       Product product =
                new Product(
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getString("sku"),
                        rs.getFloat("price"),
                        rs.getString("description"),
                        rs.getInt("stock"));


        LineItem lineItem =
                new LineItem(
                        rs.getInt("line_item_id"),
                        product,
                        rs.getInt("quantity"),
                        rs.getFloat("price"));


        if (!hashmap.containsKey(orderid)){

          // Create an order from the database data
          Order order =
                  new Order(
                          rs.getInt("order_id"),
                          user,
                          //arraylist since lineitem is specified as such
                          new ArrayList<LineItem>(),
                          billingAddress,
                          shippingAddress,
                          rs.getFloat("order_total"),
                          rs.getLong("created_at"),
                          rs.getLong("updated_at"));

          //uses put (first parameter is key and second is what value should be associated with the specified key
          hashmap.put(orderid, order);

          // Add order to our list
          orders.add(order);
        }

        Order order = hashmap.get(orderid);
        order.getLineItems().add(lineItem);
        //ArrayList<LineItem> lineItems = LineItemController.getLineItemsForOrder(rs.getInt("id"));
        //Address billingAddress = AddressController.getAddress(rs.getInt("billing_address_id"));
        //Address shippingAddress = AddressController.getAddress(rs.getInt("shipping_address_id"));


      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // return the orders
    return orders;
  }

  public static Order createOrder(Order order) {

    // Write in log that we've reach this step
    Log.writeLog(OrderController.class.getName(), order, "Actually creating a order in DB", 0);

    // Set creation and updated time for order.
    order.setCreatedAt(System.currentTimeMillis() / 1000L);
    order.setUpdatedAt(System.currentTimeMillis() / 1000L);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }



    // TODO: Enable transactions in order for us to not save the order if somethings fails for some of the other inserts (FIXED).
    //https://docs.oracle.com/javase/tutorial/jdbc/basics/transactions.html
    try{
      connection = dbCon.getConnection();
      connection.setAutoCommit(false);

      // Save addresses to database and save them back to initial order instance
      order.setBillingAddress(AddressController.createAddress(order.getBillingAddress()));
      order.setShippingAddress(AddressController.createAddress(order.getShippingAddress()));

      // Save the user to the database and save them back to initial order instance
      order.setCustomer(UserController.createUser(order.getCustomer()));

      // Insert the product in the DB
      int orderID = dbCon.insert(
              "INSERT INTO orders(user_id, billing_address_id, shipping_address_id, order_total, created_at, updated_at) VALUES("
                      + order.getCustomer().getId()
                      + ", "
                      + order.getBillingAddress().getId()
                      + ", "
                      + order.getShippingAddress().getId()
                      + ", "
                      + order.calculateOrderTotal()
                      + ", "
                      + order.getCreatedAt()
                      + ", "
                      + order.getUpdatedAt()
                      + ")");


      if (orderID != 0) {
        //Update the productid of the product before returning
        order.setId(orderID);
      }

      // Create an empty list in order to go trough items and then save them back with ID
      ArrayList<LineItem> items = new ArrayList<LineItem>();


      // Save line items to database
      for(LineItem item : order.getLineItems()){
        item = LineItemController.createLineItem(item, order.getId());
        items.add(item);
      }

      order.setLineItems(items);


      connection.commit();


    } catch (SQLException exception) {
        exception.printStackTrace();
      if (connection != null) {
        try {
          System.out.print("Transaction will be rolled back now");
          connection.rollback();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    } finally {
        try {
          connection.setAutoCommit(true);
      } catch (SQLException e) {
          e.printStackTrace();
      }
    }

    // Return order
    return order;

  }
}