/*******************************************************************************
 * (C) Copyright 2016 Dorian Cransac and Jerome Comte
 *  
 * This file is part of rtm
 *  
 * rtm is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * rtm is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License
 * along with rtm.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.rtm.commons;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * @author doriancransac
 *
 */

public class OrderedIdentifierTest{

	@BeforeClass
	public static void beforeClass() {
	}

	@AfterClass
	public static void afterClass(){
	}

	@Test
	public void basicTest(){
		Assert.assertEquals(-1, new OrderedIdentifier<>(2L).compareTo(new OrderedIdentifier<>(3L)));
		Assert.assertEquals(0, new OrderedIdentifier<>(2L).compareTo(new OrderedIdentifier<>(2L)));
		Assert.assertEquals(1, new OrderedIdentifier<>(2L).compareTo(new OrderedIdentifier<>(1L)));

		Assert.assertEquals(-1, new OrderedIdentifier<>("abc").compareTo(new OrderedIdentifier<>("abd")));
		Assert.assertEquals(0, new OrderedIdentifier<>("abc").compareTo(new OrderedIdentifier<>("abc")));
		Assert.assertEquals(1, new OrderedIdentifier<>("abc").compareTo(new OrderedIdentifier<>("abb")));
	}	
}