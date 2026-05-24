
import { useState } from "react";
import logo from "../assets/branding/secure-messaging-logo.png";
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
                minHeight: "100vh",
                background: "radial-gradient(circle at center, #0f2a4a 0%, #071a33 45%, #020617 100%)",
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
                fontFamily: "Arial, sans-serif",
                padding: "20px"
            }}
        >

            <div
                style={{
                    backgroundColor: "rgba(15,23,42,0.88)",
                    padding: "36px",
                    borderRadius: "18px",
                    width: "390px",
                    border: "1px solid #1e3a8a",
                    boxShadow: "0 0 35px rgba(59,130,246,0.18)",
                    textAlign: "center"
                }}
            >

                <img
                    src={logo}
                    alt="Secure Messaging Logo"
                    style={{
                        width: "165px",
                        height: "165px",
                        objectFit: "cover",
                        objectPosition: "center",
                        borderRadius: "50%",
                        marginBottom: "18px",
                        boxShadow: "0 0 22px rgba(56, 189, 248, 0.35)"
                    }}
                />

                <h1
                    style={{
                        color: "#ffffff",
                        fontSize: "36px",
                        marginBottom: "8px",
                        fontWeight: "bold"
                    }}
                >
                    Secure Messaging
                </h1>

                <p
                    style={{
                        color: "#38bdf8",
                        marginBottom: "28px",
                        fontSize: "14px"
                    }}
                >
                    Think secure. Stay ahead.
                </p>

                <form onSubmit={handleLogin}>

                    <input
                        type="text"
                        placeholder="Username"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        style={{
                            width: "100%",
                            boxSizing: "border-box",
                            padding: "14px",
                            marginBottom: "18px",
                            borderRadius: "10px",
                            border: "1px solid #1e40af",
                            backgroundColor: "#0f172a",
                            color: "white",
                            fontSize: "16px",
                            outline: "none"
                        }}
                    />

                    <input
                        type="password"
                        placeholder="Password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        style={{
                            width: "100%",
                            boxSizing: "border-box",
                            padding: "14px",
                            marginBottom: "22px",
                            borderRadius: "10px",
                            border: "1px solid #1e40af",
                            backgroundColor: "#0f172a",
                            color: "white",
                            fontSize: "16px",
                            outline: "none"
                        }}
                    />

                    <button
                        type="submit"
                        style={{
                            width: "100%",
                            padding: "14px",
                            borderRadius: "10px",
                            border: "none",
                            background: "linear-gradient(90deg, #2563eb, #7c3aed)",
                            color: "white",
                            fontWeight: "bold",
                            fontSize: "16px",
                            cursor: "pointer",
                            marginBottom: "24px"
                        }}
                    >
                        Login
                    </button>

                </form>

                <p
                    style={{
                        color: "#94a3b8",
                        fontSize: "13px",
                        marginBottom: "18px"
                    }}
                >
                    Protected by JWT Authentication
                </p>

                <div
                    style={{
                        borderTop: "1px solid #1e293b",
                        paddingTop: "18px"
                    }}
                >

                    <p
                        style={{
                            color: "#38bdf8",
                            marginBottom: "12px",
                            fontWeight: "bold"
                        }}
                    >
                        Connect
                    </p>

                    <div
                        style={{
                            display: "flex",
                            justifyContent: "space-between",
                            gap: "10px",
                            fontSize: "12px",
                            color: "#cbd5e1",
                            marginBottom: "14px"
                        }}
                    >

                        <div>
                            <div style={{ fontWeight: "bold" }}>GitHub</div>
                            <div style={{ color: "#38bdf8" }}>
                                github.com
                            </div>
                        </div>

                        <div>
                            <div style={{ fontWeight: "bold" }}>LinkedIn</div>
                            <div style={{ color: "#38bdf8" }}>
                                linkedin.com
                            </div>
                        </div>

                        <div>
                            <div style={{ fontWeight: "bold" }}>Website</div>
                            <div style={{ color: "#38bdf8" }}>
                                brain-secure-messaging.com
                            </div>
                        </div>

                    </div>

                    <p
                        style={{
                            color: "#64748b",
                            fontSize: "11px",
                            marginTop: "10px"
                        }}
                    >
                        © 2026 Secure Messaging System
                    </p>

                </div>

            </div>

        </div>
    );
}
