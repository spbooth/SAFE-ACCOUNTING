// Copyright - The University of Edinburgh 2015
/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.charts.Power2Labeller;
import uk.ac.ed.epcc.webapp.WebappTestBase;


public class Power2LabellerTest extends WebappTestBase{

	
	@Test
	public void testLabeller(){
		Power2Labeller<Number> lab = new Power2Labeller<Number>();
		
		assertEquals(Integer.valueOf(1), lab.getLabel(ctx, 1));
		assertEquals(Integer.valueOf(2), lab.getLabel(ctx, 2));
		assertEquals(Integer.valueOf(4), lab.getLabel(ctx, 3));
		assertEquals(Integer.valueOf(4), lab.getLabel(ctx, 4));
		assertEquals(Integer.valueOf(8), lab.getLabel(ctx, 5));
		assertEquals(Integer.valueOf(8), lab.getLabel(ctx, 7));
		assertEquals(Integer.valueOf(8), lab.getLabel(ctx, 8));
		assertEquals(Integer.valueOf(32), lab.getLabel(ctx, 24));
		assertEquals(Integer.valueOf(64), lab.getLabel(ctx, 48));
	}
}