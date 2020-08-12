package com.example.xdyblaster.mina.client;

import java.net.InetSocketAddress;
import java.text.Format;
import java.util.Date;
import java.util.List;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.example.xdyblaster.mina.vo.MinaMessageRec;
import com.example.xdyblaster.util.DataViewModel;

public class MinaClient {
    public OnSendDataListener onSendDataListener = null;

    //
//	public static void main(String[] args) {
//		new MinaClient().start();
////		System.out.println((int)'*');
////		System.out.println(Format.dateToString(new Date(), "yyMMddHHmmss"));
//	}
    public interface OnSendDataListener {
        void sendDataResult(int percent);
    }

    public void start(List<String> list, DataViewModel dataViewModel) {
        NioSocketConnector connector = new NioSocketConnector();
        connector.getFilterChain().addLast(
                "encode",
                new ProtocolCodecFilter(new MessageCodecFactory()));

        connector.getSessionConfig().setReadBufferSize(2048);
        connector.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10000);
        connector.setConnectTimeoutMillis(1000 * 60 * 3);
        connector.setHandler(new MinaHandler());
//		ConnectFuture cf = connector.connect(new InetSocketAddress("127.0.0.1",
//				1089));
        ConnectFuture cf = connector.connect(new InetSocketAddress("119.29.111.172",
                6088)); // 服务器地址
        cf.awaitUninterruptibly();
//
/*
        MinaMessageRec message = new MinaMessageRec();
        message.setLng(302.0056);
        message.setLat(30.0101);
        message.setSn("D001011699");
//		message.setQbDate(Format.dateToString(new Date(), "yyMMddHHmmss"));
        message.setQbDate("160731164748");

        cf.getSession().write(message.toByte());//触发handler
        System.out.println("发送包1");
////
        message.putLeiguan("5760708A00145","O");
        message.putLeiguan("5760708A00116","O");
        message.putLeiguan("5760700103317","O");
        message.putLeiguan("5760708A04416","E");
        message.putLeiguan("5760708A05549","O");
        System.out.println("发送包2");
        cf.getSession().write(message.toByte());//触发handler
*/




        long l = new Date().getTime();
        long m = 0;
        long n = 0;
        boolean s = true;
        MinaMessageRec message = null;
        int i = 1;
        int k = 0;
        float f;
        if (onSendDataListener != null)
            onSendDataListener.sendDataResult(0);
        while (s) {
            m = new Date().getTime();
            n = m - l;
            if (n > 1000 * 6) {
                l = new Date().getTime();
                System.out.println("++++++++++++++++开始发送(" + i + ")++++++++++++++++++++");
                message = new MinaMessageRec();

//                message.setLng(302.0056);
//                message.setLat(30.0101);
//                message.setSn("D001011699");
//                message.setQbDate("160731164748");

                f = Float.parseFloat(dataViewModel.jd);
                message.setLng(f);
                f = Float.parseFloat(dataViewModel.wd);
                message.setLat(f);
                message.setSn("53AC6001");
                String d = dataViewModel.bpsj;
                d=d.substring(2, 4) + d.substring(5, 7) + d.substring(8, 10) + d.substring(11, 13) + d.substring(14, 16) + d.substring(17, 19);
                message.setQbDate(d);//Format.dateToString(new Date(), "yyMMddHHmmss"));

//                message.setLng(105.451433);
//                message.setLat(24.355353);
//                message.setSn("53AC6001");
//                message.setQbDate("200630154323");

                cf.getSession().write(message.toByte());//触发handler
                System.out.println("发送包1");
//				message.putLeiguan("5300623900105", "O");
//				message.putLeiguan("5300623901711", "O");

                if (k < list.size()) {
                    message.putLeiguan(list.get(k), "O");
                    k++;
                }
                if (k < list.size()) {
                    message.putLeiguan(list.get(k), "O");
                    k++;
                }
                if (k < list.size()) {
                    message.putLeiguan(list.get(k), "O");
                    k++;
                }
                if (k < list.size()) {
                    message.putLeiguan(list.get(k), "O");
                    k++;
                }

                if (onSendDataListener != null)
                    onSendDataListener.sendDataResult(k * 100 / list.size());
                System.out.println("发送包2");
                cf.getSession().write(message.toByte());//触发handler
//
//                //保持连接
                i++;
                if (k >= list.size())
                    break;

            } else {
                if (n > 1000 * 100 * 60) {
                    System.out.println("发送结束。。。。。。。。。。。。。。。。。");
                    s = false;
                }
            }
        }
        if (onSendDataListener != null)
            onSendDataListener.sendDataResult(100);



        cf.getSession().getCloseFuture().awaitUninterruptibly();

        connector.dispose();
        System.out.println("断开3");

        //System.exit(1);
    }

}