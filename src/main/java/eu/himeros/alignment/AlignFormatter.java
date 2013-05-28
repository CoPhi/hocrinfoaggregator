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
 * Format gap and blank tags to use in alignments.
 * Equalize string length adding blank tags to the end of the shorter line.
 *
 * @author Federico Boschetti <federico.boschetti.73@gmail.com>
 */
public class AlignFormatter {
    private static String s1;
    private static String s2;
    private static String blankTag=" "; //default blankTag
    private static String gapTag="-"; //default gapTag

    /**
     * Set the blank tag.
     *
     * @param bt the blank tag.
     */
    public static void setBlankTag(String bt){
        blankTag=bt;
    }

    /**
     * Get the blank tag (default is space).
     * @return the blank tag.
     */
    public static String getBlankTag(){
        return blankTag;
    }

    /**
     * Set the gap tag.
     *
     * @param gt the gap tag.
     */
    public static void setGapTag(String gt){
        gapTag=gt;
    }

    /**
     * Get the gap tag (default is -).
     * @return the gap tag.
     */
    public static String getGapTag(){
        return gapTag;
    }

    /**
     * After the application of the <code>alignStrings</code> method,
     * it provides the processed second string.
     *
     * @return the first string.
     */
    public static String getAlignedS1(){
        return s1;
    }

    /**
     * After the application of the <code>alignStrings</code> method,
     * it provides the processed second string.
     *
     * @return the second string.
     */
    public static String getAlignedS2(){
        return s2;
    }

    /**
     * Add blank tags (usually spaces) at the end of the shorter line.
     * The same length strings are provided by <code>getAlignedS1</code>
     * and <code>getAlignedS2</code> methods.
     *
     * @param str1 first string
     * @param str2 second string
     */
    public static void alignStrings(String str1, String str2){
        s1=str1;
        s2=str2;
        String blankSpaces="";
        int len1=s1.length();
        int len2=s2.length();
        if(len1==len2) return;
        int i=((len1>len2)?len1-len2:len2-len1);
        while(i>0){
            blankSpaces+=blankTag;
            i--;
        }
        if(len1<len2){
            s1=s1+blankSpaces;
        }else{
            s2=s2+blankSpaces;
        }
    }
}
