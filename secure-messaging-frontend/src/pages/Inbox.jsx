import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";

export default function Inbox() {
    const [messages, setMessages] = useState([]);
    const navigate = useNavigate();

    async function loadInbox(showAlert = false) {
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

            const sortedMessages = [...response.data].sort((a, b) =>
                String(b.timestamp).localeCompare(String(a.timestamp))
            );

            setMessages(sortedMessages);

            if (showAlert) {
                alert("Inbox refreshed");
            }
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
                    marginBottom: "20px",
                }}
            >
                Inbox
            </h1>

            <div
                style={{
                    display: "flex",
                    gap: "12px",
                    marginBottom: "30px",
                    flexWrap: "wrap",
                }}
            >
                <button onClick={() => navigate("/dashboard")}>Dashboard</button>
                <button onClick={() => navigate("/send")}>Send Message</button>
                <button onClick={() => loadInbox(true)}>Refresh Messages</button>
                <button onClick={handleLogout}>Logout</button>
            </div>

            <div style={{ marginTop: "30px" }}>
                {messages.map((message, index) => (
                    <div
                        key={message.id || `${message.sender}-${message.timestamp}-${index}`}
                        style={{
                            backgroundColor: "#0f172a",
                            padding: "24px",
                            borderRadius: "14px",
                            marginBottom: "24px",
                            border: "1px solid #1e293b",
                            boxShadow: "0 0 12px rgba(56,189,248,0.08)",
                            textAlign: "left",
                            maxWidth: "700px",
                        }}
                    >
                        <p style={{ color: "#38bdf8", fontWeight: "bold" }}>
                            {message.sender}
                        </p>

                        <p
                            style={{
                                fontSize: "18px",
                                lineHeight: "1.5",
                                color: "#f8fafc",
                            }}
                        >
                            {message.message}
                        </p>

                        <p style={{ fontSize: "12px", color: "#94a3b8" }}>
                            {message.timestamp}
                        </p>
                    </div>
                ))}
            </div>
        </div>
    );
}