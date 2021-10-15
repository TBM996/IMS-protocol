package codec;

import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import message.DownCommand;
import message.Login;
import org.jetlinks.core.Value;
import org.jetlinks.core.device.DeviceOperator;
import org.jetlinks.core.device.DeviceRegistry;
import org.jetlinks.core.device.DeviceState;
import org.jetlinks.core.device.DeviceStateChecker;
import org.jetlinks.core.enums.ErrorCode;
import org.jetlinks.core.exception.DeviceOperationException;
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.core.message.Message;
import org.jetlinks.core.message.RepayableDeviceMessage;
import org.jetlinks.core.message.codec.*;
import org.jetlinks.core.message.function.FunctionInvokeMessage;
import org.jetlinks.core.message.interceptor.DeviceMessageSenderInterceptor;
import org.jetlinks.core.server.session.DeviceSession;
import org.jetlinks.core.server.session.DeviceSessionManager;
import org.jetlinks.supports.server.DecodedClientMessageHandler;
import org.reactivestreams.Publisher;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Slf4j
public class IMSMessageCodec implements DeviceMessageCodec, DeviceStateChecker, DeviceMessageSenderInterceptor {

    private DecodedClientMessageHandler handler;

    @Override
    public Transport getSupportTransport() {
        return DefaultTransport.HTTP;
    }

    @Override
    public <R extends DeviceMessage> Flux<R> afterSent(DeviceOperator device, DeviceMessage message, Flux<R> reply) {
        return reply.onErrorResume(DeviceOperationException.class,e -> {
            if (e.getCode() == ErrorCode.CLIENT_OFFLINE){
                FunctionInvokeMessage invokeMessage = (FunctionInvokeMessage) message;

                return Login.encode(device)
                        .flatMap(aBoolean -> {
                            if (aBoolean){
                                return device.getConfig("token");
                            }
                            return Mono.error(new UnsupportedOperationException("登录返回false"));
                        })
                        .switchIfEmpty(Mono.fromRunnable(() -> log.error("设备配置中未找到token")))
                        .flatMapMany(valueMono -> {
                            String token = valueMono.get().toString();
                            log.warn("token:{}",token);
                            return DownCommand.decode(invokeMessage,device,token)
                                    .flatMap(message1 ->
                                            handler.handleMessage(device,message1))
                                    .then(Mono.just((R)((RepayableDeviceMessage<?>)invokeMessage)
                                        .newReply()
                                        .code(ErrorCode.FUNCTION_UNDEFINED.name())
                                        .message("功能已下发")
                                        .success()));
                        });
            }
            return Mono.error(e);
        });
    }

    @Override
    public @NotNull Mono<Byte> checkState(@NotNull DeviceOperator device) {

        return Login.encode(device)
                .flatMap(aBoolean -> {
                    if(aBoolean){
                        return device.getConfig("token");
                    }
                    return Mono.error(new UnsupportedOperationException("登录返回false" ));
                })
                .switchIfEmpty(Mono.fromRunnable(() -> log.error("设备配置中未找到token")))
                .flatMap(valueMono ->  {
                    String token = valueMono.asString();
                    if(token != null || !token.equals("")){
                        return Mono.just((byte)1);
                    }else {
                        return Mono.just((byte)-1);
                    }
                });
    }



    @Nonnull
    @Override
    public Publisher<? extends Message> decode(@Nonnull MessageDecodeContext messageDecodeContext) {
        return null;
    }

    @Nonnull
    @Override
    public Publisher<? extends EncodedMessage> encode(@Nonnull MessageEncodeContext messageEncodeContext) {
        return null;
    }
}
