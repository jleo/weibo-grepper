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
public class Famous {
    String uid;
    String name;
    Set fields = new HashSet();
    String relationStatus;

    public Set getAreas() {
        return areas;
    }

    public void setAreas(Set areas) {
        this.areas = areas;
    }

    Set areas = new HashSet();
    String url;
    String kind;//比如是从名人堂，媒体汇，品牌馆等最大分类

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }



    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set getFields() {
        return fields;
    }

    public void setFields(Set fields) {
        this.fields = fields;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRelationStatus() {
        return relationStatus;
    }

    public void setRelationStatus(String relationStatus) {
        this.relationStatus = relationStatus;
    }
}
