//| Copyright - The University of Edinburgh 2011                            |
//|                                                                         |
//| Licensed under the Apache License, Version 2.0 (the "License");         |
//| you may not use this file except in compliance with the License.        |
//| You may obtain a copy of the License at                                 |
//|                                                                         |
//|    http://www.apache.org/licenses/LICENSE-2.0                           |
//|                                                                         |
//| Unless required by applicable law or agreed to in writing, software     |
//| distributed under the License is distributed on an "AS IS" BASIS,       |
//| WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.|
//| See the License for the specific language governing permissions and     |
//| limitations under the License.                                          |
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.apps;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.ed.epcc.safe.accounting.upload.AccountingUploadParser;
import uk.ac.ed.epcc.safe.accounting.upload.UploadException;
import uk.ac.ed.epcc.safe.accounting.upload.UploadParser;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.apps.Command;
import uk.ac.ed.epcc.webapp.apps.CommandLauncher;
import uk.ac.ed.epcc.webapp.apps.Option;
import uk.ac.ed.epcc.webapp.apps.Options;



public class UsageRecordUploadApp implements Command{
  private static final String desc =
   "Reads Usage records and inserts them in the database";
  
  private static final Options options = new Options();
  
  
  private static final Option OPT_DATA_FILE = new Option(options, 'f',
			"datafile", true, "Specify a data file to load");
// private static final Option OPT_DATA_MODE = new Option(options, 'm',
//			"mode", true, "Specify a special upload mode");
  private static final Option OPT_DATA_DIR = new Option(options, 'd',
			"datadirectory", true, "Specify a directory of data files to load");
  
  private static final Option OPT_UPLOAD_TABLE = new Option(options, 't',
			"tablename", true, "Specify the table to which data will be uploaded");

 

    private AppContext conn;
    public UsageRecordUploadApp(AppContext c){
    	conn=c;
    }
    
    
  
	
	
	
	protected void upload(Map<String,Object> params){


		
		
		
		try {
			
			String type = (String) params.get("mode");
			UploadParser parser = conn.makeObjectWithDefault(UploadParser.class, AccountingUploadParser.class, type);
			String result = parser.upload(params);
			System.out.println(result);
		} catch(UploadException ue){
			CommandLauncher.die(ue);
		}catch (Exception e) {
			CommandLauncher.die(e);
		}
		
		
		
       
		
	}
	/*
	 * ##########################################################################
	 * PRIVATE METHODS
	 * ##########################################################################
	 */

	
	
	private static String readFileAsString(String filePath) throws java.io.IOException{
	    byte[] buffer = new byte[(int) new java.io.File(filePath).length()];
	    java.io.BufferedInputStream f = new java.io.BufferedInputStream(new java.io.FileInputStream(filePath));
	    f.read(buffer);
	    f.close();
	    return new String(buffer);
	}





	public String description() {
		return desc;
	}





	public String help() {
		StringBuilder sb = new StringBuilder();
		sb.append("Options:\n");
		sb.append(options.toString());
		return sb.toString();
	}





	public void run(LinkedList<String> args) {
		
		Options.Instance opts = options.newInstance();
	
		List<String> data;
		try {
			
			data = opts.parse(args);
		} catch (IllegalArgumentException e) {
			CommandLauncher.die(e);
			return; // This will never happen but java can't spot that.
		} catch (IllegalStateException e) {
			CommandLauncher.die(e);
			return; // This will never happen but java can't spot that.
		}
		
		Map<String,Object> params= new HashMap<>();
		// default arguments
		for(String param : data){
			if( param.contains("=")){
				int pos = param.indexOf('=');
				params.put(param.substring(0, pos), param.substring(pos+1));
			}
		}
		// table into which we will load data
		String uploadTableName = null;
		if (opts.containsOption(OPT_UPLOAD_TABLE)) {
			uploadTableName = opts.getOption(OPT_UPLOAD_TABLE).getValue();
		} else
		{
			if( ! params.containsKey("mode")){
				CommandLauncher.die(new Exception("Upload tablename not specified"));
			}
		}
		// load data from an individual file
		if (opts.containsOption(OPT_DATA_FILE)) {
			String dataFileName = opts.getOption(OPT_DATA_FILE).getValue();
			File f = new File(dataFileName);
			if (!f.exists())
			{
				CommandLauncher.die("File " + dataFileName + " does not exist");
			}
			if(f.isDirectory())
			{
				CommandLauncher.die(dataFileName + " is a directory, use " + OPT_DATA_DIR.getShortName());
			}
			String update=null;
			try {
				update=UsageRecordUploadApp.readFileAsString(dataFileName);
			} catch (Exception e) {
				CommandLauncher.die(e);
			}
			
			params.put("update", update);
			params.put("table", uploadTableName);
			upload(params);
		}
		
		if (opts.containsOption(OPT_DATA_DIR)) {
			
			
			String dataDirName = opts.getOption(OPT_DATA_DIR).getValue();
			File f = new File(dataDirName);
			if(!f.exists())
			{
				CommandLauncher.die(new Exception("Directory " + dataDirName + " does not exist"));
			}
			
			if(!f.isDirectory())
			{
				CommandLauncher.die(new Exception(dataDirName + " is not a directory, use " + OPT_DATA_FILE.getShortName()));
			}
			
			File[] uploadFiles = f.listFiles();
			for( File fl : uploadFiles)
			{
				if ( fl.isFile())
				{
					String update=null;
					try {
						update=UsageRecordUploadApp.readFileAsString(fl.getAbsolutePath());
					} catch (Exception e) {
						CommandLauncher.die(e);
					}
					
					params.put("update", update);
					params.put("table", uploadTableName);
					upload(params);
				}
			}
			
		}
		
	}





	public AppContext getContext() {
		return conn;
	}
	
	public static void main(String args[]){
		AppContext c = new AppContext();
		
		CommandLauncher launcher = new CommandLauncher(c);
		launcher.run(UsageRecordUploadApp.class,args);
	}
}