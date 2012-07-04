package it.tika.weibo.grepper;

import com.mongodb.*;

import java.net.UnknownHostException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: jleo
 * Date: 3/15/11
 * Time: 10:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class FamousDB {
    private static FamousDB instance;
    private DB db;

    public static final String urlCollectionName = "famous";

    private Logger logger = Logger.getLogger(FamousDB.class.getName());

    private FamousDB() throws UnknownHostException {
        db = MongoDBFactory.getDBInstance();
    }

    public static FamousDB getInstance() {
        if (instance == null)
            try {
                instance = new FamousDB();
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }

        return instance;
    }

    public void addLog(Famous result) {
        insert(result);
    }

    public void doWithEach(Each each){
        BasicDBObject sampleObject = new BasicDBObject();
        DBCursor cursor = db.getCollection(urlCollectionName).find(sampleObject);
        while (cursor.hasNext()){
            DBObject object = cursor.next();
            each.doWith(object);

        }
    }

    public void insert(Famous result) {
        BasicDBObject sampleObject = new BasicDBObject();
        sampleObject.put("uid", result.getUid());
        sampleObject.put("name", result.getName());
        sampleObject.put("url", result.getUrl());

        DBCursor c = db.getCollection(urlCollectionName).find(sampleObject);

        if (!c.hasNext()) {
            try {
                sampleObject.put("fields", result.getFields());
                db.getCollection(urlCollectionName).insert(sampleObject);
            } catch (MongoException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }else{
            DBObject object = c.next();
            List fields = (List) object.get("fields");
            if(fields!=null && fields.size() != 0)
                result.getFields().addAll(fields);

            object.put("fields", result.getFields());
            db.getCollection(urlCollectionName).save(object);
        }
    }
}
