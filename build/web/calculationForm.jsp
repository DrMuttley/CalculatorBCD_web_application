<%-- 
    Document   : calculationForm
    Created on : 2017-12-14, 09:53:19
    Author     : Łukasz Nowak
    Version    : 1.0
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Calculation Form</title>
    </head>
    <body>
        <p><b>Provide data to calculation:</b></p>
        <form action="Calculation" method="GET">
            <p><b>First number:</b></p>
            <p><input type=text size=20 name=firstNumber></p>
            <p><b>Second number:</b></p>
            <p><input type=text size=20 name=secondNumber></p>
            <p><input type=radio name=sign value=+ checked><b>add</b>
                <input type=radio name=sign value=- checked><b>sub</b></p>
            <input type="submit" value="CALCULATE" name="calc" style="width: 120px; height: 25px"/>
        </form>
        <br>
        <hr>
        <p><b>Until now you have done <%= request.getAttribute("howManyCalculation")%> correct calculation</b></p>
    </body>
</html>
