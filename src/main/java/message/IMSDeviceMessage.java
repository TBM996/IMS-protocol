package message;

import lombok.Getter;
import lombok.Setter;
import org.jetlinks.core.device.DeviceOperator;
import org.jetlinks.core.message.DeviceMessage;
import org.reactivestreams.Publisher;

@Setter
@Getter
public abstract class IMSDeviceMessage {
    private String project_name;
    private String action;
    private String data;
    private String user;
    private String token;

    public abstract Publisher<? extends DeviceMessage> toDeviceMessage(DeviceOperator operator);
}
