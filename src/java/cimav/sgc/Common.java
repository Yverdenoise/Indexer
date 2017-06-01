/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cimav.sgc;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import org.apache.lucene.util.Version;

/**
 *
 * @author calderon
 */
public class Common {
    
    public static String VIGENTES = "/vigentes/";
    public static final Version VERSION_LUCENE = Version.LUCENE_46;
    
    public static String DropBoxVigentesPath;
    public static String dropBoxEstructuraPath;
    public static String LuceneIndexPath;
    public static File fileToLuceneIndexDirectory;
    
    public static String NO_PATH = "NoPath";

    private static Properties prop = new Properties();
    
    public static String loadSandBox(String realPath) {
        
        String result = realPath;
        
    	try {
               
                if (!realPath.endsWith("/")) {
                    //Mac termina en / pero Linux no
                    realPath = realPath + "/";
                }
                
                String sandBoxPath = realPath + "sandbox.properties";
                
                File fSandBox = new File(sandBoxPath);
                if(fSandBox.exists()) { 
                    
                    result = fSandBox.getAbsolutePath() + " >  Ok";
                    
                    prop.load(new FileInputStream(fSandBox));
                    
                    Common.DropBoxVigentesPath = prop.getProperty("dropBoxVigentesPath",Common.NO_PATH);
                    Common.dropBoxEstructuraPath = prop.getProperty("dropBoxEstructuraPath",Common.NO_PATH);
                    Common.LuceneIndexPath = prop.getProperty("lucenIndexPath",Common.NO_PATH);
                } else {
                    result = sandBoxPath + "   >  No Existe! ";
                    
                    Common.DropBoxVigentesPath = Common.NO_PATH;
                    Common.dropBoxEstructuraPath = Common.NO_PATH;
                    Common.LuceneIndexPath = Common.NO_PATH;
                }
                
//                 Common.DropBoxVigentesPath = Common.DropBoxVigentesPath.replace("vigentes", "test");
//                 Common.VIGENTES = "/test/";
                
                if (!Common.LuceneIndexPath.equals(Common.NO_PATH)) {
                    Common.fileToLuceneIndexDirectory = new File(Common.LuceneIndexPath);            
                }
                
 
    	} catch (Exception ex) {
            result = result + "\n" + ex.getMessage() + "\n";
            ex.printStackTrace();
        }
        
        return result;
    }
    
}
