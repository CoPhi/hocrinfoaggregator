/*
 * This file is part of eu.himeros_ocrevaluator_jar_0.1-SNAPSHOT
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

package eu.himeros.hocr;

/**
 * Token associated to classification and score
 * The type of the token is determined by its character set (for instance L1 could be associated to the Greek and Greek extended unicode sets 
 * in the init.xml properties file, L2 to the English character set, etc.)
 * The classification is the result of linguistic analysis, in order to determine if the token is an attested word, a syllabic sequence, etc.
 * 
 * @author federico[DOT]boschetti[DOT]73[AT]gmail[DOT]com
 */ 
public class Token {
    public enum Type{UNDEFINED,L1,L2,L3,L4,L5,L6,L7,L8,L9,L10,L11,L12,L13,L14,L15,L16,NUMBER,PUNCTUATION,PUNCTUATION1,PUNCTUATION2}
    public enum Classification{UNDEFINED,WORD,L1WORD,L2WORD,UCWORD,L1UCWORD,L2UCWORD,SYLLABICSEQ,L1SYLLABICSEQ,L2SYLLABICSEQ, CHARSEQ, L1CHARSEQ,L2CHARSEQ,BADMANY,BADONE}
    private String text;
    private String wholeText;
    private Type type;
    private int part;
    private int tot;
    private boolean hyphenated;
    private int lineNum;
    private Classification classification;
    private double score;
    
    /**
     * Constructor initialized with the tokenized chunk of text
     * @param text tokenized chunk of text
     */
    public Token(String text){
        this(text,Type.UNDEFINED,false,0);
    }
    
    /**
     * Constructor initialized with the chunk of text, type, hyphenation test and line number 
     * @param text tokenized chunk of text
     * @param type type of the chunk
     * @param hyphenated boolean
     * @param lineNum line number
     */
    public Token(String text, Type type, boolean hyphenated, int lineNum){
        this.text=text;
        this.type=type;
        this.hyphenated=hyphenated;
        part=1;
        tot=1;
        this.lineNum=lineNum;
    }
    
    /**
     * Constructor (full setting)
     * @param text Text of the token
     * @param wholeWord Whole word the token belongs to, in case of hyphenation
     * @param part Part number, in case of hyphenation
     * @param tot Total number of parts, in case of hyphenation
     * @param hyphenated Whether it is hyphenated or not
     * @param lineNum Number of the line the token belongs to
     */
    public Token(String text, String wholeWord, int part, int tot,boolean hyphenated,int lineNum){
        this.text=text;
        this.wholeText=wholeWord;
        this.part=part;
        this.tot=tot;
        this.hyphenated=hyphenated;
        this.lineNum=lineNum;
    }    
    
    /**
     * Returns true if the token is a single word or the first part of a hyphenated word
     * @return 
     */
    public boolean isLeader(){
        if(part==1) return true;
        else return false;
    }
    
    /**
     * Test if the chunk is hyphenated
     * @return 
     */
    public boolean isHyphenated(){
        return hyphenated;
    }
    
    /**
     * Set the boolean parameter
     * @param hyphenated 
     */
    public void setHyphenated(boolean hyphenated){
        this.hyphenated=hyphenated;
    }

    /**
     * Get the part number (useful if the word is hyphenated)
     * @return 
     */
    public int getPart() {
        return part;
    }

    /**
     * Set the part number (useful if the word is hyphenated) 
     * @param part 
     */
    public void setPart(int part) {
        this.part = part;
    }

    /**
     * Get the chunk text
     * @return 
     */
    public String getText() {
        return text;
    }

    /**
     * Set the chunk text 
     * @param text chunk text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Get the total number of parts
     * @return 
     */
    public int getTot() {
        return tot;
    }

    /**
     * Set the total number of parts 
     * @param tot 
     */
    public void setTot(int tot) {
        this.tot = tot;
    }

    /**
     * Get the whole text of the chunk 
     * (different from getText if the word is hyphenated)
     * @return 
     */
    public String getWholeWord() {
        return wholeText;
    }

    /**
     * Set the whole text of the chunk 
     * (different from getText if the word is hyphenated)
     * @param wholeWord 
     */
    public void setWholeWord(String wholeWord) {
        this.wholeText = wholeWord;
    }

    /**
     * Get the type of the chunk
     * @return 
     */
    public Type getType() {
        return type;
    }

    /**
     * Set the type of the chunk
     * @param type 
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Get the line number
     * @return 
     */
    public int getLineNum() {
        return lineNum;
    }

    /**
     * Set the line number
     * @param lineNum 
     */
    public void setLineNum(int lineNum) {
        this.lineNum = lineNum;
    }

    /**
     * Get the classification of the chunk
     * @return 
     */
    public Classification getClassification() {
        return classification;
    }

    /**
     * Set the classification of the chunk
     * @param classification 
     */
    public void setClassification(Classification classification) {
        this.classification = classification;
    }

    /**
     * Get the score, set according to the classification and the scoring policies 
     * @return 
     */
    public double getScore() {
        return score;
    }

    /**
     * Set the score, according to the classification and the scoring policies
     * @param score 
     */
    public void setScore(double score) {
        this.score = score;
    }
    
    /**
     * Get the length of the chunk text 
     * @return 
     */
    public int getLength(){
        return text.length();
    }
    
    /**
     * Get the length of the chunk as a double
     * @return 
     */
    public double getLengthAsDouble(){
        return (double)text.length();
    }
}
