import { useState } from "react";
import api from "../services/api";

export default function SendMessage() {

    const [recipient, setRecipient] = useState("");
    const [message, setMessage] = useState("");

    async function handleSend() {

        try {

            const token = localStorage.getItem("token");

            await api.post(
                  "/api/messages/send",
                {
                    receiver: recipient,
                    message: message
                },
                {
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                }
            );

            alert("Message sent successfully");

        } catch (error) {

            console.error(error);
            alert("Message sending failed");
        }
    }

    return (

        <div
            style={{
                minHeight: "100vh",
                backgroundColor: "#020617",
                color: "white",
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
                fontFamily: "Arial, sans-serif"
            }}
        >

            <div
                style={{
                    backgroundColor: "#0f172a",
                    padding: "40px",
                    borderRadius: "14px",
                    width: "520px"
                }}
            >

                <h1
                    style={{
                        color: "#38bdf8",
                        fontSize: "48px",
                        lineHeight: "1.15",
                        marginBottom: "32px",
                        textAlign: "center",
                        fontWeight: "bold"
                    }}
                >
                    Send Secure Message
                </h1>

                <input
                    type="text"
                    placeholder="Recipient username"
                    value={recipient}
                    onChange={(e) => setRecipient(e.target.value)}
                    style={{
                        width: "100%",
                        padding: "14px",
                        marginBottom: "20px",
                        borderRadius: "8px",
                        border: "none",
                        backgroundColor: "#1e293b",
                        color: "white"
                    }}
                />

                <textarea
                    placeholder="Type your encrypted message..."
                    value={message}
                    onChange={(e) => setMessage(e.target.value)}
                    style={{
                        width: "100%",
                        height: "160px",
                        padding: "14px",
                        borderRadius: "8px",
                        border: "none",
                        backgroundColor: "#1e293b",
                        color: "white",
                        resize: "none"
                    }}
                />

                <button
                    onClick={handleSend}
                    style={{
                        width: "100%",
                        marginTop: "24px",
                        padding: "14px",
                        borderRadius: "8px",
                        border: "none",
                        backgroundColor: "#38bdf8",
                        fontWeight: "bold",
                        cursor: "pointer"
                    }}
                >
                    Send Message
                </button>

            </div>

        </div>
    );
}
