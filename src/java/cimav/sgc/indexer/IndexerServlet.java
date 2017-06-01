/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cimav.sgc.indexer;

import cimav.sgc.Common;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author calderon
 */
@WebServlet(name = "IndexerServlet", urlPatterns = {"/indexer", "/Indexer"}, initParams = {
    @WebInitParam(name = "NameVar1", value = "testing")})
public class IndexerServlet extends HttpServlet {

    
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
            throws ServletException, IOException {

        /* Cargar Properties PAths */
        String realPath = getServletContext().getRealPath("/");
        String sandBox = Common.loadSandBox(realPath);
        
        long millis = System.currentTimeMillis();
        
        /* I n d e x a r */
        int ndocs = Indexer.indexar();
        
        millis = System.currentTimeMillis() - millis;
        String elapsedTime = String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        
        DateFormat dateFormat = new SimpleDateFormat("dd/MMM/yyy HH:mm:ss");
        Date date = new Date();
        String now = dateFormat.format(date);
        
        int numIndexedDocs = Indexer.numDocs();
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet IndexerServlet</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet IndexerServlet at " + request.getContextPath() + "</h1>");
            out.println("<h3>Real Path: " + realPath + " </h3>");
            out.println("<h3>SandBox: " + sandBox  + " </h3>");
            out.println("<h3>Path to DropBox (documentos vigentes): " + Common.DropBoxVigentesPath + " </h3>");
            out.println("<h3>Path to Lucene Index: " + Common.LuceneIndexPath + " </h3>");
            out.println("<h3>Documentos Indexados: " + ndocs + " in " + elapsedTime + "  at " + now + " </h3>");
            out.println("<h3>Index contains : " + numIndexedDocs + " </h3>");
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
        processRequest(request, response);
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
        processRequest(request, response);
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
