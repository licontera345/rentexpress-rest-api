const CatalogVehicleView = (function () {
    var listElement = null;
    var countElement = null;
    var statusElement = null;

    function init() {
        listElement = document.getElementById("vehicle-list");
        countElement = document.getElementById("vehicle-count");
        statusElement = document.getElementById("catalog-status");
    }

    function renderLoading() {
        if (statusElement) {
            statusElement.textContent = "Cargando catálogo...";
        }
        if (listElement) {
            listElement.innerHTML = "";
        }
        if (countElement) {
            countElement.textContent = "";
        }
    }

    function renderError(message) {
        if (statusElement) {
            statusElement.textContent = message;
        }
        if (countElement) {
            countElement.textContent = "";
        }
        if (listElement) {
            listElement.innerHTML = "";
        }
    }

    function renderVehicles(vehicles) {
        if (statusElement) {
            statusElement.textContent = "";
        }
        if (countElement) {
            countElement.textContent = "Vehículos encontrados: " + vehicles.length;
        }
        if (!listElement) {
            return;
        }

        if (vehicles.length === 0) {
            listElement.innerHTML = "<li class=\"catalog-empty\">No se encontraron vehículos.</li>";
            return;
        }

        var html = "";
        for (var i = 0; i < vehicles.length; i++) {
            var vehicle = vehicles[i];
            var label = buildVehicleName(vehicle);
            html = html + "<li class=\"catalog-item\">" + label + "</li>";
        }
        listElement.innerHTML = html;
    }

    function buildVehicleName(vehicle) {
        var hasBrand = vehicle && vehicle.brand;
        var hasModel = vehicle && vehicle.model;
        if (hasBrand && hasModel) {
            return vehicle.brand + " " + vehicle.model;
        }
        if (hasBrand) {
            return vehicle.brand;
        }
        if (hasModel) {
            return vehicle.model;
        }
        if (vehicle && vehicle.licensePlate) {
            return "Vehículo " + vehicle.licensePlate;
        }
        return "Vehículo sin nombre";
    }

    return {
        init: init,
        renderLoading: renderLoading,
        renderError: renderError,
        renderVehicles: renderVehicles
    };
})();

export default CatalogVehicleView;
