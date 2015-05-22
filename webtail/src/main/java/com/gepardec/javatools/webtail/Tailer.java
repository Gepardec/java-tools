package com.gepardec.javatools.webtail;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tailer implements Serializable{
	private static final long serialVersionUID = 1L;
	private static final Logger LOG =  LoggerFactory.getLogger(Tailer.class);
	
	long currPos;
	RandomAccessFile file;
	String path;
	
	public Tailer(String path, long nrOfChars) throws IOException {
		
		long charsFromEOF = (nrOfChars <= 0) ? 1000 : nrOfChars;
		
		this.path = path;
		file = new RandomAccessFile(new File(path), "r");
		currPos = Math.max(0, file.length() - charsFromEOF);
		file.close();
	}
	
	public String tail(){
		try {
			file = new RandomAccessFile(new File(path), "r");
			if(currPos == (file.length() - 1)){
				return "";
			}
			int nrBytes = (int)(file.length() - currPos);
			byte[] bytes = new byte[nrBytes];
			file.seek(currPos);
			file.read(bytes, 0, nrBytes);
			currPos = file.length() - 1;
			file.close();
			return new String(bytes);
		} catch (IOException e) {
			LOG.error("Error while reading file " + path, e);
			return "";
		}
	}
	
	@PreDestroy
	public void tearDown() throws IOException{
		file.close();
	}
}
