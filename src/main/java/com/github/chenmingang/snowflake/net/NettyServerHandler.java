package com.github.chenmingang.snowflake.net;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class NettyServerHandler extends SimpleChannelInboundHandler<RequestInfo> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RequestInfo msg) throws Exception {
		System.out.println(msg.getSequence());
		msg.setBody("server");
		ctx.writeAndFlush(msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println("error");
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("inactive");
	}


	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		System.out.println("unregistered");
		ctx.channel().close();
	}

}