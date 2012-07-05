import Tools
import it.tika.weibo.grepper.FamousDB
import it.tika.weibo.grepper.Famous
import org.htmlcleaner.*
/**
 * Created with IntelliJ IDEA.
 * User: shang
 * Date: 12-7-3
 * Time: 下午10:24
 * To change this template use File | Settings | File Templates.
 */
def first_last(text) {

    resutls = []
    def pattern = ~"ct_b.*?\\/li>"
    def matcher = text =~ pattern
    i = 0
    while (matcher.find()) {
        content = matcher.group()
        if (!content.contains("city"))
            content.indexOf(" class=\\")
        println "**"
        def url_right = content.substring(content.indexOf("a href=\\") + "a href=\\".length(), content.indexOf(" class"))[1..-2]
        def classify = content.substring(content.indexOf("class=\\\"child_link\\\">") + "class=\\\"child_link\\\">".length(), content.indexOf("<\\/a><\\/li>"))
        url_right = url_right.replaceAll('\\\\', '')
        classify = Tools.unicodeToString(classify.toString())

        println url_right
        println classify
        resutls << [url_right, classify]
    }
    return resutls
}
/*
*第一级页面
* first( 'http://verified.weibo.com/')
*/

def first = {   def url ->
    results = []
    url.toURL().eachLine {line ->
        def pattern1 = ~"nav_barMain.*?(?=nav_barMain)"
        def matcher1 = line =~ pattern1
        int i = 0
        while (matcher1.find()) {

            content = matcher1.group()
            def pattern2 = ~/a_inner.*?(?=nav_aItem)|a_inner.*/
            def matcher2 = content =~ pattern2

            while (matcher2.find()) {

                content2 = matcher2.group()
                println content2
                def url_right = content2.substring(content2.indexOf("a_inner\\\"> <a href=\\\"") + "a_inner\\\"> <a href=\\\"".length(), content2.indexOf("\" class=\\\"a_link\\\""))
                def classify = content2.substring(content2.indexOf("class=\\\"a_link\\\">") + "class=\\\"a_link\\\">".length(), content2.indexOf("<em class=\\\"nav_arr\\"))
                url_right = url_right.replaceAll('\\\\', '')

                classify = Tools.unicodeToString(classify.toString())
                //println url_right
                //println classify
                tag = i == 0 ? "industry" : "area"
                println tag
                println classify
                //   if(tag == "area")     {
                res = first_last(content2)
                results << [url_right, classify, tag, res]
                println "==="
                //    }
            }
            i = i + 1

        }


    }
    return results
}

def getUrl_rightAndClassify(content, p1, p2, p3, p4) {

    if (content.contains(p1)) {

        def url_right = content.substring(content.indexOf(p1) + p1.length(), content.indexOf(p2, content.indexOf(p1) + p1.length(),))
        def classify = content.substring(content.indexOf(p3, content.indexOf(p1) + p1.length()) + p3.length(), content.indexOf(p4, content.indexOf(p3, content.indexOf(p1) + p1.length()) + p3.length()))
        url_right = url_right.replaceAll('\\\\', '')
        classify = Tools.unicodeToString(classify.toString())
        println url_right
        println classify

        return [url_right, classify]
    }

}

/*第二级页面，比如找 娱乐-影视-配音演员，找到配音演员这级
second("http://verified.weibo.com/fame/yingshi")*/

def second(def url) {
    results = []
    url.toURL().eachLine {line ->
        //println line
        def pattern1 = ~/cat_B.*?(?=span)/
        def matcher1 = line =~ pattern1
        if (matcher1.size() == 1) {
            return null
        }
        matcher1 = line =~ pattern1
        while (matcher1.find()) {
            content = matcher1.group()
            // println content
            p1 = "href=\\\"\\"
            p2 = "\\\">"
            p3 = "\\\">"
            p4 = "<\\/a>"
            item = getUrl_rightAndClassify(content, p1, p2, p3, p4)
            if (item != null)
                results << item
        }

    }
    return results
}

/*第三级，拼接url*/

def thrid(def base_url, def clist) {
    base_url = base_url.replaceAll("\" target=\"_new", "")
    _url = ""
    if (!base_url.contains("?")) {
        _url = base_url + "?srt=4&rt=0"
    } else {
        _url = base_url + "&rt=0"
    }

    println _url

    _url = _url.replaceAll("srt=3", "srt=4")
    println "after srt=3 to srt=4: " + _url
    ("a".."z").each {num ->
        url = _url + "&letter=" + num
        try {
            thrid_last(url, clist)
        } catch (ex) {
            ex.printStackTrace()
            errorfile = new File("error.txt")
            errorfile.append("#$url\n")
            errorfile.append(clist.toString() + "\n")
        }

    }
}

def thrid_get_persons(def content, def clist) {
    def pattern1 = ~/select_user.*?(?=name W_linkc)/
    def matcher1 = content =~ pattern1

    while (matcher1.find()) {
        _content = matcher1.group()

        uid = _content.substring(_content.indexOf("uid=") + "uid=".length(), _content.indexOf("\\\"", _content.indexOf("uid=") + "uid=".length()))
        title = _content.substring(_content.indexOf("title=\\\"") + "title=\\\"".length(), _content.indexOf("\\\"", _content.indexOf("title=\\\"") + "title=\\\"".length()))
        weibo_url = _content.substring(_content.indexOf("a target=\\\"_blank\\\" href=\\\"") + "a target=\\\"_blank\\\" href=\\\"".length(), _content.indexOf("\\\"", _content.indexOf("a target=\\\"_blank\\\" href=\\\"") + "a target=\\\"_blank\\\" href=\\\"".length()))
        weibo_url = weibo_url.replaceAll('\\\\', '').replaceAll("&amp;", "&")

        weibo_avatar = _content.substring(_content.indexOf("class_card\\\" src=\\\"") + "class_card\\\" src=\\\"".length(), _content.indexOf("\\\"", _content.indexOf("class_card\\\" src=\\\"") + "class_card\\\" src=\\\"".length()))
        weibo_avatar = weibo_avatar.replaceAll('\\\\', '').replaceAll("&amp;", "&")

        title = Tools.unicodeToString(title)


       def famous = new Famous(uid: uid, name: title, url: weibo_url, kind: _from)
        Set s = new HashSet()

        s.addAll(clist[1..-1])
        if (clist[0] == "industry")
            famous.setFields(s)
        if (clist[0] == 'area')
            famous.setAreas(s)

        FamousDB.instance.addLog(famous)


        println "$uid $title $weibo_url $weibo_avatar"
        println "_from: " + _from
        println clist

//        def f = new File("r3.txt")
//        f.append("# $uid $title $weibo_url $weibo_avatar\n")
//        f.append(clist.toString()+"\n")

    }

}

/*处理最后页面，抓取人*/

def thrid_last(def url, def clist, def page_num = 1) {
    println "handle $url"
    url.toURL().eachLine {line ->
        content = line.toString()
        thrid_get_persons(content, clist)
        p1 = "<a bpfilter=\\\"true\\\" class=\\\"W_btn_a\\\" href=\\\""
        p2 = "\\\""
        if (content.contains(p1)) {
            to_url = content.substring(content.lastIndexOf(p1) + p1.length(), content.indexOf(p2, content.lastIndexOf(p1) + p1.length()))
            to_url = to_url.replaceAll('\\\\', '').replaceAll("&amp;", "&")

            nextpagenum = to_url.substring(to_url.indexOf("page=") + "page=".length())
            if (nextpagenum > page_num) {
                thrid_last(to_url, clist, nextpagenum)
            }
        }

    }
}


def do_something_when_fame = {   def _item, def url, def classify, def tag, def _url, def _classify, def weibo_main_url, def error_list ->
    if (tag == "area") {
        try {
            println "$tag $classify $_classify"
            thrid(_url, [tag, classify, _classify])
        }
        catch (ex) {
            ex.printStackTrace()
            error_list << [_url, _classify]
        }

    } else {
        try {
            _res = second(_url)
            if (_res == null) {
                println _url
                println "$tag $classify $_classify"

                thrid(_url, [tag, classify, _classify])
            } else {
                _item << _res
                println "$tag $classify $_classify"
                for (r in _res) {
                    println "@@"
                    uurl = weibo_main_url[0..-2] + r[0]
                    println r[1]
                    thrid(uurl, [tag, classify, _classify, r[1]])
                }
            }
        }
        catch (ex) {
            ex.printStackTrace()
            error_list << [_url, _classify]
        }
    }
}

//    brand   website  agency    campus  media  均可用
def do_something_when_brand = {   def _item, def url, def classify, def tag, def _url, def _classify, def weibo_main_url, def error_list ->

    try {
        println "$tag $classify $_classify"
        thrid(_url, [tag, classify, _classify])
    }
    catch (ex) {
        ex.printStackTrace()
        error_list << [_url, _classify]
    }


}



def run_main(def weibo_main_url, def first, def do_something, def _from) {
    error_list = []
//    int firstLevelSkip = 13
//    int secondLevelSkip = 3
    int firstLevelSkip = 0
    int secondLevelSkip = 0
    boolean firstTime = true
    mlist = first(weibo_main_url)[firstLevelSkip..-1]
    //println mlist
    for (item in mlist) {


        url = item[0]?.toString().contains("http://") ? item[0].toString() : weibo_main_url + item[0][1..-1]

        //url =   weibo_main_url+item[0][1..-1]
        println "@@@# " + url
        classify = item[1]
        tag = item[2]
        println classify

        def loopItem

        if (item[3].size() == 0) {
            loopItem =  [[url,classify]]
            firstTime = false
        }   else{
            loopItem = firstTime ? item[3][secondLevelSkip..-1] : item[3]
            firstTime = false
        }

        for (_item in loopItem) {
            _url = _item[0]?.toString().contains("http://") ? _item[0].toString() : weibo_main_url[0..-2] + _item[0]
            _classify = _item[1]
            println "#############"
            println _url
            println _classify
            do_something(_item, url, classify, tag, _url, _classify, weibo_main_url, error_list)
        }

    }
    save_file("mlist.obj", mlist)
}

def save_file(fname = "mlist.obj", mlist) {
    new File(fname).withObjectOutputStream { out ->
        out << mlist

    }

}

def load_from_file(fname = "mlist.obj") {
    def res;
    new File("config").withObjectInputStream { instream ->
        res = instream.readObject()
    }
    return res
}

//用于media的first
def media_first = {  def url ->
    //[url,classify,tag,[ [_ul,_classify],,,]]
    results = []
    def cleaner = new HtmlCleaner()
    def node = cleaner.clean(url.toURL())

// Convert from HTML to XML
    def props = cleaner.getProperties()
    def serializer = new SimpleXmlSerializer(props)
    def xml = serializer.getXmlAsString(node)

// Parse the XML into a document we can work with
    def page = new XmlSlurper(false, false).parseText(xml)

    page.'**'.findAll { it.@class == 'id_nav clearfix'}.eachWithIndex {item, i ->
        item.'**'.findAll {it.@class == 'nav_barMain'}.eachWithIndex {item2, i2 ->
            println i2

            tag = i2 == 0 ? "industry" : "area"
            if (i2 <= 1) {
                item2.'**'.findAll {it.@class == 'nav_aItem'}.eachWithIndex {item3, i3 ->

                    r = item3.'**'.find {it.@class == 'a_link'}
                    classify = r.toString().replaceAll(">", "")
                    url_right = r.@href
                    println classify
                    println url_right
                    rr = [url_right, classify, tag, []]
                    item3.'**'.findAll {it.@class == 'child_link'}.eachWithIndex { item4, i4 ->
                        println item4.@href
                        println item4.toString()

                        rr[3] << [item4.@href, item4.toString()]

                    }
                    results << rr
                }


            }

        }
    }
    return results
}
//########## 用于测试的
//print first( 'http://verified.weibo.com/')
//second("http://verified.weibo.com/fame/yingshi")
//thrid("http://verified.weibo.com/fame/anhui?srt=4&city=1",["area","安徽","合肥"])
//thrid_last("http://verified.weibo.com/fame/anhui?srt=4&city=1&rt=0&letter=h",["area","安徽","合肥"])

//save_file()
//run_main()

//def res = load_from_file("mlist.obj")

// media = "http://media.weibo.com/home/"

//println first("http://verified.weibo.com/brand/")
//println second("http://verified.weibo.com/fame/yingshi")
//println thrid("http://verified.weibo.com/media/jgb/?srt=4",["haha","11","22"])
//media_first("http://verified.weibo.com/media/")
//###########end



/*以下用于抓取*/

//run_main("http://verified.weibo.com/",first,do_something_when_fame   ,_from="fame")  //抓fame的
//run_main("http://verified.weibo.com/brand/",first,do_something_when_brand, _from="brand")          //抓brand的
//run_main("http://verified.weibo.com/website/",first,do_something_when_brand, _from="website")          //抓website的

//run_main("http://verified.weibo.com/agency/",first,do_something_when_brand, _from="agency")       //抓agency的
//run_main("http://verified.weibo.com/campus/",first,do_something_when_brand, _from="campus")      //抓campus的

run_main("http://verified.weibo.com/media/", media_first, do_something_when_brand, _from = "media")         //抓media的


