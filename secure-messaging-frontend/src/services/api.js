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
        if (error.response && error.response.status === 401) {
            localStorage.removeItem("token");
            localStorage.setItem("sessionExpired", "true");
            window.location.href = "/";
        }

        return Promise.reject(error);
    }
);

export default api;
