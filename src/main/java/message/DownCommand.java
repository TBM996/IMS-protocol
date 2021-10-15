package message;

import lombok.extern.slf4j.Slf4j;
import org.jetlinks.core.device.DeviceOperator;
import org.jetlinks.core.enums.ErrorCode;
import org.jetlinks.core.exception.DeviceOperationException;
import org.jetlinks.core.message.Message;
import org.jetlinks.core.message.function.FunctionInvokeMessage;
import reactor.core.publisher.Flux;

@Slf4j
public class DownCommand {

    public static Flux<? extends Message> decode (Message message, DeviceOperator deviceOperator,String token){


        if (message instanceof FunctionInvokeMessage){
            FunctionInvokeMessage invokeMessage = (FunctionInvokeMessage) message;
            log.warn("功能调用:{}",invokeMessage.getFunctionId());
            switch (invokeMessage.getFunctionId()){
                case "add" :{
                    return AddSection.decode(invokeMessage,deviceOperator,token);
                } case "addLCB":{
                    return AddLCB.decode(invokeMessage,deviceOperator,token);
                }
            }
        }
        throw  new DeviceOperationException(ErrorCode.UNSUPPORTED_MESSAGE);
    }
}
