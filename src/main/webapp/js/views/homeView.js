const HomeView = {

    container: "#home-view",

    render() {
        var c = document.querySelector(this.container);
        if (!c) {
            return;
        }

        var html = `
            <div class="home-intro">
                <h2>Bienvenido a RentExpress</h2>
                <p>Consulta el catálogo público de vehículos disponibles.</p>
            </div>
        `;

        c.innerHTML = html;
    }
};

export default HomeView;
