/**
 *   JGAAP -- Java Graphical Authorship Attribution Program
 *   Copyright (C) 2009 Patrick Juola
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation under version 3 of the License.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 **/
package com.jgaap.eventDrivers;

import com.jgaap.generics.Document;
import com.jgaap.generics.EventSet;


/**
 * Extract words with 3 or 4 letters as features
 * @author Patrick Juola
 * @since 4.1
 *
 */
/**
 * N.b use of _ to mark class name beginning with digit.
 */
public class _34LetterWordEventDriver extends MNLetterWordEventDriver {
  
    @Override
    public String displayName(){
    	return "3--4 letter Words";
    }
    
    @Override
    public String tooltipText(){
    	return "Words with 3 or 4 letters";
    }
    
    @Override
    public boolean showInGUI(){
    	return true;
    }

    private MNLetterWordEventDriver theDriver;

    @Override
    public EventSet createEventSet(Document ds) {
        theDriver = new MNLetterWordEventDriver();
        theDriver.setParameter("M", "3");
        theDriver.setParameter("N", "4");
        return theDriver.createEventSet(ds);
    }
}
