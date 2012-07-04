package it.tika.weibo.grepper;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.net.UnknownHostException;

public class MongoDBFactory {

    private static DB db;

    public synchronized static DB getDBInstance() throws UnknownHostException, MongoException {
        if (db == null) {
            PropertiesConfiguration config = null;
            try {
                config = new PropertiesConfiguration("weibo.properties");
            } catch (ConfigurationException e) {
                //LOG.warn(e.getMessage(), e);
            }

            String host = "localhost";
            int port = 27017;

            if (config != null) {
                host = config.getString("mongo.host", host);
                port = config.getInt("mongo.port", port);
            }
            db = new Mongo(host, port).getDB("weibo");

            if (config != null) {
                String user = config.getString("mongo.username");
                if (user != null) {
                    db.authenticate(user, config.getString("mongo.password").toCharArray());
                }
            }
        }
        return db;
    }

}
