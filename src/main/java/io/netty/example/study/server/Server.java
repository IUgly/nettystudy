package io.netty.example.study.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.example.study.server.codec.OrderFrameDecoder;
import io.netty.example.study.server.codec.OrderFrameEncoder;
import io.netty.example.study.server.codec.OrderProtocolDecoder;
import io.netty.example.study.server.codec.OrderProtocolEncoder;
import io.netty.example.study.server.handler.MetricHandler;
import io.netty.example.study.server.handler.OrderServerProcessHandler;
import io.netty.example.study.server.handler.ServerIdleCheckHandler;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.handler.ipfilter.IpFilterRuleType;
import io.netty.handler.ipfilter.IpSubnetFilterRule;
import io.netty.handler.ipfilter.RuleBasedIpFilter;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.UnorderedThreadPoolEventExecutor;
import oracle.jvm.hotspot.jfr.GlobalTraceBuffer;

import java.util.concurrent.ExecutionException;

/**
 * @author kuangjunlin
 *
 *
 *
 * serverBootstrap.option(NioChannelOption.SO_BACKLOG, 1024);
 * Netty在Linux下值的获取 （io.netty.util.NetUtil）:
 *  · 先尝试: /proc/sys/net/core/somaxcon
 *  · 再尝试: sysctl
 *  · 若最终没获取到, 用默认: 128
 *  使用方式:
 *  javaChannel().bind(localAddress, config.getBacklog());
 *
 *  开启native
 *  Nio替换为Nio
 */

public class Server {

    public static void main(String[] args) throws InterruptedException, ExecutionException {

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.channel(NioServerSocketChannel.class);

        serverBootstrap.handler(new LoggingHandler(LogLevel.INFO));

        //完善线程名称
        NioEventLoopGroup boss = new NioEventLoopGroup
                (0, new DefaultThreadFactory("boss"));
        NioEventLoopGroup worker = new NioEventLoopGroup
                (0, new DefaultThreadFactory("worker"));

        serverBootstrap.group(boss, worker);

        //设置是否启用Nagle算法：讲较小的碎片数据连接成更大的报文，提高发送效率
        //默认为false
        serverBootstrap.childOption(NioChannelOption.TCP_NODELAY, true);

        //设置最大的等待连接数量
        serverBootstrap.option(NioChannelOption.SO_BACKLOG, 1024);

        MetricHandler metricHandler = new MetricHandler();

        //声明一个大小为10的线程池
        UnorderedThreadPoolEventExecutor business = new UnorderedThreadPoolEventExecutor(
                10, new DefaultThreadFactory("business"));
        //使用NioNioEventLoopGroup处理时是单线程
//        NioEventLoopGroup business = new NioEventLoopGroup(0
//                , new DefaultThreadFactory("business"));

        GlobalTrafficShapingHandler globalTrafficShapingHandler = new GlobalTrafficShapingHandler(new NioEventLoopGroup(),
                100 * 1024 * 1024, 100 * 1024 * 1024);

        //设置黑名单
        //Todo
        IpSubnetFilterRule ipSubnetFilterRule
                = new IpSubnetFilterRule(
                "127.0.0.1", 8, IpFilterRuleType.REJECT);
        RuleBasedIpFilter ruleBasedIpFilter = new RuleBasedIpFilter(ipSubnetFilterRule);

        serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                //debug时打印原始数据
                pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));

                pipeline.addLast("ipfilter", ruleBasedIpFilter);

                //流量整型控制
                pipeline.addLast("TSHandler", globalTrafficShapingHandler);

                //空闲关闭
                pipeline.addLast("idleCheck", new ServerIdleCheckHandler());

                //"frameDecoder"  : 完善handler名称
                pipeline.addLast("frameDecoder",new OrderFrameDecoder());
                pipeline.addLast(new OrderFrameEncoder());
                pipeline.addLast(new OrderProtocolEncoder());
                pipeline.addLast(new OrderProtocolDecoder());

                //可视化数据
                pipeline.addLast("metricsHandler", metricHandler);

                //通过导入Log4j包，并配置日志级别以及控制Logging的位置打印想要的信息
                pipeline.addLast(new LoggingHandler(LogLevel.INFO));

                //flush增强，减少了flush次数，牺牲一定延迟，提高吞吐量
                pipeline.addLast("flushEnhance", new FlushConsolidationHandler(5, true));

                //pipeline.addLast(new OrderServerProcessHandler());
                pipeline.addLast(business, new OrderServerProcessHandler());
            }
        });

        ChannelFuture channelFuture = serverBootstrap.bind(8090).sync();

        channelFuture.channel().closeFuture().get();

    }

}
