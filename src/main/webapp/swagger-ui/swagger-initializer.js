window.onload = function() {
  //<editor-fold desc="Changeable Configuration Block">

  // the following lines will be replaced by docker/configurator, when it runs in a docker-container
  const swaggerPathIndex = window.location.pathname.indexOf("/swagger-ui");
  const basePath = swaggerPathIndex !== -1
    ? window.location.pathname.substring(0, swaggerPathIndex)
    : window.location.pathname.replace(/\/?$/, "");
  const apiUrl = `${basePath.replace(/\/?$/, "")}/api/openapi.json`;

  window.ui = SwaggerUIBundle({
    url: apiUrl,
    dom_id: '#swagger-ui',
    deepLinking: true,
    presets: [
      SwaggerUIBundle.presets.apis,
      SwaggerUIStandalonePreset
    ],
    plugins: [
      SwaggerUIBundle.plugins.DownloadUrl
    ],
    layout: "StandaloneLayout"
  });

  //</editor-fold>
};
