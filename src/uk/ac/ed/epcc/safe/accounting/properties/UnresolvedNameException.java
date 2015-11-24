package uk.ac.ed.epcc.safe.accounting.properties;


public class UnresolvedNameException extends InvalidPropertyException {
	private final String name;
	private final PropertyFinder finder;
	public UnresolvedNameException(String name,PropertyFinder finder){
		super("Unresolved name "+name+(finder != null ? " from "+finder.toString():""));
		this.name=name;
		this.finder=finder;
	}
	public String getUnresolvedName(){
		return name;
	}
	/** Get the PropertyFinder in scope when the name failed to parse.
	 * 
	 * @return PropertyFinder or null;
	 */
	public PropertyFinder getFinder(){
		return finder;
	}
}
