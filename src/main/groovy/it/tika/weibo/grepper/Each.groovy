package it.tika.weibo.grepper

import com.mongodb.DBObject

/**
 * Created with IntelliJ IDEA.
 * User: jleo
 * Date: 12-7-4
 * Time: 下午11:18
 * To change this template use File | Settings | File Templates.
 */
public interface Each {
        public void doWith(DBObject dbObject);
}