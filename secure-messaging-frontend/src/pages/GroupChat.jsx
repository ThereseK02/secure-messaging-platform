import { useEffect, useRef, useState } from "react";
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
  const currentUsername = localStorage.getItem("username");
  const messagesEndRef = useRef(null);
  const [showConversation, setShowConversation] = useState(false);

  function showNotification(type, text) {
    setNotification({type, text});

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
      await api.post("/api/groups/create", {groupName});
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
      await api.post(`/api/groups/${selectedGroupId}/send`, {message});
      setMessage("");
      await loadMessages(selectedGroupId);
      showNotification("success", "Group message sent");
    } catch (error) {
      console.error(error);
      showNotification("error", "Failed to send group message");
    }
  }

  async function leaveGroup() {
    if (!selectedGroupId) {
      showNotification("error", "Please select a group first");
      return;
    }

    try {
      await api.delete(`/api/groups/${selectedGroupId}/leave`);

      showNotification("success", "You left the group successfully");
      setSelectedGroupId("");
      setMessages([]);
      setMembers([]);
      await loadGroups();
    } catch (error) {
      console.error(error);
      showNotification(
          "error",
          error.response?.data?.error || "Failed to leave group"
      );
    }
  }

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({behavior: "smooth"});
  
}, [messages]);


useEffect(() => {
    loadGroups();
}, []);

useEffect(() => {
    if (!selectedGroupId || !showConversation) return;

    loadMessages(selectedGroupId);

    const intervalId = setInterval(() => {
        loadMessages(selectedGroupId);
    }, 3000);

    return () => clearInterval(intervalId);
}, [selectedGroupId, showConversation]);


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
                      notification.type === "success" ? "#bbf7d0" : "#fecaca",
                  padding: "12px",
                  borderRadius: "10px",
                  marginBottom: "20px",
                  maxWidth: "850px",
                  fontWeight: "bold",
                }}
            >
              {notification.text}
            </div>
        )}

        <div style={styles.navButtonRow}>
          <button style={styles.navButton} onClick={() => navigate("/dashboard")}>
            Dashboard
          </button>

          <button style={styles.navButton} onClick={() => navigate("/inbox")}>
            Inbox
          </button>
        </div>

        {!showConversation && (
            <>
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
                                setShowConversation(true);
                                loadMessages(group.id);
                                loadMembers(group.id);
                              }}
                          >
                            <div>
                              <div>
                                {group.groupName} #{group.id}
                              </div>

                              {group.createdBy && (
                                  <div style={styles.adminBadge}>
                                    Admin: {group.createdBy}
                                  </div>
                              )}
                            </div>
                          </button>
                      ))}
                    </div>
                )}
              </div>
            </>
        )}

        {showConversation && (
            <>
              <div style={styles.section}>
                <button
                    style={styles.backButton}
                    onClick={() => {
                      setShowConversation(false);
                      setMessages([]);
                      setMembers([]);
                    }}
                >
                  Back to Groups
                </button>

		<h2 style={styles.groupTitle}>
		  {groups.find(g => String(g.id) === String(selectedGroupId))?.groupName}
		</h2>

                <h2 style={styles.sectionTitle}>Group Membership</h2>

                {members.length > 0 && (
                    <div style={styles.memberBox}>
                      <p style={styles.membersLabel}>
                        Group Members ({members.length})
                      </p>

                      <div style={styles.memberPills}>
                        {members.slice(0, 5).map((member) => (
                            <span key={member} style={styles.memberPill}>
                    {member}
                  </span>
                        ))}

                        {members.length > 5 && (
                            <span style={styles.memberPill}>
                    +{members.length - 5} more
                  </span>
                        )}
                      </div>
                    </div>
                )}

                {selectedGroupId && (
                    <button style={styles.leaveButton} onClick={leaveGroup}>
                      Leave Group
                    </button>
                )}
              </div>

              <div style={styles.section}>
                <h2 style={styles.sectionTitle}>Messages</h2>

		<p style={styles.messageCount}>
		  {messages.length} messages
		</p>

		<p style={styles.liveIndicator}>
  			🟢 Live Refresh: ON
		</p>

                <div style={styles.messagesBox}>
                  {messages.length === 0 ? (
                      <p style={styles.muted}>No messages yet.</p>
                  ) : (
                      messages.map((msg) => {
                        const isMine = msg.sender === currentUsername;

                        return (
                            <div
                                key={msg.id}
                                style={{
                                  ...styles.messageRow,
                                  justifyContent: isMine ? "flex-end" : "flex-start",
                                }}
                            >
                              <div
                                  style={
                                    isMine
                                        ? styles.myMessageBubble
                                        : styles.otherMessageBubble
                                  }
                              >

				<p style={styles.sender}>
				  {isMine ? "You" : msg.sender} •{" "}
				  {new Date(msg.timestamp + "Z").toLocaleTimeString([], {
    					hour: "numeric",
    					minute: "2-digit",
  				})}
				</p>

<p style={styles.messageText}>{msg.message}</p>

                              </div>
                            </div>
                        );
                      })
                  )}

                  <div ref={messagesEndRef}/>
                </div>

                <textarea
                    style={styles.textarea}
                    placeholder="Write a group message"
                    value={message}
                    onChange={(e) => setMessage(e.target.value)}
                />

                <div style={styles.buttonRow}>
                  <button style={styles.sendButton} onClick={sendMessage}>
                    Send Group Message
                  </button>

                  <button style={styles.refreshButton} onClick={refreshMessages}>
                    Refresh Messages
                  </button>
                </div>
              </div>
            </>
        )}
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

liveIndicator: {
  color: "#22c55e",
  fontSize: "13px",
  fontWeight: "600",
  marginTop: "-10px",
  marginBottom: "15px",
},

navButtonRow: {
  display: "flex",
  gap: "16px",
  marginBottom: "30px",
  marginLeft: "26px",
  flexWrap: "wrap",
},

  
buttonRow: {
  display: "flex",
  justifyContent: "center",
  gap: "12px",
  marginBottom: "30px",
  flexWrap: "wrap",
},
  leaveGroupRow: {
    display: "flex",
    justifyContent: "flex-end",
    marginBottom: "14px",
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
  

adminBadge: {
  marginTop: "6px",
  color: "#d6c6a5",
  fontSize: "12px",
  fontWeight: "700",
},


groupButton: {
    backgroundColor: "#020617",
    color: "#ffffff",
    borderRadius: "14px",
    padding: "14px 18px",
    cursor: "pointer",
  },
  messagesBox: {
    minHeight: "360px",
    maxHeight: "520px",
    overflowY: "auto",
    scrollbarWidth: "none",
    msOverflowStyle: "none",
    padding: "16px",
    marginBottom: "16px",
  },
  memberBox: {
    marginBottom: "14px",
    textAlign: "center",
  },

  membersLabel: {
    color: "#bfdbfe",
    fontSize: "15px",
    fontWeight: "700",
    marginBottom: "10px",
  },

  memberPills: {
    display: "flex",
    justifyContent: "center",
    columnGap: "6px",
    rowGap: "6px",
    flexWrap: "wrap",
  },

  memberPill: {
    backgroundColor: "#020617",
    color: "#e0f2fe",
    border: "1px solid #38bdf8",
    borderRadius: "999px",
    padding: "8px 14px",
    fontSize: "14px",
    fontWeight: "700",
  },

  messageRow: {
    display: "flex",
    width: "78%",
    marginLeft: "auto",
    marginRight: "auto",
    marginBottom: "12px",
  },

myMessageBubble: {
  background: "linear-gradient(135deg, #60a5fa, #2563eb)",
  color: "#ffffff",
  borderRadius: "18px 18px 4px 18px",
  padding: "12px 16px",
  maxWidth: "70%",
  minWidth: "180px",
  boxShadow: "0 4px 10px rgba(236, 72, 153, 0.18)",
},

otherMessageBubble: {
  backgroundColor: "#020617",
  color: "#ffffff",
  border: "1px solid #38bdf8",
  borderRadius: "18px 18px 18px 4px",
  padding: "12px 16px",
  maxWidth: "70%",
  minWidth: "180px",
  boxShadow: "0 4px 10px rgba(96, 165, 250, 0.22)",
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
    background: "linear-gradient(135deg, #d946ef, #8b5cf6)",
    color: "#ffffff",
    border: "none",
    borderRadius: "14px",
    padding: "14px 22px",
    minWidth: "170px",
    height: "48px",
    fontWeight: "700",
    cursor: "pointer",
  },

            backButton: {
            backgroundColor: "#020617",
            color: "#e0f2fe",
            border: "1px solid #38bdf8",
            borderRadius: "12px",
            padding: "12px 18px",
            fontWeight: "700",
            cursor: "pointer",
            marginBottom: "18px",
          },

leaveButton: {
  background: "linear-gradient(135deg, #8b5cf6, #6366f1)",
  color: "#ffffff",
  border: "none",
  borderRadius: "14px",
  padding: "14px 22px",
  minWidth: "170px",
  height: "48px",
  fontWeight: "700",
  cursor: "pointer",
},

  refreshButton: {
    backgroundColor: "#1e3a8a",
    color: "#ffffff",
    border: "2px solid #38bdf8",
    borderRadius: "14px",
    padding: "14px 22px",
    minWidth: "170px",
    height: "48px", 
    fontWeight: "700",
    cursor: "pointer",
  },
  muted: {
    color: "#94a3b8",
  },
};

