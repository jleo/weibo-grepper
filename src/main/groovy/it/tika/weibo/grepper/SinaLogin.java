package it.tika.weibo.grepper;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
public class SinaLogin {

	public static DefaultHttpClient client;
 
	/**
	 * 抓取网页
	 * 
	 * @param url
	 * @throws IOException
	 */
	static String get(String url) throws IOException {
		HttpGet get = new HttpGet(url);
		HttpResponse response = client.execute(get);
		System.out.println(response.getStatusLine());
		HttpEntity entity = response.getEntity();
 
		String result = dump(entity);
		get.abort();
 
		return result;
	}
 
	/**
	 * 执行登录过程
	 * 
	 * @param user
	 * @param pwd
	 * @throws IOException
	 */
	static void login(String user, String pwd) throws IOException {

        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
        HttpProtocolParams.setUseExpectContinue(params, true);

        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

        ClientConnectionManager connman = new ThreadSafeClientConnManager(params, registry);
        client = new DefaultHttpClient(connman, params);

        System.out.println("Login user: "+user);
		HttpPost post = new HttpPost(
				"http://login.sina.com.cn/sso/login.php?client=ssologin.js(v1.3.22)");
		post.setHeader("User-Agent",
				"Mozilla/5.0 (X11; Linux i686; rv:5.0) Gecko/20100101 Firefox/5.0");
		post.setHeader("Referer", "http://weibo.com/");
		post.setHeader("Content-Type", "application/x-www-form-urlencoded");
 
		// 登录表单的信息
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair("entry", "weibo"));
		qparams.add(new BasicNameValuePair("gateway", "1"));
		qparams.add(new BasicNameValuePair("from", ""));
		qparams.add(new BasicNameValuePair("savestate", "7"));
        String nonce = makeNonce(6);
        String servetime = getServerTime();
        qparams.add(new BasicNameValuePair("nonce", nonce));
		qparams.add(new BasicNameValuePair("useticket", "1"));
		qparams.add(new BasicNameValuePair("ssosimplelogin", "1"));
		qparams.add(new BasicNameValuePair("pwencode", "wsse"));
		qparams.add(new BasicNameValuePair("servertime", servetime));
		qparams.add(new BasicNameValuePair("su", encodeAccount(user)));//"Z2d5eWxlbyU0MGdtYWlsLmNvbQ=="));
        SinaSSOEncoder sinaSSOEncoder = new SinaSSOEncoder();
        qparams.add(new BasicNameValuePair("sp", sinaSSOEncoder.encode(pwd, servetime,nonce)));//"0f5b32d9745cd5962afe2ee791d6743853d07413"));
		qparams.add(new BasicNameValuePair("service", "miniblog"));
		// servertime=1309164392
		// nonce=PJZCHM
		// qparams.add(new BasicNameValuePair("pwencode", "wsse"));
		qparams.add(new BasicNameValuePair("encoding", "utf-8"));
		qparams.add(new BasicNameValuePair(
				"url",
				"http://weibo.com/ajaxlogin.php?framelogin=1&callback=parent.sinaSSOController.feedBackUrlCallBack"));
		qparams.add(new BasicNameValuePair("returntype", "META"));
 

		UrlEncodedFormEntity params2 = new UrlEncodedFormEntity(qparams, "UTF-8");
		post.setEntity(params2);
 
		// Execute the request
		HttpResponse response = client.execute(post);
		post.abort();
		// 新浪微博登录没有301，302之类的跳转；而是返回200，然后用javascript实现的跳转
		// int statusCode = response.getStatusLine().getStatusCode();
		// if ((statusCode == HttpStatus.SC_MOVED_PERMANENTLY)
		// || (statusCode == HttpStatus.SC_MOVED_TEMPORARILY)
		// || (statusCode == HttpStatus.SC_SEE_OTHER)
		// || (statusCode == HttpStatus.SC_TEMPORARY_REDIRECT)) {
		// // 此处重定向处理 此处还未验证
		// String newUri = response.getLastHeader("Location").getValue();
		// get(newUri);
		// }
 
		// Get hold of the response entity
		HttpEntity entity = response.getEntity();
		// 取出跳转的url
		// location.replace("http://weibo.com/ajaxlogin.php?framelogin=1&callback=parent.sinaSSOController.feedBackUrlCallBack&ticket=ST-MTkxODMxOTI0Nw==-1309224549-xd-263902F174B27BAB9699691BA866EFF2&retcode=0");
		String location = getRedirectLocation(dump(entity));
		get(location);
	}
 
	private static String getRedirectLocation(String content) {
        System.out.println(content);
		String regex = "location\\.replace\\(\'(.*?)\'\\)";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);
 
		String location = null;
		if (matcher.find()) {
			location = matcher.group(1);
		}
 
		return location;
	}
 
	/**
	 * 打印页面
	 * 
	 * @param entity
	 * @throws IOException
	 */
	private static String dump(HttpEntity entity) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				entity.getContent(), "utf8"));
 
		//return EntityUtils.toString(entity);
		return IOUtils.toString(br);
	}
 
	public static void main(String[] args) throws IOException {
		login("ggyyleo@gmail.com", "3jf2hf1l");
//		String result = get("http://t.sina.com.cn/pub/tags");
//		System.out.println(result);
	}

    private static String encodeAccount(String account) {
        return Base64.encode(URLEncoder.encode(account).getBytes());
    }

    private static String makeNonce(int len) {
        String x = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String str = "";
        for (int i = 0; i < len; i++) {
            str += x.charAt((int) (Math.ceil(Math.random() * 1000000) % x
                    .length()));
        }
        return str;
    }

    private static String getServerTime() {
        long servertime = new Date().getTime() / 1000;
        return String.valueOf(servertime);
    }

    public static class SinaSSOEncoder {
        private boolean i = false;
        private int g = 8;

        public SinaSSOEncoder() {

        }

        public String encode(String psw, String servertime, String nonce) {
            String password;
            password = hex_sha1("" + hex_sha1(hex_sha1(psw)) + servertime + nonce);
            return password;
        }

        private String hex_sha1(String j) {
            return h(b(f(j, j.length() * g), j.length() * g));
        }

        private String h(int[] l) {
            String k = i ? "0123456789ABCDEF" : "0123456789abcdef";
            String m = "";
            for (int j = 0; j < l.length * 4; j++) {
                m += k.charAt((l[j >> 2] >> ((3 - j % 4) * 8 + 4)) & 15) + ""
                        + k.charAt((l[j >> 2] >> ((3 - j % 4) * 8)) & 15);
            }
            return m;
        }

        private int[] b(int[] A, int r) {
            A[r >> 5] |= 128 << (24 - r % 32);
            A[((r + 64 >> 9) << 4) + 15] = r;
            int[] B = new int[80];
            int z = 1732584193;
            int y = -271733879;
            int v = -1732584194;
            int u = 271733878;
            int s = -1009589776;
            for (int o = 0; o < A.length; o += 16) {
                int q = z;
                int p = y;
                int n = v;
                int m = u;
                int k = s;
                for (int l = 0; l < 80; l++) {
                    if (l < 16) {
                        B[l] = A[o + l];
                    } else {
                        B[l] = d(B[l - 3] ^ B[l - 8] ^ B[l - 14] ^ B[l - 16], 1);
                    }
                    int C = e(e(d(z, 5), a(l, y, v, u)), e(e(s, B[l]), c(l)));
                    s = u;
                    u = v;
                    v = d(y, 30);
                    y = z;
                    z = C;
                }
                z = e(z, q);
                y = e(y, p);
                v = e(v, n);
                u = e(u, m);
                s = e(s, k);
            }
            return new int[] { z, y, v, u, s };
        }

        private int a(int k, int j, int m, int l) {
            if (k < 20) {
                return (j & m) | ((~j) & l);
            }
            ;
            if (k < 40) {
                return j ^ m ^ l;
            }
            ;
            if (k < 60) {
                return (j & m) | (j & l) | (m & l);
            }
            ;
            return j ^ m ^ l;
        }

        private int c(int j) {
            return (j < 20) ? 1518500249 : (j < 40) ? 1859775393
                    : (j < 60) ? -1894007588 : -899497514;
        }

        private int e(int j, int m) {
            int l = (j & 65535) + (m & 65535);
            int k = (j >> 16) + (m >> 16) + (l >> 16);
            return (k << 16) | (l & 65535);
        }

        private int d(int j, int k) {
            return (j << k) | (j >>> (32 - k));
        }

        private int[] f(String m, int r) {
            int[] l;
            int j = (1 << this.g) - 1;
            int len = ((r + 64 >> 9) << 4) + 15;
            int k;
            for (k = 0; k < m.length() * g; k += g) {
                len = k >> 5 > len ? k >> 5 : len;
            }
            l = new int[len + 1];
            for (k = 0; k < l.length; k++) {
                l[k] = 0;
            }
            for (k = 0; k < m.length() * g; k += g) {
                l[k >> 5] |= (m.charAt(k / g) & j) << (24 - k % 32);
            }
            return l;
        }

    }

}