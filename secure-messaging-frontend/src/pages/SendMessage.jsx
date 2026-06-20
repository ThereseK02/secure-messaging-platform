import { useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";

export default function SendMessage() {
    const navigate = useNavigate();
    const [recipient, setRecipient] = useState("");
    const [message, setMessage] = useState("");
    const [selectedFile, setSelectedFile] = useState(null);
    const [isUploadingAttachment, setIsUploadingAttachment] = useState(false);
    const [showEmojiPanel, setShowEmojiPanel] = useState(false);
    const [notification, setNotification] = useState(null);
    const fileInputRef = useRef(null);

    const emojiOptions = [
        "😀", "😂", "😊", "😍", "🥰", "😎", "🤔", "😭",
        "👍", "👏", "🙏", "💪", "❤️", "💙", "✨", "🔥",
        "🎉", "🥳", "🎁", "🎀", "💝", "💐", "🧸", "🍫",
        "💌", "💎", "🌟", "✅", "🔒", "🔐", "🛡️", "💬"
    ];

    function addEmoji(emoji) {
        setMessage((currentMessage) =>
            `${currentMessage}${emoji}`);
        setShowEmojiPanel(false);
    }
    function showNotification(type, text) {
        setNotification({ type, text });

        setTimeout(() => {
            setNotification(null);
        }, 3500);
    }

    async function handleSend() {
        try {
            if (!recipient.trim() || !message.trim()) {
                showNotification("error", "Please enter a recipient and a message.");
                return;
            }

            const token = localStorage.getItem("token");

            const messageResponse = await api.post(
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

            const messageId = messageResponse.data.messageId;

            if (selectedFile) {
                setIsUploadingAttachment(true);

                const formData = new FormData();
                formData.append("receiver", recipient);
                formData.append("file", selectedFile);
                formData.append("messageId", messageId);

                await api.post("/api/attachments/upload", formData, {
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                });

                setSelectedFile(null);

                if (fileInputRef.current) {
                    fileInputRef.current.value = "";
                }
            }

            setMessage("");

            showNotification(
                "success",
                selectedFile
                    ? "Message and attachment sent successfully"
                    : "Message sent successfully"
            );

        } catch (error) {
            console.error(error);
            showNotification("error", "Message sending failed");
        } finally {
            setIsUploadingAttachment(false);
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
                    padding: "28px",
                    borderRadius: "14px",
                    width: "520px",
                    maxHeight: "92vh",
                    overflowY: "auto"
                }}
            >
                <div
                    style={{
                        display: "flex",
                        gap: "14px",
                        marginBottom: "24px"
                    }}
                >
                    <button
                        type="button"
                        onClick={() => navigate("/dashboard")}
                        style={{
                            flex: 1,
                            padding: "12px",
                            borderRadius: "10px",
                            border: "1px solid #38bdf8",
                            backgroundColor: "#1e3a8a",
                            color: "white",
                            fontWeight: "bold",
                            cursor: "pointer"
                        }}
                    >
                        Dashboard
                    </button>

                    <button
                        type="button"
                        onClick={() => navigate("/inbox")}
                        style={{
                            flex: 1,
                            padding: "12px",
                            borderRadius: "10px",
                            border: "1px solid #38bdf8",
                            backgroundColor: "#1e3a8a",
                            color: "white",
                            fontWeight: "bold",
                            cursor: "pointer"
                        }}
                    >
                        Inbox
                    </button>
                </div>

                <h1
                    style={{
                        color: "#38bdf8",
                        fontSize: "42px",
                        lineHeight: "1.1",
                        marginBottom: "24px",
                        textAlign: "center",
                        fontWeight: "bold"
                    }}
                >
                    Send Secure Message
                </h1>

                {notification && (
                    <div
                        style={{
                            backgroundColor:
                                notification.type === "success"
                                    ? "rgba(34, 197, 94, 0.12)"
                                    : "rgba(239, 68, 68, 0.12)",
                            border:
                                notification.type === "success"
                                    ? "1px solid #22c55e"
                                    : "1px solid #ef4444",
                            color:
                                notification.type === "success"
                                    ? "#bbf7d0"
                                    : "#fecaca",
                            padding: "12px",
                            borderRadius: "10px",
                            marginBottom: "18px",
                            fontSize: "14px",
                            textAlign: "center",
                            fontWeight: "bold"
                        }}
                    >
                        {notification.text}
                    </div>
                )}

                <input
                    type="text"
                    placeholder="Recipient username"
                    value={recipient}
                    onChange={(e) => setRecipient(e.target.value)}
                    style={{
                        width: "100%",
                        boxSizing: "border-box",
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
                    spellCheck={false}
                    data-gramm="false"
                    data-gramm_editor="false"
                    data-enable-grammarly="false"
                    style={{
                        width: "100%",
                        boxSizing: "border-box",
                        height: "190px",
                        padding: "14px",
                        borderRadius: "8px",
                        border: "none",
                        backgroundColor: "#1e293b",
                        color: "white",
                        resize: "none"
                    }}
                />

                <div
                    style={{
                        width: "100%",
                        marginBottom: "18px",
                        padding: "14px",
                        borderRadius: "12px",
                        backgroundColor: "#0f172a",
                        border: "1px solid #334155",
                        boxSizing: "border-box"
                    }}
                >
                    <label
                        style={{
                            display: "block",
                            color: "#dbeafe",
                            fontWeight: "bold",
                            marginBottom: "10px"
                        }}
                    >
                        Secure Attachment
                    </label>

                    <input
                        ref={fileInputRef}
                        type="file"
                        onChange={(e) => setSelectedFile(e.target.files[0])}
                        style={{
                            width: "100%",
                            color: "#e5e7eb",
                            marginBottom: "10px"
                        }}
                    />

                    {selectedFile && (
                        <div
                            style={{
                                color: "#93c5fd",
                                fontSize: "14px",
                                marginBottom: "10px"
                            }}
                        >
                            Selected file: {selectedFile.name}
                        </div>
                    )}

                       </div>

                <div style={{ marginTop: "14px" }}>
                    <button
                        type="button"
                        onClick={() => setShowEmojiPanel(!showEmojiPanel)}
                        style={{
                            padding: "10px 14px",
                            borderRadius: "10px",
                            border: "1px solid #38bdf8",
                            backgroundColor: "#1e293b",
                            color: "#ffffff",
                            cursor: "pointer",
                            fontWeight: "bold"
                        }}
                    >
                        😊 Emojis
                    </button>

                    {showEmojiPanel && (
                        <div
                            style={{
                                display: "flex",
                                gap: "8px",
                                marginTop: "12px",
                                flexWrap: "wrap",
                                maxHeight: "120px",
                                overflowY: "auto",
                                padding: "10px",
                                borderRadius: "10px",
                                border: "1px solid #334155",
                                backgroundColor: "#0f172a"
                            }}
                        >
                            {emojiOptions.map((emoji) => (
                                <button
                                    key={emoji}
                                    type="button"
                                    onClick={() => addEmoji(emoji)}
                                    style={{
                                        width: "34px",
                                        height: "34px",
                                        borderRadius: "8px",
                                        border: "1px solid #38bdf8",
                                        backgroundColor: "#1e293b",
                                        cursor: "pointer",
                                        fontSize: "16px"
                                    }}
                                >
                                    {emoji}
                                </button>
                            ))}
                        </div>
                    )}
                </div>
                <button
                    onClick={handleSend}
                    disabled={isUploadingAttachment}
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
                    {isUploadingAttachment ? "Sending..." : "Send Message"}
                </button>
            </div>
        </div>
    );
}
