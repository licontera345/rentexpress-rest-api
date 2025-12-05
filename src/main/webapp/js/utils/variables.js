const Global = {
    /**
     * URL base de la API REST.
     * Se calcula a partir del contexto actual para evitar dependencias rígidas.
     */
    API: (function () {
        var pathName = window.location.pathname;
        var segments = pathName.split('/').filter(function (segment) {
            return segment.length > 0;
        });
        if (segments.length > 0) {
            return '/' + segments[0] + '/api';
        }
        return '/api';
    })()
};

export default Global;
