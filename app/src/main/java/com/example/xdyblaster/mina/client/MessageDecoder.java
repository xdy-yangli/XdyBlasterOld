package com.example.xdyblaster.mina.client;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import com.example.xdyblaster.mina.server.MinaMessageOp;

public class MessageDecoder extends CumulativeProtocolDecoder {

	protected boolean doDecode(IoSession session, IoBuffer in,
			ProtocolDecoderOutput out) throws Exception {
		//按以下3块方式写代码:
		//String message = "";//可以是其他对象.
		//此处对in读取并解析,IoBuffer的理解有点类似数据库的游标
		//out.write(message); // 触发ServerHandler的接收messageReceived事件,message作为第二个参数
		
		//下面是代码.
		if (in.remaining() < 1) {
			return false;
		}
		byte[] a = in.array();
		int i = 0;
		while (in.remaining() > 0) {
			in.mark();
			byte tag = in.get();//找到开头,因对方传输的是10进制byte,而get()默认会将其认为是16进制
			a[i] = tag;
			i++;
		}
		
		out.write(a); // 触发接收Message的事件
		return false;
	}
}
