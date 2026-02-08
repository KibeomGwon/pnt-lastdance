export const handleErrors = [400, 401, 403, 404];

export const sendError = (socket, error, defaultName = "UnknownError") => {
    const timestamp = new Date().toISOString();

    if (handleErrors.includes(error.code)) {
        socket.emit("error", {
            code: error.code,
            message: error.message || error.text,
            error: error.name || defaultName,
            timestamp
        });
    } else {
        socket.emit("error", {
            code: 500,
            message: "서버에 문제가 있습니다.",
            error: "InternalServerError",
            timestamp
        });
    }
};
