/*
 * This file is part of eu.himeros_hocraggregator_jar_1.0-SNAPSHOT
 *
 * Copyright (C) 2012 federico[DOT]boschetti[DOT]73[AT]gmail[DOT]com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.himeros.alignment;

import org.jdom2.Element;

/**
 *
 * @author federico[DOT]boschetti[DOT]73[AT]gmail[DOT]com
 */
public class HocrSimEvaluator extends SimEvaluator{
    
    public HocrSimEvaluator(){
        super();
    }
    
    @Override
    public double eval(Object obj1,Object obj2){
        Element el1=(Element)obj1;
        Element el2=(Element)obj2;
        String uc1=el1.getAttributeValue("uc");
        String uc2=el2.getAttributeValue("uc");
        if((uc1==null&&uc2==null)||(uc1!=null&&uc1.equals(uc2))) return 1;      
        return 0.0;
    }
}
