package message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.jetlinks.core.device.DeviceOperator;
import org.jetlinks.core.message.event.EventMessage;
import org.jetlinks.core.message.function.FunctionInvokeMessage;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AddSection {

    private static Mono<JSONObject> Connection(FunctionInvokeMessage message,String token){
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String,Object>();
        MultiValueMap<String, String> data = new LinkedMultiValueMap<String,String>();
        data.add("layout_id","-1");
        data.add("Name","hhd");
        data.add("Name_eng","new layout");
        data.add("Width","1920");
        data.add("Height","1080");
        data.add("BackgroundPic","3140");
        data.add("BackgroundColor","#000000");
        data.add("TopMargin","0");
        data.add("RightMargin","0");
        data.add("LeftMargin","0");
        data.add("BottomMargin","0");

        map.add("project_name","Default");
        map.add("action","add");
        map.add("data", data);
        map.add("user","root");
        map.add("token","79ad7e3c-1d52-11e9-bffa-a6368786f5e04fd4c2a8-00e3-41" +
                "7b-8de0-130b6d949c61");
        return WebClient
                .builder()
                .baseUrl("http://192.168.30.110")
                .build()
                .post()
                .uri(uriBuilder -> uriBuilder.path("/backend_mgt/v1/layout")
                                    .build())
                .bodyValue(map)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError,resp -> {
                    log.error("AddSection请求失败error:{},msg:{}",resp.statusCode().value(),resp.statusCode().getReasonPhrase());
                    return Mono.error(new RuntimeException(resp.statusCode().value() + " : " + resp.statusCode().getReasonPhrase()));
                })
                .bodyToMono(String.class)
                .map(JSON::parseObject);
    }
    public static Flux<EventMessage> decode(FunctionInvokeMessage message, DeviceOperator device,String token){
        return Connection(message,token)
                .flatMapMany(jsonObject -> {
                    int ID = jsonObject.getInteger("ID");
                    String rescode = jsonObject.getString("rescode");

                    JSONArray data = new JSONArray();
                    data.add(ID);
                    data.add(rescode);
                    List<EventMessage> list = new ArrayList<>();

                    for (Object obj : data) {
                        EventMessage message1 = new EventMessage();
                        message1.setEvent("AddSection");
                        message1.setData(obj);
                        message1.setDeviceId(device.getDeviceId());
                        list.add(message1);
                    }
                    return Flux.fromIterable(list);

                });
    }
}
