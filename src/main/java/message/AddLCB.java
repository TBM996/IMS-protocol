package message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.jetlinks.core.device.DeviceOperator;
import org.jetlinks.core.message.event.EventMessage;
import org.jetlinks.core.message.function.FunctionInvokeMessage;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class AddLCB {
    private static Mono<JSONObject> Connection(FunctionInvokeMessage message,String token){
        MultiValueMap<String,Object> map = new LinkedMultiValueMap<String,Object>();
        MultiValueMap<String, String> data = new LinkedMultiValueMap<String,String>();
        data.add("Left","0");
        data.add("Width","1103");
        data.add("Top","0");
        data.add("Height","1077");
        data.add("layout_id","347");
        data.add("layout_controlbox","-1");
        data.add("Type","VideoBox");
        data.add("Zorder","0");

        map.add("project_name","Default");
        map.add("action","addLCB");
        map.add("data",data);
        map.add("user","root");
        map.add("token","79ad7e3c-1d52-11e9-bffa-a6368786f5e04fd4c2a8-00e3-417b-8de0-130b6d949c61");
        return WebClient.builder()
                .baseUrl("https://getman.cn/mock")
                .build()
                .post()
                .uri(uriBuilder -> uriBuilder.path("/backend_mgt/v1/layout").build())
                .bodyValue(map)
                .retrieve()
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
                        message1.setEvent("ADDLCB");
                        message1.setData(obj);
                        message1.setDeviceId(device.getDeviceId());
                        list.add(message1);
                    }
                    return Flux.fromIterable(list);
                });
    }
}
