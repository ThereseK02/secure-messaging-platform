import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import api from "../services/api";

const MAX_GROUP_ATTACHMENT_SIZE_BYTES = 5 * 1024 * 1024;
const MAX_GROUP_ATTACHMENT_SIZE_LABEL = "5 MB";

export default function GroupChat() {
  const navigate = useNavigate();

  const [groups, setGroups] = useState([]);
  const [selectedGroupId, setSelectedGroupId] = useState("");
  const [groupName, setGroupName] = useState("");
  const [joinGroupId, setJoinGroupId] = useState("");
  const [message, setMessage] = useState("");
  const [members, setMembers] = useState([]);
  const [messages, setMessages] = useState([]);
  const [groupMessageSearch, setGroupMessageSearch] = useState("");
  const [editingMessageId, setEditingMessageId] = useState(null);
  const [editingMessageText, setEditingMessageText] = useState("");
  const [selectedGroupAttachment, setSelectedGroupAttachment] = useState(null);
  const [groupAttachments, setGroupAttachments] = useState([]);
  const [notification, setNotification] = useState(null);
  const currentUsername = localStorage.getItem("username");
  const messagesEndRef = useRef(null);
  const messagesBoxRef = useRef(null);
  const groupAttachmentInputRef = useRef(null);
  const shouldAutoScrollRef = useRef(true);
  const stompClientRef = useRef(null);
  const [realTimeConnected, setRealTimeConnected] =
      useState(false);
  const [hasNewMessagesBelow, setHasNewMessagesBelow] =
      useState(false);
  const [showConversation, setShowConversation] =
      useState(false);
  const [selectedGroupName, setSelectedGroupName] = useState("");
  const [showGroups, setShowGroups] = useState(false);
  const [showEmojiPicker, setShowEmojiPicker] = useState(false);
  const emojiOptions = [
    "😀", "😂", "😊", "😍", "🥰", "😎", "🤔", "😭",
    "👍", "👏", "🙏", "💪", "❤️", "💙", "✨", "🔥",
    "🎉", "🥳", "🎁", "🎀", "💝", "💐", "🧸", "🍫",
    "💌", "💎", "🌟", "✅", "🔒", "🔐", "🛡️", "💬"
  ];

  function addEmoji(emoji) {
    setMessage((currentMessage) => `${currentMessage}${emoji}`);
    setShowEmojiPicker(false);
  }
  function scrollToLatestGroupMessage() {
    const box = messagesBoxRef.current;

    if (!box) return;

    shouldAutoScrollRef.current = true;
    setHasNewMessagesBelow(false);

    requestAnimationFrame(() => {
      box.scrollTop = box.scrollHeight;
    });
  }

  function getWebSocketUrl() {
    const apiBaseUrl = api.defaults.baseURL || "";

    if (apiBaseUrl.startsWith("http")) {
      return `${apiBaseUrl.replace(/\/$/, "")}/ws`;
    }

    return `${window.location.origin}/ws`;
  }
  function formatGroupMessageTimestamp(timestamp) {
    if (!timestamp) {
      return "";
    }

    const timestampText = String(timestamp);
    const hasTimezone =
        timestampText.endsWith("Z") ||
        /[+-]\d{2}:\d{2}$/.test(timestampText);

    const normalizedTimestamp = hasTimezone
        ? timestampText
        : `${timestampText}Z`;

    return new Date(normalizedTimestamp).toLocaleString([], {
      year: "numeric",
      month: "numeric",
      day: "numeric",
      hour: "numeric",
      minute: "2-digit",
    });
  }

  function getMessageDateLabel(timestamp) {
    const messageDate = new Date(timestamp + "Z");
    const today = new Date();
    const yesterday = new Date();

    yesterday.setDate(today.getDate() - 1);

    if (messageDate.toDateString() === today.toDateString()) {
      return "Today";
    }

    if (messageDate.toDateString() === yesterday.toDateString()) {
      return "Yesterday";
    }

    return messageDate.toLocaleDateString([], {
      month: "long",
      day: "numeric",
      year: "numeric",
    });
  }

  function shouldShowDateSeparator(messages, index) {
    if (index === 0) return true;

    const currentDate = new Date(messages[index].timestamp + "Z").toDateString();
    const previousDate = new Date(messages[index - 1].timestamp + "Z").toDateString();

    return currentDate !== previousDate;
  }

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
  async function loadGroupAttachments(groupId = selectedGroupId) {
    if (!groupId) return;

    try {
      const response = await api.get(`/api/groups/${groupId}/attachments`);
      setGroupAttachments(response.data);
    } catch (error) {
      console.error(error);
      showNotification("error", "Failed to load group attachments");
    }
  }

  async function downloadGroupAttachment(attachment) {
    if (!selectedGroupId || !attachment?.id) {
      showNotification("error", "Attachment is not available");
      return;
    }

    try {
      const response = await api.get(
          `/api/groups/${selectedGroupId}/attachments/${attachment.id}/download`,
          { responseType: "blob" }
      );

      const downloadUrl = window.URL.createObjectURL(new Blob([response.data]));
      const downloadLink = document.createElement("a");

      downloadLink.href = downloadUrl;
      downloadLink.download = attachment.filename || "group-attachment";
      document.body.appendChild(downloadLink);
      downloadLink.click();
      downloadLink.remove();

      window.URL.revokeObjectURL(downloadUrl);

      showNotification("success", "Group attachment downloaded");
    } catch (error) {
      console.error(error);
      showNotification(
          "error",
          error.response?.data?.error || "Failed to download group attachment"
      );
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
    await loadGroupAttachments(selectedGroupId);
    showNotification("success", "Group messages refreshed");
  }

  function clearSelectedGroupAttachment() {
    setSelectedGroupAttachment(null);

    if (groupAttachmentInputRef.current) {
      groupAttachmentInputRef.current.value = "";
    }
  }

  const normalizedGroupMessageSearch = groupMessageSearch.trim().toLowerCase();

  const filteredGroupMessages = messages.filter((msg) => {
    if (!normalizedGroupMessageSearch) {
      return true;
    }

    const messageAttachments = groupAttachments.filter(
        (attachment) => String(attachment.groupMessageId) === String(msg.id)
    );

    const searchableText = [
      msg.sender,
      msg.message,
      formatGroupMessageTimestamp(msg.timestamp),
      getMessageDateLabel(msg.timestamp),
      ...messageAttachments.map((attachment) => attachment.filename),
    ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase();

    return searchableText.includes(normalizedGroupMessageSearch);
  });

  function formatFileSize(sizeInBytes) {
    if (!sizeInBytes && sizeInBytes !== 0) {
      return "";
    }

    if (sizeInBytes < 1024) {
      return `${sizeInBytes} B`;
    }

    if (sizeInBytes < 1024 * 1024) {
      return `${(sizeInBytes / 1024).toFixed(1)} KB`;
    }

    return `${(sizeInBytes / (1024 * 1024)).toFixed(1)} MB`;
  }

  async function sendMessage() {
    if (!selectedGroupId) {
      showNotification("error", "Please select a group first");
      return;
    }

    if (!message.trim() && !selectedGroupAttachment) {
      showNotification("error", "Please type a message or choose a file");
      return;
    }

    if (
        selectedGroupAttachment &&
        selectedGroupAttachment.size > MAX_GROUP_ATTACHMENT_SIZE_BYTES
    ) {
      showNotification(
          "error",
          `File is too large. Maximum group attachment size is ${MAX_GROUP_ATTACHMENT_SIZE_LABEL}.`
      );
      clearSelectedGroupAttachment();
      return;
    }

    try {
      shouldAutoScrollRef.current = true;

      const messageResponse = await api.post(`/api/groups/${selectedGroupId}/send`, {
        message: message.trim() || `Attachment: ${selectedGroupAttachment?.name || "file"}`,
      });

      const groupMessageId = messageResponse.data?.messageId;

      if (selectedGroupAttachment) {
        if (!groupMessageId) {
          showNotification("error", "Group message was created, but its ID was not returned");
          return;
        }

        const formData = new FormData();

        formData.append("groupMessageId", groupMessageId);
        formData.append("file", selectedGroupAttachment);

        await api.post(
            `/api/groups/${selectedGroupId}/attachments/upload`,
            formData,
            {
              headers: {
                "Content-Type": "multipart/form-data",
              },
            }
        );

        clearSelectedGroupAttachment();
      }
      setMessage("");
      setGroupMessageSearch("");

      await loadMessages(selectedGroupId);
      await loadGroupAttachments(selectedGroupId);

      showNotification("success", "Group message sent");

    } catch (error) {
      console.error(error);

      if (error.response?.status === 413) {
        showNotification(
            "error",
            `File is too large. Maximum group attachment size is ${MAX_GROUP_ATTACHMENT_SIZE_LABEL}.`
        );
        clearSelectedGroupAttachment();
        return;
      }

      showNotification(
          "error",
          error.response?.data?.error || "Failed to send group message"
      );
    }
  }

  async function deleteGroupMessage(msg) {
    if (!selectedGroupId) {
      showNotification("error", "Please select a group first");
      return;
    }

    if (msg.sender !== currentUsername) {
      showNotification("error", "You can delete only your own group messages");
      return;
    }

    const messageAttachments = groupAttachments.filter(
        (attachment) => String(attachment.groupMessageId) === String(msg.id)
    );

    if (messageAttachments.length > 0) {
      showNotification("error", "Messages with attachments cannot be deleted yet");
      return;
    }

    const confirmed = window.confirm("Delete this group message?");

    if (!confirmed) {
      return;
    }

    try {

      await api.delete(`/api/groups/${selectedGroupId}/messages/${msg.id}`);

      setGroupMessageSearch("");

      await loadMessages(selectedGroupId);
      await loadGroupAttachments(selectedGroupId);

      showNotification("success", "Group message deleted");

    } catch (error) {
      console.error(error);
      showNotification(
          "error",
          error.response?.data?.error || "Failed to delete group message"
      );
    }
  }

  function startEditingGroupMessage(msg) {
    setEditingMessageId(msg.id);
    setEditingMessageText(msg.message || "");
  }

  function cancelEditingGroupMessage() {
    setEditingMessageId(null);
    setEditingMessageText("");
  }

  async function saveEditedGroupMessage(msg) {
    if (!selectedGroupId) {
      showNotification("error", "Please select a group first");
      return;
    }

    if (msg.sender !== currentUsername) {
      showNotification("error", "You can edit only your own group messages");
      return;
    }

    const updatedMessage = editingMessageText.trim();

    if (!updatedMessage) {
      showNotification("error", "Edited message cannot be empty");
      return;
    }

    const messageAttachments = groupAttachments.filter(
        (attachment) => String(attachment.groupMessageId) === String(msg.id)
    );

    if (messageAttachments.length > 0) {
      showNotification("error", "Messages with attachments cannot be edited yet");
      return;
    }

    try {
      await api.put(`/api/groups/${selectedGroupId}/messages/${msg.id}`, {
        message: updatedMessage,
      });

      setEditingMessageId(null);
      setEditingMessageText("");

      await loadMessages(selectedGroupId);
      await loadGroupAttachments(selectedGroupId);

      showNotification("success", "Group message edited");

    } catch (error) {
      console.error(error);
      showNotification(
          "error",
          error.response?.data?.error || "Failed to edit group message"
      );
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
      setGroupAttachments([]);
      setMembers([]);
      setGroupMessageSearch("");
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
    loadGroups();
  }, []);

  useEffect(() => {
    if (!selectedGroupId || !showConversation) return;

    loadMessages(selectedGroupId);
    loadGroupAttachments(selectedGroupId);

    const intervalId = setInterval(() => {
      loadMessages(selectedGroupId);
      loadGroupAttachments(selectedGroupId);
    }, 3000);

    return () => clearInterval(intervalId);
  }, [selectedGroupId, showConversation]);

  useEffect(() => {
    if (!selectedGroupId || !showConversation) {
      setRealTimeConnected(false);
      return;
    }

    const client = new Client({
      webSocketFactory: () => new SockJS(getWebSocketUrl()),
      reconnectDelay: 5000,
      debug: () => {},
      onConnect: () => {
        setRealTimeConnected(true);

        client.subscribe(`/topic/groups/${selectedGroupId}`, () => {
          loadMessages(selectedGroupId);
          loadGroupAttachments(selectedGroupId);
        });
      },
      onStompError: () => {
        setRealTimeConnected(false);
      },
      onWebSocketClose: () => {
        setRealTimeConnected(false);
      },
    });

    stompClientRef.current = client;
    client.activate();

    return () => {
      setRealTimeConnected(false);

      if (stompClientRef.current) {
        stompClientRef.current.deactivate();
        stompClientRef.current = null;
      }
    };
  }, [selectedGroupId, showConversation]);

  useEffect(() => {
    const box = messagesBoxRef.current;

    if (!box) return;

    if (!shouldAutoScrollRef.current) {
      setHasNewMessagesBelow(true);
      return;
    }

    setHasNewMessagesBelow(false);

    requestAnimationFrame(() => {
      box.scrollTop = box.scrollHeight;
    });
  }, [messages]);

  useEffect(() => {
    window.scrollTo({ top: 0, left: 0, behavior: "auto" });
  }, [showConversation]);

  return (
      <div style={styles.page}>
      
<style>
  {`
    .messagesBox::-webkit-scrollbar {
      display: none;
    }

    .messagesBox {
      scrollbar-width: none;
      -ms-overflow-style: none;
    }
  `}
</style>

  <h1 style={styles.title}>Group Chat</h1>

        {notification && (
            <div
                style={{
                  position: "fixed",
                  top: "18px",
                  left: "50%",
                  transform: "translateX(-50%)",
                  zIndex: 9999,
                  width: "min(420px, calc(100vw - 36px))",
                  backgroundColor:
                      notification.type === "success"
                          ? "rgba(34, 197, 94, 0.16)"
                          : "rgba(239, 68, 68, 0.16)",
                  border:
                      notification.type === "success"
                          ? "1px solid #22c55e"
                          : "1px solid #ef4444",
                  color:
                      notification.type === "success" ? "#bbf7d0" : "#fecaca",
                  padding: "12px 14px",
                  borderRadius: "10px",
                  fontWeight: "bold",
                  fontSize: "14px",
                  lineHeight: "1.35",
                  boxShadow: "0 12px 30px rgba(0, 0, 0, 0.35)",
                  boxSizing: "border-box",
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
                <button
                    style={styles.sectionTitleButton}
                    onClick={() => setShowGroups(!showGroups)}
                >
                  My Groups {showGroups ? "▲" : "▼"}
                </button>

                {showGroups && (
                    <>
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
                                      setSelectedGroupId(String(group.id));
                                      setSelectedGroupName(group.groupName);
                                      setGroupMessageSearch("");
                                      setShowConversation(true);
                                      window.scrollTo({ top: 0, behavior: "auto" });
                                      loadMessages(group.id);
                                      loadGroupAttachments(group.id);
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
                    </>
                )}
              </div>
            </>
        )}

        {showConversation && (
            <div style={styles.chatLayout}>

              <div style={styles.chatSidebar}>

                <button
                    style={styles.backButton}
                    onClick={() => {
                      setShowConversation(false);
                      setMessages([]);
                      setGroupAttachments([]);
                      setMembers([]);
                      setGroupMessageSearch("");
                    }}
                >
                  Back to Groups
                </button>

                <div style={styles.memberBox}>
                  <p style={styles.membersLabel}>
                    Members ({members.length})
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

                {selectedGroupId && (
                    <button
                        style={styles.leaveButton}
                        onClick={leaveGroup}
                    >
                      Leave Group
                    </button>
                )}

              </div>

              <div style={styles.chatMain}>
                <p style={styles.groupContext}>
                  Group: {selectedGroupName} (#{selectedGroupId})
                </p>
                <h2 style={styles.sectionTitle}>Messages</h2>

                <p style={styles.messageCount}>
                  {messages.length} messages
                </p>

                <div style={styles.groupSearchRow}>
                  <input
                      type="text"
                      placeholder="Search group messages..."
                      value={groupMessageSearch}
                      onChange={(event) => setGroupMessageSearch(event.target.value)}
                      style={styles.groupSearchInput}
                  />

                  {groupMessageSearch && (
                      <button
                          type="button"
                          onClick={() => setGroupMessageSearch("")}
                          style={styles.groupSearchClearButton}
                      >
                        Clear
                      </button>
                  )}
                </div>

                {groupMessageSearch && (
                    <p style={styles.groupSearchSummary}>
                      Showing {filteredGroupMessages.length} of {messages.length} messages
                    </p>
                )}

                <div style={styles.liveStatusRow}>

                  <p style={styles.liveIndicator}>
                    {realTimeConnected
                        ? "Real-Time Chat: Connected"
                        : "Live Refresh fallback active"}
                  </p>

                  <button
                      type="button"
                      style={styles.secondaryRefreshButton}
                      onClick={refreshMessages}
                  >
                    Refresh
                  </button>
                </div>

                {hasNewMessagesBelow && (
                    <button
                        type="button"
                        style={styles.newMessagesIndicator}
                        onClick={scrollToLatestGroupMessage}
                    >
                      New group messages below
                    </button>
                )}

                <div
                    ref={messagesBoxRef}
                    className="messagesBox"
                    style={styles.messagesBox}
                    onScroll={() => {
                      const box = messagesBoxRef.current;

                      if (!box) return;

                      const distanceFromBottom =
                          box.scrollHeight - box.scrollTop - box.clientHeight;

                      shouldAutoScrollRef.current = distanceFromBottom < 80;

                      if (distanceFromBottom < 80) {
                        setHasNewMessagesBelow(false);
                      }
                    }}
                >

                  {messages.length === 0 ? (
                      <p style={styles.muted}>No messages yet.</p>
                  ) : filteredGroupMessages.length === 0 ? (
                      <p style={styles.muted}>No group messages match your search.</p>
                  ) : (

                      filteredGroupMessages.map((msg, index) => {
                        const previousMessage = filteredGroupMessages[index - 1];
                        const sameSenderAsPrevious =
                            previousMessage && previousMessage.sender === msg.sender;

                        const messageAttachments = groupAttachments.filter(
                            (attachment) => String(attachment.groupMessageId) === String(msg.id)
                        );

                        const canModifyMessage =
                            msg.sender === currentUsername && messageAttachments.length === 0;

                        const isEditingThisMessage =
                            editingMessageId === msg.id;

                        return (
                            <div key={msg.id}>
                              {shouldShowDateSeparator(filteredGroupMessages, index) && (
                                  <div style={styles.dateSeparator}>
                                    {getMessageDateLabel(msg.timestamp)}
                                  </div>
                              )}

                              <div
                                  style={{
                                    display: "flex",
                                    justifyContent: msg.sender === currentUsername ? "flex-end" : "flex-start",
                                    marginTop: sameSenderAsPrevious ? "6px" : "22px",
                                    marginBottom: "0",
                                  }}
                              >
                                <div
                                    style={
                                      msg.sender === currentUsername
                                          ? styles.myMessageBubble
                                          : styles.otherMessageBubble
                                    }
                                >

                                  <p style={styles.sender}>
                                    {msg.sender === currentUsername ? "You" : msg.sender} •{" "}
                                    {new Date(msg.timestamp + "Z").toLocaleTimeString([], {
                                      hour: "numeric",
                                      minute: "2-digit",
                                    })}
                                  </p>

                                  {isEditingThisMessage ? (
                                      <div style={styles.editMessageBox}>
                                        <textarea
                                            value={editingMessageText}
                                            onChange={(e) => setEditingMessageText(e.target.value)}
                                            style={styles.editMessageTextarea}
                                            spellCheck={false}
                                            data-gramm="false"
                                            data-gramm_editor="false"
                                            data-enable-grammarly="false"
                                        />

                                        <div style={styles.editMessageActions}>
                                          <button
                                              type="button"
                                              style={styles.saveEditButton}
                                              onClick={() => saveEditedGroupMessage(msg)}
                                          >
                                            Save
                                          </button>

                                          <button
                                              type="button"
                                              style={styles.cancelEditButton}
                                              onClick={cancelEditingGroupMessage}
                                          >
                                            Cancel
                                          </button>
                                        </div>
                                      </div>
                                  ) : (
                                      <p style={styles.messageText}>{msg.message}</p>
                                  )}

                                  {messageAttachments.map((attachment) => (
                                      <button
                                          key={attachment.id}
                                          type="button"
                                          style={styles.groupAttachmentCard}
                                          onClick={() => downloadGroupAttachment(attachment)}
                                          title="Download attachment"
                                      >
                                        <span style={styles.groupAttachmentIcon}>📎</span>

                                        <span style={styles.groupAttachmentText}>
                                            <span style={styles.groupAttachmentName}>
                                            {attachment.filename}
                                        </span>
                                          <span style={styles.groupAttachmentSize}>
                                           {formatFileSize(attachment.fileSize)}
                                        </span>
                                        </span>
                                      </button>
                                  ))}

                                  {canModifyMessage && !isEditingThisMessage && (
                                      <div style={styles.messageActions}>
                                        <button
                                            type="button"
                                            style={styles.editMessageButton}
                                            onClick={() => startEditingGroupMessage(msg)}
                                        >
                                          Edit
                                        </button>

                                        <button
                                            type="button"
                                            style={styles.deleteMessageButton}
                                            onClick={() => deleteGroupMessage(msg)}
                                        >
                                          Delete
                                        </button>
                                      </div>
                                  )}

                                  {msg.seenCount !== undefined && msg.memberCount !== undefined && (
                                      <p style={styles.seenStatus}>
                                        Seen by {msg.seenCount} of {msg.memberCount}
                                        {msg.editedAt && " · edited"}
                                      </p>
                                  )}

                                </div>
                              </div>
                            </div>
                        );
                      })
                  )}

                  <div ref={messagesEndRef}/>
                </div>

                <div style={styles.messageInputRow}>
  <textarea
      placeholder="Write a group message"
      value={message}
      onChange={(e) => setMessage(e.target.value)}
      onKeyDown={(e) => {
        if (e.key === "Enter" && !e.shiftKey) {
          e.preventDefault();
          sendMessage();
        }
      }}
      spellCheck={false}
      data-gramm="false"
      data-gramm_editor="false"
      data-enable-grammarly="false"
      style={styles.textarea}
  />

                  <div style={styles.emojiPickerWrapper}>
                    <button
                        type="button"
                        onClick={() => setShowEmojiPicker((current) => !current)}
                        style={styles.emojiToggleButton}
                    >
                      😊
                    </button>

                    {showEmojiPicker && (
                        <div style={styles.emojiPicker}>
                          {emojiOptions.map((emoji) => (
                              <button
                                  key={emoji}
                                  type="button"
                                  onClick={() => addEmoji(emoji)}
                                  style={styles.emojiButton}
                              >
                                {emoji}
                              </button>
                          ))}
                        </div>
                    )}
                  </div>

                  <div style={styles.messageButtonColumn}>
                    <div style={styles.groupAttachmentInputRow}>
                      <label style={styles.groupAttachmentUploadLabel}>
                        📎 Choose file
                        <input
                            ref={groupAttachmentInputRef}
                            type="file"
                            style={{ display: "none" }}
                            onChange={(e) => {
                              const file = e.target.files?.[0];

                              if (!file) {
                                clearSelectedGroupAttachment();
                                return;
                              }

                              if (file.size > MAX_GROUP_ATTACHMENT_SIZE_BYTES) {
                                showNotification(
                                    "error",
                                    `File is too large. Maximum group attachment size is ${MAX_GROUP_ATTACHMENT_SIZE_LABEL}.`
                                );
                                clearSelectedGroupAttachment();
                                return;
                              }

                              setSelectedGroupAttachment(file);
                            }}
                        />
                      </label>

                      {selectedGroupAttachment && (
                          <div style={styles.selectedGroupAttachmentRow}>
                            <div style={styles.selectedGroupAttachmentText}>
                              <span style={styles.selectedGroupAttachmentName}>
                                {selectedGroupAttachment.name}
                              </span>

                              <span style={styles.selectedGroupAttachmentSize}>
                                {formatFileSize(selectedGroupAttachment.size)}
                              </span>
                            </div>

                             <button
                                type="button"
                                style={styles.removeGroupAttachmentButton}
                                onClick={clearSelectedGroupAttachment}
                             >
                              Remove
                            </button>
                          </div>
                      )}
                    </div>

                    <button style={styles.sendButton} onClick={sendMessage}>
                      Send Group Message
                    </button>
                  </div>
                </div>

              </div>

            </div>
        )}
      </div>
  );
}

const styles = {
  page: {
    minHeight: "100vh",
    width: "100%",
    boxSizing: "border-box",
    backgroundColor: "#020617",
    color: "#ffffff",
    padding: "clamp(18px, 2.5vh, 32px) clamp(12px, 3vw, 40px) clamp(24px, 4vh, 48px)",
    fontFamily: "Arial, sans-serif",
  },
  title: {
    color: "#38bdf8",
    fontSize: "clamp(32px, 3.6vw, 46px)",
    marginBottom: "8px",
  },

  sectionTitleButton: {
    background: "transparent",
    border: "none",
    color: "#38bdf8",
    fontSize: "22px",
    fontWeight: "600",
    cursor: "pointer",
    marginBottom: "16px",
  },
  liveStatusRow: {
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    gap: "10px",
    marginTop: "4px",
    marginBottom: "15px",
  },

  liveIndicator: {
    color: "#d6c6a8",
    fontSize: "13px",
    fontWeight: "700",
    margin: 0,
  },

  secondaryRefreshButton: {
    border: "1px solid rgba(56, 189, 248, 0.45)",
    backgroundColor: "rgba(30, 58, 138, 0.35)",
    color: "#bfdbfe",
    borderRadius: "999px",
    padding: "4px 10px",
    fontSize: "12px",
    fontWeight: "700",
    cursor: "pointer",
  },

  newMessagesIndicator: {
    color: "#0f172a",
    backgroundColor: "#d6c6a8",
    border: "1px solid #f5e6c8",
    borderRadius: "999px",
    padding: "6px 12px",
    fontSize: "13px",
    fontWeight: "700",
    textAlign: "center",
    width: "fit-content",
    margin: "0 auto 10px auto",
    cursor: "pointer",
  },
  navButtonRow: {
    display: "flex",
    gap: "16px",
    marginBottom: "18px",
    marginLeft: "26px",
    flexWrap: "wrap",
  },

  groupSearchRow: {
    display: "flex",
    alignItems: "center",
    gap: "10px",
    width: "100%",
    maxWidth: "720px",
    margin: "10px auto 8px",
  },

  groupSearchInput: {
    flex: 1,
    minWidth: "220px",
    padding: "10px 12px",
    borderRadius: "10px",
    border: "1px solid #38bdf8",
    backgroundColor: "#020617",
    color: "#ffffff",
    fontSize: "14px",
    outline: "none",
    boxSizing: "border-box",
  },

  groupSearchClearButton: {
    padding: "9px 12px",
    borderRadius: "10px",
    border: "1px solid #64748b",
    backgroundColor: "#0f172a",
    color: "#e2e8f0",
    fontWeight: "700",
    cursor: "pointer",
  },

  groupSearchSummary: {
    color: "#94a3b8",
    fontSize: "13px",
    textAlign: "center",
    marginTop: "0",
    marginBottom: "8px",
  },

  messageInputRow: {
    display: "grid",
    gridTemplateColumns: "minmax(320px, 1fr) 44px 200px",
    gap: "12px",
    alignItems: "end",
    width: "100%",
    marginTop: "6px",
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
    padding: "12px 24px",
    marginBottom: "10px",
    width: "100%",
    maxWidth: "100%",
    boxSizing: "border-box",
    boxShadow: "0 0 12px rgba(56,189,248,0.08)",
  },

  sectionTitle: {
    color: "#38bdf8",
    marginBottom: "8px",
  },

  input: {
    display: "block",
    width: "100%",
    maxWidth: "420px",
    padding: "10px 14px",
    marginBottom: "8px",
    borderRadius: "10px",
    border: "1px solid #38bdf8",
    backgroundColor: "#020617",
    color: "#ffffff",
    fontSize: "16px",
  },
  conversationHeaderCard: {
    backgroundColor: "#0f172a",
    border: "1px solid #1e293b",
    borderRadius: "16px",
    padding: "clamp(8px, 1.5vw, 16px)",
    marginBottom: "clamp(10px, 2vh, 18px)",
    width: "100%",
    maxWidth: "850px",
    boxSizing: "border-box",
    boxShadow: "0 0 12px rgba(56,189,248,0.08)",
  },
  primaryButton: {
    background: "linear-gradient(135deg, #4f46e5, #2563eb)",
    color: "#ffffff",
    border: "none",
    borderRadius: "14px",
    padding: "10px 20px",
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
  groupContext: {
    color: "#bfdbfe",
    fontSize: "16px",
    fontWeight: "700",
    textAlign: "center",
    marginBottom: "8px",
  },
  messagesBox: {
    height: "clamp(220px, 32vh, 360px)",
    minHeight: "210px",
    maxHeight: "360px",
    overflowY: "auto",
    scrollbarWidth: "none",
    msOverflowStyle: "none",
    padding: "18px 14px 22px",
    marginBottom: "6px",
    display: "flex",
    flexDirection: "column",
    gap: "16px",
    scrollPaddingTop: "24px",
    scrollPaddingBottom: "28px",
    scrollBehavior: "smooth",
    scrollSnapType: "y proximity",
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
    width: "90%",
    marginLeft: "auto",
    marginRight: "auto",
    flexShrink: 0,
    scrollSnapAlign: "start",
  },
  myMessageBubble: {
    background: "linear-gradient(135deg, #1d4ed8, #2563eb)",
    color: "#f8fafc",
    border: "1px solid #38bdf8",
    borderRadius: "16px 16px 4px 16px",
    padding: "12px 16px",
    maxWidth: "72%",
    overflowWrap: "break-word",
    boxShadow: "0 8px 20px rgba(56, 189, 248, 0.18)",
  },

  otherMessageBubble: {
    backgroundColor: "#020617",
    color: "#f8fafc",
    border: "1px solid #38bdf8",
    borderRadius: "16px 16px 16px 4px",
    padding: "12px 16px",
    maxWidth: "72%",
    overflowWrap: "break-word",
    boxShadow: "0 8px 20px rgba(56, 189, 248, 0.14)",
  },
  chatLayout: {
    display: "flex",
    gap: "20px",
    width: "100%",
    maxWidth: "1200px",
    alignItems: "stretch",
    minHeight: "clamp(470px, 64vh, 620px)",
  },
  chatSidebar: {
    width: "260px",
    minWidth: "260px",
    height: "100%",
    justifyContent: "flex-start",
    backgroundColor: "#0f172a",
    border: "1px solid #1e293b",
    borderRadius: "16px",
    padding: "20px",
    display: "flex",
    flexDirection: "column",
    gap: "18px",
    boxSizing: "border-box",
  },
  chatMain: {
    flex: 1,
    display: "flex",
    flexDirection: "column",
    height: "100%",
    backgroundColor: "#0f172a",
    border: "1px solid #1e293b",
    borderRadius: "16px",
    padding: "clamp(12px, 2vw, 24px)",
    boxSizing: "border-box",
    boxShadow: "0 0 12px rgba(56,189,248,0.08)",
  },
messageCard: {
    backgroundColor: "#020617",
    border: "1px solid #1e293b",
    borderRadius: "14px",
    padding: "16px",
    marginBottom: "12px",
  },
  sender: {
    color: "#bae6fd",
    fontSize: "13px",
    fontWeight: "800",
    margin: "0 0 4px 0"
  },
  messageText: {
    color: "#f8fafc",
    fontSize: "15px",
    margin: 0,
    lineHeight: "1.35"
  },

  messageActions: {
    display: "flex",
    justifyContent: "flex-end",
    gap: "10px",
    marginTop: "10px",
    alignItems: "center",
  },

  editMessageButton: {
    backgroundColor: "#1e3a8a",
    color: "#dbeafe",
    border: "1px solid #60a5fa",
    borderRadius: "999px",
    padding: "5px 13px",
    fontSize: "12px",
    fontWeight: "700",
    cursor: "pointer",
  },

  deleteMessageButton: {
    backgroundColor: "#d6c6a8",
    color: "#0f172a",
    border: "1px solid #f5e6c8",
    borderRadius: "999px",
    padding: "5px 13px",
    fontSize: "12px",
    fontWeight: "700",
    cursor: "pointer",
  },

  editMessageBox: {
    marginTop: "8px",
  },

  editMessageTextarea: {
    width: "100%",
    boxSizing: "border-box",
    minHeight: "70px",
    padding: "10px",
    borderRadius: "8px",
    border: "1px solid #334155",
    backgroundColor: "#0f172a",
    color: "white",
    resize: "vertical",
    fontSize: "14px",
  },

  editMessageActions: {
    display: "flex",
    justifyContent: "flex-end",
    gap: "8px",
    marginTop: "8px",
  },

  saveEditButton: {
    backgroundColor: "#2563eb",
    color: "white",
    border: "none",
    borderRadius: "999px",
    padding: "5px 12px",
    fontSize: "12px",
    cursor: "pointer",
  },

  cancelEditButton: {
    backgroundColor: "transparent",
    color: "#cbd5e1",
    border: "1px solid #475569",
    borderRadius: "999px",
    padding: "5px 12px",
    fontSize: "12px",
    cursor: "pointer",
  },

  seenStatus: {
    color: "#94a3b8",
    fontSize: "12px",
    marginTop: "8px",
    marginBottom: "0",
    textAlign: "right",
  },

  dateSeparator: {
    alignSelf: "center",
    color: "#cbd5e1",
    backgroundColor: "rgba(148, 163, 184, 0.14)",
    border: "1px solid rgba(148, 163, 184, 0.25)",
    borderRadius: "999px",
    padding: "5px 12px",
    fontSize: "12px",
    fontWeight: "700",
    margin: "14px auto 8px auto",
    width: "fit-content",
  },

  timestamp: {
    color: "#94a3b8",
    fontSize: "12px",
  },
  textarea: {
    width: "100%",
    height: "68px",
    minHeight: "68px",
    maxHeight: "68px",
    padding: "9px 14px",
    borderRadius: "10px",
    border: "1px solid #38bdf8",
    backgroundColor: "#020617",
    color: "white",
    fontSize: "15px",
    resize: "none",
    boxSizing: "border-box",
    outline: "none"
  },
  messageButtonColumn: {
    display: "flex",
    flexDirection: "column",
    gap: "8px",
    justifyContent: "center",
  },

sendButton: {
  width: "170px",
  height: "42px",
  borderRadius: "12px",
  border: "none",
  background: "linear-gradient(135deg, #d946ef, #8b5cf6)",
  color: "#ffffff",
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
  borderRadius: "12px",
  padding: "10px 18px",
  minWidth: "150px",
  height: "42px",
  fontWeight: "700",
  cursor: "pointer",
},

refreshButton: {
  width: "170px",
  height: "46px",
  borderRadius: "12px",
  border: "1px solid #38bdf8",
  backgroundColor: "#1e3a8a",
  color: "#ffffff",
  fontWeight: "700",
  cursor: "pointer",
},
  emojiPickerWrapper: {
    position: "relative",
    display: "flex",
    alignItems: "flex-start"
  },

  emojiToggleButton: {
    width: "42px",
    height: "42px",
    borderRadius: "10px",
    border: "1px solid #38bdf8",
    backgroundColor: "#1e293b",
    cursor: "pointer",
    fontSize: "20px"
  },

  emojiPicker: {
    position: "absolute",
    bottom: "52px",
    left: "0",
    display: "grid",
    gridTemplateColumns: "repeat(8, 30px)",
    gap: "6px",
    padding: "10px",
    borderRadius: "12px",
    border: "1px solid #38bdf8",
    backgroundColor: "#0f172a",
    zIndex: 20,
    boxShadow: "0 12px 30px rgba(0, 0, 0, 0.45)"
  },

  emojiButton: {
    width: "30px",
    height: "30px",
    borderRadius: "8px",
    border: "1px solid #38bdf8",
    backgroundColor: "#1e293b",
    cursor: "pointer",
    fontSize: "15px",
    padding: 0
  },
    muted: {
    color: "#94a3b8",
  },

  groupAttachmentCard: {
    display: "flex",
    alignItems: "center",
    gap: "8px",
    marginTop: "10px",
    padding: "8px 10px",
    borderRadius: "10px",
    backgroundColor: "rgba(15, 23, 42, 0.55)",
    border: "1px solid rgba(148, 163, 184, 0.25)",
    color: "#e5e7eb",
    fontSize: "13px",
    maxWidth: "100%",
    boxSizing: "border-box",
    cursor: "pointer",
    textAlign: "left",
  },

  groupAttachmentIcon: {
    fontSize: "14px",
    lineHeight: "1",
  },

  groupAttachmentText: {
    display: "flex",
    flexDirection: "column",
    gap: "2px",
    minWidth: 0,
  },

  groupAttachmentName: {
    overflow: "hidden",
    textOverflow: "ellipsis",
    whiteSpace: "nowrap",
  },
  groupAttachmentSize: {
    color: "#94a3b8",
    fontSize: "11px",
  },

  groupAttachmentInputRow: {
    display: "flex",
    alignItems: "center",
    gap: "10px",
    marginTop: "10px",
    marginBottom: "10px",
    flexWrap: "wrap",
  },

  groupAttachmentUploadLabel: {
    display: "inline-flex",
    alignItems: "center",
    justifyContent: "center",
    gap: "6px",
    padding: "8px 12px",
    borderRadius: "10px",
    backgroundColor: "rgba(15, 23, 42, 0.75)",
    border: "1px solid rgba(148, 163, 184, 0.35)",
    color: "#e5e7eb",
    fontSize: "13px",
    cursor: "pointer",
  },

  selectedGroupAttachmentRow: {
    display: "flex",
    alignItems: "center",
    gap: "8px",
    maxWidth: "100%",
    flexWrap: "wrap",
  },

  selectedGroupAttachmentText: {
    display: "flex",
    flexDirection: "column",
    gap: "2px",
    minWidth: 0,
  },

  selectedGroupAttachmentName: {
    color: "#cbd5e1",
    fontSize: "13px",
    maxWidth: "260px",
    overflow: "hidden",
    textOverflow: "ellipsis",
    whiteSpace: "nowrap",
  },

  selectedGroupAttachmentSize: {
    color: "#94a3b8",
    fontSize: "11px",
  },

  removeGroupAttachmentButton: {
    padding: "5px 8px",
    borderRadius: "8px",
    border: "1px solid rgba(248, 113, 113, 0.45)",
    backgroundColor: "rgba(127, 29, 29, 0.35)",
    color: "#fecaca",
    fontSize: "12px",
    cursor: "pointer",
  },

};

