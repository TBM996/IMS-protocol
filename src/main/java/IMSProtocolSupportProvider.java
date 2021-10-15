import codec.IMSMessageCodec;
import org.jetlinks.core.ProtocolSupport;
import org.jetlinks.core.defaults.CompositeProtocolSupport;
import org.jetlinks.core.device.DeviceRegistry;
import org.jetlinks.core.message.codec.DefaultTransport;
import org.jetlinks.core.metadata.DefaultConfigMetadata;
import org.jetlinks.core.server.session.DeviceSessionManager;
import org.jetlinks.core.spi.ProtocolSupportProvider;
import org.jetlinks.core.spi.ServiceContext;
import org.jetlinks.supports.official.JetLinksDeviceMetadataCodec;
import org.jetlinks.supports.server.DecodedClientMessageHandler;
import reactor.core.publisher.Mono;

public class IMSProtocolSupportProvider implements ProtocolSupportProvider {
    private static final DefaultConfigMetadata httpConfig = new DefaultConfigMetadata("http认证配置","属性");

    @Override
    public Mono<? extends ProtocolSupport> create(ServiceContext serviceContext) {
        CompositeProtocolSupport support = new CompositeProtocolSupport();
        support.setId("IMS-protocol");
        support.setName("清鹤IMS3.0接口协议");
        support.setDescription("IMS-HTTP-DF");
        support.setMetadataCodec(new JetLinksDeviceMetadataCodec());
        serviceContext.getService(DecodedClientMessageHandler.class)
                .ifPresent(handler->{
                    IMSMessageCodec codec = new IMSMessageCodec(handler);
                    support.setDeviceStateChecker(codec);
                    support.addMessageCodecSupport(codec);
                    support.addMessageSenderInterceptor(codec);
                });
        return Mono.just(support);
    }
}
