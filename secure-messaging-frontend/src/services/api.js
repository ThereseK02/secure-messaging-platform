import axios from "axios";

const api = axios.create({
    baseURL: "https://secure-messaging-platform-v2.onrender.com",
});

export default api;