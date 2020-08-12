package com.example.xdyblaster.mina.vo;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.mina.core.buffer.IoBuffer;

import com.example.xdyblaster.mina.server.MinaMessageOp;

public class MinaMessageRec {
	// 此处添加属性和消息body对象(或接口)以及其set方法
	private double lng;// 经度 Longitude 简写Lng 纬度 Latitude 简写Lat
	private double lat;
	private String sn;// 终端序列号
	private String qbDate;// 起爆时间,yyyymmddHHmiss
	private ConcurrentMap<String, String> leiguan = new ConcurrentHashMap<String, String>();//

	//以下用于检测漏包补发
	private int packCount;
	//已解析的包序号
	private List<String> packNo = new ArrayList<String>();
	//是否已接收完毕
	private boolean isOver = false;
	//首个分包的接收时间,用于判断补发,当前设定为2分钟未收完就检测是否需要补发
	private Date recordTime = new Date();
	// 回复次数
	private int sendCount = 3; 
	
	
	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public String getSn() {
		return sn;
	}

	public void setSn(String sn) {
		this.sn = sn;
	}

	public String getQbDate() {
		return qbDate;
	}

	public void setQbDate(String qbDate) {
		this.qbDate = qbDate;
	}

	public boolean isOver() {
		return isOver;
	}

	public void setOver(boolean isOver) {
		this.isOver = isOver;
	}

	public Date getRecordTime() {
		return recordTime;
	}

	public void setRecordTime(Date recordTime) {
		this.recordTime = recordTime;
	}

	public Map<String, String> getLeiguan() {
		return leiguan;
	}

	public void putLeiguan(String a, String b) {
		leiguan.put(a, b);
	}

	public int getPackCount() {
		return packCount;
	}

	public void setPackCount(int packCount) {
		this.packCount = packCount;
	}

	public List<String> getUnPackNo() {
		List<String> tmp = new ArrayList<String>();
		if(packNo.size()==packCount){
			return tmp;
		}
		for(int i=1;i<=packCount;i++){
			String s = i<10?("0"+i):String.valueOf(i);
			if(!packNo.contains(s)){
				tmp.add(s);
			}
		}
		return tmp;
	}

	public void addPackNo(String packNo) {
		boolean iscz = false;
		for(String no : this.packNo){
			if(no.equals(packNo)){
				iscz = true;
				break;
			}
		}
		if(!iscz){
			this.packNo.add(packNo);
		}
	}
	
	public String getLngStr() {
		return String.valueOf(lng*10000).split("\\.")[0];
	}
	
	public String getLatStr() {
		return String.valueOf(lat*10000).split("\\.")[0];
	}
	
	public int getSendCount() {
		sendCount = sendCount>0?sendCount-1:-1;
		return sendCount;
	}

	public void setSendCount(int sendCount) {
		this.sendCount = sendCount;
	}

	public byte[] toByte(){
		byte bw = 36;//$
		//该方法用于模拟发送,测试用
		IoBuffer buf = IoBuffer.allocate(300).setAutoExpand(true);
		buf.mark();
		String a = null;
		if(leiguan.size()==0){
			a = "*02"+"01042"+this.getSn()+this.getLngStr()+this.getLatStr()+this.getQbDate();
			System.out.println(a.length());
		}else{
			a = "*02"+"02158"+this.getSn();
			Iterator<String> it = leiguan.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				a+=key+leiguan.get(key);
			}
		}
		//赋值,因长度不定
		byte[] b = a.getBytes();
		buf.put(b);
		//计算xor
		String xor = String.valueOf(MinaMessageOp.getXor(b, 0, b.length));
		while (xor.length()<3) {
			xor = "0"+xor;
		}
		buf.put(xor.getBytes());
//		buf.put("120".getBytes());
		buf.put(bw);
		//返回有效部分
		byte[] bb = new byte[buf.position()];
		buf.reset();
		buf.get(bb);
//		System.out.println(new String(buf.array()));
		return bb;
	}
}
