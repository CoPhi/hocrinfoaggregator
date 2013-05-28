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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Aligner for generic Objects.
 *
 * @author Federico Boschetti <federico.boschetti.73@gmail.com>
 */
public class ObjectAligner<T> extends Aligner {
    protected ArrayList<T> vect1 = new ArrayList<>(100);
    protected ArrayList<T> vect2 = new ArrayList<>(100);
    protected T[][] alignedObjects = null;
    Class<T> elementType;

    /**
     * Default Constructor.
     */
    public ObjectAligner() {
        super();
    }
    
    
    /**
     * Align two object arrays.
     *
     * @param objs1 the first object array.
     * @param objs2 the second object array.
     * @return the aligned arrays.
     */
    
    public List<List<T>> align(List<T> objAl1,List<T> objAl2){
        List<List<T>> resAl=new ArrayList<>();
        if(objAl1.isEmpty()&&objAl2.isEmpty()){
            resAl.add(new ArrayList<T>());
            resAl.add(new ArrayList<T>());
            return resAl;
        }
        else{
            if(objAl1.isEmpty()) elementType=(Class<T>)objAl2.get(0).getClass();
            else elementType=(Class<T>)objAl1.get(0).getClass();
        }
        T[][] res= align(objAl1.toArray(
                (T[])Array.newInstance(elementType,objAl1.size())
                ),
                objAl2.toArray(
                (T[])Array.newInstance(elementType,objAl2.size())
                ));
        resAl.add(new ArrayList<>(Arrays.asList(res[0])));
        resAl.add(new ArrayList<>(Arrays.asList(res[1])));
        return resAl;
    }
  
    
    private T[][] align(T[] objs1, T[] objs2) {
        vect1 = new ArrayList<>(100);
        vect2 = new ArrayList<>(100);
        //Similarity matrix
        double[][] simMatrix = new double[objs1.length][objs2.length];
        double[][] matrix = new double[objs1.length + 1][objs2.length + 1];
        for (int i = 0; i < objs1.length; i++) {
            for (int j = 0; j < objs2.length; j++) {
                simMatrix[i][j] = simScore(objs1[i], objs2[j]);
            }
        }
        matrix[0][0] = 0;
        //Fill matrix marginals
        for (int i = 1; i <= objs1.length; i++) {
            matrix[i][0] = i * gapPenalty;
        }
        for (int j = 1; j <= objs2.length; j++) {
            matrix[0][j] = j * gapPenalty;
        }
        double scoreDown;
        double scoreRight;
        double scoreDiag;
        double bestScore;
        //Fill matrix
        for (int i = 1; i <= objs1.length; i++) {
            for (int j = 1; j <= objs2.length; j++) {
                scoreDown = matrix[i - 1][j] + gapPenalty;
                scoreRight = matrix[i][j - 1] + gapPenalty;
                scoreDiag = matrix[i - 1][j - 1] + simMatrix[i - 1][j - 1];
                bestScore = Math.max(Math.max(scoreDown, scoreRight), scoreDiag);
                matrix[i][j] = bestScore;
            }
        }
        //Backtrack the path
        int i = objs1.length, j = objs2.length;
        double score, scoreLeft, scoreDiagInv;
        while (i > 0 && j > 0) {
            score = matrix[i][j];
            scoreDiagInv = matrix[i - 1][j - 1];
            scoreLeft = matrix[i - 1][j];
            if (score == scoreDiagInv + simMatrix[i - 1][j - 1]) {
                makeAlignment(objs1[i - 1], objs2[j - 1]);
                i = i - 1;
                j = j - 1;
            } else if (score == scoreLeft + gapPenalty) {
                makeAlignment(objs1[i - 1], null);
                i = i - 1;
            } else {
                makeAlignment(null, objs2[j - 1]);
                j = j - 1;
            }
        }
        while (i > 0) {
            makeAlignment(objs1[i - 1], null);
            i = i - 1;
        }
        while (j > 0) {
            makeAlignment(null, objs2[j - 1]);
            j = j - 1;
        }
        return makeResult();
    }

    /**
     * Evaluate the similarity between objects.
     *
     * @param obj1 the first object to evaluate.
     * @param obj2 the second object to evaluate.
     * @return the similarity score.
     */
    
    protected double simScore(T obj1,T obj2){
        return simEval.eval(obj1,obj2);
    }
    
    //TODO transpose the content of this method inside the eval method of an IntegerSimEvaluator!!!
    /*
    protected double simScore(Object obj1, Object obj2) {
        double res = 0;
        if ((obj1 instanceof Integer) && (obj2 instanceof Integer)) {
            double db1 = Double.parseDouble(obj1.toString());
            double db2 = Double.parseDouble(obj2.toString());
            if (db1 == db2) {
                return 1.0;
            }
            double diff = Math.abs(db1 - db2);
            if (diff >= maxDiff) {
                return 0.0;
            } else {
                return 1 - (diff / maxDiff);
            }
        }
        return res;
    }
    */
    /**
     * Add objects to the result vectors, aligning them with the suitable null object gaps.
     *
     * @param obj1 the first object (possibly null).
     * @param obj2 the second object (possibly null).
     */
    private void makeAlignment(T obj1, T obj2) {
        vect1.add(obj1);
        vect2.add(obj2);
    }

    /**
     * Make the rusult arrays.
     * @return the result arrays.
     */
    private T[][] makeResult() {
        alignedObjects =(T[][])Array.newInstance(elementType,2,vect1.size());
        int i=vect1.size()-1;
        for(T element:vect1){
            alignedObjects[0][i]=element;
            i--;
        }
        i=vect2.size()-1;
        for(T element:vect2){
            alignedObjects[1][i]=element;
            i--;
        }
        return alignedObjects;
    }
}
