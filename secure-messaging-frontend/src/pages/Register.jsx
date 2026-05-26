import { useState } from "react";
import api from "../services/api";
import logo from "../assets/branding/secure-messaging-logo.png";

export default function Register() {

    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");

    async function handleRegister(e) {

        e.preventDefault();

        try {

            await api.post("/users/register", {
                username,
                password
            });

            alert("Registration successful");

        } catch (error) {

            console.error(error);
            alert("Registration failed");
        }
    }

    return (

        <div
            style={{
                backgroundColor: "#021024",
                minHeight: "100vh",
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
                fontFamily: "Arial, sans-serif",
                padding: "20px"
            }}
        >

            <div
                style={{
                    backgroundColor: "#052659",
                    padding: "40px",
                    borderRadius: "20px",
                    width: "360px",
                    minHeight: "760px",
                    display: "flex",
                    flexDirection: "column",
                    justifyContent: "space-between",
                    boxShadow: "0 0 25px rgba(0,0,0,0.4)"
                }}
            >

                <img
                    src={logo}
                    alt="Secure Messaging"
                    style={{
                        width: "140px",
                        display: "block",
                        margin: "0 auto 20px auto"
                    }}
                />

                <h1
                    style={{
                        color: "white",
                        textAlign: "center",
                        marginBottom: "10px",
                        fontSize: "42px"
                    }}
                >
                    Register
                </h1>

                <p
                    style={{
                        color: "#38bdf8",
                        textAlign: "center",
                        marginBottom: "30px"
                    }}
                >
                    Create your secure account
                </p>

                <form onSubmit={handleRegister}>

                    <input
                        type="text"
                        placeholder="Username"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        style={{
                            width: "100%",
                            padding: "14px",
                            marginBottom: "24px",
                            borderRadius: "10px",
                            border: "1px solid #2563eb",
                            backgroundColor: "#0f172a",
                            color: "white",
                            fontSize: "16px",
                            boxSizing: "border-box"
                        }}
                    />

                    <input
                        type="password"
                        placeholder="Password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        style={{
                            width: "100%",
                            padding: "14px",
                            marginBottom: "30px",
                            borderRadius: "10px",
                            border: "1px solid #2563eb",
                            backgroundColor: "#0f172a",
                            color: "white",
                            fontSize: "16px",
                            boxSizing: "border-box"
                        }}
                    />

                    <button
                        type="submit"
                        style={{
                            width: "100%",
                            padding: "14px",
                            background: "linear-gradient(to right, #2563eb, #7c3aed)",
                            border: "none",
                            borderRadius: "10px",
                            color: "white",
                            fontWeight: "bold",
                            fontSize: "16px",
                            cursor: "pointer",
                            transition: "0.3s ease",
                            boxShadow: "0 4px 15px rgba(124,58,237,0.35)"
                        }}
                    >
                        Register
                    </button>

                    <div
                        style={{
                            marginTop: "80px",
                            textAlign: "center",
                            color: "#64748b"
                        }}
                    >

                        <hr
                            style={{
                                border: "0.5px solid rgba(59,130,246,0.25)",
                                marginBottom: "10px"
                            }}
                        />

                        <p
                            style={{
                                color: "#64748b",
                                marginBottom: "8px",
                                fontWeight: "500",
                                fontSize: "12px"
                            }}
                        >
                            Connect
                        </p>

                        <div
                            style={{
                                display: "flex",
                                justifyContent: "center",
                                alignItems: "flex-start",
                                gap: "28px",
                                fontSize: "11px"
                            }}
                        >

                            <div style={{ minWidth: "100px" }}>
                                <div style={{ color: "white", marginBottom: "2px" }}>
                                    GitHub
                                </div>

                                <a
                                    href="https://github.com"
                                    target="_blank"
                                    rel="noreferrer"
                                    style={{
                                        color: "#38bdf8",
                                        textDecoration: "none",
                                        whiteSpace: "nowrap"
                                    }}
                                >
                                    github.com
                                </a>
                            </div>

                            <div style={{ minWidth: "100px" }}>
                                <div style={{ color: "white", marginBottom: "2px" }}>
                                    LinkedIn
                                </div>

                                <a
                                    href="https://linkedin.com"
                                    target="_blank"
                                    rel="noreferrer"
                                    style={{
                                        color: "#38bdf8",
                                        textDecoration: "none",
                                        whiteSpace: "nowrap"
                                    }}
                                >
                                    linkedin.com
                                </a>
                            </div>

                            <div style={{ minWidth: "140px" }}>
                                <div style={{ color: "white", marginBottom: "2px" }}>
                                    Website
                                </div>

                                <a
                                    href="https://brain-secure-messaging.com"
                                    target="_blank"
                                    rel="noreferrer"
                                    style={{
                                        color: "#38bdf8",
                                        textDecoration: "none",
                                        whiteSpace: "nowrap"
                                    }}
                                >
                                    brain-secure-messaging.com
                                </a>
                            </div>

                        </div>

                        <p
                            style={{
                                marginTop: "12px",
                                fontSize: "11px",
                                color: "#64748b"
                            }}
                        >
                            © 2026 Secure Messaging System
                        </p>

                    </div>

                </form>

            </div>

        </div>
    );
}