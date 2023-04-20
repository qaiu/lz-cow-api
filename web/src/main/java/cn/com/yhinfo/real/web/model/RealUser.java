package cn.com.yhinfo.real.web.model;

import cn.com.yhinfo.real.common.ToJson;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DataObject
public class RealUser implements ToJson {
    private String username;
    private String password;

    public RealUser(JsonObject json) {
        this.username = json.getString("username");
        this.password = json.getString("password");
    }
}
