package it.tika.weibo.grepper;

import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: jleo
 * Date: 12-7-4
 * Time: 上午10:45
 * To change this template use File | Settings | File Templates.
 */
public class Relation {
    String uid;
    Set<String> fans = new HashSet<String>();

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Set<String> getFans() {
        return fans;
    }

    public void setFans(Set<String> fans) {
        this.fans = fans;
    }
}
