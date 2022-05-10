package com.somesky;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;

public class Sftp {

	private Session sshSession;
	private Channel channel;
	private ChannelSftp sftp;
	private JSch jsch;
	private Log log;

	public Log getLog() {
		if (this.log == null) {
			this.log = new SystemStreamLog();
		}

		return this.log;
	}
	/**
	 * 连接sftp服务器
	 * 
	 * @param host
	 *            主机
	 * @param port
	 *            端口
	 * @param username
	 *            用户名
	 * @return
	 * @throws JSchException 
	 */
	public void createSession(String host, int port, String username) throws JSchException {
		sshSession=jsch.getSession(username, host, port);
		Properties sshConfig = new Properties();
		sshConfig.put("StrictHostKeyChecking", "no");
		sshSession.setConfig(sshConfig);
	}
	
	public void connect() throws JSchException {
		sshSession.connect();
		channel = sshSession.openChannel("sftp");
		channel.connect();
		sftp = (ChannelSftp) channel;
	}
	
	public void connectWithIdentify(String host, int port, String username,
			String key) throws JSchException {
		jsch = new JSch();
		jsch.addIdentity(key);
		createSession(host,port,username);
		getLog().info("sftp连接已创建");
		connect();
	}
	
	public void connectWithPasswd(String host, int port, String username,
			String password) throws JSchException {
		jsch = new JSch();
		createSession(host,port,username);
		sshSession.setPassword(password);
		connect();
	}


	public void close(){
		sftp.exit();
		channel.disconnect();
		sshSession.disconnect();
	}
	
	/**
	 * 上传文件
	 */
	public void uploadFile(String destDirectory, String sourceFile) {
		try {
			File file = new File(sourceFile);
			System.out.println("destDirectory:"+destDirectory);
			System.out.println("sourceFile:"+sourceFile);
			sftp.put(new FileInputStream(file), file.getName());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void uploadDirectory(String destDirectory, String sourceDirectory) {
		String baseDir=destDirectory;
		String curPath="";
		File sourceFile=new File(sourceDirectory);
		if(!sourceFile.exists()){
			throw new RuntimeException("Directory need upload does not exist:"+sourceDirectory);
		}
		File[] subFile=sourceFile.listFiles();
		for(File itemFile:subFile){
			if(itemFile.isDirectory()){
				curPath=getPath(sourceDirectory,itemFile.getAbsolutePath());
				String remotePath=baseDir+curPath;
				cdRemoteDirectory(remotePath);
				uploadDirectory(remotePath,itemFile.getPath());
			}else{
				cdRemoteDirectory(baseDir);
				uploadFile(baseDir,itemFile.getAbsolutePath());
			}
		}
	}

	public void cdRemoteDirectory(String remotePath){
		//enter remote directory
		String remoteDir=getParentPath(remotePath);
		try {
			sftp.cd(remoteDir);
		} catch (SftpException e) {
			throw new RuntimeException("cd "+remoteDir+" has error!");
		}
		String dir=remotePath.replace(remoteDir+"/", "");
		try{
		    sftp.cd(dir);
		}catch(SftpException sException){
		    if(ChannelSftp.SSH_FX_NO_SUCH_FILE == sException.id){
			    try {
					sftp.mkdir(dir);
					sftp.cd(dir);
				} catch (SftpException e) {
					throw new RuntimeException(remoteDir+" mkdir "+dir+"on  has error!");
				}
		    }
		}
	}
	
	public String getParentPath(String dir){
		dir=dir.replace("\\", "/");
		if(dir.endsWith("/")) dir=dir.substring(0,dir.length()-1);
		int index=dir.lastIndexOf('/');
		if(index<1) return dir; 
		return dir.substring(0,index);
	}
	
	public String getPath(String base,String filePath){
		filePath=filePath.replace("\\", "/");
		base=base.replace("\\", "/");
		String curPath=filePath.replace(base, "");
		return curPath;
	}
}