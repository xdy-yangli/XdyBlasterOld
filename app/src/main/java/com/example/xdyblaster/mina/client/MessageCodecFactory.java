package com.example.xdyblaster.mina.client;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

import com.example.xdyblaster.mina.server.MessageEncoder;

public class MessageCodecFactory implements ProtocolCodecFactory {
	
	private final MessageDecoder decoder;
	private final MessageEncoder encoder;

	public MessageCodecFactory() {
		this.decoder = new MessageDecoder();
		this.encoder = new MessageEncoder();
	}

	public ProtocolDecoder getDecoder(IoSession arg0) throws Exception {
		return this.decoder;
	}

	public ProtocolEncoder getEncoder(IoSession arg0) throws Exception {
		return this.encoder;
	}
}
