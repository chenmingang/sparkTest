package com.github.chenmingang.snowflake.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MessageEncoder extends MessageToByteEncoder<RequestInfo> {

	private static final String DEFAULT_ENCODE = "utf-8";

	private static final int MAGIC_NUMBER = 0x0CAFFEE0;

	public MessageEncoder() {
    }

	@Override
	protected void encode(ChannelHandlerContext ctx, RequestInfo msg, ByteBuf out) throws Exception {

		@SuppressWarnings("resource")
		ByteBufOutputStream writer = new ByteBufOutputStream(out);
		byte[] body = null;

		if (null != msg && null != msg.getBody(String.class) && "" != msg.getBody(String.class)) {
			body = msg.getBody(String.class).getBytes(DEFAULT_ENCODE);
		}

		writer.writeInt(MAGIC_NUMBER);

		writer.writeByte(1);


		writer.writeLong(msg.getSequence());
		writer.writeLong(msg.getType());

		if (null == body || 0 == body.length) {
			writer.writeInt(0);
		} else {
			writer.writeInt(body.length);
			writer.write(body);
		}
	}

}