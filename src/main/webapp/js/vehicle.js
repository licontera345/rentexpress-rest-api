function uploadVehicleImage() {
    const id = document.getElementById("vehicleId").value;
    const file = document.getElementById("vehicleFile").files[0];

    if (!id || !file) {
        alert("ID y archivo necesarios");
        return;
    }

    const form = new FormData();
    form.append("file", file);

    fetch("/rentexpress-rest-api/api/file/vehicle/" + id, {
        method: "POST",
        body: form
    })
    .then(r => {
        if (!r.ok) throw new Error();
        return r.text();   // TU BACKEND DEVUELVE TEXTO, NO JSON
    })
    .then(() => loadVehicleGallery())
    .catch(() => alert("Error al subir imagen"));
}

function loadVehicleGallery() {
    const id = document.getElementById("vehicleId").value;
    if (!id) return;

    fetch("/rentexpress-rest-api/api/file/vehicle/" + id)
        .then(r => r.json()) // ESTO SÍ ES JSON: la lista de imágenes
        .then(images => {
            const div = document.getElementById("vehicleGallery");
            div.innerHTML = "";

            images.forEach(img => {
                const container = document.createElement("div");

                container.innerHTML = `
                    <img src="/rentexpress-rest-api/api/file/vehicle/${id}/${img}" width="120">
                    <button onclick="deleteImage('${img}')">Eliminar</button>
                `;

                div.appendChild(container);
            });
        })
        .catch(() => alert("Error al cargar imágenes"));
}

function deleteImage(imageName) {
    const id = document.getElementById("vehicleId").value;

    fetch(`/rentexpress-rest-api/api/file/vehicle/${id}/${imageName}`, {
        method: "DELETE"
    })
    .then(r => {
        if (!r.ok) throw new Error();
        return r.text(); // DELETE también devuelve texto, NO JSON
    })
    .then(() => loadVehicleGallery())
    .catch(() => alert("Error al borrar imagen"));
}
