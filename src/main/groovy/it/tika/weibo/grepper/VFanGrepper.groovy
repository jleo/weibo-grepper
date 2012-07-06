package it.tika.weibo.grepper

import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import org.apache.http.client.params.ClientPNames
import org.apache.http.impl.cookie.BrowserCompatSpec
import org.apache.http.params.HttpParams

import java.util.concurrent.BlockingQueue

import org.apache.http.cookie.*
import java.util.concurrent.ArrayBlockingQueue

/**
 * Created with IntelliJ IDEA.
 * User: jleo
 * Date: 12-7-4
 * Time: 下午11:14
 * To change this template use File | Settings | File Templates.
 */
class VFanGrepper {
    private static threadNumber = 5;
    private static BlockingQueue queue = new ArrayBlockingQueue(threadNumber);

    public static void main(String[] args) {

        CookieSpecFactory csf = new CookieSpecFactory() {
            public CookieSpec newInstance(HttpParams params) {
                return new BrowserCompatSpec() {
                    @Override
                    public void validate(Cookie cookie, CookieOrigin origin)
                    throws MalformedCookieException {
                        //Oh, I am easy
                    }
                };
            }
        };

        String username = "ggyyleo@gmail.com"
        String passwd = "3jf2hf1l"

        if (args.length == 2) {
            username = args[0]
            passwd = args[1]
        }

        SinaLogin.login(username, passwd)
        SinaLogin.client.getCookieSpecs().register("easy", csf);
        SinaLogin.client.getParams().setParameter(ClientPNames.COOKIE_POLICY, "easy");
        BasicDBObject sampleObject = new BasicDBObject();
        sampleObject.put("relationStatus", "NEW");

        (1..threadNumber).each {
            Thread.start {
                while (true) {
                    DBObject object = queue.take()

                    String vuid = object.get("uid");

                    FansGrepper grepper = new FansGrepper(uid: vuid);
                    grepper.getFans()
                }
            }
        }
        FamousDB.instance.doWithEach(sampleObject, [doWith: {DBObject object ->
            queue.put(object);
        }] as Each);

    }
}
