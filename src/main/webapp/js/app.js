import CatalogVehicleController from "./controllers/catalogVehicleController.js";

const App = (function () {
    function init() {
        CatalogVehicleController.init();
    }

    return {
        init: init
    };
})();

window.addEventListener("DOMContentLoaded", function () {
    App.init();
});
