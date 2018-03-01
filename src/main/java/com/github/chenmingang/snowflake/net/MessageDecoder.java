package com.github.chenmingang.snowflake.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class MessageDecoder extends ByteToMessageDecoder {
    private static final int MAGIC_NUMBER = 0x0CAFFEE0;
	public MessageDecoder() {

	}
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (in.readableBytes() < 14) {
			return;
		}
		// 标记开始读取位置
		in.markReaderIndex();

		int magic_number = in.readInt();

		if (MAGIC_NUMBER != magic_number) {
			ctx.close();
			return;
		}

		@SuppressWarnings("unused")
		byte version = in.readByte();

		long squence = in.readLong();
		long type = in.readLong();
		int length = in.readInt();

		if (length < 0) {
			ctx.close();
			return;
		}

		if (in.readableBytes() < length) {
			// 重置到开始读取位置
			in.resetReaderIndex();
			return;
		}

		byte[] body = new byte[length];
		in.readBytes(body);

		RequestInfo req = new RequestInfo();
		req.setBody(new String(body, "utf-8"));
		req.setSequence(squence);
		req.setType(type);
		out.add(req);
	}
}