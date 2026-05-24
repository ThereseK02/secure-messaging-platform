import axios from "axios";

const api = axios.create({
baseURL: "http://44.249.176.34:8080"
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

export default api;
