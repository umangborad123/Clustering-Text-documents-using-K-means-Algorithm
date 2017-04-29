
package document.processing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author umang_borad
 */
public class Documents {
    
    //instances
    
    private final File file;
    private final List<String> tokens;
    private ArrayList<Double> docVector=new ArrayList<>();
    private ArrayList<Double> unitVector=new ArrayList<>();
    
    public Documents(File file1) throws IOException, BusinessException
    {   
        
        file=file1;
        tokens=Document_reader.tokenizeDocument(file);
    }
    public String getName()
    {
        return file.getName();
    }
    
    public List<String> get_tokens() throws IOException, BusinessException
    {
        return tokens;
    }
    
    public int number_of_tokens()
    {
        return tokens.size();
    }
    
    
    public File getFile()
    {
    return file;
    }

    double countOccurences(String get) {
       int count = 0;
        for (int x=0;x<tokens.size();x++) {
            if (get.equals(tokens.get(x))) {
                count++;
            }
        }
       return count;
    }

    void add_component_to_vector(double TF_IDF) {
        docVector.add(TF_IDF);
    }
    
    ArrayList<Double> getVector()
    {
        return docVector;
    }
    
    void normalized_Vector()
    {
        double squaredSum=0;
    if(!docVector.isEmpty())
    {   
        
        for(int d=0;d<docVector.size();d++)
        {
            squaredSum+=(docVector.get(d)*docVector.get(d));
        }
        
        squaredSum=Math.sqrt(squaredSum);
    
        for(int d=0;d<docVector.size();d++)
        {
            unitVector.add(d,docVector.get(d)/squaredSum);
        }
    }
   
    }

    ArrayList<Double> getUnitVector() {
       return unitVector;
    }
    
    
}
