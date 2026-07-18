import axios from "axios";

const api = axios.create({
    baseURL: "https://brain-secure-messaging.com",
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem("token");

    if (
        token &&
        !config.url.includes("/users/login") &&
        !config.url.includes("/users/register")
    ) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
});

api.interceptors.response.use(
    (response) => response,
    (error) => {
        const requestUrl =
            error.config?.url || "";

        const isPublicAuthRequest =
            requestUrl.includes("/users/login") ||
            requestUrl.includes("/users/register");

        const isUnauthorized =
            error.response?.status === 401 ||
            error.response?.status === 403;

        if (isUnauthorized && !isPublicAuthRequest) {
            localStorage.removeItem("token");
            localStorage.removeItem("username");
            localStorage.setItem("sessionExpired", "true");
            window.location.href = "/login";
        }

        return Promise.reject(error);
    }
);

export default api;
