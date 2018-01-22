package pl.polsl.java.lab4.servlets;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Class supports reading data from the database.
 * 
 * @author Łukasz Nowak 
 * @version 4.0
 */

public class LoadResults extends HttpServlet { 

    /**
     * Flag represents information about status of exception 
     */   
    private boolean exceptionFlag = false;
    
     /**
     * Method initializes LoadResults servlet.
     *
     * @param config ServletConfig object
     */

    @Override
    public void init(ServletConfig config){

        try {
            super.init(config);
        } catch (ServletException e) {
            System.err.println("LoadResult servlet - initialization exception: " + e.getMessage());
            exceptionFlag = true;
        }  
    }
    
    /**
     * Method destroy servlet.
     */
    
    @Override
    public void destroy(){
        super.destroy();
    }
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    
    public void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        //blockade against the attempt to bypass FrontServlet
        if (!exceptionFlag && getServletContext().getAttribute("DBConnected") != null
                && (boolean)getServletContext().getAttribute("DBConnected")) {

            PrintWriter out = response.getWriter();

            Connection con = (Connection) getServletContext().getAttribute("con");

            try {
                //creating expression object
                Statement statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, 
                        ResultSet.CONCUR_READ_ONLY);
                
                //sending a query to the database
                ResultSet rs = statement.executeQuery("SELECT * FROM Results");

                //checking if there are results in the database
                if (rs.first()) {
                    
                    rs.previous();

                    //displaying results from database 
                    out.print("<html>"
                            + "<head>"
                            + "<title>History result</title>"
                            + "<meta charset=\"UTF-8\">"
                            + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                            + "</head>"
                            + "<body>"
                            + "<table BORDER='1'>"
                            + "<tr>"
                            + "<td><center><b>&nbsp ID &nbsp</b></center></td>"
                            + "<td><center><b>&nbsp FIRST NUMBER &nbsp</b></center></td>"
                            + "<td><center><b>&nbsp SIGN &nbsp</b></center></td>"
                            + "<td><center><b>&nbsp SECOND NUMBER &nbsp</b></center></td>"
                            + "<td><center><b>&nbsp RESULT &nbsp</b></center></td>"
                            + "<td><center><b>&nbsp DATE &nbsp</b></center></td>"
                            + "</tr>");

                    while (rs.next()) {
                        out.print("<tr>"
                                + "<td><center><b>&nbsp" + Integer.toString((rs.getInt("id"))) + "&nbsp</b></center></td>"
                                + "<td><center><b>&nbsp" + rs.getString("firstNumber") + "&nbsp</b></center></td>"
                                + "<td><center><b>&nbsp" + rs.getString("sign") + "&nbsp</b></center></td>"
                                + "<td><center><b>&nbsp" + rs.getString("secondNumber") + "&nbsp</b></center></td>"
                                + "<td><center><b>&nbsp" + rs.getString("singleResult") + "&nbsp</b></center></td>"
                                + "<td><center><b>&nbsp" + rs.getTimestamp("date").toString() + "&nbsp</b></center></td>"
                                + "<tr>");
                    }
                    out.print("</tr>"
                            + "</table>"
                            + "</body>"
                            + "</html>");
                } else {
                    out.print("<p><b>Table with history results is empty, "
                            + "no result has been added yet!</b></p><hr>");
                }
                rs.close();
            } catch (SQLException e) {
                System.err.println("SQL exception: " + e.getMessage());

                getServletContext().setAttribute("DBConnected", false);

                getServletContext().getRequestDispatcher("/databaseErrorMessage.jsp").forward(request, response);
            }

        } else {
            getServletContext().getRequestDispatcher("/index.jsp").forward(request, response);
        }
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}