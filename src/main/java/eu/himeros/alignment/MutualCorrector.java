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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Perform mutual correction of aligned texts.
 *
 * @author Federico Boschetti <federico.boschetti.73@gmail.com>
 */
public class MutualCorrector {
    private HashMap[] errorPatternHts=null;
    private BufferedReader br=null;
    private int ngramMaxLen=0;
    private double pMax=0.0; // max probability

    /**
     * Default Constructor
     */
    public MutualCorrector(){}

    /**
     * Constructor that set the Vector of error pattern file names.
     */
    public MutualCorrector(ArrayList<String> errorPatternFileNames){
        int ngl;
        String line;
        String[] lineItems;
        String fileName;
        errorPatternHts=new HashMap[errorPatternFileNames.size()];
        // BEG: load errorPattern probabilities from files
        for(int i=0;i<errorPatternFileNames.size();i++){
            fileName=errorPatternFileNames.get(i);
            errorPatternHts[i]=new HashMap<String,Double>();
            try {
                br=new BufferedReader(new FileReader(fileName));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MutualCorrector.class.getName()).log(Level.SEVERE, null, ex);
            }
            try{
                while((line=br.readLine())!=null){
                    lineItems=line.split("\t");
                    try{
                        //ngl=Integer.parseInt(lineItems[0]);
                        ngl=lineItems[0].length();
                    }catch(Exception ex){ngl=0;}
                    if(ngl>ngramMaxLen) ngramMaxLen=ngl;
                    errorPatternHts[i].put(ngl+"\t"+lineItems[0]+"\t"+lineItems[1],Double.valueOf(lineItems[2]));
                }
            }catch(Exception ex){ex.printStackTrace(System.err);}
        }
        // END: load errorPattern probabilities from files
    }

    /**
     * Merge the aligned strings in a single, mutually corrected string.
     * The selection of the best character depends by the probability evaluation
     * of error patterns.
     *
     * @param inStrV the vector of aligned strings.
     * @return the merged string.
     */
    public String merge(ArrayList<String> inStrV){
        StringBuilder outSb=new StringBuilder();
        String bestChunk;
        int j;
        for(int i=0;i<inStrV.get(0).length();i++){
            j=getRightMisalignedBound(inStrV,i);
            if(i>=j){
                outSb.append(inStrV.get(0).charAt(i));
            }else{
                bestChunk=getBestChunk(inStrV,i,j);
                outSb.append(bestChunk);
                i=i+bestChunk.length()-1;
            }
        }
        return outSb.toString();
    }

    /**
     * Get the best chunk, according to the probability of error patterns.
     *
     * @param inStrV the vector of aligned strings.
     * @param i left iterator.
     * @param j right iterator.
     * @return the best chunk.
     */
    private String getBestChunk(ArrayList<String> inStrV, int i,int j){
        String res=getBestNgram(inStrV,i,j);
        String partialRes;
        double pNgramMax=0.0;
        for(int k=j-1;k>i;k--){
            partialRes=getBestNgram(inStrV,i,k);
            if(pMax>pNgramMax){
                pNgramMax=pMax;
                res=partialRes;
            }
        }
        return res;
    }

    /**
     * Get the best ngram, according to error pattern probabilities.
     *
     * @param inStrV the vector of aligned strings.
     * @param i left iterator.
     * @param j right iterator.
     * @return the best ngram.
     */
    private String getBestNgram(ArrayList<String> inStrV, int i,int j){
        String res;
        double p;
        pMax=0.0;
        Double pD;
        int idx;
        double[] ps=new double[inStrV.size()]; // probability matrix
        String kCk;
        String hCk;
        ArrayList<String> chunkV=new ArrayList<String>();
        //populate chunkV with mismatching engrams
        for(int k=0;k<inStrV.size();k++){
            chunkV.add(inStrV.get(k).substring(i,j));
        }
        //search for errorPattern and calculate probabilities
        for(int k=0;k<chunkV.size();k++){
            ps[k]=1.0;
            kCk=chunkV.get(k);
            for(int h=0;h<chunkV.size();h++){
                hCk=chunkV.get(h);
                pD=(Double)errorPatternHts[h].get(""+kCk.length()+"\t"+kCk+"\t"+hCk);
                if(pD==null){
                    p=(kCk.equals(hCk))?1.0:0.0;
                }else{
                    p=pD.doubleValue();
                }
                ps[k]*=p;
            }
        }
        idx=0;
        pMax=0.0;
        for(int k=0;k<inStrV.size();k++){
            if(ps[k]>pMax){
                idx=k;
                pMax=ps[k];
            }
        }
        res=chunkV.get(idx);
        return res;
    }

    /**
     * Get the right bound of the chunk, moving on the right while the chunks are misaligned.
     *
     * @param inStrV the vector of aligned strings.
     * @param i
     * @return
     */
    private int getRightMisalignedBound(ArrayList<String> inStrV,int i){
        int ngram=ngramMaxLen;
        int ng=0;
        int k=i;
        boolean eq;
            for(;i<inStrV.get(0).length();i++){
                eq=true;
                for(int j=1;j<inStrV.size();j++){
                    if(inStrV.get(0).charAt(i)!=inStrV.get(j).charAt(i)) eq=false;
                }
                ng++;
                if(eq||ng>ngram) break;
            }
            return i;
    }

}
