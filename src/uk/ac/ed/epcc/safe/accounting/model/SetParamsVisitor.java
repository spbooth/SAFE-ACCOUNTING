package uk.ac.ed.epcc.safe.accounting.model;

import java.util.Iterator;
import java.util.Map;

import uk.ac.ed.epcc.webapp.forms.exceptions.ParseException;
import uk.ac.ed.epcc.webapp.forms.inputs.BinaryInput;
import uk.ac.ed.epcc.webapp.forms.inputs.FileInput;
import uk.ac.ed.epcc.webapp.forms.inputs.Input;
import uk.ac.ed.epcc.webapp.forms.inputs.InputVisitor;
import uk.ac.ed.epcc.webapp.forms.inputs.LengthInput;
import uk.ac.ed.epcc.webapp.forms.inputs.ListInput;
import uk.ac.ed.epcc.webapp.forms.inputs.LockedInput;
import uk.ac.ed.epcc.webapp.forms.inputs.MultiInput;
import uk.ac.ed.epcc.webapp.forms.inputs.OptionalInput;
import uk.ac.ed.epcc.webapp.forms.inputs.ParseInput;
import uk.ac.ed.epcc.webapp.forms.inputs.ParseMultiInput;
import uk.ac.ed.epcc.webapp.forms.inputs.PasswordInput;
import uk.ac.ed.epcc.webapp.forms.inputs.UnmodifiableInput;

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
        return visitBaseInput(checkBoxInput);
    }
    @Override
    public <V, I extends Input> Object visitParseMultiInput(ParseMultiInput<V, I> multiInput) throws Exception {
        if(set_map){
            params.putAll(multiInput.getMap());
        }else{
            if (multiInput.parse(params)) {
                missing = true;
            }
        }
        return null;
    }
    @Override
    public <V, I extends Input> Object visitMultiInput(MultiInput<V, I> multiInput) throws Exception {
        if( multiInput instanceof ParseInput){
            // input will accept a single param
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
            visitBaseInput(listInput);
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
        if (set_map) {
            if (input instanceof LockedInput<?>) {
                return visitBaseInput((LockedInput<?>)input);
            }
        }
        return null;
    }
    @Override
    public Object visitFileInput(FileInput input) throws Exception {
        return null;
    }
    @Override
    public Object visitPasswordInput(PasswordInput input) throws Exception {
        return visitBaseInput(input);
    }

    private <T> Object visitBaseInput(Input<T> input) throws ParseException{
        if( set_map){
            T value = input.getValue();
            if( value != null ){
                params.put(input.getKey(),input.getString(value));
            }
        }else{
            String s = (String) params.get(input.getKey());
            if( s != null ){
                if( input instanceof ParseInput){
                    ((ParseInput<T>)input).parse(s);
                }else{
                    input.setValue(input.convert(s));
                }
            }
            else if (!(input instanceof OptionalInput)) {
                missing = true;
            }
        }
        return null;
    }

    public boolean getMissing() {
        return missing;
    }

}
