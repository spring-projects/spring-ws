package org.springframework.webflow;

import junit.framework.TestCase;

import org.springframework.webflow.test.MockRequestContext;

public class NullViewSelectionTests extends TestCase {

	private MockRequestContext context = new MockRequestContext();
	
	public void testMakeSelection() {
		assertEquals(ViewSelection.NULL_VIEW, NullViewSelector.INSTANCE.makeSelection(context));
	}

	public void testMakeRefreshSelection() {
		assertEquals(ViewSelection.NULL_VIEW, NullViewSelector.INSTANCE.makeRefreshSelection(context));
	}
}
