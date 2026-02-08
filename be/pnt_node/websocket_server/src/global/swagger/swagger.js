import swaggerJsdoc from "swagger-jsdoc";

const options = {
    definition: {
        openapi: "3.0.0",
        info: {
            title: "WebSocket Server API",
            version: "1.0.0",
            description: "API documentation for the WebSocket Server",
        },
        servers: [
            {
                url: "http://localhost:8090",
            },
        ],
    },
    apis: ["./src/rest/**/*.js", "./src/global/swagger/apis/**/*.yaml"], // Path to the API docs
};

const specs = swaggerJsdoc(options);

export default specs;
