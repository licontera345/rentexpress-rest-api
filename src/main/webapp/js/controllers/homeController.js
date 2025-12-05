import HomeView from "../views/homeView.js";

const HomeController = (function () {
    function init() {
        HomeView.init();
        HomeView.render();
    }

    return {
        init: init
    };
})();

export default HomeController;
