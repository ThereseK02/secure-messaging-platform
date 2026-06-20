import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";

export default function Inbox() {
  const [messages, setMessages] = useState([]);
  const [attachments, setAttachments] = useState([]);
  const [notification, setNotification] = useState(null);
  const navigate = useNavigate();

  function showNotification(type, text) {
    setNotification({ type, text });

    setTimeout(() => {
      setNotification(null);
    }, 3500);
  }

  async function loadInbox(showAlert = false) {
    try {
      const token = localStorage.getItem("token");

      const messageResponse = await api.post(
          "/api/messages/inbox/decrypted",
          {},
          { headers: { Authorization: `Bearer ${token}` } }
      );

      const attachmentResponse = await api.get(
          "/api/attachments/inbox",
          { headers: { Authorization: `Bearer ${token}` } }
      );

      const sortedMessages = [...messageResponse.data].sort((a, b) =>
          String(b.timestamp).localeCompare(String(a.timestamp))
      );

      const sortedAttachments = [...attachmentResponse.data].sort((a, b) =>
          String(b.timestamp).localeCompare(String(a.timestamp))
      );

      setMessages(sortedMessages);
      setAttachments(sortedAttachments);

      if (showAlert) {
        showNotification("success", "Inbox refreshed");
      }
    } catch (error) {
      console.error(error);
      showNotification("error", "Failed to load inbox");
    }
  }
  useEffect(() => {
    loadInbox(false);

    const interval = setInterval(() => {
      loadInbox(false);
    }, 5000);

    return () => clearInterval(interval);
  }, []);

  function handleLogout() {
    localStorage.removeItem("token");
    navigate("/");
  }

  function formatFileSize(bytes) {
    if (!bytes && bytes !== 0) {
      return "Unknown size";
    }

    if (bytes < 1024) {
      return `${bytes} B`;
    }

    if (bytes < 1024 * 1024) {
      return `${(bytes / 1024).toFixed(1)} KB`;
    }

    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  }

  async function handleDownloadAttachment(attachment) {
    try {
      const response = await api.get(
          `/api/attachments/${attachment.id}/download`,
          { responseType: "blob" }
      );

      const blob = new Blob([response.data], {
        type: attachment.contentType || "application/octet-stream",
      });

      const downloadUrl = window.URL.createObjectURL(blob);
      const link = document.createElement("a");

      link.href = downloadUrl;
      link.download = attachment.filename || "secure-attachment";
      document.body.appendChild(link);
      link.click();

      link.remove();
      window.URL.revokeObjectURL(downloadUrl);

      showNotification("success", "Attachment downloaded");
    } catch (error) {
      console.error(error);
      showNotification("error", "Attachment download failed");
    }
  }

  const buttonStyle = {
    backgroundColor: "#1e3a8a",
    color: "#ffffff",
    border: "2px solid #38bdf8",
    borderRadius: "14px",
    padding: "14px 22px",
    fontWeight: "700",
    fontSize: "16px",
    cursor: "pointer",
    width: "180px",
    height: "54px",
    boxShadow: "0 0 10px rgba(56, 189, 248, 0.25)",
  };

  const logoutStyle = {
    ...buttonStyle,
    background: "linear-gradient(135deg, #ec4899, #8b5cf6)",
    color: "#ffffff",
    border: "none",
  };

  return (
    <div
      style={{
        minHeight: "100vh",
        backgroundColor: "#020617",
        color: "#ffffff",
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
            marginBottom: "20px",
            maxWidth: "700px",
            fontWeight: "bold"
          }}
        >
          {notification.text}
        </div>
      )}

      <div
        style={{
          display: "flex",
          gap: "18px",
          marginBottom: "30px",
          flexWrap: "wrap",
        }}

      >
        <button style={buttonStyle} onClick={() => navigate("/dashboard")}>
          Dashboard
        </button>

        <button style={buttonStyle} onClick={() => navigate("/send")}>
          Send Message
        </button>

        <button style={buttonStyle} onClick={() => loadInbox(true)}>
          Refresh Messages
        </button>

        <button style={logoutStyle} onClick={handleLogout}>
          Logout
        </button>
      </div>


         <h2
          style={{
            color: "#38bdf8",
            fontSize: "30px",
            marginBottom: "18px",
          }}
      >
        Messages
      </h2>

      <div style={{ marginTop: "30px" }}>
        {messages.length === 0 ? (
            <p style={{ color: "#94a3b8" }}>No messages received yet.</p>
        ) : (
            messages.map((message, index) => {
              const messageAttachments = attachments.filter(
                  (attachment) =>
                      attachment.messageId &&
                      message.id &&
                      String(attachment.messageId) === String(message.id)
              );

              return (
                  <div
                      key={message.id || index}
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
                    <p
                        style={{
                          color: "#38bdf8",
                          fontWeight: "bold",
                          fontSize: "20px",
                        }}
                    >
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

                    {messageAttachments.length > 0 && (
                        <div
                            style={{
                              marginTop: "18px",
                              padding: "16px",
                              borderRadius: "12px",
                              backgroundColor: "#020617",
                              border: "1px solid #334155",
                            }}
                        >
                          <p
                              style={{
                                color: "#93c5fd",
                                fontWeight: "bold",
                                marginBottom: "10px",
                              }}
                          >
                            Attachment
                          </p>

                          {messageAttachments.map((attachment) => (
                              <div key={attachment.id} style={{ marginBottom: "12px" }}>
                                <p
                                    style={{
                                      color: "#f8fafc",
                                      fontSize: "16px",
                                      marginBottom: "6px",
                                    }}
                                >
                                  {attachment.filename}
                                </p>

                                <button
                                    type="button"
                                    onClick={() => handleDownloadAttachment(attachment)}
                                    style={{
                                      padding: "10px 16px",
                                      borderRadius: "10px",
                                      border: "none",
                                      background: "linear-gradient(135deg, #0ea5e9, #2563eb)",
                                      color: "white",
                                      fontWeight: "bold",
                                      cursor: "pointer",
                                    }}
                                >
                                  Download
                                </button>
                              </div>
                          ))}
                        </div>
                    )}

                    <p
                        style={{
                          marginTop: "14px",
                          fontSize: "12px",
                          color: "#94a3b8",
                        }}
                    >
                      {new Date(message.timestamp).toLocaleString()}
                    </p>
                  </div>
              );
            })
        )}
      </div>
    </div>
  );
}
