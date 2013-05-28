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
import java.io.FileReader;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Find unique ngrams in different files to anchor the boundaries of text chunks.
 * Text chunks limited by these boundaries are supposed to be similar, and then
 * good candidates for alignment.
 *
 * @author Federico Boschetti <federico.boschetti.73@gmail.com>
 */
public class AnchorFinder{
    private String[] fileNames=null;
    private String[] strs=null;
    private String regexToCleanWord=null;
    private BufferedReader br=null;
    private int ngramLen=3;
    private Vector<String>[] hapaxV=null;
    private Vector<Integer>[] posV=null;
    private Vector[] anchorV=null;
    private int offset=0;
    private int[] offsets=null;
    private int[][] anchors=null;
    private String[] anchorStrs=null;
    private boolean anchorsToDo=true;

    /**
     * Default Constructor.
     */
    public AnchorFinder(){}

    /**
     * Constructor that set the file names containing the text to anchor and
     * a regex to filter out characters non suitable for alignment (e.g. punctuation marks).
     *
     * @param fileNames the array with file names.
     * @param regexToCleanWord  the regex to filter out irrelevant characters.
     */
    public AnchorFinder(String[] fileNames, String regexToCleanWord){
        this.fileNames=fileNames;
        this.regexToCleanWord=regexToCleanWord;
    }

    /**
     * Constructor that receive two strings, a filtering regex and a boolean to
     * determine whether the strings  are file names or if they contain the text
     * to anchor.
     *
     * @param strs the strings (file names or text to anchor).
     * @param regexToCleanWord the regex to filter out the irrelevant characters.
     * @param isFile the boolean to determine if strings are file names or texts to anchor.
     */
    public AnchorFinder(String[] strs, String regexToCleanWord, boolean isFile){
        if(isFile){
            this.fileNames=strs;
        }else{
            this.strs=strs;
            this.regexToCleanWord=regexToCleanWord;
        }
    }

    /**
     * Set the array of file names
     *
     * @param fileNames the file names.
     */
    public void setFileNames(String[] fileNames){
        this.fileNames=fileNames;
    }

    /**
     * Set the array of strings with the text to anchor.
     *
     * @param strs the strings with the text to anchor.
     */
    public void setStrings(String[] strs){
        this.strs=strs;
    }

    /**
     * Get the array of file names.
     * @return the array of file names.
     */
    public String[] getFileNames(){
        return fileNames;
    }

    /**
     * Set the regex to filter out the irrelevant characters.
     * @param regexToCleanWord the regex to filter out the irrelevant characters.
     */
    public void setRegexToCleanWord(String regexToCleanWord){
        this.regexToCleanWord=regexToCleanWord;
    }

    /**
     * Get the regex to filter out the irrelevant characters.
     * @return the regex to filter out the irrelevant characters.
     */
    public String getRegexToCleanWord(){
        return this.regexToCleanWord;
    }

    /**
     * Set the length of the ngrams used to anchor the text chunks.
     * @param ngramLen the length of the ngrams used to anchor the text chunks.
     */
    public void setNgramLen(int ngramLen){
        this.ngramLen=ngramLen;
    }

    /**
     * Get the length of the ngrams used to anchor the text chunks.
     * @return the length of the ngrams used to anchor the text chunks.
     */
    public int getNgramLen(){
        return this.ngramLen;
    }

    /**
     * Find ngrams that occur just once.
     */
    public void findHapax(){
        int len=0;
        if(fileNames!=null){
            len=fileNames.length;
        }else{
            len=strs.length;
        }
        hapaxV=new Vector[len];
        posV=new Vector[len];
        Vector<String>[] nonHapax=new Vector[len];
        offsets=new int[len];
        int idx=0;
        for(int i=0;i<len;i++){
            hapaxV[i]=new Vector<String>();
            posV[i]=new Vector<Integer>();
            nonHapax[i]=new Vector<String>();
            try{
                if(fileNames!=null){
                    br=new BufferedReader(new FileReader(fileNames[i]));
                }else{
                    //!!!br=IoManager.getReaderFromString(strs[i]);
                    br=new BufferedReader(new FileReader(strs[i]));
                }
            }catch(Exception ex){
                Logger.getLogger(AnchorFinder.class.getName()).log(Level.SEVERE, null, ex);
            }
            String line;
            offset=0;
            int ln=0;
            try{while((line=br.readLine())!=null){
                ln++;
                for(int j=0;j<line.length()-ngramLen;j++){
                    String token=line.substring(j,j+ngramLen);
                    if(token.matches(regexToCleanWord)){
                        if(!hapaxV[i].contains(token)){
                            if(!nonHapax[i].contains(token)){
                                hapaxV[i].add(token);
                                posV[i].add(new Integer(offset+j));
                            }
                        }else{
                            idx=hapaxV[i].indexOf(token);
                            hapaxV[i].remove(idx);
                            posV[i].remove(idx);
                            nonHapax[i].add(token);
                        }
                    }
                }
                offset+=line.length()+1;
            }}catch(Exception ex){ex.printStackTrace();}
            offsets[i]=offset-1;
        }
        try{br.close();}catch(Exception ex){ex.printStackTrace();}
    }

    /**
     * Make the anchors.
     */
    private void makeAnchors(){
        findAnchors();
        int j=0;
        anchorStrs=new String[anchorV[0].size()];
        anchors=new int[anchorV.length-1][anchorV[1].size()];
        for(int i=0;i<anchorV.length;i++){
            j=0;
            if(i==0){
                for(String str:(Vector<String>)(anchorV[0])){
                    anchorStrs[j]=str;
                    j++;
                }
            }else{
                for(Integer valInt:(Vector<Integer>)(anchorV[i])){
                    anchors[i-1][j]=valInt.intValue();
                    j++;
                }
            }
        }
    }

    /**
     * Get the matrix with anchor positions.
     * First index is the file/text number,<br/>
     * second index is the next anchor position.
     *
     * @return the matrix with anchor positions.
     */
    public int[][] getAnchorPositions(){
        if(anchorsToDo){ 
            makeAnchors();
            anchorsToDo=false;
        }
        return anchors;
    }

    /**
     * Get the string array with anchors, i.e. the ngrams that occur just once
     * in all the texts.
     *
     * @return
     */
    public String[] getAnchors(){
        if(anchorsToDo){
            makeAnchors();
            anchorsToDo=false;
        }
        return anchorStrs;
    }


    /**
     * Find the anchors.
     * The first Vector is a String vector with the anchor ngrams.<br/>
     * The next Vectors are Integer vectors with the anchor positions.<br/>
     * The method is deprecated. Use <code>getAnchors</code> and <code>getAnchorPositions</code> instead.
     *
     * @return the vector array with anchors and anchor positions.
     */
    @Deprecated
    public Vector<Object>[] findAnchors(){
        findHapax();
        Integer[] poss=new Integer[hapaxV.length];
        if(hapaxV==null||hapaxV.length==1) return null;
        anchorV=new Vector[hapaxV.length+1];
        anchorV[0]=new Vector<String>();
        for(int i=1;i<hapaxV.length+1;i++){
            anchorV[i]=new Vector<Integer>();
        }
        boolean anchor=true;
        for(int i=0;i<hapaxV[0].size();i++){
            String token=hapaxV[0].get(i);
            anchor=true;
            poss[0]=posV[0].get(i);
            for(int j=1;j<hapaxV.length;j++){
                if(hapaxV[j].contains(token)){
                    poss[j]=posV[j].get(hapaxV[j].indexOf(token));
                }else{
                    anchor=false;
                    break;
                }
            }
            if(anchor){
                anchorV[0].add(token);
                for(int j=1;j<hapaxV.length+1;j++){
                    anchorV[j].add(poss[j-1]);
                }
            }
        }
        compactAnchors();
        addLastAnchor();
        return anchorV;
    }

    /**
     * Compact intersected ngrams in a single, longer ngram.
     */
    public void compactAnchors(){
        String anchorStr=null;
        String currNgram=null;
        boolean inAnchor=false;
        int diff=0;
        int totdiff=0; //cumulative difference
        int prevPos=-1000;
        int pos=0;
        for(int i=anchorV[0].size()-1;i>-1;i--){
            currNgram=(String)anchorV[0].get(i);
            pos=((Integer)anchorV[1].get(i)).intValue();
            prevPos=(i==0)?-1000:((Integer)anchorV[1].get(i-1)).intValue();
            diff=pos-prevPos;
            inAnchor=(diff<ngramLen);
            if(!inAnchor){
                totdiff=0;
                anchorStr=currNgram;
            }else{
                totdiff+=diff;
                anchorStr=(String)anchorV[0].get(i-1);
                anchorStr=anchorStr+currNgram.substring(currNgram.length()-totdiff);
                anchorV[0].set(i-1,anchorStr);
                anchorV[0].remove(i);
                for(int j=1;j<anchorV.length;j++){
                    anchorV[j].remove(i);
                }
            }
            for(int j=2;j<anchorV.length;j++){
                if(i>0&&i<anchorV[j].size()){
                    if(((Integer)anchorV[j].get(i)).intValue()<((Integer)anchorV[j].get(i-1)).intValue()){
                        for(int k=0;k<anchorV.length;k++) anchorV[k].remove(i);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Add the last anchor, always a dollar sign, to identify the end position.
     */
    public void addLastAnchor(){
        anchorV[0].add("$");
        for(int i=1;i<anchorV.length;i++){
            anchorV[i].add(offsets[i-1]);
        }
    }

}
