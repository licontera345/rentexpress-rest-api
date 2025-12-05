import HomeController from "./controllers/homeController.js";
import CatalogVehicleController from "./controllers/catalogVehicleController.js";

const App = {

    init() {
        HomeController.init();
        CatalogVehicleController.init();
    }

};

window.addEventListener("DOMContentLoaded", function () {
    App.init();
});
