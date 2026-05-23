import { useState } from "react";
import api from "../services/api";
import { useNavigate } from "react-router-dom";

export default function Login() {

    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const navigate = useNavigate();

    async function handleLogin(e) {
        e.preventDefault();

        try {

            const response = await api.post("/users/login", {
                username,
                password,
            });

            localStorage.setItem("token", response.data.token);

            navigate("/dashboard");

        } catch (error) {

            console.error(error);
            alert("Login failed");
        }
    }

    return (

        <div
            style={{
                backgroundColor: "#1e293b",
                height: "100vh",
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
                fontFamily: "Arial, sans-serif"
            }}
        >

            <div
                style={{
                    backgroundColor: "#334155",
                    padding: "40px",
                    borderRadius: "12px",
                    width: "350px",
                    boxShadow: "0 0 20px rgba(0,0,0,0.3)"
                }}
            >

                <h1
                    style={{
                        color: "#38bdf8",
                        textAlign: "center",
                        marginBottom: "30px"
                    }}
                >
                    Secure Messaging
                </h1>

                <form onSubmit={handleLogin}>

                    <input
                        type="text"
                        placeholder="Username"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        style={{
                            width: "100%",
                            padding: "12px",
                            marginBottom: "20px",
                            borderRadius: "8px",
                            border: "none",
                            backgroundColor: "#475569",
                            color: "white",
                            fontSize: "16px"
                        }}
                    />

                    <input
                        type="password"
                        placeholder="Password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        style={{
                            width: "100%",
                            padding: "12px",
                            marginBottom: "20px",
                            borderRadius: "8px",
                            border: "none",
                            backgroundColor: "#334155",
                            color: "white",
                            fontSize: "16px"
                        }}
                    />

                    <button
                        type="submit"
                        style={{
                            width: "100%",
                            padding: "12px",
                            borderRadius: "8px",
                            border: "none",
                            backgroundColor: "#60a5fa",
                            color: "#0f172a",
                            fontWeight: "bold",
                            fontSize: "16px",
                            cursor: "pointer"
                        }}
                    >
                        Login
                    </button>

                </form>

            </div>

        </div>
    );
}