const CatalogVehicleView = {

    container: "#vehicle-list",
    count: "#vehicle-count",
    status: "#catalog-status",

    render(vehicles) {
        var html = "";

        if (Array.isArray(vehicles) && vehicles.length > 0) {

            for (var i = 0; i < vehicles.length; i++) {
                var v = vehicles[i];

                var brand = v.brand;
                var model = v.model;
                var plate = v.licensePlate;
                var name = "";

                if (brand && model && plate) {
                    name = brand + " " + model + " (" + plate + ")";
                } else if (brand && model) {
                    name = brand + " " + model;
                } else if (brand) {
                    name = brand;
                } else if (model) {
                    name = model;
                } else if (plate) {
                    name = "Vehículo (" + plate + ")";
                } else {
                    name = "Vehículo sin nombre";
                }

                html = html + "<li class='catalog-item'>" + name + "</li>";
            }

            document.querySelector(this.container).innerHTML = html;
            document.querySelector(this.count).textContent =
                "Vehículos encontrados: " + vehicles.length;
            document.querySelector(this.status).textContent = "";

        } else {
            document.querySelector(this.container).innerHTML =
                "<li class='catalog-empty'>No se encontraron vehículos.</li>";
            document.querySelector(this.count).textContent = "";
            document.querySelector(this.status).textContent = "";
        }
    },

    renderLoading() {
        document.querySelector(this.status).textContent = "Cargando catálogo...";
        document.querySelector(this.container).innerHTML = "";
        document.querySelector(this.count).textContent = "";
    },

    renderError(msg) {
        document.querySelector(this.status).textContent = msg;
        document.querySelector(this.container).innerHTML = "";
        document.querySelector(this.count).textContent = "";
    }
};

export default CatalogVehicleView;
