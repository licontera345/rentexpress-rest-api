import Global from "../utils/variables.js";

const CatalogVehicleService = (function () {
    function getVehicles() {
        return fetch(Global.API + "/vehicles/open/search")
            .then(function (response) {
                if (!response.ok) {
                    throw new Error("No se pudo recuperar el catálogo (" + response.status + ")");
                }
                return response.json();
            });
    }

    return {
        getVehicles: getVehicles
    };
})();

export default CatalogVehicleService;
