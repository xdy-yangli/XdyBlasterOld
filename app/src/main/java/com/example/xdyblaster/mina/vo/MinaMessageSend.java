package com.example.xdyblaster.mina.vo;


public class MinaMessageSend {
	// 此处添加属性和消息body对象(或接口)以及其set方法
	private String sn;// 终端序列号
	private String commType = "R";// 命令类型"O/E/R"
	private String packNo;//补发或者回应的包号

	public MinaMessageSend(String sn, String commType, String packNo) {
		super();
		this.sn = sn;
		this.commType = commType;
		this.packNo = packNo;
	}

	public String getSn() {
		return sn;
	}

	public void setSn(String sn) {
		this.sn = sn;
	}

	public String getCommType() {
		return commType;
	}

	public void setCommType(String commType) {
		this.commType = commType;
	}

	public String getPackNo() {
		return packNo;
	}

	public void setPackNo(String packNo) {
		this.packNo = packNo;
	}

}
