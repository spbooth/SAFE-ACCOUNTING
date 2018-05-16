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
package uk.ac.ed.epcc.safe.accounting.db;

import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.model.Classification;
import uk.ac.ed.epcc.webapp.model.ClassificationFactory;
/** A {@link ClassificationFactory} with property support.
 * 
 * Other than installing a standard {@link PropertyRegistry} this just adds the necessary {@link ExpressionTargetFactoryComposite}.
 * 
 * @author spb
 *
 * @param <T>
 */
public class PropertyClassificationFactory<T extends Classification> extends
		ClassificationFactory<T> implements AccessorContributer<T> {
	
	public static final PropertyRegistry classification = new PropertyRegistry("classification", "Standard properties for a Classification table");
	public static final PropertyTag<String> NAME_PROP = new PropertyTag<String>(classification,Classification.NAME,String.class);
	public static final PropertyTag<String> DESCRIPTION_PROP = new PropertyTag<String>(classification,Classification.DESCRIPTION,String.class);
	static{
		classification.lock();
	}
	@Override
	public void customAccessors(AccessorMap<T> mapi2, MultiFinder finder, PropExpressionMap derived) {
		finder.addFinder(classification);
		
	}
	private ExpressionTargetFactoryComposite<T> expression_comp = new ExpressionTargetFactoryComposite<>(this);
	public PropertyClassificationFactory(AppContext ctx, String homeTable) {
		super();
		setContext(ctx, homeTable);
	}
	protected PropertyClassificationFactory() {
		super();
	}
}