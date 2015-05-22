package com.gepardec.javatools.webtail;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.SessionScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SessionScoped
public class TailerFactory implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG =  LoggerFactory.getLogger(TailerFactory.class);
	
	private Map<String, Tailer> tailServices;
	
	public TailerFactory() {
		tailServices = new HashMap<String, Tailer>();
	}
	
	/**
	 * Returns Tailor instance that tails a log file on the given path.
	 * @param path Path to the log file
	 * @param nrOfChars number of last characters from the EOF that will be initially showed
	 * @return
	 */
	public Tailer getTailer(String path, long nrOfChars){
		Tailer tailService = tailServices.get(path);
		if(tailService == null){
			try {
				tailService = new Tailer(path, nrOfChars);
			} catch (IOException e) {
				LOG.error("Error while producing tailer");
				return null;
			}
			tailServices.put(path, tailService);
		}
		
		return tailService;
	}
	
	/**
	 * Returns newly initialized clear Tailor instance that tails a log file on the given path.
	 * @param path Path to the log file
	 * @param nrOfChars number of last characters from the EOF that will be initially showed
	 * @return
	 */
	public Tailer getClearTailer(String path, long nrOfChars){
		Tailer tailService = tailServices.get(path);
		if(tailService != null){
			try {
				tailService.tearDown();
			} catch (IOException e) {
				LOG.warn("Error while removing tailer");
			}
			tailServices.remove(path);
		}
		
		return getTailer(path, nrOfChars);
	}

}
