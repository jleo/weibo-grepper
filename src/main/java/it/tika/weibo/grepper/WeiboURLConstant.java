package it.tika.weibo.grepper;

/**
 * Created with IntelliJ IDEA.
 * User: jleo
 * Date: 12-7-3
 * Time: 下午10:19
 * To change this template use File | Settings | File Templates.
 */
public class WeiboURLConstant {
    //return fans url by uid
    public static String getFansURLByPage(String uid, int page){
        return "http://weibo.com/"+uid+"/fans?page="+page;
    }
}
