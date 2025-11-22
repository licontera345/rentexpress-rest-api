function uploadUserAvatar() {
    const id = document.getElementById("userId").value;
    const file = document.getElementById("userAvatarFile").files[0];

    if (!id || !file) {
        alert("ID y archivo necesarios");
        return;
    }

    const form = new FormData();
    form.append("file", file);

    fetch(`/rentexpress-rest-api/api/file/user-avatar/${id}`, {
        method: "POST",
        body: form
    })
    .then(() => loadUserAvatar())
    .catch(() => alert("Error al subir avatar"));
}

function loadUserAvatar() {
    const id = document.getElementById("userId").value;
    if (!id) return;

    document.getElementById("userAvatarPreview").src =
        `/rentexpress-rest-api/api/file/user-avatar/${id}`;
}
