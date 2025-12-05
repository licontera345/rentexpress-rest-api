import CatalogVehicleController from "./controllers/catalogVehicleController.js";
import HomeView from "./views/homeView.js";

const App = (function () {
    function init() {
        HomeView.init();
        HomeView.render();
        CatalogVehicleController.init();
    }

    return {
        init: init
    };
})();

window.addEventListener("DOMContentLoaded", function () {
    App.init();
});
