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
package uk.ac.ed.epcc.safe.accounting.upload;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ed.epcc.safe.accounting.db.AccountingClassification;
import uk.ac.ed.epcc.safe.accounting.db.AccountingClassificationFactory;
import uk.ac.ed.epcc.safe.accounting.model.PropertyPerson;
import uk.ac.ed.epcc.safe.accounting.model.PropertyPersonFactory;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceTag;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.forms.exceptions.ParseException;
import uk.ac.ed.epcc.webapp.model.Classification;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.relationship.Relationship;
import uk.ac.ed.epcc.webapp.model.relationship.Relationship.Link;
import uk.ac.ed.epcc.webapp.session.SessionService;



public class  DEISARolesUploadParser<P extends PropertyPerson> implements UploadParser, Contexed {
	
	
	private final String mGlobalSiteRole="SiteAll";
	private final String mRelationPermissionView = "view";
	private final String mRelationPermissionUpdate = "update";
	
	private final AppContext conn;
    private final String mode;
    
    // classification tables, populated by usage data upload
    private AccountingClassificationFactory<AccountingClassification> mProjectFactory;
    private AccountingClassificationFactory<AccountingClassification> mSiteFactory;
    private AccountingClassificationFactory<AccountingClassification> mUserFactory;
    
    // relationship tables
    private Relationship<P,AccountingClassification> mProjectRelationship;
    private Relationship<P,AccountingClassification> mSiteRelationship;
    
    // users of the website, as distinct from users in the usage data
    private PropertyPersonFactory<P> mPersonFactory;
    

    // keep a count of skipped lines
    private int mSkippedLines;
    
    // keep a count of rejected lines
    private int mRejectedLines;
    
    // keep a count of number of exceptions
    private int mErrors;
    
    
    private SessionService<P> mSessionService;

    
    @SuppressWarnings("unchecked")
    
    // initialise all the classes needed to interact with database
	private void initialise() throws Exception
    {
    	mPersonFactory = (PropertyPersonFactory<P>) conn.getService(SessionService.class).getLoginFactory();
    	
    	mProjectFactory = conn.makeObject(AccountingClassificationFactory.class,"Project");
    	if( mProjectFactory == null)
    	{
    		throw new Exception("Cant instantiate project factory");
    	}
    	
    	mSiteFactory = conn.makeObject(AccountingClassificationFactory.class,"Site");
    	if( mSiteFactory == null)
    	{
    		throw new Exception("Cant instantiate site factory");
    	}
    	
    	mUserFactory = conn.makeObject(AccountingClassificationFactory.class,"User");
    	if( mUserFactory == null)
    	{
    		throw new Exception("Cant instantiate user factory");
    	}
    	
    	mProjectRelationship = conn.makeObject(Relationship.class,"ProjectRelationship");
    	if( mProjectRelationship == null)
    	{
    		throw new Exception("Cant instantiate project relationship");
    	}
    	
    	mSiteRelationship = conn.makeObject(Relationship.class,"SiteRelationship");
    	if( mSiteRelationship == null)
    	{
    		throw new Exception("Cant instantiate site relationship");
    	}
    	
    	
    	mSessionService = conn.getService(SessionService.class);
    }
    
    // update a user based on DN and username
    @SuppressWarnings("unchecked")
	private void updateUser(String DN, String userName) throws DataFault, InvalidExpressionException, ParseException
    {
    	
    	P p = mPersonFactory.makeFromString(DN);
    	AccountingClassification user = mUserFactory.findFromString(userName);
    	
    	
    	AccountingClassification userClassification = mUserFactory.makeFromString(userName);

    	
    	ReferenceTag person_site_tag = (ReferenceTag)mPersonFactory.getFinder().find(mSiteFactory.getTag());
    	ReferenceTag user_site_tag = (ReferenceTag)mUserFactory.getFinder().find(mSiteFactory.getTag());

    	
		ReferenceTag<P,PropertyPersonFactory<P>> person_tag = (ReferenceTag<P,PropertyPersonFactory<P>>) mUserFactory.getFinder().find(mPersonFactory.getTag());

    	
        if(  person_tag != null ){
        	userClassification.setProperty(person_tag, mPersonFactory.makeReference(p));
        	userClassification.commit();
        } 
    	
    	
    	PropertyTag<? extends String> regexp_tag = mSiteFactory.getFinder().find(String.class,"UserMatch");
    	for( AccountingClassification site: mSiteFactory.all()){
    		String regexp = site.getProperty(regexp_tag);
    		if( regexp != null ){
    		Pattern pat = Pattern.compile(regexp);
    		Matcher m = pat.matcher(userName);
    		if( m.matches()){
    			
    			p.setProperty(person_site_tag, mSiteFactory.makeReference(site));
                p.commit();
                user.setProperty(user_site_tag, mSiteFactory.makeReference(site));
                user.commit();
                break;
    		}
    		}
    	}
    
    }
    
    // update a project based on DN and project name
    private void updateProject(String DN, String projectName) throws DataFault, ParseException
    {
    	
    	P p = mPersonFactory.makeFromString(DN);
    	
    	
    	AccountingClassification projectClassification = mProjectFactory.makeFromString(projectName);
    	
    	
    	mProjectRelationship.setRole(p, projectClassification, mRelationPermissionView, true);
    	
    	
    }
    
    // update a site.
    // siteName is either a name like "EPCC" or ALL.
    // If the siteName is "ALL", set a global role.
    // Otherwise, update person/site relationship
    private void updateSite(String DN, String siteName) throws DataFault, ParseException
    {
    	
    	
    	P p = mPersonFactory.makeFromString(DN);
    	
    	
    	if ( siteName.compareTo("all")==0)
    	{
    			mSessionService.setRole(p, mGlobalSiteRole, true);
    	}
    	else 
    	{
    		

    		Classification siteClassification = mSiteFactory.makeFromString(siteName);
    		
    		try
    		{
    			mSiteRelationship.setRole(p, (AccountingClassification) siteClassification, mRelationPermissionView, true);
    			mSiteRelationship.setRole(p, (AccountingClassification) siteClassification, mRelationPermissionUpdate, true);
    			
    		} catch (Exception e)
    		{
    			throw new DataFault("Error setting site relationship between " + DN + " and " + siteName);
    		}
    		

    	}
    }
    
    // clear down any current permissions and relationships.
    // we do not have a way to automatically remove existing permissions,
    // so we clear out everything at the start of the upload.
    private void clearPermissions() throws Exception {
    	
    
    	// clear all project relationships
    	
    	{
    		Iterator it = mProjectRelationship.all().iterator();
    		while(it.hasNext())
    		{
    			Link l = (Link)it.next();
    			if(l.hasRole(mRelationPermissionView)) {
    				l.setRole(mRelationPermissionView, false);
    				l.commit();
    			}
    		}
    	}
    	
    	
    	// clear all site relationships
    	{	
    		Iterator it = mSiteRelationship.all().iterator();
    		while(it.hasNext())
    		{
    			Link l = (Link)it.next();
    			if(l.hasRole(mRelationPermissionView)) {
    				l.setRole(mRelationPermissionView, false);
    				l.setRole(mRelationPermissionUpdate, false);
    				l.commit();
    			}
    		}
    	}
    	
    	// clear global role
    	{
    		
    		Iterator<P> it = mPersonFactory.all().iterator();
    		while(it.hasNext())
    		{
    			P p = it.next();
    			mSessionService.setRole(p, mGlobalSiteRole, false);
    		}
    	}
    }
    
    public DEISARolesUploadParser(AppContext c, String mode){
    	conn=c;
    	this.mode=mode;
    }

	public String upload(Map<String, Object> parameters) throws UploadException {
		
		StringBuilder sb = new StringBuilder();
		
		try
		{
			initialise();
		} catch (Exception e)
		{
			throw new UploadException("Could not instantiate a factory class");
		}
		try
		{
			clearPermissions();
		} catch (Exception e)
		{
			throw new UploadException("Error clearing existing reporting permissions");
		}
		
		String data = (String)parameters.get("update");
		
		String[] lines = data.split("\n");
		for(String s:lines)
		{
			try
			{
				parse(s);
			} catch (DataFault df)
			{
				sb.append(df.getMessage()).append("\n");
				df.printStackTrace();
			} catch (InvalidPropertyException ipe)
			{
				sb.append(ipe.getMessage()).append("\n");
				ipe.printStackTrace();
			}
			catch (Exception e)
			{	
				sb.append(e.getMessage()).append("\n");
				e.printStackTrace();
				mErrors++;
			}
		}
		
		String report = " Skipped " + mSkippedLines + "\n"
							+ "Rejected " + mRejectedLines + "\n"
							+ " Errors " + mErrors;
		
		return report + "\n" + sb.toString();
	}

	public AppContext getContext() {
		return conn;
	}
	
	public boolean parse(String record) throws DataFault, InvalidExpressionException, ParseException
	{

		// ignore lines beginning with #
		
	
		if ( record.trim().indexOf('#')!= -1)
		{
			mSkippedLines++;
			return false;
		}

		// look for a DN string denoted by enclosing double quotes
		// TODO - this is a bit clunky, can we use a regex for this?
		int indexOfStart = record.indexOf('\"');
		if ( indexOfStart == -1)
		{
			mRejectedLines++;
			return false;
		}

		int indexOfEnd = record.indexOf('\"', indexOfStart+1);
		if ( indexOfEnd == -1)
		{
			mRejectedLines++;
			return false;
		}

		String dn = record.substring(indexOfStart+1, indexOfEnd);
		String roleInfo = record.replace("\"" + dn + "\"", "").trim();
		
		// roleInfo can be a username, 
		// or a project name, as project-<projectname>
		// or a site role as site-<sitename>
		// or a supervisor role as site-all
	
		// project role
		if ( roleInfo.indexOf("project-")>-1)
		{
			String projectName = roleInfo.replace("project-","");
			updateProject(dn, projectName);
		} // site or site admin role 
		else if ( roleInfo.indexOf("site-")>-1)
		{
			String siteName = roleInfo.replace("site-","");
			updateSite(dn, siteName);
		}  
		else // assume we are just adding a username
		{
			updateUser(dn, roleInfo);
		}

		return true;
	}

}