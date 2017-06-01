/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cimav.sgc.search;

import cimav.sgc.Common;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;

/**
 *
 * @author calderon
 */
@WebServlet(name = "SearchServlet", urlPatterns = {"/Searcher", "/searcher"}, initParams = {@WebInitParam(name = "depto", value = "MAC")})
public class SearchServlet extends HttpServlet {
    
    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, InvalidTokenOffsetsException {
        
        /* Cargar Properties PAths */
        String realPath = getServletContext().getRealPath("/");
        String sandBox = Common.loadSandBox(realPath);
        
        // Parametros
        ServletConfig config= getServletConfig();
        String pDeptos= request.getParameter("depto"); 
        pDeptos = pDeptos == null ? "" : pDeptos.trim(); 
        pDeptos = pDeptos.isEmpty() ? config.getInitParameter("depto") : pDeptos;
        String pTipo= request.getParameter("tipo");
        pTipo = pTipo == null ? "" : pTipo.trim(); 
        String pTerminos= request.getParameter("terminos");
        pTerminos = pTerminos == null ? "" : pTerminos.trim(); 
        
        long millis = System.currentTimeMillis();
        
        /* B u s c a r */
        List<Documento> resultados = Search.search(pDeptos, pTipo, pTerminos);
        
        millis = System.currentTimeMillis() - millis;
        String elapsedTime = String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        
        DateFormat dateFormat = new SimpleDateFormat("dd/MMM/yyy HH:mm:ss");
        Date date = new Date();
        String now = dateFormat.format(date);
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet SearchServlet</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet SearchServlet at " + request.getContextPath() + "</h1>");
            out.println("<h3>SandBox: " + sandBox  + " </h3>");
            out.println("<h3>Real Path to Lucene Index : " + Common.LuceneIndexPath + " </h3>");
            out.println("<h3>Consulta: " + Search.QueryString + " </h3>");
            out.println("<h3>Total de resultados: " + resultados.size() + " en " + elapsedTime + " </h3>");
            for(Documento doc : resultados) {
                out.println("<strong>" + doc.depto + " : " + doc.tipo + " : " + doc.codigo + " : " + doc.nombre +  "</strong></br>");
                out.println("<font size='2'>" + doc.codigosReferenciados +  "</font></br>");
                String segmento = "<code>";
                for(String frag : doc.fragmentos) {
                    segmento = segmento + " " + frag;
                }
                segmento = segmento + "</code>";
                out.println(segmento);
                out.println("</br></br><div style='width:100%; border:1px dotted black;'></div>");
            }
            out.println("</body>");
            out.println("</html>");
        } finally {            
            out.close();
        }
    }
    

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (InvalidTokenOffsetsException ex) {
            Logger.getLogger(SearchServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (InvalidTokenOffsetsException ex) {
            Logger.getLogger(SearchServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
