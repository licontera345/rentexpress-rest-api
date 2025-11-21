function uploadVehicleImage() {
    var id = document.getElementById("vehicleId").value;
    var file = document.getElementById("vehicleFile").files[0];

    if (id == "" || !file) {
        alert("ID y archivo necesarios");
    } else {

        var form = new FormData();
        form.append("file", file);

        var xhr = new XMLHttpRequest();
        xhr.open("POST", "/rentexpress-rest-api/api/file/vehicle/" + id, true);

        xhr.onload = function () {
            if (xhr.status == 200) {
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
        // no hacemos nada
    } else {
        var xhr = new XMLHttpRequest();
        xhr.open("GET", "/rentexpress-rest-api/api/file/vehicle/" + id, true);

        xhr.onload = function () {
            if (xhr.status == 200) {
                var images = JSON.parse(xhr.responseText);
                var div = document.getElementById("vehicleGallery");
                div.innerHTML = "";

                for (var i = 0; i < images.length; i++) {
                    var img = images[i];
                    var container = document.createElement("div");

                    container.innerHTML =
                        "<img src='/rentexpress-rest-api/api/file/vehicle/" + id + "/" + img + "' width='120'>" +
                        "<button onclick=\"deleteImage('" + img + "')\">Eliminar</button>";

                    div.appendChild(container);
                }
            } else {
                alert("Error al cargar imágenes");
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
