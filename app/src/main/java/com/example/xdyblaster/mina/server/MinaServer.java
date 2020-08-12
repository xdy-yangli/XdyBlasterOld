package com.example.xdyblaster.mina.server;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LogLevel;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.example.xdyblaster.mina.vo.MinaMessageRec;
import com.example.xdyblaster.mina.vo.MinaMessageSend;

public class MinaServer{

	private static Logger logger = Logger.getLogger(MinaServer.class);
	public boolean isOpen = false;
	
	public IoAcceptor dataAccepter = null;
	private MinaHandler handler = new MinaHandler();

	private static MinaServer minaServer = null;
	
	private Thread bufaProcess = null;
	private boolean bufaProcessSwitch = false;
	
	private ExecutorService fixedThreadPool = Executors.newFixedThreadPool(10);//线程池
	
	private MinaServer(){
		//单例模式
	}
	
	public static MinaServer getInstance(){
		if(minaServer==null){
			minaServer = new MinaServer();
		}
		return minaServer;
	}
	
	public static void main(String[] args) {
		MinaServer.getInstance().start();
//		MinaServer.getInstance().stop();
	}
	
	/**
	 * 启动mina服务器,用于响应终端请求
	 * @return
	 */
	public boolean start() {
		try {
			dataAccepter = new NioSocketAcceptor();
			//设定日志过滤级别
			LoggingFilter log = new LoggingFilter();
			log.setMessageReceivedLogLevel(LogLevel.WARN);
			dataAccepter.getFilterChain().addLast("logger", log);

			//设置解码器(接收时)和编码器(发送时)
			dataAccepter.getFilterChain().addLast("codec",
					new ProtocolCodecFilter(new MessageCodecFactory()));
			
			//设定配置参数
			IoSessionConfig config = dataAccepter.getSessionConfig();
			config.setReadBufferSize(4096);
			config.setWriteTimeout(1000*10);
			config.setWriterIdleTime(100000);
			config.setIdleTime(IdleStatus.BOTH_IDLE, 30);
			
			//设置编码和解码后的回调类
			dataAccepter.setHandler(handler);
			//绑定监听端口,此处应为局域网IP,为127.0.0.1则不能使用IP连接本服务
			dataAccepter.bind(new InetSocketAddress(1089));
			
			logger.info("Mina服务器启动成功!端口号:" + 1089);
			System.out.println("Mina服务器启动成功!端口号:" + 1089);
			isOpen = true;
			
			bufaProcessSwitch = true;
			bufaProcess = new Thread(new Runnable() {
				public void run() {
					int times=0;//计数
					try {
						while (bufaProcessSwitch) {
//							yingda(times);
							yingdaBufa(times);
							//阻塞
							times++;
							Thread.sleep(5000L);
						}
					} catch (Exception e) {
						logger.error(e);
					}
				}
			});
			bufaProcess.start();
		} catch (Exception e) {
			isOpen = false;
			logger.error("Mina服务器启动失败:" + e);
		}
		return isOpen;
	}
	

	protected void yingdaBufa(int times) {
		//补发必须每次全部.
		if(MinaMessageOp.msgMap.size()==0){
			return;
		}
		//中间缺失的分包...最后一个分包缺失...如果第一分包缺失则后续所有包都会丢弃,如何要求补发.
		final List<MinaMessageSend> sendlist = new ArrayList<MinaMessageSend>();
		Iterator<String> it = MinaMessageOp.msgMap.keySet().iterator();
		long now = new Date().getTime();
		List<String> removeKey = new ArrayList<String>(); // 标记要被移除的sn
		while (it.hasNext()) {
			String sn = it.next();
			MinaMessageRec rec = MinaMessageOp.msgMap.get(sn);
			List<String> unpack = rec.getUnPackNo();
			if(unpack.size()==0){// 接收正确
				if (times % 10 == 0) {
					logger.info("等待应答队列数:" + MinaMessageOp.msgMap.size());
				}
				MinaMessageSend mess = new MinaMessageSend(sn, "O", null); 
				sendlist.add(mess);
				if(rec.getSendCount() < 0){  // 回复次数小于0，标记sn被移除（默认回复3次）
					removeKey.add(mess.getSn());
				}
			}
			//验证是否2分钟之前
			long cz = now-rec.getRecordTime().getTime();
			if(cz<2*60*1001){
				continue;
			}
			if(cz>4*60*1000){ // 连接超过4分钟，就移除sn
				removeKey.add(sn);
				continue;
			}
			if(unpack.size() > 0){// 丢包
				//每10次输出日志
				if (times % 10 == 0) {
					logger.info("当前接收待处理数(未接收完毕的起爆信息):" + MinaMessageOp.msgMap.size());
					logger.info("即将对2分钟前的仍未接收完毕的检测要求补发.");
				}
				StringBuffer sb = new StringBuffer();
				for (String s : unpack) {
					sb.append(s);
				}
				MinaMessageSend mess = new MinaMessageSend(sn, "R", sb.toString());
				sendlist.add(mess);
			}
		}
		for(String key : removeKey){
			MinaMessageOp.msgMap.remove(key);
		}
		//高频率应答,20个线程池
		fixedThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				send(sendlist);
			}
		});
	}

	/**
	 * 停止监听
	 */
	public void stop() {
		bufaProcessSwitch = false;
		if (dataAccepter!=null) {
			//释放端口
			dataAccepter.unbind();
			//清空日志过滤器
			dataAccepter.getFilterChain().clear(); // 清空Filter,防止下次重新启动时出现重名错误
			//释放资源
			dataAccepter.dispose(); // 可以另写一个类存储IoAccept，通过spring来创建，这样调用dispose后也会重新创建一个新的。或者可以在init方法内部进行创建。
			dataAccepter = null;
		}
	}
	

	protected void send(List<MinaMessageSend> msgList) {
		for (MinaMessageSend msend : msgList) {
			if(MinaMessageOp.msgMap.get(msend.getSn()) != null){
				send(msend);
			}
		}
	}

	private boolean send(MinaMessageSend msg) {
		IoSession session = handler.getConnction(msg.getSn());
		if (session != null && session.isConnected()) {
			byte[] a = new MinaMessageOp().writeToBytes(msg);
			WriteFuture wf = session.write(a);
			wf.awaitUninterruptibly(1000);
			if (wf.isWritten()){
				return true;
			}else {
				Throwable tr = wf.getException();
				if (tr != null) {
					logger.error(tr.getMessage(), tr);
				}
			}
		}
		return false;
	}

	/*public IoSession getSession(long sid) {
		return dataAccepter.getManagedSessions().get(sid);
	}*/

}
