package it.tika.weibo.grepper

import org.apache.http.client.methods.HttpGet

/**
 * Created with IntelliJ IDEA.
 * User: jleo
 * Date: 12-7-3
 * Time: 下午10:25
 * To change this template use File | Settings | File Templates.
 */
class FansGrepper {
    String uid

    def getFans() {
        Famous famous = new Famous(uid: uid)
        famous.relationStatus = "UPDATING"

        FamousDB.instance.insert(famous)

        def fanSet = new HashSet()
        def last = false
        (1..250).each {page ->
            println "page " + page
            if (last)
                return

            String url = WeiboURLConstant.getFansURLByPage(uid, page)


            HttpGet get = new HttpGet(url)

            String target = ""
            try {
                def result = SinaLogin.client.execute(get)
                def content = result.getEntity().getContent().text
                content.eachLine {it ->
                    if (it.indexOf("\"pid\":\"pl_relation_hisFans\"") != -1)
                        target = it
                }
            } catch (SocketTimeoutException e) {
                println "timed out,skip this page"
                return
            } catch (e) {
                last = true
                return
            }

            if (target.indexOf("\\u8fd8\\u6ca1\\u6709\\u4eba\\u5173\\u6ce8") != -1) {
                last = true
                return
            }
            target.findAll(~"usercard=\\\\\"id=[0-9]{10}").each {
                fanSet << it[14..23]
            }
            println fanSet.size()
        }
        famous.relationStatus = "DONE"

        FamousDB.instance.insert(famous)

        Relation r = new Relation(uid: uid)
        r.fans = fanSet.toList()
        r.status = "UPDATED"
        r.updateTime = new Date()
        RelationDB.instance.addLog(r)
    }

    public static void main(String[] args) {
        SinaLogin.login("ggyyleo@gmail.com", "3jf2hf1l")

        FansGrepper grepper = new FansGrepper(uid: "1197161814")
        grepper.getFans()
    }
}
