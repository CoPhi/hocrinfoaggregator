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

import java.util.Properties;

/**
 * Superclass of Aligners.
 *
 * @author Federico Boschetti <federico.boschetti.73@gmail.com>
 */
public class Aligner {
    protected double gapPenalty = -1;
    protected double maxDiff = 50;
    protected Properties props = null;
    protected char gapChar=AlignFormatter.getGapTag().charAt(0);
    protected int errorSum = 0;
    protected int approxSum = 0;
    protected int insSum = 0;
    protected int delSum = 0;
    protected int subSum = 0;
    protected int matchSum = 0;
    protected SimEvaluator simEval = null;

    /**
     * Default Constructor.
     */
    public Aligner() {
        simEval=new SimEvaluator();
    }

    /**
     * Set the Similarity Evaluator.
     *
     * @param simEval the Similarity Evaluator.
     */
    public void setSimEvaluator(SimEvaluator simEval) {
        this.simEval = simEval;
    }

    /**
     * Get the Similarity Evaluator.
     * @return the Similarity Evaluator.
     */
    public SimEvaluator getSimEvaluator() {
        return simEval;
    }

    /**
     * Get the total number of errors (i.e. insertions, deletions or substitutions).
     * @return the total number of errors.
     */
    public int getErrorSum() {
        return errorSum;
    }

    public int getEditDistance(){
        return errorSum;
    }

    /**
     * Get the total number of errors checked as "approximate errors" (for example, same upper case representation
     * of characters with different accents).
     *
     * @return the total number of approximate errors.
     */
    public int getApproxSum() {
        return approxSum;
    }

    /**
     * Get the total number of exact matches.
     * @return the total number of exact matches.
     */
    public int getMatches() {
        return matchSum;
    }

    /**
     * Get the total number of insertions.
     * @return the total number of insertions.
     */
    public int getInsSum() {
        return insSum;
    }

    /**
     * Get the total number of deletions.
     * @return the total number of deletions.
     */
    public int getDelSum() {
        return delSum;
    }

    /**
     * Get the total number of substitutions.
     * @return the total number of substitutions.
     */
    public int getSubSum() {
        return subSum;
    }

    /**
     * Set the gap penalty.
     * @param gapPenalty the gap penalty.
     */
    public void setGapPenalty(double gapPenalty) {
        this.gapPenalty = gapPenalty;
    }

    /**
     * Get the gap penalty.
     * @return the gap penalty.
     */
    public double getGapPenalty() {
        return gapPenalty;
    }

    /**
     * Set the gap character.
     * @param gapChar the gap character.
     */
    public void setGapChar(char gapChar) {
        this.gapChar = gapChar;
    }

}
