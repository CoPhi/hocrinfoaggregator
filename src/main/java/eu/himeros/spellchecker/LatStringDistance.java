
package eu.himeros.spellchecker;

import org.apache.lucene.search.spell.StringDistance;

/**
 * Calculate the similarity between Latin strings
 * @author federico
 */
public class LatStringDistance implements StringDistance {
   
    /**
     * Constructor
     * 
     */
    public LatStringDistance(){
        super();
    }

    @Override
    public float getDistance(String s1, String s2) {
        //ToDo
        return 1;
    }

}
