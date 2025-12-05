import CatalogVehicleService from "../services/catalogVehicleService.js";
import CatalogVehicleView from "../views/catalogVehicleView.js";

const CatalogVehicleController = (function () {
    function init() {
        CatalogVehicleView.init();
        loadCatalog();
    }

    function loadCatalog() {
        CatalogVehicleView.renderLoading();
        CatalogVehicleService.getVehicles()
            .then(function (data) {
                var vehicles = normalizeVehicles(data);
                CatalogVehicleView.renderVehicles(vehicles);
            })
            .catch(function (error) {
                CatalogVehicleView.renderError(error.message);
            });
    }

    function normalizeVehicles(data) {
        if (Array.isArray(data)) {
            return data;
        }
        if (data && Array.isArray(data.content)) {
            return data.content;
        }
        if (data && Array.isArray(data.vehicles)) {
            return data.vehicles;
        }
        return [];
    }

    return {
        init: init
    };
})();

export default CatalogVehicleController;
