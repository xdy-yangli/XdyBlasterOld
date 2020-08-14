package com.example.xdyblaster.mina.server;

import android.util.Log;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.transport.socket.SocketSessionConfig;

import com.example.xdyblaster.mina.vo.MinaMessageRec;

public class MinaHandler extends IoHandlerAdapter {

	//private static Logger logger = Logger.getLogger(MinaHandler.class);
	
	private static ConcurrentMap<String, IoSession> connctionMap = new ConcurrentHashMap<String, IoSession>();

	public void messageReceived(IoSession session, Object message) {
		if(message==null){
			Log.e("mina","异常发生");
			return;
		}
		//注:message对象由MessageCodecFactory.decoder产生,可以强转为该decoder返回的类型
		MinaMessageRec mess = (MinaMessageRec) message;
		//保持,供MinaServer子线程应答用.
		session.setAttribute("sn", mess.getSn());
		//保持连接,用于应答
		connctionMap.put(mess.getSn(), session);
		Log.e("mina","缺失包数量:"+mess.getUnPackNo().size());
		if(!mess.isOver() &&
				mess.getUnPackNo().size()==0){
			mess.setOver(true);//避免多C线程引起的线程安全问题.出现问题时会造成重复
//			MinaMessageOp.msgMap.remove(mess.getSn());
			//将mess实例化到数据库,再次期间如出现失败则使用session应答对方(按对方说法应在三分钟内应答,其在发送完毕后4分钟会断开连接)
			Log.e("mina","收到包已完整:"+mess.getQbDate());
			// 保存数据
		}
	}

	public void messageSent(IoSession session, Object message) {
		//注:message对象由MessageCodecFactory.encoder产生,可以强转为该decoder返回的类型
		Log.e("mina",session.getId()+ "发送数据:"+new String((byte[])message));
	}

	public void sessionClosed(IoSession session){
		connctionMap.remove(session.getAttribute("sn"));
		session.closeNow();//close(true);
		Log.e("mina",session.getId()+ "连接关闭:"+session.getAttribute("sn"));
		System.out.println(session.getId()+ "连接关闭:"+session.getAttribute("sn"));
	}

	public void sessionCreated(IoSession session) {
		// 当网络连接被创建时此方法被调用（这个肯定在sessionOpened(IoSession
		// session)方法之前被调用），这里可以对Socket设置一些网络参数
		IoSessionConfig cfg1 = session.getConfig();
		if (cfg1 instanceof SocketSessionConfig) {
			SocketSessionConfig cfg = (SocketSessionConfig) session.getConfig();
			// ((SocketSessionConfig) cfg).setReceiveBufferSize(4096);
			cfg.setReceiveBufferSize(2 * 1024 * 1024);
			cfg.setReadBufferSize(2 * 1024 * 1024);
			cfg.setKeepAlive(true);
			// if (session.== TransportType.SOCKET) {
			// ((SocketSessionConfig) cfg).setKeepAlive(true);
			cfg.setSoLinger(0);
			cfg.setTcpNoDelay(true);
			cfg.setWriteTimeout(1000*10);
		}
		Log.e("mina",session.getId()+ "连接创建");
	}

	public void sessionIdle(IoSession session, IdleStatus idle)
			throws Exception {		
		String sn = String.valueOf(session.getAttribute("sn"));
		MinaMessageRec rec = MinaMessageOp.msgMap.get(sn);
		if(rec==null || (rec.getUnPackNo().size()==0 && rec.getSendCount()==0)){ // 正确接收数据包并已回复3次后关闭连接
			sessionClosed(session);
		}
	}

	public void sessionOpened(IoSession session) {
		//logger.info(session.getId()+ "连接超时关闭");
	}

	public IoSession getConnction(String sn) {
		Log.e("mina",sn+ "连接建立");
		return connctionMap.get(sn);
	}  
}
