package com.example.xdyblaster.mina.client;

import android.util.Log;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.transport.socket.SocketSessionConfig;

public class MinaHandler extends IoHandlerAdapter {

	//private static Logger logger = Logger.getLogger(MinaHandler.class);
	int i=0;
	public void messageReceived(IoSession session, Object message) {
		if(message==null){
		//	logger.error("异常发生");
			Log.e("tcp","error");
			return;
		}
		byte[] bt = (byte[])message;
		i++;
		System.out.println(new String(bt)+i);
//		byte[] tmp = new byte[1]; // 只解析指令
//		tmp[0] = bt[3];
//		String str = new String(tmp);
//		if("R".equals(str)){ // 补发
//			
//		}
	}

	public void messageSent(IoSession session, Object message) {
		//注:message对象由MessageCodecFactory.encoder产生,可以强转为该decoder返回的类型
		//logger.info(session.getId()+ "发送数据:"+message);
	}

	public void sessionClosed(IoSession session){
		session.close(true);
		//logger.info(session.getId()+ "连接关闭");
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
			cfg.setWriteTimeout(1000);
		}
		//logger.info(session.getId()+ "连接创建(未连接上)");
	}

	public void sessionIdle(IoSession session, IdleStatus idle)
			throws Exception {
		System.out.println("=====连接超时关闭");
		//logger.info(session.getId()+ "连接超时关闭");
	}

	public void sessionOpened(IoSession session) {
		//logger.info(session.getId()+ "连接建立");
	}
}
