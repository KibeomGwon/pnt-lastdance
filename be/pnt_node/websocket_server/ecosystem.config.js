const path = require('path');

module.exports = {
    apps: [
        {
            name: "websocket-server-1",
            script: "./src/server-register.js",
            env: {
                PORT: 8090
            },
            output: path.resolve(__dirname, '../../../logs/pm2/access.log'),
            error: path.resolve(__dirname, '../../../logs/pm2/error.log'),
            merge_logs: true,
            log_date_format: "YYYY-MM-DD HH:mm:ss"
        },
        {
            name: "websocket-server-2",
            script: "./src/server-register.js",
            env: {
                PORT: 8091
            },
            output: path.resolve(__dirname, '../../../logs/pm2/access.log'),
            error: path.resolve(__dirname, '../../../logs/pm2/error.log'),
            merge_logs: true,
            log_date_format: "YYYY-MM-DD HH:mm:ss"
        }
    ]
}
