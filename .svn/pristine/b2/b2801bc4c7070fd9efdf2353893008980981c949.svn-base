/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cimav.sgc.search;

import cimav.sgc.Common;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.es.SpanishLightStemFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author calderon
 */
public class Search {

    public static String QueryString = "";
    

//    public static void main(String[] args)  {
//        SimpleSearcher simpleSearcher = new SimpleSearcher();
//        
//        // Los deptos o es uno en particular o son todos los del usuario. No puede estar vacio.
//        // String deptos = "(=RH OR =TIC OR =AD OR =CA)"; // usar =  y OR
//        String deptos = "(=RH OR =TIC)"; // usar =  y OR
//        // El tipo es 1 en particular o ninguno que significa ignorar al tipo en el filtro
//        String tipo = "=MAN";
//        // Puede ser vacio
//        String terminos = "";
//        List<Documento> results = simpleSearcher.search(deptos, tipo, terminos);
//        
//        int j =0;
//        for(Documento doc:results) {
//            System.out.println("");
//            System.out.println(j++ + ") >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//            System.out.println(doc.depto + " : " + doc.tipo + " : " + doc.codigo + " : " + doc.nombre + " : " + doc.codigosReferenciados);
//            int i =0;
//            for(String frag : doc.fragmentos) {
//                System.out.println(i++ + ") " + frag);
//            }
//        }
//    }
    
    public static List<Documento> search(String depto, String tipo, String terminos) throws InvalidTokenOffsetsException {
        
//        depto = "RH TIC AD MAC CA";
//        tipo = "PRO MAN";
//        terminos = "calidad equipo computación";
        
        // dejar solo alpanumericos y acentos
        terminos = terminos.replaceAll("[^\\p{L}\\p{Nd}\\s]", ""); 
        terminos = terminos.replaceAll("\\s+", " "); 
        
        List<Documento> results = new ArrayList<>();
                
        try (IndexReader reader = DirectoryReader.open(FSDirectory.open(Common.fileToLuceneIndexDirectory))) {
            IndexSearcher searcher = new IndexSearcher(reader);
            SpanishAnalyzer spAnalyzer = new SpanishAnalyzer(Common.VERSION_LUCENE);
            
            // Armar Query
            BooleanQuery booleanQuery = new BooleanQuery();
            // Depto siempre va. O es uno seleccionado por el Usuario, o son todos los del Usuario
            // TODO normalizar depto(s)
            Query qDepto = new QueryParser(Common.VERSION_LUCENE, "depto", spAnalyzer).parse(depto);
            booleanQuery.add(qDepto, BooleanClause.Occur.MUST); 
            if (!tipo.isEmpty()) {
                // Tipo solo va cuando lo selecciona el Usuario y solo es uno.
                Query qTipo = new QueryParser(Common.VERSION_LUCENE, "tipo", spAnalyzer).parse(tipo);
                booleanQuery.add(qTipo, BooleanClause.Occur.MUST);
            }
            if (!terminos.isEmpty()) {
                // Los Terminos pueden ir o no
                Query qContenido = new QueryParser(Common.VERSION_LUCENE, "contenido", spAnalyzer).parse(terminos);
                booleanQuery.add(qContenido, BooleanClause.Occur.MUST); 
            }

            // Busqueda
            TopDocs documentos = searcher.search(booleanQuery, 100);
            // Resultados
            ScoreDoc[] hits = documentos.scoreDocs;
                 
            //System.out.println(hits.length + ")  " + booleanQuery);
            
            Search.QueryString = booleanQuery.toString();
            
            for (int i = 0; i < hits.length; ++i) {
                int docId = hits[i].doc;
                Document doc = null;
                try {
                    doc = searcher.doc(docId);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                String strDepto = doc.get("depto");
                String strTipo = doc.get("tipo");
                String strCodigo = doc.get("codigo");
                String strNombre = doc.get("nombre");
                String strCodigosReferenciados = doc.get("codigosReferenciados");                
                String strContenido = doc.get("contenido");                
                Documento encontrado = new Documento(strDepto, strTipo, strCodigo, strNombre, strContenido, strCodigosReferenciados);
                
                TokenStream stream = TokenSources.getTokenStream("contenido", strContenido, spAnalyzer);
                stream = new StandardFilter(Common.VERSION_LUCENE, stream);
                stream = new LowerCaseFilter(Common.VERSION_LUCENE, stream);
                stream = new StopFilter(Common.VERSION_LUCENE, stream, CharArraySet.EMPTY_SET);
                stream = new SpanishLightStemFilter(stream);
                        
                //QueryScorer queryScorer = new QueryScorer(query);
                QueryScorer queryScorer = new QueryScorer(booleanQuery);
                Highlighter highlighter = new Highlighter(queryScorer);
                Fragmenter fragmenter = new SimpleSpanFragmenter(queryScorer, 50);
                highlighter.setTextFragmenter(fragmenter);
                
                String[] fragments = highlighter.getBestFragments(stream, strContenido, 5);
                for(int j=0; j<fragments.length; j++) {
                    String textHigh = fragments[j];
                    encontrado.fragmentos.add(textHigh);
                    //System.out.println(textHigh); 
                }
                
                results.add(encontrado);
            }
        } catch (IOException | ParseException | InvalidTokenOffsetsException ex) {
            Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, ex);
        }
        return results;
    }
    
}
