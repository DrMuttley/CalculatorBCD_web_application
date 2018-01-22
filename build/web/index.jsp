<%-- 
    Document   : index
    Created on : 2017-12-14, 09:48:39
    Author     : Łukasz Nowak
    Version    : 1.0
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Calculator BCD</title>
    </head>
    <body>
        <h1>Calculator BCD</h1>
        <hr>
        <p><b>Show last results:</b></p>
        <form action="FrontServlet">
            <input type="submit" value="LOAD RESULTS" name="load" style="width: 120px; height: 25px"/>
        </form>
        <hr>
        <p><b>Make calculation:</b></p>
        <form action="FrontServlet">
            <input type="submit" value="CALCULATION" name="calc" style="width: 120px; height: 25px"/>
        </form>
        <hr>
    </body>
</html>
