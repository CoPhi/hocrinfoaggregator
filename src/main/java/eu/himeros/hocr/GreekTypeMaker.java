/*
 * This file is part of eu.himeros_hocraggregator_jar_1.0-SNAPSHOT
 *
 * Copyright (C) 2013 federico[DOT]boschetti[DOT]73[AT]gmail[DOT]com
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

package eu.himeros.hocr;

/**
 *
 * @author federico[DOT]boschetti[DOT]73[AT]gmail[DOT]com
 */
public class GreekTypeMaker implements TypeMaker{
    
    public GreekTypeMaker(){
    }
 
    @Override
    public Token.Type makeType(String token) {
        if (token.matches("[\\(]?[’\u0370-\u0386\u0388-\u03FF\u1F00-\u1FFF¬]+[¬.,;\u0387\\)]?")) {
            return Token.Type.L1;
        } else if (token.matches("[\\(]?[a-zA-Z¬]+[¬.,;\\)]?")) {
            return Token.Type.L2;
        } else if (token.matches("[0-9]+")) {
            return Token.Type.NUMBER;
        } else if (token.matches("[.,;:?!()\\[\\]\u0387'\"]")) {
            return Token.Type.PUNCTUATION;
        } else {
            return Token.Type.UNDEFINED;
        }
    }
}
