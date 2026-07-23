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
    const [recipientBlocked, setRecipientBlocked] = useState(null);
    const [isCheckingBlock, setIsCheckingBlock] = useState(false);
    const [isUpdatingBlock, setIsUpdatingBlock] = useState(false);
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

    async function checkRecipientBlockStatus() {
        const normalizedRecipient = recipient.trim();

        if (!normalizedRecipient) {
            showNotification("error", "Please enter a recipient username.");
            return;
        }

        try {
            setIsCheckingBlock(true);

            const response = await api.get(
                `/api/blocked/check/${encodeURIComponent(normalizedRecipient)}`
            );

            setRecipientBlocked(Boolean(response.data.blocked));
        } catch (error) {
            console.error(error);

            const errorMessage =
                error.response?.data?.error ||
                "Unable to check block status";

            showNotification("error", errorMessage);
            setRecipientBlocked(null);
        } finally {
            setIsCheckingBlock(false);
        }
    }

    async function handleBlockToggle() {
        const normalizedRecipient = recipient.trim();

        if (!normalizedRecipient) {
            showNotification("error", "Please enter a recipient username.");
            return;
        }

        if (recipientBlocked === null) {
            await checkRecipientBlockStatus();
            return;
        }

        try {
            setIsUpdatingBlock(true);

            const response = recipientBlocked
                ? await api.delete(
                    `/api/blocked/${encodeURIComponent(normalizedRecipient)}`
                )
                : await api.post(
                    `/api/blocked/${encodeURIComponent(normalizedRecipient)}`
                );

            setRecipientBlocked(!recipientBlocked);
            showNotification("success", response.data.message);
        } catch (error) {
            console.error(error);

            const errorMessage =
                error.response?.data?.error ||
                "Unable to update block status";

            showNotification("error", errorMessage);
        } finally {
            setIsUpdatingBlock(false);
        }
    }

    async function handleSend() {
        try {
            if (!recipient.trim() || !message.trim()) {
                showNotification("error", "Please enter a recipient and a message.");
                return;
            }

            if (recipientBlocked === true) {
                showNotification(
                    "error",
                    "Unblock this user before sending a direct message."
                );
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

            const errorMessage =
                error.response?.data?.error ||
                "Message sending failed";

            showNotification("error", errorMessage);
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
                alignItems: "flex-start",
                padding: "clamp(8px, 1.5vh, 16px)",
                boxSizing: "border-box",
                fontFamily: "Arial, sans-serif"
            }}
        >
            <div
                style={{
                    backgroundColor: "#0f172a",
                    padding: "clamp(14px, 2vh, 20px)",
                    borderRadius: "14px",
                    width: "min(620px, 100%)",
                    maxHeight: "none",
                    overflowY: "visible",
                    boxSizing: "border-box"
                }}
            >
                <div
                    style={{
                        display: "flex",
                        gap: "12px",
                        marginBottom: "16px"
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
                        fontSize: "clamp(30px, 4.5vw, 38px)",
                        lineHeight: "1",
                        marginBottom: "12px",
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
                    onChange={(e) => {
                        setRecipient(e.target.value);
                        setRecipientBlocked(null);
                    }}
                    style={{
                        width: "100%",
                        boxSizing: "border-box",
                        padding: "10px 12px",
                        marginBottom: "10px",
                        borderRadius: "8px",
                        border: "none",
                        backgroundColor: "#1e293b",
                        color: "white"
                    }}
                />

                <button
                    type="button"
                    onClick={handleBlockToggle}
                    disabled={isCheckingBlock || isUpdatingBlock}
                    style={{
                        width: "100%",
                        marginBottom: "10px",
                        padding: "9px 12px",
                        borderRadius: "8px",
                        border: "1px solid #c8b68a",
                        backgroundColor: "#1e293b",
                        color: "#f5e6c8",
                        fontWeight: "bold",
                        cursor:
                            isCheckingBlock || isUpdatingBlock
                                ? "not-allowed"
                                : "pointer",
                        opacity:
                            isCheckingBlock || isUpdatingBlock
                                ? 0.65
                                : 1
                    }}
                >
                    {isCheckingBlock
                        ? "Checking..."
                        : isUpdatingBlock
                            ? "Updating..."
                            : recipientBlocked === true
                                ? "Unblock User"
                                : recipientBlocked === false
                                    ? "Block User"
                                    : "Check Block Status"}
                </button>

                {recipientBlocked !== null && (
                    <p
                        style={{
                            margin: "0 0 10px",
                            color: "#cbd5e1",
                            fontSize: "13px",
                            textAlign: "center"
                        }}
                    >
                        {recipientBlocked
                            ? "You have blocked this user."
                            : "You have not blocked this user."}
                    </p>
                )}
                <textarea
                    placeholder="Type your encrypted message..."
                    value={message}
                    onChange={(e) => setMessage(e.target.value)}
                    onKeyDown={(e) => {
                        if (e.key === "Enter" && !e.shiftKey) {
                            e.preventDefault();
                            handleSend();
                        }
                    }}
                    spellCheck={false}
                    data-gramm="false"
                    data-gramm_editor="false"
                    data-enable-grammarly="false"
                    style={{
                        width: "100%",
                        boxSizing: "border-box",
                        height: "105px",
                        padding: "10px 12px",
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
                        marginTop: "8px",
                        marginBottom: "8px",
                        padding: "10px",
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
                            marginBottom: "6px"
                        }}
                        title="Secure attachment"
                    >
                        <span
                            style={{
                                color: "#ffffff",
                                fontWeight: "900",
                                fontSize: "18px",
                                marginRight: "8px",
                                textShadow: "0 0 6px rgba(255, 255, 255, 0.35)",
                            }}
                        >
                            📎
                        </span>
                        Secure file
                    </label>

                    <input
                        ref={fileInputRef}
                        type="file"
                        onChange={(e) => setSelectedFile(e.target.files[0])}
                        style={{
                            width: "100%",
                            color: "#e5e7eb",
                            marginBottom: "6px"
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
        <span
            style={{
                color: "#ffffff",
                fontWeight: "900",
                fontSize: "16px",
                marginRight: "6px",
                textShadow: "0 0 6px rgba(255, 255, 255, 0.35)",
            }}
        >
            📎
        </span>
                            {selectedFile.name}
                        </div>
                    )}
                       </div>

                <div style={{ marginTop: "6px" }}>
                    <button
                        type="button"
                        onClick={() => setShowEmojiPanel(!showEmojiPanel)}
                        style={{
                            padding: "8px 12px",
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
                                marginTop: "8px",
                                flexWrap: "wrap",
                                maxHeight: "90px",
                                overflowY: "auto",
                                padding: "8px",
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
                        marginTop: "10px",
                        padding: "10px",
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
