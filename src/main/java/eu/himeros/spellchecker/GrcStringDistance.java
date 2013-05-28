
package eu.himeros.spellchecker;

import eu.himeros.alignment.StringAligner;
import eu.himeros.alignment.UpperCaseSimEvaluator;
import org.apache.lucene.search.spell.StringDistance;

/**
 * Calculate the similarity between Greek strings
 * @author federico
 */
public class GrcStringDistance implements StringDistance {
    StringAligner sa=new StringAligner(new UpperCaseSimEvaluator());

    /**
     * Constructor
     */
    public GrcStringDistance(){
        super();
    }

    /**
     * Evaluate the similarity between Greek strings
     * @param s1
     * @param s2
     * @return 
     */
    public float getSimilarity(String s1, String s2) {
        sa.align(s1, s2);
        try{
            return 1/(float)sa.getEditDistance();
        }catch(Exception ex){
            return 1;
        }
    }
    
    @Override
    public float getDistance(String s1,String s2){
        String[] alignedStrs=sa.align(s1,s2);
        try{
            float len=((s1.length()>s2.length())?s1.length():s2.length());
            float distance=(float)((float)sa.getSimEvaluator().eval(alignedStrs[0],alignedStrs[1]));
            //return 1-((float)sa.getEditDistance()/len);
            return distance;
        }catch(Exception ex){
            ex.printStackTrace(System.err);
            return 1;
        }
    }
}
