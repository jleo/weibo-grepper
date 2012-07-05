package it.tika.weibo.grepper

import com.mongodb.DBObject
import com.mongodb.BasicDBObject

/**
 * Created with IntelliJ IDEA.
 * User: jleo
 * Date: 12-7-4
 * Time: 下午11:14
 * To change this template use File | Settings | File Templates.
 */
class VFanGrepper {
    public static void main(String[] args) {
        SinaLogin.login("ggyyleo@gmail.com","3jf2hf1l")
        BasicDBObject sampleObject = new BasicDBObject();
        sampleObject.put("relationStatus","NEW");

        FamousDB.instance.doWithEach(sampleObject, [doWith:{DBObject object->
            String vuid = object.get("uid");
            FansGrepper grepper = new FansGrepper(uid:vuid);
            grepper.getFans()
        }] as Each);

    }
}
