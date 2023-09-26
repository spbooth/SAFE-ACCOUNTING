package uk.ac.ed.epcc.safe.accounting.model;

import java.util.Iterator;
import java.util.Map;

import uk.ac.ed.epcc.webapp.forms.exceptions.ParseException;
import uk.ac.ed.epcc.webapp.forms.inputs.*;

/** An {@link InputVisitor} that adds/sets the form contents to a map.
 *
 * @author spb
 *
 */
public class SetParamsVisitor implements InputVisitor<Object> {

    private Map<String,Object> params;
    private final boolean set_map;
    private boolean missing = false;

    public SetParamsVisitor(boolean set_map,Map<String,Object> params) {
        this.set_map=set_map;
        this.params=params;
    }
    @Override
    public Object visitBinaryInput(BinaryInput checkBoxInput) throws Exception {
        if( set_map ) {
        	params.put(checkBoxInput.getKey(), checkBoxInput.isChecked() ? "true" : "false");
        }else {
        	String val = (String) params.get(checkBoxInput.getKey());
        	if( val != null && ! val.isEmpty()) {
        		checkBoxInput.setChecked(Boolean.parseBoolean(val));
        	}
        }
        return null;
    }
    @Override
    public <V, I extends Input> Object visitParseMultiInput(ParseMultiInput<V, I> multiInput) throws Exception {
    	// use single value parse
        return visitBaseInput(multiInput);
    }
    @Override
    public <V, I extends Input> Object visitMultiInput(MultiInput<V, I> multiInput) throws Exception {
        if( multiInput instanceof ParseInput){
            // input will accept a single param (this should really be accepting ParseMultiUnput
            return visitBaseInput(multiInput);
        }
        for(Iterator<I> it = multiInput.getInputs(); it.hasNext();){
            I i = it.next();
            i.accept(this);
        }
        return null;
    }
    @Override
    public <V, T> Object visitListInput(ListInput<V, T> listInput) throws Exception {
        if( set_map){
            V value = listInput.getValue();
            if( value != null ){
                params.put(listInput.getKey(), listInput.getTagByValue(value));
            }
        }else{
        	Object val = params.get(listInput.getKey());
        	if( val == null ) {
        		missing=true;
        	}else if( val instanceof String) {
        		listInput.setValue(listInput.getValueByTag((String) val));
        	}else {
        		listInput.setValue(listInput.convert(val));
        	}
        }
        return null;
    }
    @Override
    public <V, T> Object visitRadioButtonInput(ListInput<V, T> listInput) throws Exception {
        return visitListInput(listInput);
    }
    @Override
    public Object visitLengthInput(LengthInput input) throws Exception {
        return visitBaseInput(input);
    }
    @Override
    public Object visitUnmodifyableInput(UnmodifiableInput input) throws Exception {
        return null;
    }
    @Override
	public Object visitLockedInput(LockedInput l) throws Exception {
		return l.getNested().accept(this);
	}
	@Override
    public Object visitFileInput(FileInput input) throws Exception {
        return null;
    }
    @Override
    public Object visitPasswordInput(PasswordInput input) throws Exception {
        return visitBaseInput(input);
    }

    private <T> Object visitBaseInput(Input<T> input) throws ParseException, TypeException{
        if( set_map){
            T value = input.getValue();
            if( value != null ){
                params.put(input.getKey(),input.getString(value));
            }
        }else{
            Object o = params.get(input.getKey());
            if( o instanceof String) {
            	String s = (String) o;
            	if( s != null ){
            		if( input instanceof ParseInput){
            			((ParseInput<T>)input).parse(s);
            		}else{
						input.setValue(input.convert(s));
            		}
            	}
            	else  {
            		missing = true;
            	}
            }else {
            	// This should cover item inputs
            	T x = input.convert(o);
            	if( x != null ) {
            		input.setValue(x);
            	}else  {
            		missing = true;
            	}
            }
        }
        return null;
    }
    /** Were any mandatory inputs missing when setting from the map;
     * 
     * @return
     */
    public boolean getMissing() {
        return missing;
    }
    public void setMissing(boolean v) {
    	missing=v;
    }

}
