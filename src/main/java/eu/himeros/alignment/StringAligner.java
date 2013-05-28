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
 * Align strings character by character.
 * Inserts suitable gaps.
 *
 * @author Federico Boschetti <federico.boschetti.73@gmail.com>
 */
public class StringAligner extends Aligner {
    protected String aligned1 = null;
    protected String aligned2 = null;
    protected StringBuilder sb1 = new StringBuilder(1000);
    protected StringBuilder sb2 = new StringBuilder(1000);
    protected StringBuilder rSb1 = new StringBuilder(1000); // right sb (not reversed)
    protected StringBuilder rSb2 = new StringBuilder(1000); // right sb (not reversed)
    protected StringBuilder alignSb = new StringBuilder(1000); //alignment sb
    protected String[][] alignedStrings = null;
    protected char crChar='\u00B6';

    /**
     * Default Constructor.
     */
    public StringAligner() {}

    /**
     * Constructor that set the Similarity Evaluator.
     *
     * @param simEval the Similarity Evaluator.
     */
    public StringAligner(SimEvaluator simEval) {
        this.simEval = simEval;
    }

    /**
     * Align two strings.
     * @param strs1 the first string to align.
     * @param strs2 the second string to align.
     * @return the aligned string array (with the suitable gaps).
     */
    public String[] align(String strs1, String strs2) {
        sb1 = new StringBuilder(1000);
        sb2 = new StringBuilder(1000);
        //Similarity matrix
        double[][] simMatrix = new double[strs1.length()][strs2.length()];
        double[][] matrix = new double[strs1.length() + 1][strs2.length() + 1];
        for (int i = 0; i < strs1.length(); i++) {
            for (int j = 0; j < strs2.length(); j++) {
                simMatrix[i][j] = simScore(strs1.charAt(i), strs2.charAt(j));
            }
        }
        matrix[0][0] = 0;
        //Fill matrix marginals
        for (int i = 1; i <= strs1.length(); i++) {
            matrix[i][0] = i * gapPenalty;
        }
        for (int j = 1; j <= strs2.length(); j++) {
            matrix[0][j] = j * gapPenalty;
        }
        double scoreDown, scoreRight, scoreDiag, bestScore;
        //Fill matrix
        for (int i = 1; i <= strs1.length(); i++) {
            for (int j = 1; j <= strs2.length(); j++) {
                scoreDown = matrix[i - 1][j] + gapPenalty;
                scoreRight = matrix[i][j - 1] + gapPenalty;
                scoreDiag = matrix[i - 1][j - 1] + simMatrix[i - 1][j - 1];
                bestScore = Math.max(Math.max(scoreDown, scoreRight), scoreDiag);
                matrix[i][j] = bestScore;
            }
        }
        //Backtrack the path
        aligned1 = "";
        aligned2 = "";
        int i = strs1.length(), j = strs2.length();
        double score, scoreLeft, scoreDiagInv;
        while (i > 0 && j > 0) {
            score = matrix[i][j];
            scoreDiagInv = matrix[i - 1][j - 1];
            scoreLeft = matrix[i - 1][j];
            if (score == scoreDiagInv + simMatrix[i - 1][j - 1]) {
                makeAlignment(strs1.charAt(i - 1), strs2.charAt(j - 1));
                i = i - 1;
                j = j - 1;
            } else if (score == scoreLeft + gapPenalty) {
                makeAlignment(strs1.charAt(i - 1), gapChar);
                i = i - 1;
            } else {
                makeAlignment(gapChar, strs2.charAt(j - 1));
                j = j - 1;
            }
        }
        while (i > 0) {
            makeAlignment(strs1.charAt(i - 1), gapChar);
            i = i - 1;
        }
        while (j > 0) {
            makeAlignment(gapChar, strs2.charAt(j - 1));
            j = j - 1;
        }
        return makeResult();
    }

    /**
     * Evaluate the similarity score between characters, according to the Similarity Evaluator.
     * For strings, an UpperCaseSimEvaluator could be suitable.
     *
     * @param c1 the first character to compare.
     * @param c2 the second character to compare.
     * @return the similarity score.
     */
    protected double simScore(char c1, char c2) {
        return simEval.eval(c1, c2);
    }

    /**
     * Perform the alignment of characters (inserting suitable gaps).
     * @param c1 the first character.
     * @param c2 the second character.
     */
    private void makeAlignment(char c1, char c2) {
        sb1.append(c1);
        sb2.append(c2);
    }

    /**
     * Put the alignment result in a string array with three strings:<br/>
     * 0. the first aligned string<br/>
     * 1. the second aligned string<br/>
     * 2. the alignment mask, with the following characters:<br/>
     * <code>|</code> for alignment<br/>
     * <code>~</code> for approximate errors (i.e. errors with a similarity score greater than 0.0)<br/>
     * <code>-</code> for deletions<br/>
     * <code>+</code> for insertions<br/>
     * <code>#</code> for substitutions.
     *
     * @return the alignment result string array.
     */
    private String[] makeResult() {
        char c1, c2;
        double simSc; // similarity score
        errorSum = 0;
        approxSum = 0;
        insSum = 0;
        delSum = 0;
        subSum = 0;
        matchSum = 0;
        String[] res = new String[3];
        rSb1 = new StringBuilder(1000);
        rSb2 = new StringBuilder(1000);
        alignSb = new StringBuilder(1000);
        for (int i = 0; i < sb1.length(); i++) {
            c1 = sb1.charAt(sb1.length() - 1 - i);
            c2 = sb2.charAt(sb2.length() - 1 - i);
            rSb1.append(c1);
            rSb2.append(c2);
            simSc = simScore(c1, c2);
            if (simSc == 1) {
                alignSb.append('|');
                matchSum++;
            } else if (simSc == 0.5 && c1 != gapChar && c2 != gapChar) {
                alignSb.append('~');
                approxSum++;
                subSum++;
            } else if (c1 == gapChar) {
                alignSb.append('-');
                errorSum++;
                delSum++;
            } else if (c2 == gapChar) {
                alignSb.append('+');
                errorSum++;
                insSum++;
            } else {
                alignSb.append('#');
                errorSum++;
                subSum++;
            }
        }
        res[0] = rSb1.toString();
        res[1] = rSb2.toString();
        res[2] = alignSb.toString();
        return res;
    }

    /**
     * Get the aligned strings.
     * @return the aligned strings.
     */
    public String[] getAlignedStrings() {
        String st1;
        String st2;
        String[] res = new String[2];
        for (int k = 0; k < alignedStrings[0].length; k++) {
            st1 = alignedStrings[0][k];
            st2 = alignedStrings[1][k];
            if (st1 == null) {
                st1 = AlignFormatter.getGapTag();
            }
            if (st2 == null) {
                st2 = AlignFormatter.getGapTag();
            }
            AlignFormatter.alignStrings(st1, st2);
            aligned1 = AlignFormatter.getAlignedS1() + AlignFormatter.getBlankTag() + aligned1;
            aligned2 = AlignFormatter.getAlignedS2() + AlignFormatter.getBlankTag() + aligned2;
        }
        res[0] = aligned1;
        res[1] = aligned2;
        return res;
    }

    /**
     * Adjust gaps for multiple alignments.
     *
     * @param str1 the string to be adjusted.
     * @param str2 the string with the gaps for adjustment.
     * @return the adjusted string.
     */
    public String adjustGap(String str1, String str2) {
        StringBuilder sb = new StringBuilder(str1);
        for (int i = 0; i < str2.length(); i++) {
            if (str2.charAt(i) == gapChar) {
                sb.insert(i, gapChar);
            }
        }
        return sb.toString();
    }

    /**
     * Get a single string with marked insertions, deletions (and substitutions).
     * @return the mixed string with philological diacritical signs for insertions and deletions.
     */
    public String getMixedString() {
        StringBuilder sb = new StringBuilder();
        StringBuilder chunkSb1;
        StringBuilder chunkSb2;
        String charStr;
        char c1;
        char c2;
        int lastButOne = rSb1.length() - 1;
        for (int i = 0; i < rSb1.length(); i++) {
            chunkSb1 = new StringBuilder();
            chunkSb2 = new StringBuilder();
            while (true) {
                c1 = rSb1.charAt(i);
                if (c1 == gapChar) {
                    charStr = "";
                }
                else {
                    charStr = "" + c1;
                }
                chunkSb1.append(charStr);
                c2 = rSb2.charAt(i);
                if (c2 == gapChar) {
                    charStr = "";
                }else {
                    charStr = "" + c2;
                }
                chunkSb2.append(charStr);
                if (c1 == c2 || i == lastButOne || rSb1.charAt(i + 1) == rSb2.charAt(i + 1)) {
                    break;
                }
                i++;
            }
            if (chunkSb1.toString().equals(chunkSb2.toString())) {
                sb.append(chunkSb1);
            } else {
                if (chunkSb1.length() > 0) {
                    chunkSb1.insert(0, "<span class=\"del\">{");
                    chunkSb1.append("}</span>");
                }
                if (chunkSb2.length() > 0) {
                    chunkSb2.insert(0, "<span class=\"add\">&lt;");
                    chunkSb2.append("&gt;</span>");
                }
                sb.append(chunkSb1).append(chunkSb2);
            }
        }
        return sb.toString().replaceAll(""+crChar, "<span class=\"par\">"+crChar+"</span>\n");
    }

    public char getCrChar() {
        return crChar;
    }

    public void setCrChar(char crChar) {
        this.crChar = crChar;
    }

}
