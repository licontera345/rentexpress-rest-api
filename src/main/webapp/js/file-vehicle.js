function uploadVehicleImage() {
    var id = document.getElementById("vehicleId").value;
    var fileInput = document.getElementById("vehicleFile");
    var file = fileInput.files[0];

    if (id == "" || !file) {
        alert("ID y archivo necesarios");
    } else {
        var form = new FormData();
        form.append("file", file);

        var xhr = new XMLHttpRequest();
        xhr.open("POST", "/rentexpress-rest-api/api/file/vehicle/" + id, true);

        xhr.onload = function () {
			if (xhr.status == 200 || xhr.status == 201) {
                loadVehicleGallery();
            } else {
                alert("Error al subir imagen");
            }
        };

        xhr.send(form);
    }
}

function loadVehicleGallery() {
    var id = document.getElementById("vehicleId").value;

    if (id == "") {
        alert("Falta ID de vehículo");
    } else {
        var xhr = new XMLHttpRequest();
        xhr.open("GET", "/rentexpress-rest-api/api/file/vehicle/" + id, true);

        xhr.onload = function () {
            console.log("GET imágenes:", xhr.status, xhr.responseText);

            if (xhr.status == 200) {
                var images;

                try {
                    images = JSON.parse(xhr.responseText);   // backend debe devolver JSON (lista de nombres)
                } catch (e) {
                    alert("Respuesta del servidor no es JSON válido");
                    images = null;
                }

                if (images != null) {
                    var div = document.getElementById("vehicleGallery");
                    div.innerHTML = "";

                    for (var i = 0; i < images.length; i++) {
                        var imgName = images[i];
                        var cont = document.createElement("div");

                        cont.innerHTML =
                            "<img src='/rentexpress-rest-api/api/file/vehicle/" + id + "/" + imgName + "' width='120'>" +
                            "<button onclick=\"deleteImage('" + imgName + "')\">Eliminar</button>";

                        div.appendChild(cont);
                    }
                }
            } else {
                alert("Error al cargar imágenes: " + xhr.status);
            }
        };

        xhr.send();
    }
}

function deleteImage(imageName) {
    var id = document.getElementById("vehicleId").value;

    var xhr = new XMLHttpRequest();
    xhr.open("DELETE", "/rentexpress-rest-api/api/file/vehicle/" + id + "/" + imageName, true);

    xhr.onload = function () {
        if (xhr.status == 200) {
            loadVehicleGallery();
        } else {
            alert("Error al borrar imagen");
        }
    };

    xhr.send();
}
