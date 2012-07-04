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
public class RelationDB {
    private static RelationDB instance;
    private DB db;

    public static final String urlCollectionName = "vrelation";

    private Logger logger = Logger.getLogger(RelationDB.class.getName());

    private RelationDB() throws UnknownHostException {
        db = MongoDBFactory.getDBInstance();
    }

    public static RelationDB getInstance() {
        if (instance == null)
            try {
                instance = new RelationDB();
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }

        return instance;
    }

    public void addLog(Relation result) {
        insert(result);
    }

    public void insert(Relation result) {
        BasicDBObject sampleObject = new BasicDBObject();
        sampleObject.put("uid", result.getUid());

        DBCursor c = db.getCollection(urlCollectionName).find(sampleObject);
        if (!c.hasNext()) {
            sampleObject.put("fans", result.getFans());
            try {
                db.getCollection(urlCollectionName).insert(sampleObject);
            } catch (MongoException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }else{
            DBObject object = c.next();
            result.getFans().addAll((List) object.get("fans"));
            object.put("fans", result.getFans());
            db.getCollection(urlCollectionName).save(object);
        }
    }
}
