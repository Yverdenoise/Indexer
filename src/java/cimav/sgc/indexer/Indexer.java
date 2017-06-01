/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cimav.sgc.indexer;

import cimav.sgc.Common;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author calderon
 */
public class Indexer {
    
    public static void main(String[] args)  {
        /* Cargar Properties PAths */
        String sandBox = Common.loadSandBox("C:\\Users\\juan.calderon\\Documents\\Projects\\Indexer\\web");
        /* I n d e x a r */
        int ndocs = Indexer.indexar();
        System.out.println("" + ndocs + " :::: " + sandBox);
    }
    
    public static int indexar() {
        
        List<Document> grabDocs = Indexer.grabDocs(Common.DropBoxVigentesPath);
        int i = Indexer.indexer(grabDocs);
        return i;
    }
    
    private static List<Document> grabDocs(final String dropBoxVigentesPath) {
        final List<Document> documentos = new ArrayList<>();
        final Parser parser = new AutoDetectParser(); // TODO AutoDetectParser es MUY LENTO
        String regexpFiles = "^((?i).*(txt|htm|html|xml|pdf|doc|docx|xls|xlsx|ppt|pptx))";

        // TODO poner en documentacio: Los Archivos deben tener CODIGO + Nombre + Extension conocida. 
        /***
         *  Los Archivos deben tener CODIGO + Nombre + Extension. 
         ***/
        
        final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("regex:" + regexpFiles);
        try {
            Files.walkFileTree(Paths.get(dropBoxVigentesPath), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path pathFile, BasicFileAttributes attrs) throws IOException {
                    if (matcher.matches(pathFile)) {

                        String vigentes = Common.VIGENTES;
                        String sep = "/";
                        if (pathFile.toString().contains("\\")) {
                            // es Windows
                            sep = "\\";
                            vigentes = vigentes.replace("/", sep);
                            sep = "\\\\";
                        }
                        
                        int idx = pathFile.toString().indexOf(vigentes) + vigentes.length();
                        String subPath = pathFile.toString().substring(idx);
                        String subPaths[] = subPath.split(sep);
                        String depto = subPaths[0];
                        String tipo = subPaths[1];
                        String nombreCompleto = subPaths[2];
                        String codigoNombre[] = nombreCompleto.split(" ", 2);
                        String codigo = codigoNombre[0];
                        String nombre = "";
                        if (codigoNombre.length >= 2) {
                            nombre = codigoNombre[1];
                        }
                        
                        if (nombre.length() > 1) try {
                            InputStream is = java.nio.file.Files.newInputStream(pathFile);
                            String textContent = grabTextContent(parser, is);
                            
                            String codigosReferenciados = grabCodigos(textContent);
                            
                            Document docLucene = new Document();
                            // StringField se guarda completo
                            // TextField se guarda Tokenized
                            docLucene.add(new TextField("codigo", codigo, Field.Store.YES)); // Codigo sirve de Id
                            docLucene.add(new TextField("depto", depto, Field.Store.YES));
                            docLucene.add(new TextField("tipo", tipo, Field.Store.YES));
                            docLucene.add(new TextField("nombre", nombre, Field.Store.YES));
                            docLucene.add(new TextField("contenido", textContent, Field.Store.YES)); 
                            docLucene.add(new TextField("codigosReferenciados", codigosReferenciados, Field.Store.YES));
                                           
                            SpanishAnalyzer stdSp = new SpanishAnalyzer(Common.VERSION_LUCENE);
                            TokenStream stream = stdSp.tokenStream("contenido", textContent);
                            stream.reset();
                            CharTermAttribute term = stream.getAttribute(CharTermAttribute.class);
                            while (stream.incrementToken()) {
                                System.out.println(" " + term);
                            }    
                            
                            boolean creado = documentos.add(docLucene);

                            System.out.println(creado + " : " + depto + " : " + tipo + " : " + codigo + " : " + nombre + " >> " + textContent);
//                            if (codigo.contains("CP01F02")) {
//                                System.out.println("Here");
//                            }
                         
                        } catch (ArrayIndexOutOfBoundsException ex) {
                            Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (Exception ex) {
                            Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return documentos;
    }
    
    private static int indexer(List<Document> documentos)  {
        int result = 0;
        try {
            if (Common.fileToLuceneIndexDirectory == null) {
                Common.fileToLuceneIndexDirectory = new File(Common.LuceneIndexPath);
            }
            Directory dir = FSDirectory.open(Common.fileToLuceneIndexDirectory);
            SpanishAnalyzer analyzer = new SpanishAnalyzer(Version.LUCENE_46, CharArraySet.EMPTY_SET);
            //StandardAnalyzer analyzer = new StandardAnalyzer(Common.VERSION_LUCENE);
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_46, analyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE); // creates a new index or overwrites an existing one. Tumba todo lo anterior.
            try (IndexWriter writer = new IndexWriter(dir, iwc)) {
                writer.addDocuments(documentos, analyzer);
            }
            result = documentos.size();

            
            
        } catch (Exception e) {
            result = -1;
        }
        return result;
    }    
    
    private static String grabTextContent(final Parser parser, final InputStream is) throws Exception {
        // Todo con Tika
        String textContent;
        BodyContentHandler contenthandler = new BodyContentHandler(10000000);
        Metadata metadata = new Metadata();
        try {
            
            parser.parse(is, contenthandler, metadata, new ParseContext());
            textContent = contenthandler.toString();
            // sustituye caracteres No Alfanumericos por nada; excepto espacios. Respeta acentuacion europea.
            textContent = textContent.replaceAll("[^\\p{L}\\p{Nd}\\s\\-]", ""); 
            // sustituye espacios innecesarios por un solo espacio.
            textContent = textContent.replaceAll("\\s+", " "); 
            
        } catch (IOException | SAXException | TikaException e) { 
            textContent = "";
            //e.printStackTrace();
            throw new Exception(e.getMessage());
        }
        return textContent;
    }
    
    private static String grabCodigos(String contenido) {
        String result = "";
        Pattern pattern = Pattern.compile("[A-Za-z]{2,3}[0-9]{2}([A-Za-z]{1}[0-9]{2})?(-[0-9]{2})?"); // ej. CA01H01-04, CAB01, CA01-04
        Matcher matcher = pattern.matcher(contenido);
        while (matcher.find()) {
            // TODO Identificar aquellos que no sean del Cimav
            String referencia = matcher.group();
            // agregar las no repetidas
            result = !result.contains(referencia) ? result +  "|" + referencia : result;
        }        
        result = result.replaceFirst("\\|", ""); // elimina el 1er |
        //System.out.println(result);
        return result;
    }
    
    public static int numDocs() {
        int result = -1;
        try {
            IndexReader reader = IndexReader.open(FSDirectory.open(Common.fileToLuceneIndexDirectory));
            result = reader.numDocs();
        } catch (IOException ex) {
            Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
            result = -3;
        }
        return result;
    }
    
}
