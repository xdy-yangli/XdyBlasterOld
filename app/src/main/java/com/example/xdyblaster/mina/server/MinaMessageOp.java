package com.example.xdyblaster.mina.server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import org.apache.mina.core.buffer.IoBuffer;

import com.example.xdyblaster.mina.vo.MinaMessageRec;
import com.example.xdyblaster.mina.vo.MinaMessageSend;

/**
 * 核心类: gps终端发送的消息被解析成本类的实例 入口方法为decoder调用的readFromBytes()
 */
public class MinaMessageOp {
	private String sn;// 终端序列号
	//ConcurrentMap线程安全
	//本次子包数据,当有分包时才用到该值总是被加入到msgMap中
	//用于存放分包信息,以便所有分包到达时触发解析,由sn和dataType组成key,thisChildData组成value
	public static ConcurrentMap<String, MinaMessageRec> msgMap = new ConcurrentHashMap<String, MinaMessageRec>();
	//回复消息,收到消息时仅需将消息压入本map,由MinaServer.bufaProcess,key=sn,value[0]=num[1] [1]=O/E
	public static ConcurrentLinkedQueue<MinaMessageSend> yingdaQueue = new ConcurrentLinkedQueue<MinaMessageSend>();
	
	private int packCount;//表示包总数 值1表示不分包
	
	private String packNo;//当前包序号,二位
	
	private String errorMessage;//错误信息,暂未想好怎么用,对方是否需要该值.
	
	/**
	 * 将本对象转成应答(byte[])
	 * @return
	 */
	public final byte[] writeToBytes(MinaMessageSend mess) {
		byte bw = 36;//$
		//该方法用于模拟发送,测试用
		IoBuffer buf = IoBuffer.allocate(300).setAutoExpand(true);
		buf.mark();
		String a = null;
		if("R".equals(mess.getCommType())){ // 补发
			// 补发数据包总数
			String sjbNum = mess.getPackNo().length()<20?"0"+(mess.getPackNo().length()/2)
					:String.valueOf(mess.getPackNo().length()/2);
			// 数据包长度,命令代码,错误包数,错误包编号
			a = "#"+(mess.getPackNo().length()+10)+mess.getCommType()+sjbNum+mess.getPackNo();
		}else{ // 正确
			a = "#08"+mess.getCommType();
		}
		byte[] b = a.getBytes();
		buf.put(b);
		//计算xor
		String xor = String.valueOf(MinaMessageOp.getXor(b, 0, b.length));
		while (xor.length()<3) {
			xor = "0"+xor;
		}
		buf.put(xor.getBytes());
		buf.put(bw);
		//返回有效部分
		byte[] bb = new byte[buf.position()];
		buf.reset();
		buf.get(bb);
		//byte[] bb = buf.array();
		return bb;
	}

	/**
	 * 解析接收到的终端数据到本对象
	 * @param messageBytes,包括头尾
	 */
	public final MinaMessageRec readFromBytes(byte[] messageBytes) {
		//验证数据完整性,取到xor码与计算的xor码getXor()比对,如果是其他算法,请修改getCheckXor方法
		byte xor = getXor(messageBytes, 0, messageBytes.length-4);//
		//对方要求xor用byte值的字符串形式,即3个字节
		byte[] tmp = new byte[3];
		System.arraycopy(messageBytes, messageBytes.length-4, tmp, 0, 3);
		if(xor == Integer.parseInt(new String(tmp))){
			//执行parseOneData得到本对象的一些属性,以供ServerHandler.messageReceived()使用
			MinaMessageRec a = this.parseOneData(messageBytes);
			//压入应答消息
			yingdaQueue.add(new MinaMessageSend(sn, "O", packNo));
			return a;
		}else{
			errorMessage = "校验码不正确";
			System.out.println(xor+errorMessage+new String(tmp));
			System.out.println("接收数据"+new String(messageBytes));
		}
		yingdaQueue.add(new MinaMessageSend(sn, "E", packNo));
		return null;
	}

	/**
	 * 一次包的数据
	 * @param messageBytes
	 * @return isover
	 */
	private MinaMessageRec parseOneData(byte[] messageBytes){
		//解析messageBytes得到num的三个值
		byte[] tmp = new byte[14];//一个雷管长度,避免后面再重复new
		System.arraycopy(messageBytes, 1, tmp, 0, 4);
		String pack = new String(tmp,0,4);
		packCount = Integer.parseInt(pack.substring(0,2));
		packNo = pack.substring(2);
		//起爆器编码
		System.arraycopy(messageBytes, 8, tmp, 0, 8);
//		System.arraycopy(messageBytes, 5, tmp, 0, 6);
		sn = new String(tmp,0,8);
		//取得data对象
		MinaMessageRec md = null;
		//解析起爆信息
		int i = 16;
//		int i = 11;
		if("01".equals(packNo)){//首包
			md = msgMap.get(sn);
			if(md == null){
				md = new MinaMessageRec();
			}
			msgMap.put(sn, md);
			md.setSn(sn);
			md.setPackCount(packCount);
			//解析经纬度13字节
			System.arraycopy(messageBytes, i, tmp, 0, 13);
			pack = new String(tmp,0,13);
			md.setLng(Integer.parseInt(pack.substring(0,7))/10000d);
			md.setLat(Integer.parseInt(pack.substring(7))/10000d);
			//起爆时间12字节
			i+=13;
			System.arraycopy(messageBytes, i, tmp, 0, 12);
			md.setQbDate(new String(tmp,0,12));
			i+=12;
		}else{
			md = msgMap.get(sn);
			if(md == null){
				md = new MinaMessageRec();
			}
			msgMap.put(sn, md);
			md.setSn(sn);
			md.setPackCount(packCount);
		}
		md.addPackNo(packNo);
		//解析雷管码
		while(messageBytes.length>i+10){
			System.arraycopy(messageBytes, i, tmp, 0, 14);
			pack = new String(tmp);
			md.putLeiguan(pack.substring(0,13), pack.substring(13));
			i+=14;
		}
		return md;
	}

	/**
	 * 获取校验和
	 */
	public static byte getXor(byte[] data, int begin, int end) {
		byte A = 0;
		for (int i = begin; i < end; i++) {
			A ^= data[i];
		}
		return A;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
	
}