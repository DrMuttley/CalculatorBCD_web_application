package pl.polsl.java.lab4.servlets;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

/**
 * Class performs calculations and saves the correct result to the database.
 * 
 * @author Łukasz Nowak 
 * @version 4.0
 */
public class Calculation extends HttpServlet { 

    /**
     * Flag represents information about status of exception
     */
    private boolean exceptionFlag = false;
    
    /**
     * Method initializes Calculation servlet.
     *
     * @param config ServletConfig object
     */

    @Override
    public void init(ServletConfig config){

        try {
            super.init(config);
        } catch (ServletException e) {
            System.err.println("Calculation servlet - initialization exception: " + e.getMessage());
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
        if (!exceptionFlag && getServletContext().getAttribute("frontServletExist") != null
                && (boolean) getServletContext().getAttribute("frontServletExist")) {

            PrintWriter out = response.getWriter();

            //downloading the calculation map from the context
            Map<Character, CalculationInterface> calculationMap
                    = (Map<Character, CalculationInterface>) getServletContext().getAttribute("calculationMap");

            //creating variables for calculation
            String firstNumber = new String();
            String secondNumber = new String();
            String sign = new String();

            //checking if there are initialization parameters
            if (getServletContext().getAttribute("initParam") != null &&
                    (boolean) getServletContext().getAttribute("initParam")) {
                
                firstNumber = (String) getServletContext().getAttribute("firstNumber");
                secondNumber = (String) getServletContext().getAttribute("secondNumber");
                sign = (String) getServletContext().getAttribute("sign");
            } else {//downloading data from the form
                firstNumber = request.getParameter("firstNumber");
                secondNumber = request.getParameter("secondNumber");
                sign = request.getParameter("sign");
            }
            String result = new String();

            //creating flag for information about status of result
            boolean correctResult = false;
            
            if (firstNumber == null || sign == null || secondNumber == null) {
                getCookies(request);
                getServletContext().getRequestDispatcher("/calculationForm.jsp").forward(request, response);
                
            }else if (!firstNumber.equals("") && !secondNumber.equals("")) {
                switch (sign) {
                    case "+": {
                        result = calculationMap.get('+').calculate(firstNumber, secondNumber);
                        break;
                    }
                    case "-": {
                        result = calculationMap.get('-').calculate(firstNumber, secondNumber);
                        break;
                    }
                    default: {
                        result = "Wrong input data";
                        getCookies(request);
                        getServletContext().getRequestDispatcher("/calculationForm.jsp").forward(request, response); 
                        break;
                    }
                }

                if (result.contains("Wrong input data")) {
                    result += " Back and try again.";
                } else {
                    correctResult = true;
                    updateCookies(response);
                }
        
            } else if (firstNumber.equals("") || secondNumber.equals("")) {
                result = "None of the numbers can't be empty, back and try again.";
            } else {
                getServletContext().getRequestDispatcher("/index.jsp").forward(request, response);
            }
            
            //saving to the database
            if (correctResult && (boolean) getServletContext().getAttribute("DBConnected")) {

                saveToDB(firstNumber, sign, secondNumber, result);
            }            
            displayResult(out, correctResult, result);
        }else{
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
    
    /**
     * Method saves parameters and result to the database.
     * 
     * @param firstNumber represents first number for the calculation
     * @param sign represents sign for the calculation
     * @param secondNumber represents second number for the calculation
     * @param result represents result of the calculation
     */
    
    private void saveToDB(String firstNumber, String sign, String secondNumber, String result){
         
        Connection con = (Connection) getServletContext().getAttribute("con");

        Date utilDate = new Date();

        Timestamp timeStamp = new Timestamp(utilDate.getTime());

        try {
            Statement stmt = con.createStatement();
            
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM Results");
            
            //creating counter for checking the last id number
            int IDCounter = 1;
            
            while(resultSet.next()){
                IDCounter++;
            }
  
            PreparedStatement preparedStmt = con.prepareStatement("INSERT INTO Results "
                    + "(id, firstNumber, sign, secondNumber, singleResult, date) VALUES(?,?,?,?,?,?)");
            preparedStmt.setString(1, Integer.toString(IDCounter));
            preparedStmt.setString(2, firstNumber);
            preparedStmt.setString(3, sign);
            preparedStmt.setString(4, secondNumber);
            preparedStmt.setString(5, result);
            preparedStmt.setTimestamp(6, timeStamp);
            preparedStmt.executeUpdate();
        } catch (SQLException sqle) {
            System.err.println("SQL exception: " + sqle.getMessage());
            getServletContext().setAttribute("DBConnected", false);
        }   
    }
    
    /**
     * Method displays the result to the user.
     * 
     * @param out represents PrintWriter
     * @param correctResult represents information about correctness of the result
     * @param result represents result of calculation
     */
    
    private void displayResult(PrintWriter out, boolean correctResult, String result){
        
        String historySavingMessage = new String();
        
        if(correctResult && (boolean)getServletContext().getAttribute("DBConnected")){
            result = "RESULT: " + result;
            historySavingMessage = "The result saved in history.";
        }else if (correctResult && !(boolean)getServletContext().getAttribute("DBConnected")){ 
            result = "RESULT: " + result;
            historySavingMessage = "Sorry, but because of technical reasons the "
                    + "result couldn't be saved in history.";       
        }
        
        out.print("<html>"
                + "<head>"
                + "<title>Result</title>"
                + "<meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                + "</head>"
                + "<body>"
                + "<b>"
                + result
                + "</b>" 
                + "<hr>"
                + historySavingMessage    
                + "</body>"
                + "</html>");
    }
    
    /**
     * Method updates cookies.
     * 
     * @param response servlet response
     */
    
    private void updateCookies(HttpServletResponse response){
   
        int howManyCalculation = 0;
        
        if (getServletContext().getAttribute("howManyCalculation") != null) {
            
            howManyCalculation = (int) getServletContext().getAttribute("howManyCalculation");

            //updating number of correct results
            howManyCalculation++;
        }

        Cookie cookie = new Cookie("howManyCalculation", Integer.toString(howManyCalculation));
        response.addCookie(cookie);
    }      
    
    /**
     * Method finds parameter howManyCalculation (number of calculations made 
     * by user) in cookies and sets attribute howManyCalculation in request and 
     * in context.
     * 
     * @param request servlet request
     */    
        
    private void getCookies(HttpServletRequest request){
   
        Cookie[] cookies = request.getCookies();
        int howManyCalculation = 0;
        
        //protection against no cookies
        if (cookies != null) {
            //searching number of calculations made by user
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("howManyCalculation")) {
                    howManyCalculation = Integer.parseInt(cookie.getValue());
                    break;
                }
            }
        }
        //setting attribute in request and in context
        request.setAttribute("howManyCalculation", howManyCalculation);
        getServletContext().setAttribute("howManyCalculation", howManyCalculation);
    }      
}
