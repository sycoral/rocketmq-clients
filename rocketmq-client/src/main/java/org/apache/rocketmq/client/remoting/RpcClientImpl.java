package org.apache.rocketmq.client.remoting;

import apache.rocketmq.v1.AckMessageRequest;
import apache.rocketmq.v1.AckMessageResponse;
import apache.rocketmq.v1.EndTransactionRequest;
import apache.rocketmq.v1.EndTransactionResponse;
import apache.rocketmq.v1.HealthCheckRequest;
import apache.rocketmq.v1.HealthCheckResponse;
import apache.rocketmq.v1.HeartbeatRequest;
import apache.rocketmq.v1.HeartbeatResponse;
import apache.rocketmq.v1.MessagingServiceGrpc;
import apache.rocketmq.v1.MultiplexingRequest;
import apache.rocketmq.v1.MultiplexingResponse;
import apache.rocketmq.v1.NackMessageRequest;
import apache.rocketmq.v1.NackMessageResponse;
import apache.rocketmq.v1.PullMessageRequest;
import apache.rocketmq.v1.PullMessageResponse;
import apache.rocketmq.v1.QueryAssignmentRequest;
import apache.rocketmq.v1.QueryAssignmentResponse;
import apache.rocketmq.v1.QueryOffsetRequest;
import apache.rocketmq.v1.QueryOffsetResponse;
import apache.rocketmq.v1.QueryRouteRequest;
import apache.rocketmq.v1.QueryRouteResponse;
import apache.rocketmq.v1.ReceiveMessageRequest;
import apache.rocketmq.v1.ReceiveMessageResponse;
import apache.rocketmq.v1.SendMessageRequest;
import apache.rocketmq.v1.SendMessageResponse;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.grpc.stub.MetadataUtils;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLException;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * A typical implementation for {@link RpcClient}
 */
@Slf4j
@EqualsAndHashCode
public class RpcClientImpl implements RpcClient {

    private final ManagedChannel channel;
    private final MessagingServiceGrpc.MessagingServiceFutureStub stub;

    private long lastCallNanoTime;

    public RpcClientImpl(Endpoints endpoints) throws SSLException {
        final SslContextBuilder builder = GrpcSslContexts.forClient();
        // TODO: discard the insecure way.
        builder.trustManager(InsecureTrustManagerFactory.INSTANCE);
        SslContext sslContext = builder.build();

        final NettyChannelBuilder channelBuilder =
                NettyChannelBuilder.forTarget(endpoints.getFacade())
                                   .intercept(new ClientTraceInterceptor())
                                   .sslContext(sslContext);
        // Disable grpc's auto-retry here.
        channelBuilder.disableRetry();

        final List<InetSocketAddress> socketAddresses = endpoints.convertToSocketAddresses();
        if (null != socketAddresses) {
            final IpNameResolverFactory ipNameResolverFactory = new IpNameResolverFactory(socketAddresses);
            channelBuilder.nameResolverFactory(ipNameResolverFactory);
        }

        this.channel = channelBuilder.build();
        this.stub = MessagingServiceGrpc.newFutureStub(channel);
        this.lastCallNanoTime = System.nanoTime();
    }

    @Override
    public long idleSeconds() {
        return TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - lastCallNanoTime);
    }

    @Override
    public void shutdown() {
        if (null != channel) {
            channel.shutdown();
        }
    }

    @Override
    public ListenableFuture<SendMessageResponse> sendMessage(Metadata metadata, SendMessageRequest request,
                                                             Executor executor, long duration, TimeUnit timeUnit) {
        this.lastCallNanoTime = System.nanoTime();
        return MetadataUtils.attachHeaders(stub, metadata).withExecutor(executor)
                            .withDeadlineAfter(duration, timeUnit).sendMessage(request);
    }

    @Override
    public ListenableFuture<QueryAssignmentResponse> queryAssignment(Metadata metadata, QueryAssignmentRequest request,
                                                                     Executor executor, long duration,
                                                                     TimeUnit timeUnit) {
        this.lastCallNanoTime = System.nanoTime();
        return MetadataUtils.attachHeaders(stub, metadata).withExecutor(executor)
                            .withDeadlineAfter(duration, timeUnit).queryAssignment(request);
    }

    @Override
    public ListenableFuture<HealthCheckResponse> healthCheck(Metadata metadata, HealthCheckRequest request,
                                                             Executor executor, long duration, TimeUnit timeUnit) {
        this.lastCallNanoTime = System.nanoTime();
        return MetadataUtils.attachHeaders(stub, metadata).withExecutor(executor)
                            .withDeadlineAfter(duration, timeUnit).healthCheck(request);
    }

    @Override
    public ListenableFuture<ReceiveMessageResponse> receiveMessage(Metadata metadata, ReceiveMessageRequest request,
                                                                   Executor executor, long duration,
                                                                   TimeUnit timeUnit) {
        this.lastCallNanoTime = System.nanoTime();
        return MetadataUtils.attachHeaders(stub, metadata).withExecutor(executor)
                            .withDeadlineAfter(duration, timeUnit).receiveMessage(request);
    }

    @Override
    public ListenableFuture<AckMessageResponse> ackMessage(Metadata metadata, AckMessageRequest request,
                                                           Executor executor, long duration, TimeUnit timeUnit) {
        this.lastCallNanoTime = System.nanoTime();
        return MetadataUtils.attachHeaders(stub, metadata).withExecutor(executor)
                            .withDeadlineAfter(duration, timeUnit).ackMessage(request);
    }

    @Override
    public ListenableFuture<NackMessageResponse> nackMessage(Metadata metadata, NackMessageRequest request,
                                                             Executor executor, long duration, TimeUnit timeUnit) {
        this.lastCallNanoTime = System.nanoTime();
        return MetadataUtils.attachHeaders(stub, metadata).withExecutor(executor)
                            .withDeadlineAfter(duration, timeUnit).nackMessage(request);
    }

    @Override
    public ListenableFuture<HeartbeatResponse> heartbeat(Metadata metadata, HeartbeatRequest request, Executor executor,
                                                         long duration, TimeUnit timeUnit) {
        this.lastCallNanoTime = System.nanoTime();
        return MetadataUtils.attachHeaders(stub, metadata).withExecutor(executor)
                            .withDeadlineAfter(duration, timeUnit).heartbeat(request);
    }

    @Override
    public ListenableFuture<QueryRouteResponse> queryRoute(Metadata metadata, QueryRouteRequest request,
                                                           Executor executor, long duration, TimeUnit timeUnit) {
        this.lastCallNanoTime = System.nanoTime();
        return MetadataUtils.attachHeaders(stub, metadata).withExecutor(executor)
                            .withDeadlineAfter(duration, timeUnit).queryRoute(request);
    }

    @Override
    public ListenableFuture<EndTransactionResponse> endTransaction(Metadata metadata, EndTransactionRequest request,
                                                                   Executor executor, long duration,
                                                                   TimeUnit timeUnit) {
        this.lastCallNanoTime = System.nanoTime();
        return MetadataUtils.attachHeaders(stub, metadata).withExecutor(executor)
                            .withDeadlineAfter(duration, timeUnit).endTransaction(request);
    }

    @Override
    public ListenableFuture<QueryOffsetResponse> queryOffset(Metadata metadata, QueryOffsetRequest request,
                                                             Executor executor, long duration, TimeUnit timeUnit) {
        this.lastCallNanoTime = System.nanoTime();
        return MetadataUtils.attachHeaders(stub, metadata).withExecutor(executor)
                            .withDeadlineAfter(duration, timeUnit).queryOffset(request);
    }

    @Override
    public ListenableFuture<PullMessageResponse> pullMessage(Metadata metadata, PullMessageRequest request,
                                                             Executor executor, long duration, TimeUnit timeUnit) {
        this.lastCallNanoTime = System.nanoTime();
        return MetadataUtils.attachHeaders(stub, metadata).withExecutor(executor)
                            .withDeadlineAfter(duration, timeUnit).pullMessage(request);
    }

    @Override
    public ListenableFuture<MultiplexingResponse> multiplexingCall(Metadata metadata, MultiplexingRequest request,
                                                                   Executor executor, long duration,
                                                                   TimeUnit timeUnit) {
        this.lastCallNanoTime = System.nanoTime();
        return MetadataUtils.attachHeaders(stub, metadata).withExecutor(executor)
                            .withDeadlineAfter(duration, timeUnit).multiplexingCall(request);
    }
}
