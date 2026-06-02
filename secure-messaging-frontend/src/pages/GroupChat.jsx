import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";

export default function GroupChat() {
  const navigate = useNavigate();

  const [groups, setGroups] = useState([]);
  const [selectedGroupId, setSelectedGroupId] = useState("");
  const [groupName, setGroupName] = useState("");
  const [joinGroupId, setJoinGroupId] = useState("");
const [message, setMessage] = useState("");
const [members, setMembers] = useState([]);
const [messages, setMessages] = useState([]);
const [notification, setNotification] = useState(null);

function showNotification(type, text) {
  setNotification({ type, text });

  setTimeout(() => {
    setNotification(null);
  }, 5000);
}

  async function loadGroups() {
    try {
      const response = await api.get("/api/groups/my-groups");
      setGroups(response.data);
    } catch (error) {
      console.error(error);
showNotification("error", "Failed to load groups");
    }
  }

  async function createGroup() {
    try {
      await api.post("/api/groups/create", { groupName });
      setGroupName("");
      await loadGroups();
showNotification("success", "Group created");

    } catch (error) {
      console.error(error);
showNotification("error", "Failed to create group");
    }
  }

  async function joinGroup() {
    if (!joinGroupId.trim()) {
      showNotification("error", "Please enter a group ID");
      return;
    }

    try {
      const response = await api.post(`/api/groups/${joinGroupId}/join`);
      setJoinGroupId("");
      await loadGroups();

      showNotification(
        "success",
        response.data?.status || "Joined group successfully"
      );
    } catch (error) {
      console.error(error);
      showNotification("error", "Failed to join group");
    }
  }

  async function loadMessages(groupId = selectedGroupId) {
    if (!groupId) return;

    try {
      const response = await api.get(`/api/groups/${groupId}/messages`);
      setMessages(response.data);
    } catch (error) {
      console.error(error);
showNotification("error", "Failed to load group messages");
    }
  }

async function loadMembers(groupId = selectedGroupId) {
  if (!groupId) return;

  try {
    const response = await api.get(`/api/groups/${groupId}/members`);
    setMembers(response.data);
  } catch (error) {
    console.error(error);
    showNotification("error", "Failed to load group members");
  }
}

async function refreshMessages() {
  if (!selectedGroupId) {
    showNotification("error", "Please select a group first");
    return;
  }

  await loadMessages(selectedGroupId);
  showNotification("success", "Group messages refreshed");
}
  async function sendMessage() {
    if (!selectedGroupId || !message.trim()) return;

    try {
      await api.post(`/api/groups/${selectedGroupId}/send`, { message });
      setMessage("");
      await loadMessages(selectedGroupId);
      showNotification("success", "Group message sent");
    } catch (error) {
      console.error(error);
showNotification("error", "Failed to send group message");
    }
  }

  useEffect(() => {
    loadGroups();
  }, []);

  return (
    <div style={styles.page}>
      <h1 style={styles.title}>Group Chat</h1>
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
            maxWidth: "850px",
            fontWeight: "bold"
          }}
        >
          {notification.text}
        </div>
      )}

      <div style={styles.buttonRow}>
        <button style={styles.navButton} onClick={() => navigate("/dashboard")}>
          Dashboard
        </button>

        <button style={styles.navButton} onClick={() => navigate("/inbox")}>
          Inbox
        </button>
      </div>

      <div style={styles.section}>
        <h2 style={styles.sectionTitle}>Create Group</h2>
        <input
          style={styles.input}
          placeholder="Group name"
          value={groupName}
          onChange={(e) => setGroupName(e.target.value)}
        />
        <button style={styles.primaryButton} onClick={createGroup}>
          Create Group
        </button>
      </div>

      <div style={styles.section}>
        <h2 style={styles.sectionTitle}>Join Group</h2>
        <input
          style={styles.input}
          placeholder="Group ID"
          value={joinGroupId}
          onChange={(e) => setJoinGroupId(e.target.value)}
        />
        <button style={styles.primaryButton} onClick={joinGroup}>
          Join Group
        </button>
      </div>

      <div style={styles.section}>
        <h2 style={styles.sectionTitle}>My Groups</h2>

        {groups.length === 0 ? (
          <p style={styles.muted}>No groups yet.</p>
        ) : (
          <div style={styles.groupList}>
            {groups.map((group) => (
              <button
                key={group.id}
                style={{
                  ...styles.groupButton,
                  border:
                    String(selectedGroupId) === String(group.id)
                      ? "2px solid #38bdf8"
                      : "1px solid #1e293b",
                }}
                onClick={() => {

setSelectedGroupId(group.id);
loadMessages(group.id);
loadMembers(group.id);

                }}
              >
                {group.groupName} #{group.id}
              </button>
            ))}
          </div>
        )}
      </div>

      <div style={styles.section}>

<h2 style={styles.sectionTitle}>Messages</h2>

{members.length > 0 && (
  <p style={styles.muted}>
    Members ({members.length}): {members.join(", ")}
  </p>
)}

<div style={styles.messagesBox}>

          {messages.length === 0 ? (
            <p style={styles.muted}>Select a group to view messages.</p>
          ) : (
            messages.map((msg) => (
              <div key={msg.id} style={styles.messageCard}>
                <p style={styles.sender}>{msg.sender}</p>
                <p style={styles.messageText}>{msg.message}</p>
                <p style={styles.timestamp}>{new Date(msg.timestamp + "Z").toLocaleString()}</p>
              </div>
            ))
          )}
        </div>

        <textarea
          style={styles.textarea}
          placeholder="Write a group message"
          value={message}
          onChange={(e) => setMessage(e.target.value)}
        />

        <button style={styles.sendButton} onClick={sendMessage}>
          Send Group Message
        </button>

        <button
          style={styles.refreshButton}
onClick={refreshMessages}
        >
          Refresh Messages
        </button>
      </div>
    </div>
  );
}

const styles = {
  page: {
    minHeight: "100vh",
    backgroundColor: "#020617",
    color: "#ffffff",
    padding: "40px",
    fontFamily: "Arial, sans-serif",
  },
  title: {
    color: "#38bdf8",
    fontSize: "56px",
    marginBottom: "20px",
  },
  buttonRow: {
    display: "flex",
    gap: "16px",
    marginBottom: "30px",
    flexWrap: "wrap",
  },
  navButton: {
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
  },
  section: {
    backgroundColor: "#0f172a",
    border: "1px solid #1e293b",
    borderRadius: "16px",
    padding: "24px",
    marginBottom: "24px",
    maxWidth: "850px",
    boxShadow: "0 0 12px rgba(56,189,248,0.08)",
  },
  sectionTitle: {
    color: "#38bdf8",
    marginBottom: "16px",
  },
  input: {
    display: "block",
    width: "100%",
    maxWidth: "420px",
    padding: "14px",
    marginBottom: "14px",
    borderRadius: "10px",
    border: "1px solid #38bdf8",
    backgroundColor: "#020617",
    color: "#ffffff",
    fontSize: "16px",
  },
  primaryButton: {
    background: "linear-gradient(135deg, #4f46e5, #2563eb)",
    color: "#ffffff",
    border: "none",
    borderRadius: "14px",
    padding: "14px 22px",
    fontWeight: "700",
    cursor: "pointer",
  },
  groupList: {
    display: "flex",
    gap: "12px",
    flexWrap: "wrap",
  },
  groupButton: {
    backgroundColor: "#020617",
    color: "#ffffff",
    borderRadius: "14px",
    padding: "14px 18px",
    cursor: "pointer",
  },
  messagesBox: {
    minHeight: "160px",
    marginBottom: "16px",
  },
  messageCard: {
    backgroundColor: "#020617",
    border: "1px solid #1e293b",
    borderRadius: "14px",
    padding: "16px",
    marginBottom: "12px",
  },
  sender: {
    color: "#38bdf8",
    fontWeight: "700",
  },
  messageText: {
    color: "#f8fafc",
    fontSize: "16px",
  },
  timestamp: {
    color: "#94a3b8",
    fontSize: "12px",
  },
  textarea: {
    display: "block",
    width: "100%",
    maxWidth: "700px",
    minHeight: "90px",
    padding: "14px",
    borderRadius: "10px",
    border: "1px solid #38bdf8",
    backgroundColor: "#020617",
    color: "#ffffff",
    fontSize: "16px",
    marginBottom: "14px",
  },
  sendButton: {
    background: "linear-gradient(135deg, #ec4899, #8b5cf6)",
    color: "#ffffff",
    border: "none",
    borderRadius: "14px",
    padding: "14px 22px",
    fontWeight: "700",
    cursor: "pointer",
    marginRight: "12px",
  },
  refreshButton: {
    backgroundColor: "#1e3a8a",
    color: "#ffffff",
    border: "2px solid #38bdf8",
    borderRadius: "14px",
    padding: "14px 22px",
    fontWeight: "700",
    cursor: "pointer",
  },
  muted: {
    color: "#94a3b8",
  },
};
