<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Nueva cita</title>
</head>
<body>
    <h1>Nueva cita (NeroVeterinaria)</h1>

    <c:if test="${not empty error}">
        <div style="color:#c00">${error}</div>
    </c:if>
    <c:if test="${not empty success}">
        <div style="color:#070">${success}</div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/public/nero/consumesapi">
        <input type="hidden" name="action" value="create_appointment"/>

        <label>Sede</label>
        <select name="headquarters" required>
            <option value="">Selecciona...</option>
            <option value="1">Sede Central</option>
            <option value="2">Clínica Norte</option>
            <option value="3">Clínica Sur</option>
        </select>

        <label>Veterinario (opcional)</label>
        <select name="veterinarians">
            <option value="">--</option>
            <option value="10">Dr. García</option>
            <option value="11">Dra. López</option>
        </select>

        <label>Animal (opcional)</label>
        <select name="animals">
            <option value="">--</option>
            <option value="1">Perro</option>
            <option value="2">Gato</option>
        </select>

        <label>Fecha y hora (ISO)</label>
        <input type="datetime-local" name="dateTime" required />

        <label>Detalles</label>
        <textarea name="details" rows="3"></textarea>

        <button>Crear cita</button>
    </form>

    <c:if test="${not empty confirmationJson}">
        <h3>Confirmación</h3>
        <pre><c:out value="${confirmationJson}"/></pre>
    </c:if>
</body>
</html>
