import sun.nio.cs.UnicodeEncoder
import Tools

/**
 * Created with IntelliJ IDEA.
 * User: shang
 * Date: 12-7-3
 * Time: 下午10:24
 * To change this template use File | Settings | File Templates.
 */

def  weibo_main_url = 'http://verified.weibo.com/'

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
        resutls <<[url_right,classify]
    }
    return resutls
}
/*
*第一级页面
* first( 'http://verified.weibo.com/')
*/

def first(def url) {
    results = []
    url.toURL().eachLine {line ->
        def pattern1 = ~"nav_barMain.*?(?=nav_barMain)"
        def matcher1 = line =~ pattern1
        i = 0
        while (matcher1.find()) {
            content = matcher1.group()
            def pattern2 = ~/a_inner.*?(?=nav_aItem)|a_inner.*/
            def matcher2 = content =~ pattern2

            while (matcher2.find()) {
                content2 = matcher2.group()
                //println content2
                def url_right = content2.substring(content2.indexOf("a_inner\\\"> <a href=\\\"") + "a_inner\\\"> <a href=\\\"".length(), content2.indexOf("\" class=\\\"a_link\\\""))
                def classify = content2.substring(content2.indexOf("class=\\\"a_link\\\">") + "class=\\\"a_link\\\">".length(), content2.indexOf("<em class=\\\"nav_arr\\"))
                url_right = url_right.replaceAll('\\\\', '')

                classify = Tools.unicodeToString(classify.toString())
                //println url_right
                //println classify
                tag = i==0?"industry":"area"
                res = first_last(content2)
                results<<[url_right, classify , tag,res]
                println "==="

            }
            i++

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
        return [url_right, classify ]
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
        if( matcher1.size() == 1){
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
            item =  getUrl_rightAndClassify(content, p1, p2, p3, p4)
            if (item != null)
            results << item
        }

    }
    return results
}

/*第三级，拼接url*/

def thrid(def base_url,def clist) {

    _url = base_url + "&rt=0"
    println _url
    ("a".."z").each {num ->
        url = _url + "&letter=" + num
        thrid_last(url, clist)
    }
}

def thrid_get_persons(def content, def clist){
    def pattern1 = ~/select_user.*?(?=name W_linkc)/
    def matcher1 = content =~ pattern1

    while (matcher1.find()) {
        _content = matcher1.group()
//        println _content
        uid =  _content.substring(_content.indexOf("uid=")+"uid=".length(), _content.indexOf("\\\"",_content.indexOf("uid=")+"uid=".length()))
        title = _content.substring(_content.indexOf("title=\\\"")+"title=\\\"".length(), _content.indexOf("\\\"",_content.indexOf("title=\\\"")+"title=\\\"".length()))
        weibo_url = _content.substring(_content.indexOf("a target=\\\"_blank\\\" href=\\\"")+"a target=\\\"_blank\\\" href=\\\"".length(), _content.indexOf("\\\"",_content.indexOf("a target=\\\"_blank\\\" href=\\\"")+"a target=\\\"_blank\\\" href=\\\"".length()))
        weibo_url = weibo_url.replaceAll('\\\\', '').replaceAll("&amp;", "&")

        weibo_avatar =  _content.substring(_content.indexOf("class_card\\\" src=\\\"")+"class_card\\\" src=\\\"".length(), _content.indexOf("\\\"",_content.indexOf("class_card\\\" src=\\\"")+"class_card\\\" src=\\\"".length()))
        weibo_avatar = weibo_avatar.replaceAll('\\\\', '').replaceAll("&amp;", "&")

        title = Tools.unicodeToString(title)
        println   "$uid $title $weibo_url $weibo_avatar"
        println clist
    }

}

/*处理最后页面，抓取人*/
def thrid_last(def url, def clist ,def page_num = 1) {
    println "handle$url"
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
                thrid_last(to_url,clist, nextpagenum)
            }
        }

    }
}

def run_main(){
    error_list =[]
    def weibo_main_url = 'http://verified.weibo.com/'
    mlist = first( weibo_main_url)
    //println mlist
    for (item in mlist){
        url =  weibo_main_url+item[0][1..-1]
        classify =  item[1]
        tag = item[2]
        for (_item in item[3]){
            _url = weibo_main_url[0..-2]+_item[0]
            _classify = _item[1]
            println "#############"
            println _url
            println _classify
            try{
                _res = second(_url)
                if(_res ==null ){
                    println _url
                    println "$tag $classify $_classify"
                    //thrid(_url)
                }else{
                    _item<<_res
                    println "$tag $classify $_classify"
                    for (r in _res){
                        println"@@"
                        uurl = weibo_main_url[0..-2]+r[0]
                        println r[1]
                        thrid(uurl,[tag,classify,_classify,r[1]])


                    }
                }
            }
            catch (ex) {
                ex.printStackTrace()
                error_list<<[_url,_classify]




            }



        }

    }


    save_file("mlist.obj",mlist)
}

//print first( 'http://verified.weibo.com/')
//second("http://verified.weibo.com/fame/yingshi")
//thrid("/fame/peiyinyanyuan/?srt=4")
//thrid_last("http://verified.weibo.com/fame/yanyuan/?rt=0&srt=4&letter=l")


def save_file(fname="mlist.obj", mlist){
    new File(fname).write(mlist.toString())

}
def load_from_file(fname="mlist.obj") {
      return new File(fname).readLines()

}






//save_file()
//run_main()

//def res = load_from_file("mlist.obj")


run_main()