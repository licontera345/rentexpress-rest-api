<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Login NeroVeterinaria</title>
</head>
<body>
    <h1>Acceso a NeroVeterinaria</h1>
    <c:if test="${not empty loginError}">
        <div style="color:#c00">${loginError}</div>
    </c:if>
    <form method="post" action="${pageContext.request.contextPath}/public/nero/consumesapi">
        <input type="hidden" name="action" value="login"/>
        <label>Usuario/Email</label>
        <input type="text" name="usernameOrEmail" required />
        <label>Contraseña</label>
        <input type="password" name="password" required />
        <button>Entrar</button>
    </form>
</body>
</html>
