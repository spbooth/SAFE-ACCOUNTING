// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.reference;


import java.util.Date;
import java.util.Map;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.config.ConfigService;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
/** PropertyRegistry that builds its entries from configuration parameters.
 * 
 * A description for the registry can be set using the property 
 * <b>registry.<i>registry-name</i>.description</b>
 * <p>
 * Properties are created by setting:
 * <b>property.<i>registry-name</i>.<i>prop-name</i>=<i>type</i>
 * <p>
 * A description can be set using:
 * <b>description.<i>registry-name</i>.<i>prop-name</i>
 * <p>
 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: ConfigPropertyRegistry.java,v 1.3 2014/09/15 14:32:27 spb Exp $")

public class ConfigPropertyRegistry extends PropertyRegistry {

	
	private String prefix;
	@SuppressWarnings("unchecked")
	public ConfigPropertyRegistry(AppContext c, String name) {
		super(name, c.getInitParameter("registry."+name+".description",name));
		Logger log = c.getService(LoggerService.class).getLogger(getClass());
		prefix = "property."+name+".";
		log.debug("Building ConfigPropertyRegistry "+name);
		Map<String,String> params =c.getInitParameters(prefix);
		for(String key : params.keySet()){
			String prop_name=key.substring(prefix.length());
			String type=params.get(key);
			if( type == null ){
				continue;
			}
			type=type.trim();
			log.debug("Consider "+prop_name+" type="+type);
			String description = c.getInitParameter("description."+name+"."+prop_name);
			
			
		    if( type.equalsIgnoreCase("String")){
		    	new PropertyTag<String>(this, prop_name,String.class,description);
		    	continue;
		    }else if ( type.equalsIgnoreCase("Number")){
		    	new PropertyTag<Number>(this, prop_name, Number.class,description);
		    	continue;
		    }else if ( type.equalsIgnoreCase("Double")){
		    	new PropertyTag<Double>(this, prop_name, Double.class,description);
		    	continue;
		    }else if ( type.equalsIgnoreCase("Long")){
		    	new PropertyTag<Long>(this, prop_name, Long.class,description);
		    	continue;
		    }else if ( type.equalsIgnoreCase("Float")){
		    	new PropertyTag<Float>(this, prop_name, Float.class,description);
		    	continue;
		    }else if ( type.equalsIgnoreCase("Integer")){
		    	new PropertyTag<Integer>(this, prop_name, Integer.class,description);
		    	continue;
		    }else if ( type.equalsIgnoreCase("Date")){
		    	new PropertyTag<Date>(this, prop_name,Date.class,description);
		    	continue;
		    }else{
		    	Class<? extends DataObjectFactory> clazz = c.getPropertyClass(DataObjectFactory.class, null, type);
		    	if( clazz != null ){
		    		new ReferenceTag(this, prop_name, clazz,type);
		    		continue;
		    	}
		    }
		    log.debug("No tag made for "+prop_name);
		}
		lock();
	}


	public boolean addDefinition(AppContext ctx,String name,String type){
		if( find(name) != null){
			return false;
		}
		ConfigService serv = ctx.getService(ConfigService.class);
		if( serv == null ){
			return false;
		}
		serv.setProperty(prefix+name,type);
		return true;
		
	}
		
	}