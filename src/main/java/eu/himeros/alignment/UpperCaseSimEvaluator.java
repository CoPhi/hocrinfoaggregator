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

import eu.himeros.transcoder.Transcoder;
import java.io.File;
import java.io.InputStream;

/**
 * Similarity Evaluator that scores 0.5 if characters have the same upper case representation.
 * Similarity scores:<br/>
 * 1.0 in case of equality;<br/>
 * 0.5 in case of same upper case representation;<br/>
 * 0.0 in case of inequality.
 *
 * @author Federico Boschetti <federico.boschetti.73@gmail.com>
 */
public class UpperCaseSimEvaluator extends SimEvaluator{
    public static String resourceName=System.getProperty("eu.himeros.alignment.evaluator");
    public static Transcoder trans=null;

    /**
     * Default Constructor.
     */
    public UpperCaseSimEvaluator(){
        if(resourceName!=null&&(new File(resourceName)).exists()){  //if the resource is a file with a canonical path
            trans=new Transcoder(resourceName);
        }else if(resourceName!=null){ //if the resource is contained in the .jar
            trans=new Transcoder(ClassLoader.getSystemResourceAsStream(resourceName));
        }
    }

    /**
     * Constructor that set a transcoder for upper case similarity evaluation.
     *
     * @param fileName
     */
    public UpperCaseSimEvaluator(String fileName){
        trans=new Transcoder(fileName);
    }

    public UpperCaseSimEvaluator(InputStream is){
        trans=new Transcoder(is);
    }

    /**
     * Evaluate the similarity.
     * @param c1 the first character to be compared.
     * @param c2 the second character to be compared.
     * @return the similarity score.
     */
    @Override
    public double eval(char c1, char c2){
        if(c1==c2) return 1.0;
        String str1=trans.decode(c1);
        String str2=trans.decode(c2);
        if(str1!=null&&str2!=null&&str1.equals(str2)) return 0.5; else return 0.0;
    }
    
    @Override
    public double eval(String str1, String str2){
        char[] str1Ar=str1.toCharArray();
        char[] str2Ar=str2.toCharArray();
        double sum=0;
        for(int i=0;i<str1Ar.length;i++){
            sum+=eval(str1Ar[i],str2Ar[i]);
        }
        return sum/(double)str1Ar.length;
    }
    
    public static void setResourceName(String resourceName){
        UpperCaseSimEvaluator.resourceName=resourceName;
                if((new File(resourceName)).exists()){  //if the resource is a file with a canonical path
            trans=new Transcoder(resourceName);
        }else{ //if the resource is contained in the .jar
            trans=new Transcoder(ClassLoader.getSystemResourceAsStream(resourceName));
        }
    }
    
    public static String getResourceName(){
        return resourceName;
    }

}
