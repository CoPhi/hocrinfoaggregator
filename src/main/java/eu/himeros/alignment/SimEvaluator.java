/*
 * Copyright © 2009 Perseus Project - Tufts University <http://www.perseus.tufts.edu>
 *
 * This file is part of UniCollatorPerseus.
 *
 * AlignmentPerseus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * AlignmentPerseus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with AlignmentPerseus.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.himeros.alignment;

/**
 * Superclass for the Similarity Evaluators.
 * Provide default evaluation criteria, scoring 1.0 for equality and 0.0 for inequality.
 *
 * @author Federico Boschetti <federico.boschetti.73@gmail.com>
 */
public class SimEvaluator {

    /**
     * Default Constructor.
     */
    public SimEvaluator(){}

    /**
     * Evaluate the similarity of two objects:<br/>
     * 0.0 if they are different;<br/>
     * 1.0 if they are equal.
     *
     * @param obj1 the first object to compare.
     * @param obj2 the second object to compare.
     * @return the similarity score.
     */
    public double eval(Object obj1, Object obj2){
        if((obj1==null&&obj2==null)||obj1!=null&&obj2!=null&&obj1.equals(obj2)) return 1.0; else return 0.0;
    }

    /**
     * Evaluate the similarity of two Strings:<br/>
     * 0.0 if they are different;<br/>
     * 1.0 if they are equal.
     *
     * @param str1 the first String to compare.
     * @param str2 the second String to compare.
     * @return the similarity score.
     */
    public double eval(String str1, String str2){
        if((str1==null&&str2==null)||str1!=null&&str2!=null&&str1.equals(str2)) return 1.0; else return 0.0;
    }

    /**
     * Evaluate the similarity of two characters:<br/>
     * 0.0 if they are different;<br/>
     * 1.0 if they are equal.
     *
     * @param c1 the first character to compare.
     * @param c2 the second character to compare.
     * @return the similarity score.
     */
    public double eval(char c1, char c2){
        if(c1==c2) return 1.0; else return 0.0;
    }

    /**
     * Evaluate the similarity of two integers:<br/>
     * 0.0 if they are different;<br/>
     * 1.0 if they are equal.
     *
     * @param i1 the first integer to compare.
     * @param i2 the second integer to compare.
     * @return the similarity score.
     */
    public double eval(int i1, int i2){
        if(i1==i2) return 1.0; else return 0.0;
    }

    /**
     * Evaluate the similarity of two doubles:<br/>
     * 0.0 if they are different;<br/>
     * 1.0 if they are equal.
     *
     * @param d1 the first double to compare.
     * @param d2 the second double to compare.
     * @return the similarity score.
     */
    public double eval(double d1, double d2){
        if(d1==d2) return 1.0; else return 0.0;
    }

}
