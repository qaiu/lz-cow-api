package cn.com.yhinfo.real.web.http;

import cn.com.yhinfo.core.annotaions.RouteHandler;
import cn.com.yhinfo.core.annotaions.RouteMapping;
import cn.com.yhinfo.core.annotaions.SockRouteMapper;
import cn.com.yhinfo.core.enums.RouteMethod;
import cn.com.yhinfo.core.model.JsonResult;
import cn.com.yhinfo.core.util.AsyncServiceUtil;
import cn.com.yhinfo.core.util.SnowflakeIdWorker;
import cn.com.yhinfo.core.util.VertxHolder;
import cn.com.yhinfo.real.web.model.RealUser;
import cn.com.yhinfo.real.web.service.UserService;
import io.vertx.core.Future;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import lombok.extern.slf4j.Slf4j;

/**
 * 连接服务API
 * <br>Create date 2021/4/28 9:15
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
@Slf4j
@RouteHandler("serverApi")
public class ServerApi {

    private final UserService userService = AsyncServiceUtil.getAsyncServiceInstance(UserService.class);

    @RouteMapping(value = "/login", method = RouteMethod.POST)
    public Future<String> login(RealUser user) {
        log.info("<------- login: {}", user.getUsername());
        return userService.login(user);
    }

    long sid = 0;

    @SockRouteMapper(value = "/test")
    public void test02(SockJSSocket sock) {
        String s = sock.writeHandlerID();
        System.out.println("客户端连接 --> " + s);
        sock.handler(sock::write);
        sock.endHandler(v -> System.out.println("客户端断开"));
        String id = sock.writeHandlerID();
        System.out.println("客户端连接 --> " + id);
//        sock.handler(sock::write);
        sock.handler(buffer -> {
            sock.write("服务端开始处理------->");
            final String msg = buffer.toString();
            if ("1".equals(msg)) {
                sid = VertxHolder.getVertxInstance().setPeriodic(1000, v ->
                        sock.write(v + "-->" + SnowflakeIdWorker.idWorker().nextId()));
            } else {
                if (sid != 0) {
                    if (VertxHolder.getVertxInstance().cancelTimer(sid)) {
                        sock.write(sid + " -----> 定时推送取消");
                    }
                } else {

                    sock.write(msg + "----- ok");
                }
            }
        });
        sock.endHandler(v -> {
            System.out.println("客户端断开");
            if (VertxHolder.getVertxInstance().cancelTimer(sid)) {
                sock.write(sid + " -----> 定时推送取消");
            }
        });
    }

    @RouteMapping(value = "/test2", method = RouteMethod.GET)
    public JsonResult<String> test01() {
        return JsonResult.data("ok");
    }

}
