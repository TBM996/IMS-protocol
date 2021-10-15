package message;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.jetlinks.core.device.DeviceOperator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class Login {
    public static Mono<ClientResponse> Connection(DeviceOperator deviceOperator){
        String valueMessage = "eyJ4eHg0IjogImUxMGFkYzM5NDliYTU5YWJiZTU2ZTA1N2YyMGY4ODNlIiwgInh4eDEiOiAiR2V0VG9rZW4iLCAieHh4MiI6ICJkZWZhdWx0IiwgInh4eDMiOiAicm9vdCJ9";
        return WebClient
                .builder()
                .baseUrl("https://getman.cn/mock")
                .build()
                .post()
                .uri(uriBuilder -> uriBuilder.path("/backend_mgt/v2/logon").build())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(valueMessage)
                .exchange();
    }

    public static Mono<Boolean> encode(DeviceOperator deviceOperator){
        return Connection(deviceOperator)
                .flatMap(loginResponse -> {
                    //判断连接是否成功
                    log.warn("登录回复状态:" + loginResponse.statusCode());
                    if (!(loginResponse.statusCode() == HttpStatus.OK)){
                        log.error("请求建立失败：错误" + loginResponse.statusCode());
                        return Mono.error(new UnsupportedOperationException("请求建立失败:错误" + loginResponse.statusCode()));
                    }
                    //获得body判断登录是否成功
                    return loginResponse.bodyToMono(String.class)
                            .map(JSON::parseObject)
                            .flatMap(body -> {
                                String token = body.getString("access_token");
                                return deviceOperator.setConfig("token",token);
                            });
                });
    }
}
