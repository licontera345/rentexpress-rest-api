function uploadEmployeeAvatar() {
    const id = document.getElementById("employeeId").value;
    const file = document.getElementById("employeeFile").files[0];

    if (!id || !file) {
        alert("ID y archivo necesarios");
        return;
    }

    const form = new FormData();
    form.append("file", file);

    fetch(`/rentexpress-rest-api/api/file/employee-avatar/${id}`, {
        method: "POST",
        body: form
    })
    .then(() => loadEmployeeAvatar())
    .catch(() => alert("Error al subir avatar"));
}

function loadEmployeeAvatar() {
    const id = document.getElementById("employeeId").value;
    if (!id) return;

    document.getElementById("employeeAvatarPreview").src =
        `/rentexpress-rest-api/api/file/employee-avatar/${id}`;
}
