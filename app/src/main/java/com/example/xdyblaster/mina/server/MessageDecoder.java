package com.example.xdyblaster.mina.server;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

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
		int pos = 0;
		while (in.remaining() > 0) {
			in.mark();
			byte tag = in.get();//找到开头,因对方传输的是10进制byte,而get()默认会将其认为是16进制
			// 搜索包的开始位置
			if (tag == 0x2A //包头标记*
					&& in.remaining() > 0) {
				// 寻找包的结束位置, 防止是两个0x2A,可能取到后面包的开始位置(因包开始和结束标记一样)
				tag = in.get();//取下一个
				while (tag != 0x24) { // 包尾标记$
					if (in.remaining() <= 0) {
						//没有找到结束标记，等待下一次发包
						in.reset();//回到mark即开头标记,等待后续继续发送(注: 是指同一个连接继续发送)
						return false;
					}
					tag = in.get();
				}
				//找到结束标记
				pos = in.position();
				//取得两标记之间的数据进行解析
				int packetLength = pos - in.markValue();
				if (packetLength > 1) {
					byte[] tmp = new byte[packetLength];
					in.reset();
					in.get(tmp);
					MinaMessageOp message = new MinaMessageOp();
					out.write(message.readFromBytes(tmp)); // 触发接收Message的事件
				} else {
					// 说明是两个0x2A
					in.reset();
					in.get(); // 两个2A说明前面是包尾，后面是包头
				}
			}
		}
		return false;//只能false
	}
}
