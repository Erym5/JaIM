package org.erym.im.netty.serverHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.erym.im.common.concurrent.FutureTaskScheduler;
import org.erym.im.common.constant.ServerConstants;
import org.erym.im.common.model.protocol.ProtoMsg;
import org.erym.im.netty.server.session.service.SessionManger;

import java.util.concurrent.TimeUnit;

/**
 * create by 尼恩 @ 疯狂创客圈
 **/
@Slf4j
public class HeartBeatServerHandler extends IdleStateHandler
{

    private static final int READ_IDLE_GAP = 1500;

    public HeartBeatServerHandler()
    {
        super(READ_IDLE_GAP, 0, 0, TimeUnit.SECONDS);

    }

    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception
    {
        //判断消息实例
        if (null == msg || !(msg instanceof ProtoMsg.Message))
        {
            super.channelRead(ctx, msg);
            return;
        }

        ProtoMsg.Message pkg = (ProtoMsg.Message) msg;
        //判断消息类型
        ProtoMsg.HeadType headType = pkg.getType();
        if (headType.equals(ProtoMsg.HeadType.HEART_BEAT))
        {
            //异步处理,将心跳包，直接回复给客户端
            FutureTaskScheduler.add(() ->
            {
                if (ctx.channel().isActive())
                {
                    log.info("心跳");
                    ctx.writeAndFlush(msg);
                }
            });

        }
        super.channelRead(ctx, msg);

    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception
    {
        log.info(READ_IDLE_GAP + "秒内未读到数据，关闭连接", ctx.channel().attr(ServerConstants.CHANNEL_NAME).get());
        SessionManger.inst().closeSession(ctx);
    }
}