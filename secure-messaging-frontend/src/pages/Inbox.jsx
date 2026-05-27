import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";

export default function Inbox() {

    const [messages, setMessages] = useState([]);

    const navigate = useNavigate();

    async function loadInbox() {

        try {

            const token = localStorage.getItem("token");

            const response = await api.post(
    "/api/messages/inbox/decrypted",
                {},
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );
            console.log("Inbox API response:", response.data);
            setMessages(
                [...response.data].sort(
                    (a, b) => new Date(b.timestamp) - new Date(a.timestamp)
                )
            );
        } catch (error) {

            console.error(error);

            alert("Failed to load inbox");
        }
    }

    useEffect(() => {

        loadInbox();

        const interval = setInterval(() => {

            loadInbox();

        }, 5000);

        return () => clearInterval(interval);

    }, []);

    function handleLogout() {

        localStorage.removeItem("token");

        navigate("/");
    }

    return (

        <div
            style={{
                minHeight: "100vh",
                backgroundColor: "#020617",
                color: "white",
                padding: "40px",
                fontFamily: "Arial, sans-serif",
            }}
        >

            <h1
                style={{
                    color: "#38bdf8",
                    fontSize: "56px",
                    marginBottom: "20px"
                }}
            >
                Inbox
            </h1>

            <div
                style={{
                    display: "flex",
                    gap: "12px",
                    marginBottom: "30px",
                    flexWrap: "wrap"
                }}
            >

                <button
                    onClick={() => navigate("/dashboard")}
                    style={{
                        padding: "10px 18px",
                        borderRadius: "8px",
                        border: "none",
                        backgroundColor: "#1e293b",
                        color: "white",
                        cursor: "pointer",
                        fontWeight: "bold"
                    }}
                >
                    Dashboard
                </button>

                <button
                    onClick={() => navigate("/send")}
                    style={{
                        padding: "10px 18px",
                        borderRadius: "8px",
                        border: "none",
                        backgroundColor: "#38bdf8",
                        cursor: "pointer",
                        fontWeight: "bold"
                    }}
                >
                    Send Message
                </button>

                <button
                    onClick={loadInbox}
                    style={{
                        padding: "10px 18px",
                        borderRadius: "8px",
                        border: "none",
                        backgroundColor: "#0ea5e9",
                        cursor: "pointer",
                        fontWeight: "bold"
                    }}
                >
                    Refresh Messages
                </button>

                <button
                    onClick={handleLogout}
                    style={{
                        padding: "10px 18px",
                        borderRadius: "8px",
                        border: "none",
                        background: "linear-gradient(90deg, #9333ea, #ec4899)",
                        color: "white",
                        cursor: "pointer",
                        fontWeight: "bold"
                    }}
                >
                    Logout
                </button>

            </div>

            <div style={{ marginTop: "30px" }}>

                {messages.map((message, index) =>(

                    <div
                        key={index}
                        style={{
                            backgroundColor: "#0f172a",
                            padding: "24px",
                            borderRadius: "14px",
                            marginBottom: "24px",
                            border: "1px solid #1e293b",
                            boxShadow: "0 0 12px rgba(56,189,248,0.08)",
                            textAlign: "left",
                            maxWidth: "700px"
                        }}
                    >

                        <p
                            style={{
                                color: "#38bdf8",
                                fontWeight: "bold",
                                marginBottom: "10px",
                                fontSize: "18px"
                            }}
                        >
                            {msg.sender}
                        </p>

                        <p
                            style={{
                                fontSize: "18px",
                                lineHeight: "1.5",
                                marginBottom: "14px",
                                color: "#f8fafc"
                            }}
                        >
                            {msg.message}
                        </p>

                        <p
                            style={{
                                fontSize: "12px",
                                color: "#94a3b8"
                            }}
                        >
                            {msg.timestamp}
                        </p>

                    </div>

                ))}

            </div>

        </div>
    );
}
