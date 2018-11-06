package cache;

import controllers.UserController;
import model.User;
import utils.Config;

import java.util.ArrayList;

//TODO: Build this cache and use it (FIXED).
public class UserCache {

    private ArrayList<User> users;

    private long ttl;

    private long created;

    public UserCache(){
        this.ttl = Config.getUserTtl();
    }


    public ArrayList<User> getUsers(Boolean forceUpdate){
        //USED FOR DEBUGGING:
        //System.out.println(forceUpdate);
        //System.out.println(this.ttl);
        //System.out.println(System.currentTimeMillis() / 1000L);

        if (forceUpdate
        || ((this.created / 1000L + this.ttl) >= (System.currentTimeMillis() / 1000L))
        || this.users == null){

            ArrayList<User> users = UserController.getUsers();

            this.users = users;
            this.created = System.currentTimeMillis() / 1000L;
        }

        return this.users;
    }
}
