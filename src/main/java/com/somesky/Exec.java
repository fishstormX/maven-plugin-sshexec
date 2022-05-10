package com.somesky;
/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;

public class Exec{
	
	private Session session;
	private JSch jsch;

	private Log log;

	public Log getLog() {
		if (this.log == null) {
			this.log = new SystemStreamLog();
		}

		return this.log;
	}
	
	public void createSession(String host, int port, String user) throws JSchException {
		session=jsch.getSession(user, host, port);
		Properties sshConfig = new Properties();
		sshConfig.put("StrictHostKeyChecking", "no");
		session.setConfig(sshConfig);
	}
	
	public void connectWithPasswd(String host, int port, String user,
			String password) throws JSchException {
		jsch=new JSch();
		createSession(host,port,user);
		getLog().info("创建链接 \n用户名："+user);
		session.setPassword(password);
		session.connect(20000);
	}
	
	public void connectWithIdentify(String host, int port, String user,
			String key) throws JSchException {
		jsch=new JSch(); 
		createSession(host,port,user);
		getLog().info("创建链接 \n用户名："+user+"\n公钥路径:"+key);
		jsch.addIdentity(key);
		session.connect(20000);
	}
	
	public void close(){
		 session.disconnect();
	}
	
	public Result exec(String command) throws JSchException, IOException{
		Channel channel=session.openChannel("exec");
	    ((ChannelExec)channel).setCommand(command);
	    channel.setInputStream(null);
	    InputStream in=channel.getInputStream();
	    InputStream err=((ChannelExec)channel).getErrStream();
	    channel.connect();
	    Result r=out(in, err, channel);
	    channel.disconnect();
	    return r;
	}

	public Result out(InputStream in,InputStream err,Channel channel) throws IOException {
		Result ret=new Result();
		StringBuffer msg=new StringBuffer();
		StringBuffer errMsg=new StringBuffer();
		byte[] tmpIn=new byte[1024];
		byte[] tmpErr=new byte[1024];
		while(true){
			while(in.available()>0){
				int i=in.read(tmpIn, 0, 1024);
				if(i<0)break;
				msg.append(new String(tmpIn, 0, i));
	        }
			while(err.available()>0){
				int i=err.read(tmpErr, 0, 1024);
				if(i<0)break;
				errMsg.append(new String(tmpErr, 0, i));
	        }
	        if(channel.isClosed()){
	        	if(in.available()>0) continue;
	        	ret.status=channel.getExitStatus();
	        	break;
	        }
	        try{Thread.sleep(1000);}catch(Exception ee){}
	      }
	      ret.msg=msg.toString();
	      ret.err=errMsg.toString();
	      return ret;
	}
  
	public static class Result{
		int status;
		String msg;
		String err;
	}
	
}