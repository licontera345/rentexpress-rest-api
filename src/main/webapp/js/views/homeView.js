const HomeView = (function () {
    var container = null;

    /**
     * Inicializa la referencia al contenedor donde se insertará la introducción.
     */
    function init() {
        container = document.getElementById("home-view");
    }

    /**
     * Renderiza un bloque de introducción sencillo para la página principal.
     */
    function render() {
        if (!container) {
            return;
        }

        container.innerHTML = "";

        var intro = document.createElement("div");
        intro.className = "home-intro";

        var title = document.createElement("h2");
        title.textContent = "Bienvenido a RentExpress";

        var description = document.createElement("p");
        description.textContent = "Consulta el catálogo público de vehículos disponibles.";

        intro.appendChild(title);
        intro.appendChild(description);
        container.appendChild(intro);
    }

    return {
        init: init,
        render: render
    };
})();

export default HomeView;
