package com.gepardec.javatools.webtail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/WebTailServlet")
public class WebTailServlet extends HttpServlet{

	private static final long serialVersionUID = 1L;
	private static final Logger LOG =  LoggerFactory.getLogger(WebTailServlet.class);
	
	@Inject
	TailerFactory tailerFactory;

	/**
	 * Expects URL WebTailServlet?path=[path]&nrOfChars=[nr].
	 * <ul><li>If nrOfChars is specified, servlet is called for configuration only:
	 * new tailer instance will be created but no event-stream will be open. In the configuration mode
	 * it is allowed for file path to contain system properties.</li>
	 * <li>If nrOfChars is not specified, servlet switches to push mode with event stream. In the working mode
	 * it is not allowed for file path to contain system properties.</li>
	 * <li>If download=true is specified, servlet prepairs log for download.</li></ul>
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		if("true".equals(request.getParameter("download"))){
			performDownload(request, response, StrSubstitutor.replaceSystemProperties(request.getParameter("path")));
			return;
		}

		String pathParam = request.getParameter("path");
		String nrOfCharsParam = request.getParameter("nrOfChars");
		long nrOfChars = 0;
		
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer = response.getWriter();
		
		if(nrOfCharsParam != null){	//config
			try{
				nrOfChars = Long.parseLong(nrOfCharsParam);
			} catch (NumberFormatException e){
				LOG.warn(nrOfCharsParam + " can not be parsed as long");
				nrOfChars = 0;
			}
			response.setContentType("text/plain");
			tailerFactory.getClearTailer(StrSubstitutor.replaceSystemProperties(pathParam), nrOfChars);
			writer.write(StrSubstitutor.replaceSystemProperties(pathParam));
		} else {	//act
			response.setContentType("text/event-stream");
			Tailer tailer = tailerFactory.getTailer(pathParam, 0);
			if(tailer == null){
				writer.write("data: Tailor could not be produced. Possible the file " + pathParam +" does not exist.\n\n");
				writer.close();
				return;
			}
			for(String line : tailer.tail().split(System.getProperty("line.separator"))){
				if(line.isEmpty() || line.equals("\n") || line.length() < 3){
					continue;
				}
				writer.write("data: " + line + "\n\n");
			}
		}		
		writer.close();
	}

	private void performDownload(HttpServletRequest request,
			HttpServletResponse response, String filename) throws IOException {
		response.setContentType("text/plain");
		OutputStream out = response.getOutputStream();
		try{
        	Path p = Paths.get(filename);
        	String file = p.getFileName().toString();
            response.setHeader("Content-disposition","attachment; filename="+file);

            File logFile = new File(filename);
	        
	        FileInputStream in = new FileInputStream(logFile);
	        byte[] buffer = new byte[4096];
	        int length;
	        while ((length = in.read(buffer)) > 0){
	           out.write(buffer, 0, length);
	        }
	        in.close();
	        out.flush();
        } catch (Exception e){
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
	}
	

}
