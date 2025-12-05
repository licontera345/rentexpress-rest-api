import CatalogVehicleController from "./controllers/catalogVehicleController.js";
import HomeController from "./controllers/homeController.js";

const App = (function () {
    function init() {
        HomeController.init();
        CatalogVehicleController.init();
    }

    return {
        init: init
    };
})();

window.addEventListener("DOMContentLoaded", function () {
    App.init();
});
