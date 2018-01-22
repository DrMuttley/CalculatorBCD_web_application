package pl.polsl.java.lab4.servlets;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import pl.polsl.java.lab4.model.*;

/**
 * Main class of the servlets receives requests and redirects them to the 
 * appropriate servlet. Class supports initialization parameters - example of 
 * three parameters values: 1001 + 1000 (first number value, sign value, second 
 * number value)
 *
 * @author Łukasz Nowak
 * @version 4.0
 */

public class FrontServlet extends HttpServlet { 
    
    /**
     * Model for calculation
     */
    private Model model;
    
    /**
     * Calculation map for calculation
     */
    private Map<Character, CalculationInterface> calculationMap;
    
    /**
     * Connection with the database
     */
    private Connection con;
    
    /**
     * Flag represents information about status of exception 
     */   
    private boolean exceptionFlag = false;
    
    /**
     * Method initializes FrontServlet servlet, creates connection to database 
     * and creates calculation map for calculation.
     *
     * @param config ServletConfig object
     */
    
    @Override
    public void init(ServletConfig config){

        try {
            super.init(config);
        } catch (ServletException e) {
            System.err.println("FrontServlet - initialization exception: " 
                    + e.getMessage());
            exceptionFlag = true;
        }               
        //setting in context information about status of FrontServlet
        getServletContext().setAttribute("frontServletExist", true);     
        
        //creating instance of model
        model = new Model();

        //creating calculation map for calculations
        createCalculationMap();
        
        //creating flag for status of database connection
        boolean DBConnected = false;
        getServletContext().setAttribute("DBConnected", DBConnected);

        //creating connection with the database
        createDataBaseConnection();        
    }
    
    /**
     * Method closing connection to database and destroy servlet.
     */
    
    @Override
    public void destroy(){
 
        if ((boolean)getServletContext().getAttribute("DBConnected")) {
            try {
                con.close();
            } catch (SQLException sqle) {
                System.err.println("Close DBconnection exception: " + sqle.getMessage());
            }
        }
        //setting in context information about status of FrontServlet
        getServletContext().setAttribute("frontServletExist", false);   
        
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
         
    //loading information from cookies   
    getCookies(request);
     
    String inputQueryString = request.getQueryString();
    
        if (!exceptionFlag && inputQueryString != null) {

            boolean DBConnected = (boolean) getServletContext().getAttribute("DBConnected");

            //checking which button the request came from
            if (inputQueryString.equals("load=LOAD+RESULTS") && DBConnected) {

                //transferring control to LoadResults servlet
                getServletContext().getRequestDispatcher("/LoadResults").forward(request, response);

                //checking information about status of initialization parameters
            } else if (inputQueryString.equals("load=LOAD+RESULTS") && !DBConnected) {

                getServletContext().getRequestDispatcher("/databaseErrorMessage.jsp").forward(request, response);

            } else if (inputQueryString.equals("calc=CALCULATION") && !verifyInitParameters()) {

                //sending the form for data
                getServletContext().getRequestDispatcher("/calculationForm.jsp").forward(request, response);

            } else if (inputQueryString.equals("calc=CALCULATION") && verifyInitParameters()) {

                //transferring control to Calculation servlet
                getServletContext().getRequestDispatcher("/Calculation").forward(request, response);

            } else {
                getServletContext().getRequestDispatcher("/index.jsp").forward(request, response);
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
    
    /**
     * Method creats calculation map for calculation.
     */
    
    private void createCalculationMap(){
                
        calculationMap = new HashMap<>();
        
        //lambda expression for addition
        calculationMap.put('+', (a, b) -> {

            String additionResult = new String();

            try {
                additionResult = model.addition(a, b);
            } catch (ModelException e) {
                additionResult = (e.getMessage());
            }
            return additionResult;
        });
        //lambda expression for subtraction
        calculationMap.put('-', (a, b) -> {

            String substractionResult = new String();

            try {
                substractionResult = model.subtraction(a, b);
            } catch (ModelException e) {
                substractionResult = (e.getMessage());
            }
            return substractionResult;
        });
        //setting in the context calculationMap attribute
        getServletContext().setAttribute("calculationMap", calculationMap);
    }
    
    /**
     * Method creats connection with the database.
     */
    
    private void createDataBaseConnection() {

        //creating list for paramaters to connection with the datbase
        List<String> dataToDBConnect = loadParametersToDBConnection();
        
        //verification of the number of parameters
        if (dataToDBConnect.size() == 7) {
            try {
                //loading file with data base controller
                Class.forName(dataToDBConnect.get(0));
                //creating connection to data base
                con = DriverManager.getConnection(dataToDBConnect.get(1) + "://"
                        + dataToDBConnect.get(2) + ":" + dataToDBConnect.get(3)
                        + "/" + dataToDBConnect.get(4),
                        dataToDBConnect.get(5), dataToDBConnect.get(6));

                //changing the flag in context
                getServletContext().setAttribute("DBConnected", true);
   
            } catch (SQLException sqle) {
                System.err.println("SQL exception: " + sqle.getMessage());
            } catch (ClassNotFoundException cnfe) {
                System.err.println("ClassNotFound exception: " + cnfe.getMessage());
            }
            //setting in the context connection attribute
            getServletContext().setAttribute("con", con);
        }
    }

    /**
     * Method loads from web.xml parameters to connection with the database.
     * 
     * @return data to connection with the database
     */
    
    private List<String> loadParametersToDBConnection(){

        //creating list for parameters to connection with the databas
        List<String> dataToDBCOnnect = new LinkedList();

        dataToDBCOnnect.add(getServletContext().getInitParameter("driver"));
        dataToDBCOnnect.add(getServletContext().getInitParameter("protocol"));
        dataToDBCOnnect.add(getServletContext().getInitParameter("host"));
        dataToDBCOnnect.add(getServletContext().getInitParameter("portNumber"));
        dataToDBCOnnect.add(getServletContext().getInitParameter("DBName"));
        dataToDBCOnnect.add(getServletContext().getInitParameter("login"));
        dataToDBCOnnect.add(getServletContext().getInitParameter("pass"));

        //checking if all parameters have been found
        for (int i = 0; i < dataToDBCOnnect.size(); i++) {
            if (dataToDBCOnnect.get(i) == null) {
                dataToDBCOnnect.remove(i);
            }
        }
        return dataToDBCOnnect;
    }
   
    /**
     * Method checks initialization parameters and if they are, downloading
     * them.
     *
     * @return flag with information about status of initialization parameters
     */
    
    private boolean verifyInitParameters() {

        //flag with information about status of initialization parameters
        boolean initParameters = false;
        //creating list for initialization parameters
        List<String> initParametersList = new LinkedList();

        Enumeration enumeration = getServletConfig().getInitParameterNames();

        //variable for information about sign status (-1 not found, >= 0 found on position)
        int signPosition = -1;
        //getting three initialization parameters if they are
        for (int i = 0; i < 3; i++) {
            //creating variable for names of parameters
            String name = new String();

            if (enumeration.hasMoreElements()) {
                name = (String) enumeration.nextElement();
                initParametersList.add(getServletConfig().getInitParameter(name));

                //searching sign 
                if (getServletConfig().getInitParameter(name).equals("+")
                        || getServletConfig().getInitParameter(name).equals("-")) {
                    signPosition = i;
                }
            }
        }
        //placing parameters in context
        if (initParametersList.size() == 3 && signPosition != -1) {
            getServletContext().setAttribute("sign", initParametersList.get(signPosition));
            initParametersList.remove(signPosition);
            getServletContext().setAttribute("firstNumber", initParametersList.get(0));
            getServletContext().setAttribute("secondNumber", initParametersList.get(1));

            //correct number of parameters found
            initParameters = true;
        }
        //placing in context information about found parameters
        getServletContext().setAttribute("initParam", initParameters);

        return initParameters;
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