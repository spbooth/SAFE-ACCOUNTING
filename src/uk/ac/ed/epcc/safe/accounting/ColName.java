package uk.ac.ed.epcc.safe.accounting;

import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.content.Transform;
/** Simple parameter class for building tables from properties.
 * 
 * @author spb
 *
 */
public class ColName{
	private final PropExpression tag;
	private final String name;
	private final Transform transform;
	public ColName(PropExpression tag, String name){
		this(tag,name,null);
	}
	public ColName(PropExpression tag, String name,Transform transform){
		this.tag=tag;
		this.name=name;
		this.transform=transform;
	}
	public PropExpression getTag(){
		return tag;
	}
	public String getName(){
		if( name != null){
			return name;
		}
		if( tag instanceof PropertyTag){
			return ((PropertyTag)tag).getName();
		}
		return tag.toString();
	}
	public Transform getTransform(){
		return transform;
	}
}