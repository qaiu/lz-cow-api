package cn.qaiu.lz.common.parser.impl;

import cn.qaiu.lz.common.parser.IPanTool;
import cn.qaiu.lz.common.parser.PanBase;
import cn.qaiu.lz.common.util.JsExecUtils;
import cn.qaiu.vx.core.util.VertxHolder;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.ScriptException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 蓝奏云解析工具
 *
 * @author QAIU
 * @version 1.0 update 2021/5/16 10:39
 */
public class LzTool extends PanBase implements IPanTool {

    public static final String SHARE_URL_PREFIX = "https://wwwa.lanzoui.com";

    public static final String LINK_KEY = "lanzou";

    public LzTool(String key, String pwd) {
        super(key, pwd);
    }

    /*
		var wsk_sign = 'c20230818';
		var aihidcms = 'Fn5L';
		var iucjdsd = '';
		var ws_sign = 'c20230818';
		var sasign = 'B2EBPw8_bBDVRWFdoAzMGOlI5ATwEbVFgV2NVZwVhATYILld0WjoAZQhoVDsEYlZjBm4BNgVoVGcHMQ_c_c';
		var ajaxdata = '?ctdf';
		$.ajax({
			type : 'post',
			url : '/ajaxm.php',
			//b data : { 'action':'downprocess','signs':ajaxdata,'sign':'w},
			data : { 'action':'downprocess','signs':ajaxdata,'sign':sasign,'websign':iucjdsd,'websignkey':aihidcms,'ves':1 },
			//b data : { 'action':'downprocess','signs':ajaxdata,'sign':'','websign':ws_sign,'websignkey':wsk_sign,'ves':1 },
			dataType : 'json',
			success:function(msg){
				var date = msg;
				if(date.zt == '1'){
										$("#tourl").html("<a href="+date.dom+"/file/"+ date.url +" target=_blank rel=noreferrer><span class=txt>电信下载</span><span class='txt txtc'>联通下载</span><span class=txt>普通下载</span></a>");
					setTimeout('$("#outime").css("display","block");',1800000);
									}else{
					$("#tourl").html("网页超时，请刷新");
				};

			},
			error:function(){
				$("#tourl").html("获取失败，请刷新");
			}

		});
     */

    /**
     * 解析器
     * @return url String
     */
    @SuppressWarnings("unchecked")
    public Future<String> parse() {
        String sUrl = key.startsWith("https://") ? key : SHARE_URL_PREFIX + "/" + key;

        WebClient client = WebClient.create(VertxHolder.getVertxInstance(),
                new WebClientOptions().setFollowRedirects(false));
        client.getAbs(sUrl).send().onSuccess(res -> {
            String html = res.bodyAsString();
            // 匹配iframe
            Pattern compile = Pattern.compile("src=\"(/fn\\?[a-zA-Z\\d_+/=]{16,})\"");
            Matcher matcher = compile.matcher(html);
            if (!matcher.find()) {
                // 没有Iframe说明是加密分享, 匹配sign通过密码请求下载页面
                // 去TMD正则
                String jsText = getJsText(html);
                try {
                    ScriptObjectMirror scriptMirror = JsExecUtils.executeDynamicJs(jsText, "down_p");
                    System.out.println(scriptMirror);

                } catch (ScriptException | NoSuchMethodException e) {
                    fail(e, "js引擎执行失败");
                    return;
                }

//                getDownURL(promise, sUrl, client, sign);
                return;
            }
            String iframePath = matcher.group(1);
            client.getAbs(SHARE_URL_PREFIX + iframePath).send().onSuccess(res2 -> {
                String html2 = res2.bodyAsString();

                // 去TMD正则
                // Matcher matcher2 = Pattern.compile("'sign'\s*:\s*'(\\w+)'").matcher(html2);
                String jsText = getJsText(html2);
                if (jsText == null) {
                    fail(SHARE_URL_PREFIX + iframePath + " -> " + sUrl + ": sign正则匹配失败, 可能分享已失效");
                    return;
                }
                try {
                    ScriptObjectMirror scriptObjectMirror = JsExecUtils.executeDynamicJs(jsText, null);
                    getDownURL(promise, sUrl, client, (Map<String, String>) scriptObjectMirror.get("data"));
                } catch (ScriptException | NoSuchMethodException e) {
                    fail(e, "js引擎执行失败");
                }
            }).onFailure(handleFail(SHARE_URL_PREFIX));
        }).onFailure(handleFail(sUrl));
        return promise.future();
    }

    private String getJsText(String html) {
        String jsTagStart = "<script type=\"text/javascript\">";
        String jsTagEnd = "</script>";
        int index = html.indexOf(jsTagStart);
        if (index == -1) {
            return null;
        }
        int startPos = index + jsTagStart.length();
        int endPos = html.indexOf(jsTagEnd, startPos);
        return html.substring(startPos, endPos);
    }

    private void getDownURL(Promise<String> promise, String key, WebClient client, Map<String, ?> signMap) {
        MultiMap map = MultiMap.caseInsensitiveMultiMap();
        signMap.forEach((k,v)->{
            map.set(k,v.toString());
        });
        MultiMap headers = MultiMap.caseInsensitiveMultiMap();
        var userAgent2 = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, " +
                "like " +
                "Gecko) Chrome/111.0.0.0 Mobile Safari/537.36";
        headers.set("User-Agent", userAgent2);
        headers.set("referer", key);
        headers.set("sec-ch-ua-platform", "Android");
        headers.set("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");
        headers.set("sec-ch-ua-mobile", "sec-ch-ua-mobile");

        String url = SHARE_URL_PREFIX + "/ajaxm.php";
        client.postAbs(url).putHeaders(headers).sendForm(MultiMap
                .caseInsensitiveMultiMap()
                .setAll(map)).onSuccess(res2 -> {
            JsonObject urlJson = res2.bodyAsJsonObject();
            if (urlJson.getInteger("zt") != 1) {
                fail(urlJson.getString("inf"));
                return;
            }
            String downUrl = urlJson.getString("dom") + "/file/" + urlJson.getString("url");
            client.getAbs(downUrl).putHeaders(headers).send()
                    .onSuccess(res3 -> promise.complete(res3.headers().get("Location")))
                    .onFailure(handleFail(downUrl));
        }).onFailure(handleFail(url));
    }
}
