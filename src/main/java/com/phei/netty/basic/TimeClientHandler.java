/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.phei.netty.basic;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.logging.Logger;

import static io.netty.buffer.Unpooled.buffer;

/**
 * @author lilinfeng
 * @date 2014年2月14日
 * @version 1.0
 */
//public class TimeClientHandler extends SimpleChannelInboundHandler {

public class TimeClientHandler extends ChannelHandlerAdapter {

	private static final Logger logger = Logger
			.getLogger(TimeClientHandler.class.getName());

	ByteBuf firstMessage;

	public TimeClientHandler(){
		String str= "QUERY TIME ORDER\r\n";
		byte[] req =str.getBytes();
		firstMessage = buffer(req.length);
		firstMessage.writeBytes(req);



	}
	public void channelActive(ChannelHandlerContext ctx) {
		System.out.println("Client active ");
		ctx.writeAndFlush(firstMessage);
	}



	protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
		System.out.println("Client channelRead ");
		ByteBuf buf = (ByteBuf) msg;
		byte[] req = new byte[buf.readableBytes()];
		buf.readBytes(req);
		String body = new String(req, "UTF-8");
		System.out.println("Now is : " + body);

		// 需要重新构建 ByteBuf ,因为 ByteBuf是有netty 维护的, 用了就释放,不能重复用
		ByteBuf firstMessage;
		String str= "QUERY TIME ORDER\r\n";
		req =str.getBytes();
		firstMessage = Unpooled.buffer(req.length);
		firstMessage.writeBytes(req);
		channelHandlerContext.writeAndFlush(firstMessage);


	}


	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// 释放资源
		logger.warning("Unexpected exception from downstream : "
				+ cause.getMessage());
		ctx.close();
	}
}
