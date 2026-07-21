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
  const [pendingInvitations, setPendingInvitations] = useState([]);
  const [selectedGroupId, setSelectedGroupId] = useState("");
  const [groupName, setGroupName] = useState("");
  const [inviteUsername, setInviteUsername] = useState("");
  const [inviteEmail, setInviteEmail] = useState("");
  const [emailRegistrationLink, setEmailRegistrationLink] =
      useState("");
  const [message, setMessage] = useState("");
  const [members, setMembers] = useState([]);
  const [messages, setMessages] = useState([]);
  const [groupMessageSearch, setGroupMessageSearch] = useState("");
  const [editingMessageId, setEditingMessageId] = useState(null);
  const [editingMessageText, setEditingMessageText] = useState("");
  const [openMessageActionsId, setOpenMessageActionsId] =
      useState(null);
  const [decisionMessage, setDecisionMessage] =
      useState(null);
  const [decisionGovernanceMode, setDecisionGovernanceMode] =
      useState("OWNER_REVIEW");
  const [creatingDecision, setCreatingDecision] =
      useState(false);
  const [groupDecisions, setGroupDecisions] =
      useState([]);
  const [resolvingDecisionId, setResolvingDecisionId] =
      useState(null);
  const [openingVotingDecisionId, setOpeningVotingDecisionId] =
      useState(null);
  const [votingDeadlineInputs, setVotingDeadlineInputs] =
      useState({});
  const [castingVoteDecisionId, setCastingVoteDecisionId] =
      useState(null);
  const [recordedBallotDecisionIds, setRecordedBallotDecisionIds] =
      useState({});
  const [openDecisionActionsId, setOpenDecisionActionsId] = useState(null);
  const [hoveredMessageActionsId, setHoveredMessageActionsId] = useState(null);
  const [selectedGroupAttachment, setSelectedGroupAttachment] = useState(null);
  const [groupAttachments, setGroupAttachments] = useState([]);
  const [notification, setNotification] = useState(null);
  const currentUsername = localStorage.getItem("username");
  const messagesEndRef = useRef(null);
  const messagesBoxRef = useRef(null);
  const groupAttachmentInputRef = useRef(null);
  const shouldAutoScrollRef = useRef(true);
  const stompClientRef = useRef(null);
  const typingStopTimeoutRef = useRef(null);
  const remoteTypingTimeoutsRef = useRef({});
  const lastTypingStatusRef = useRef(false);
  const [typingUsernames, setTypingUsernames] = useState([]);
  const [onlineUsernames, setOnlineUsernames] = useState([]);
  const [realTimeConnected, setRealTimeConnected] =
      useState(false);
  const [hasNewMessagesBelow, setHasNewMessagesBelow] =
      useState(false);
  const [showConversation, setShowConversation] =
      useState(false);
  const [selectedGroupName, setSelectedGroupName] = useState("");
  const [selectedGroupAdmin, setSelectedGroupAdmin] = useState("");
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

  async function loadPendingInvitations() {
    try {
      const response = await api.get(
          "/api/groups/invitations/pending"
      );

      setPendingInvitations(response.data);
    } catch (error) {
      console.error(error);

      showNotification(
          "error",
          error.response?.data?.error ||
          "Failed to load pending invitations"
      );
    }
  }

  async function acceptGroupInvitation(invitationId) {
    try {
      const response = await api.post(
          `/api/groups/invitations/${invitationId}/accept`
      );

      await loadPendingInvitations();
      await loadGroups();
      setShowGroups(true);

      showNotification(
          "success",
          response.data?.status || "Group invitation accepted"
      );
    } catch (error) {
      console.error(error);

      showNotification(
          "error",
          error.response?.data?.error ||
          "Failed to accept group invitation"
      );
    }
  }

  async function declineGroupInvitation(invitationId) {
    try {
      const response = await api.post(
          `/api/groups/invitations/${invitationId}/decline`
      );

      await loadPendingInvitations();

      showNotification(
          "success",
          response.data?.status || "Group invitation declined"
      );
    } catch (error) {
      console.error(error);

      showNotification(
          "error",
          error.response?.data?.error ||
          "Failed to decline group invitation"
      );
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
  async function loadGroupDecisions(
      groupId = selectedGroupId
  ) {
    if (!groupId) {
      setGroupDecisions([]);
      return;
    }

    try {
      const response = await api.get(
          `/api/groups/${groupId}/decisions`
      );

      setGroupDecisions(
          Array.isArray(response.data)
              ? response.data
              : []
      );
    } catch (error) {
      console.error(error);
      setGroupDecisions([]);

      showNotification(
          "error",
          error.response?.data?.error ||
              "Failed to load group decisions"
      );
    }
  }
  async function resolveOwnerReviewDecision(
      decision,
      action
  ) {
    if (!selectedGroupId) {
      showNotification(
          "error",
          "Please select a group first"
      );
      return;
    }

    if (!decision?.decisionId) {
      showNotification(
          "error",
          "Group decision information is unavailable"
      );
      return;
    }

    if (currentGroupRole !== "OWNER") {
      showNotification(
          "error",
          "Only the group owner can approve or reject this proposal"
      );
      return;
    }

    if (
        decision.governanceMode !== "OWNER_REVIEW" ||
        decision.status !== "PROPOSED"
    ) {
      showNotification(
          "error",
          "Only a proposed Owner Review decision can be resolved"
      );
      return;
    }

    if (action !== "approve" && action !== "reject") {
      showNotification(
          "error",
          "Unsupported decision action"
      );
      return;
    }

    try {
      setResolvingDecisionId(decision.decisionId);

      const response = await api.post(
          `/api/groups/${selectedGroupId}/decisions/${decision.decisionId}/${action}`
      );

      await loadGroupDecisions(selectedGroupId);

      showNotification(
          "success",
          response.data?.status ||
              (
                  action === "approve"
                      ? "Group decision approved"
                      : "Group decision rejected"
              )
      );
    } catch (error) {
      console.error(error);

      showNotification(
          "error",
          error.response?.data?.error ||
              (
                  action === "approve"
                      ? "Failed to approve group decision"
                      : "Failed to reject group decision"
              )
      );
    } finally {
      setResolvingDecisionId(null);
    }
  }


  async function resolveMemberVoting(decision) {
    if (!selectedGroupId) {
      showNotification("error", "Please select a group first");
      return;
    }

    if (currentGroupRole !== "OWNER") {
      showNotification(
          "error",
          "Only the group owner can resolve voting"
      );
      return;
    }

    if (
        decision?.governanceMode !== "MEMBER_VOTE" ||
        decision?.status !== "VOTING_OPEN"
    ) {
      showNotification(
          "error",
          "Only an open member vote can be resolved"
      );
      return;
    }

    try {
      setResolvingDecisionId(decision.decisionId);

      const response = await api.post(
          `/api/groups/${selectedGroupId}/decisions/${decision.decisionId}/voting/resolve`
      );

      await loadGroupDecisions(selectedGroupId);

      showNotification(
          "success",
          response.data?.status ||
          "Group decision voting resolved"
      );
    } catch (error) {
      console.error(error);

      showNotification(
          "error",
          error.response?.data?.error ||
          "Failed to resolve group decision voting"
      );
    } finally {
      setResolvingDecisionId(null);
    }
  }

  async function resolveMemberVoteTieBreak(
      decision,
      tieBreakChoice
  ) {
    if (!selectedGroupId) {
      showNotification("error", "Please select a group first");
      return;
    }

    if (currentGroupRole !== "OWNER") {
      showNotification(
          "error",
          "Only the group owner can resolve a tie"
      );
      return;
    }

    if (
        decision?.governanceMode !== "MEMBER_VOTE" ||
        decision?.status !== "WAITING_FOR_TIE_BREAK"
    ) {
      showNotification(
          "error",
          "This decision is not waiting for a tie-break"
      );
      return;
    }

    if (
        tieBreakChoice !== "APPROVE" &&
        tieBreakChoice !== "REJECT"
    ) {
      showNotification(
          "error",
          "Tie-break choice must be Approve or Reject"
      );
      return;
    }

    try {
      setResolvingDecisionId(decision.decisionId);

      const response = await api.post(
          `/api/groups/${selectedGroupId}/decisions/${decision.decisionId}/tie-break`,
          {
            tieBreakChoice,
          }
      );

      await loadGroupDecisions(selectedGroupId);

      showNotification(
          "success",
          response.data?.status ||
          "Decision tie-break resolved"
      );
    } catch (error) {
      console.error(error);

      showNotification(
          "error",
          error.response?.data?.error ||
          "Failed to resolve decision tie-break"
      );
    } finally {
      setResolvingDecisionId(null);
    }
  }

  async function openMemberVoting(decision) {
    if (!selectedGroupId) {
      showNotification("error", "Please select a group first");
      return;
    }

    if (currentGroupRole !== "OWNER") {
      showNotification(
          "error",
          "Only the group owner can open voting"
      );
      return;
    }

    if (
        decision?.governanceMode !== "MEMBER_VOTE" ||
        decision?.status !== "PROPOSED"
    ) {
      showNotification(
          "error",
          "This decision is not ready to open for voting"
      );
      return;
    }

    const localDeadline =
        votingDeadlineInputs[decision.decisionId];

    if (!localDeadline) {
      showNotification(
          "error",
          "Please select a voting deadline"
      );
      return;
    }

    const parsedDeadline = new Date(localDeadline);

    if (
        Number.isNaN(parsedDeadline.getTime()) ||
        parsedDeadline.getTime() <= Date.now()
    ) {
      showNotification(
          "error",
          "Voting deadline must be in the future"
      );
      return;
    }

    const utcVotingDeadline =
        parsedDeadline.toISOString().slice(0, 19);

    try {
      setOpeningVotingDecisionId(decision.decisionId);

      const response = await api.post(
          `/api/groups/${selectedGroupId}/decisions/${decision.decisionId}/voting/open`,
          {
            votingDeadline: utcVotingDeadline,
          }
      );

      setVotingDeadlineInputs((currentInputs) => {
        const nextInputs = { ...currentInputs };
        delete nextInputs[decision.decisionId];
        return nextInputs;
      });

      await loadGroupDecisions(selectedGroupId);

      showNotification(
          "success",
          response.data?.status ||
          "Group decision voting opened"
      );
    } catch (error) {
      console.error(error);

      showNotification(
          "error",
          error.response?.data?.error ||
          "Failed to open group decision voting"
      );
    } finally {
      setOpeningVotingDecisionId(null);
    }
  }

  async function castMemberVote(decision, voteChoice) {
    if (!selectedGroupId) {
      showNotification("error", "Please select a group first");
      return;
    }

    if (
        decision?.governanceMode !== "MEMBER_VOTE" ||
        decision?.status !== "VOTING_OPEN"
    ) {
      showNotification(
          "error",
          "Voting is not open for this decision"
      );
      return;
    }

    if (
        voteChoice !== "APPROVE" &&
        voteChoice !== "REJECT" &&
        voteChoice !== "ABSTAIN"
    ) {
      showNotification(
          "error",
          "Please select a valid ballot choice"
      );
      return;
    }

    try {
      setCastingVoteDecisionId(decision.decisionId);

      const response = await api.post(
          `/api/groups/${selectedGroupId}/decisions/${decision.decisionId}/votes`,
          {
            voteChoice,
          }
      );

      setRecordedBallotDecisionIds(
          (currentDecisionIds) => ({
            ...currentDecisionIds,
            [decision.decisionId]:
                response.data?.hasVoted === true,
          })
      );

      await loadGroupDecisions(selectedGroupId);

      showNotification(
          "success",
          response.data?.status ||
          "Secret ballot recorded"
      );
    } catch (error) {
      console.error(error);

      showNotification(
          "error",
          error.response?.data?.error ||
          "Failed to record secret ballot"
      );
    } finally {
      setCastingVoteDecisionId(null);
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

  async function inviteRegisteredUser() {
    if (!selectedGroupId) {
      showNotification("error", "Please select a group first");
      return;
    }

    if (
        currentGroupRole !== "OWNER" &&
        currentGroupRole !== "ADMIN"
    ) {
      showNotification(
          "error",
          "Only the group owner or an admin can invite users"
      );
      return;
    }

    const normalizedInviteUsername = inviteUsername.trim();

    if (!normalizedInviteUsername) {
      showNotification("error", "Please enter a username");
      return;
    }

    try {
      const response = await api.post(
          `/api/groups/${selectedGroupId}/invitations`,
          {username: normalizedInviteUsername}
      );

      setInviteUsername("");

      showNotification(
          "success",
          response.data?.status || "Group invitation sent"
      );
    } catch (error) {
      console.error(error);

      showNotification(
          "error",
          error.response?.data?.error || "Failed to send group invitation"
      );
    }
  }

  async function inviteUnregisteredUserByEmail() {
    if (!selectedGroupId) {
      showNotification("error", "Please select a group first");
      return;
    }

    if (
        currentGroupRole !== "OWNER" &&
        currentGroupRole !== "ADMIN"
    ) {
      showNotification(
          "error",
          "Only the group owner or an admin can invite users"
      );
      return;
    }

    const normalizedInviteEmail =
        inviteEmail.trim().toLowerCase();

    if (!normalizedInviteEmail) {
      showNotification("error", "Please enter an email address");
      return;
    }

    try {
      const response = await api.post(
          `/api/groups/${selectedGroupId}/email-invitations`,
          { email: normalizedInviteEmail }
      );

      setInviteEmail("");
      setEmailRegistrationLink(
          response.data?.registrationLink || ""
      );

      showNotification(
          "success",
          response.data?.status ||
          "Email group invitation created"
      );
    } catch (error) {
      console.error(error);

      setEmailRegistrationLink("");

      showNotification(
          "error",
          error.response?.data?.error ||
          "Failed to create email invitation"
      );
    }
  }

  async function copyEmailRegistrationLink() {
    if (!emailRegistrationLink) {
      showNotification(
          "error",
          "No registration link is available"
      );
      return;
    }

    try {
      await navigator.clipboard.writeText(
          emailRegistrationLink
      );

      showNotification(
          "success",
          "Registration link copied"
      );
    } catch (error) {
      console.error(error);

      showNotification(
          "error",
          "Failed to copy registration link"
      );
    }
  }

  async function removeGroupMember(username) {
    if (!selectedGroupId) {
      showNotification("error", "Please select a group first");
      return;
    }

    if (
        currentGroupRole !== "OWNER" &&
        currentGroupRole !== "ADMIN"
    ) {
      showNotification(
          "error",
          "Only the group owner or an admin can remove members"
      );
      return;
    }

    if (username === selectedGroupAdmin) {
      showNotification("error", "The group owner cannot be removed");
      return;
    }

    const confirmed = window.confirm(
        `Remove ${username} from this group?`
    );

    if (!confirmed) {
      return;
    }

    try {
      const response = await api.delete(
          `/api/groups/${selectedGroupId}/members/${encodeURIComponent(username)}`
      );

      await loadMembers(selectedGroupId);

      showNotification(
          "success",
          response.data?.status || `${username} was removed from the group`
      );
    } catch (error) {
      console.error(error);

      showNotification(
          "error",
          error.response?.data?.error || "Failed to remove group member"
      );
    }
  }

  async function promoteGroupMember(username) {
    if (!selectedGroupId) {
      showNotification("error", "Please select a group first");
      return;
    }

    if (currentUsername !== selectedGroupAdmin) {
      showNotification("error", "Only the group owner can promote members");
      return;
    }

    if (username === selectedGroupAdmin) {
      showNotification(
          "error",
          "The group owner already has the highest role"
      );
      return;
    }

    const confirmed = window.confirm(
        `Promote ${username} to group admin?`
    );

    if (!confirmed) {
      return;
    }

    try {
      const response = await api.put(
          `/api/groups/${selectedGroupId}/members/${encodeURIComponent(username)}/promote`
      );

      await loadMembers(selectedGroupId);

      showNotification(
          "success",
          response.data?.status ||
          `${username} was promoted to group admin`
      );
    } catch (error) {
      console.error(error);

      showNotification(
          "error",
          error.response?.data?.error ||
          "Failed to promote group member"
      );
    }
  }

  async function demoteGroupAdmin(username) {
    if (!selectedGroupId) {
      showNotification("error", "Please select a group first");
      return;
    }

    if (currentUsername !== selectedGroupAdmin) {
      showNotification("error", "Only the group owner can demote admins");
      return;
    }

    if (username === selectedGroupAdmin) {
      showNotification("error", "The group owner cannot be demoted");
      return;
    }

    const confirmed = window.confirm(
        `Demote ${username} to regular member?`
    );

    if (!confirmed) {
      return;
    }

    try {
      const response = await api.put(
          `/api/groups/${selectedGroupId}/members/${encodeURIComponent(username)}/demote`
      );

      await loadMembers(selectedGroupId);

      showNotification(
          "success",
          response.data?.status ||
          `${username} was demoted to regular member`
      );
    } catch (error) {
      console.error(error);

      showNotification(
          "error",
          error.response?.data?.error ||
          "Failed to demote group admin"
      );
    }
  }

  function clearSelectedGroupAttachment() {
    setSelectedGroupAttachment(null);

    if (groupAttachmentInputRef.current) {
      groupAttachmentInputRef.current.value = "";
    }
  }

  const currentGroupMember = members.find(
      (member) => member.username === currentUsername
  );

  const currentGroupRole = currentGroupMember?.role;

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

  function getDecisionForMessage(messageId) {
    return groupDecisions.find(
        (decision) =>
            String(decision.sourceMessageId) ===
            String(messageId)
    );
  }

  function formatDecisionGovernanceMode(mode) {
    if (mode === "OWNER_LED") {
      return "Owner Led";
    }

    if (mode === "OWNER_REVIEW") {
      return "Owner Review";
    }

    if (mode === "MEMBER_VOTE") {
      return "Member Vote";
    }

    return mode || "Decision";
  }

  function formatDecisionStatus(status) {
    if (status === "PROPOSED") {
      return "Proposed";
    }

    if (status === "DISCUSSION_OPEN") {
      return "Discussion Open";
    }

    if (status === "VOTING_OPEN") {
      return "Voting Open";
    }

    if (status === "WAITING_FOR_TIE_BREAK") {
      return "Waiting for Tie-Break";
    }

    if (status === "APPROVED") {
      return "Approved";
    }

    if (status === "REJECTED") {
      return "Rejected";
    }

    if (status === "WITHDRAWN") {
      return "Withdrawn";
    }

    if (status === "EXPIRED_WITHOUT_QUORUM") {
      return "Expired Without Quorum";
    }

    if (status === "EXPIRED_WITHOUT_DECISION") {
      return "Expired Without Decision";
    }

    return status || "Unknown";
  }

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

  async function sendPresenceHeartbeat() {
    try {
      await api.post("/api/presence/heartbeat");
    } catch (error) {
      console.error(error);
    }
  }


  async function sendGroupTypingStatus(typing) {
    if (!selectedGroupId) {
      return;
    }

    if (lastTypingStatusRef.current === typing) {
      return;
    }

    lastTypingStatusRef.current = typing;

    try {
      await api.post(
          `/api/groups/${selectedGroupId}/typing`,
          { typing }
      );
    } catch (error) {
      console.error(error);
    }
  }

  function handleGroupMessageChange(event) {
    const nextMessage = event.target.value;

    setMessage(nextMessage);

    if (!nextMessage.trim()) {
      if (typingStopTimeoutRef.current) {
        clearTimeout(typingStopTimeoutRef.current);
        typingStopTimeoutRef.current = null;
      }

      sendGroupTypingStatus(false);
      return;
    }

    sendGroupTypingStatus(true);

    if (typingStopTimeoutRef.current) {
      clearTimeout(typingStopTimeoutRef.current);
    }

    typingStopTimeoutRef.current = setTimeout(() => {
      sendGroupTypingStatus(false);
      typingStopTimeoutRef.current = null;
    }, 1500);
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

      if (typingStopTimeoutRef.current) {
        clearTimeout(typingStopTimeoutRef.current);
        typingStopTimeoutRef.current = null;
      }

      await sendGroupTypingStatus(false);
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

  function openDecisionGovernancePanel(msg) {
    if (!selectedGroupId) {
      showNotification("error", "Please select a group first");
      return;
    }

    if (!msg?.id) {
      showNotification("error", "Unable to select this group message");
      return;
    }

    if (!msg.message?.trim()) {
      showNotification(
          "error",
          "A blank group message cannot become a decision"
      );
      return;
    }

    setOpenMessageActionsId(null);
    setDecisionMessage(msg);
    setDecisionGovernanceMode("OWNER_REVIEW");
  }

  function closeDecisionGovernancePanel() {
    if (creatingDecision) {
      return;
    }

    setDecisionMessage(null);
    setDecisionGovernanceMode("OWNER_REVIEW");
  }

  async function createGroupDecision() {
    if (!selectedGroupId) {
      showNotification("error", "Please select a group first");
      return;
    }

    if (!decisionMessage?.id) {
      showNotification("error", "Please select a group message");
      return;
    }

    if (
        decisionGovernanceMode === "OWNER_LED" &&
        currentGroupRole !== "OWNER"
    ) {
      showNotification(
          "error",
          "Only the group owner can select Owner Led"
      );
      return;
    }

    try {
      setCreatingDecision(true);

      const response = await api.post(
          `/api/groups/${selectedGroupId}/messages/${decisionMessage.id}/decision`,
          {
            governanceMode: decisionGovernanceMode,
          }
      );

      setDecisionMessage(null);
      setDecisionGovernanceMode("OWNER_REVIEW");

      await loadGroupDecisions(selectedGroupId);

      showNotification(
          "success",
          response.data?.status || "Group decision created"
      );
    } catch (error) {
      console.error(error);

      showNotification(
          "error",
          error.response?.data?.error ||
          "Failed to create group decision"
      );
    } finally {
      setCreatingDecision(false);
    }
  }
  async function togglePinGroupMessage(msg) {
    if (!selectedGroupId) {
      showNotification("error", "Please select a group first");
      return;
    }

    try {
      const response = await api.put(
          `/api/groups/${selectedGroupId}/messages/${msg.id}/pin`
      );

      await loadMessages(selectedGroupId);

      showNotification(
          "success",
          response.data?.status || "Group message pin status updated"
      );
    } catch (error) {
      console.error(error);

      showNotification(
          "error",
          error.response?.data?.error || "Failed to update pinned message"
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
      setSelectedGroupName("");
      setSelectedGroupAdmin("");
      setMessages([]);
      setGroupDecisions([]);
      setGroupAttachments([]);
      setMembers([]);
      setGroupMessageSearch("");
      setDecisionMessage(null);
      setDecisionGovernanceMode("OWNER_REVIEW");
      setCreatingDecision(false);
      setOpenMessageActionsId(null);
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
    loadPendingInvitations();
  }, []);

  useEffect(() => {
    if (!selectedGroupId || !showConversation) return;

    loadMessages(selectedGroupId);
    loadGroupAttachments(selectedGroupId);

    const intervalId = setInterval(() => {
      loadMessages(selectedGroupId);
      loadGroupAttachments(selectedGroupId);
      loadMembers(selectedGroupId);
    }, 3000);

    return () => clearInterval(intervalId);
  }, [selectedGroupId, showConversation]);

  useEffect(() => {
    void sendPresenceHeartbeat();

    const heartbeatIntervalId = setInterval(() => {
      void sendPresenceHeartbeat();
    }, 10000);

    return () => {
      clearInterval(heartbeatIntervalId);
    };
  }, []);

  useEffect(() => {
    if (!selectedGroupId || !showConversation) {
      setOnlineUsernames([]);
      return;
    }

    let active = true;

    async function refreshGroupPresence() {
      try {
        const response = await api.get(
            `/api/presence/groups/${selectedGroupId}`
        );

        if (!active) {
          return;
        }

        setOnlineUsernames(
            Array.isArray(response.data?.onlineUsernames)
                ? response.data.onlineUsernames
                : []
        );
      } catch (error) {
        console.error(error);

        if (active) {
          setOnlineUsernames([]);
        }
      }
    }

    void refreshGroupPresence();

    const presenceIntervalId = setInterval(() => {
      void refreshGroupPresence();
    }, 3000);

    return () => {
      active = false;
      clearInterval(presenceIntervalId);
      setOnlineUsernames([]);
    };
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

        client.subscribe(`/topic/groups/${selectedGroupId}`, (frame) => {
          let groupEvent = {};

          try {
            groupEvent = JSON.parse(frame.body || "{}");
          } catch (error) {
            console.error(error);
          }

          if (groupEvent.type === "GROUP_TYPING_STATUS") {
            const typingUsername = groupEvent.username;

            if (
                !typingUsername ||
                typingUsername === currentUsername
            ) {
              return;
            }

            const existingTimeout =
                remoteTypingTimeoutsRef.current[typingUsername];

            if (existingTimeout) {
              clearTimeout(existingTimeout);
              delete remoteTypingTimeoutsRef.current[typingUsername];
            }

            if (groupEvent.typing) {
              setTypingUsernames((currentUsernames) =>
                  currentUsernames.includes(typingUsername)
                      ? currentUsernames
                      : [...currentUsernames, typingUsername]
              );

              remoteTypingTimeoutsRef.current[typingUsername] =
                  setTimeout(() => {
                    setTypingUsernames((currentUsernames) =>
                        currentUsernames.filter(
                            (username) => username !== typingUsername
                        )
                    );

                    delete remoteTypingTimeoutsRef.current[
                        typingUsername
                    ];
                  }, 2500);
            } else {
              setTypingUsernames((currentUsernames) =>
                  currentUsernames.filter(
                      (username) => username !== typingUsername
                  )
              );
            }

            return;
          }

          if (
              groupEvent.type ===
              "GROUP_DECISION_RESOLVED"
          ) {
            setGroupDecisions((currentDecisions) =>
                currentDecisions.map((decision) =>
                    String(decision.decisionId) ===
                    String(groupEvent.decisionId)
                        ? {
                            ...decision,
                            status:
                                groupEvent.decisionStatus,
                          }
                        : decision
                )
            );

            if (
                groupEvent.resolvedBy &&
                groupEvent.resolvedBy !==
                    currentUsername
            ) {
              const resolutionLabel =
                  groupEvent.decisionStatus ===
                  "APPROVED"
                      ? "approved"
                      : "rejected";

              showNotification(
                  "success",
                  `Group decision ${resolutionLabel} by ${groupEvent.resolvedBy}`
              );
            }

            return;
          }

          if (
              groupEvent.type ===
              "GROUP_DECISION_CREATED"
          ) {
            loadGroupDecisions(selectedGroupId);
            return;
          }

          loadMessages(selectedGroupId);
          loadGroupAttachments(selectedGroupId);
          loadMembers(selectedGroupId);
          loadGroupDecisions(selectedGroupId);
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

      if (lastTypingStatusRef.current) {
        void sendGroupTypingStatus(false);
      }

      if (typingStopTimeoutRef.current) {
        clearTimeout(typingStopTimeoutRef.current);
        typingStopTimeoutRef.current = null;
      }

      Object.values(remoteTypingTimeoutsRef.current).forEach(
          (timeoutId) => clearTimeout(timeoutId)
      );

      remoteTypingTimeoutsRef.current = {};
      lastTypingStatusRef.current = false;
      setTypingUsernames([]);

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

    .memberList {
      scrollbar-width: thin;
      scrollbar-color: rgba(100, 116, 139, 0.35) transparent;
    }

    .memberList::-webkit-scrollbar {
      width: 4px;
    }

    .memberList::-webkit-scrollbar-track {
      background: transparent;
    }

    .memberList::-webkit-scrollbar-thumb {
      background: rgba(100, 116, 139, 0.28);
      border-radius: 999px;
    }

    .memberList:hover::-webkit-scrollbar-thumb {
      background: rgba(100, 116, 139, 0.5);
    }
  `}
        </style>

  <h1 style={styles.title}>Group Chat</h1>

        {notification && (
            <div
                style={{
                  position: "fixed",
                  top: "22px",
                  right: "clamp(16px, 4vw, 48px)",
                  zIndex: 9999,
                  width: "min(220px, calc(100vw - 32px))",
                  maxWidth: "calc(100vw - 32px)",
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
                  whiteSpace: "normal",
                  overflowWrap: "break-word",
                  wordBreak: "break-word",
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
                  <h2 style={styles.sectionTitle}>
                    Pending Invitations
                    {pendingInvitations.length > 0 &&
                        ` (${pendingInvitations.length})`}
                  </h2>

                  {pendingInvitations.length === 0 ? (
                      <p style={styles.muted}>
                        No pending group invitations.
                      </p>
                  ) : (
                      <div style={styles.pendingInvitationList}>
                        {pendingInvitations.map((invitation) => (
                            <div
                                key={invitation.invitationId}
                                style={styles.pendingInvitationCard}
                            >
                              <div style={styles.pendingInvitationDetails}>
                                <div style={styles.pendingInvitationGroupName}>
                                  {invitation.groupName} #{invitation.groupId}
                                </div>

                                <div style={styles.pendingInvitationMeta}>
                                  Invited by {invitation.invitedBy}
                                </div>

                                {invitation.createdAt && (
                                    <div style={styles.pendingInvitationTime}>
                                      {formatGroupMessageTimestamp(
                                          invitation.createdAt
                                      )}
                                    </div>
                                )}
                              </div>

                              <div style={styles.pendingInvitationActions}>
                                <button
                                    type="button"
                                    style={styles.acceptInvitationButton}
                                    onClick={() =>
                                        acceptGroupInvitation(
                                            invitation.invitationId
                                        )
                                    }
                                >
                                  Accept
                                </button>

                                <button
                                    type="button"
                                    style={styles.declineInvitationButton}
                                    onClick={() =>
                                        declineGroupInvitation(
                                            invitation.invitationId
                                        )
                                    }
                                >
                                  Decline
                                </button>
                              </div>
                            </div>
                        ))}
                      </div>
                  )}
                </div>

                <div style={styles.section}>
                  <button
                      style={styles.sectionTitleButton}
                      onClick={() => setShowGroups(!showGroups)}
                  >
                    My Groups {showGroups ? "\u25B2" : "\u25BC"}
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
                                      setSelectedGroupAdmin(group.createdBy || "");
                                      setGroupMessageSearch("");
                                      setInviteUsername("");
                                      setInviteEmail("");
                                      setEmailRegistrationLink("");
                                      setDecisionMessage(null);
                                      setDecisionGovernanceMode(
                                          "OWNER_REVIEW"
                                      );
                                      setCreatingDecision(false);
                                      setOpenMessageActionsId(null);
                                      setShowConversation(true);
                                      window.scrollTo({ top: 0, behavior: "auto" });
                                      loadMessages(group.id);
                                      loadGroupDecisions(group.id);
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
                      setGroupDecisions([]);
                      setGroupAttachments([]);
                      setMembers([]);
                      setSelectedGroupAdmin("");
                      setGroupMessageSearch("");
                      setInviteUsername("");
                      setInviteEmail("");
                      setEmailRegistrationLink("");
                    }}
                >
                  Back to Groups
                </button>

                <div style={styles.memberBox}>
                  {(currentGroupRole === "OWNER" ||
                      currentGroupRole === "ADMIN") && (
                      <div style={styles.groupInviteBox}>
                        <div style={styles.groupInviteSection}>
                          <p style={styles.groupInviteLabel}>
                            Invite registered user
                          </p>

                          <div style={styles.groupInviteRow}>
                            <input
                                type="text"
                                placeholder="Enter username"
                                value={inviteUsername}
                                onChange={(event) =>
                                    setInviteUsername(
                                        event.target.value
                                    )
                                }
                                onKeyDown={(event) => {
                                  if (event.key === "Enter") {
                                    event.preventDefault();
                                    inviteRegisteredUser();
                                  }
                                }}
                                style={styles.groupInviteInput}
                                autoComplete="off"
                            />

                            <button
                                type="button"
                                style={styles.groupInviteButton}
                                onClick={inviteRegisteredUser}
                            >
                              Invite
                            </button>
                          </div>
                        </div>

                        <div style={styles.groupInviteSection}>
                          <p style={styles.groupInviteLabel}>
                            Invite unregistered user by email
                          </p>

                          <div style={styles.groupInviteRow}>
                            <input
                                type="email"
                                placeholder="Enter email"
                                value={inviteEmail}
                                onChange={(event) => {
                                  setInviteEmail(
                                      event.target.value
                                  );
                                  setEmailRegistrationLink("");
                                }}
                                onKeyDown={(event) => {
                                  if (event.key === "Enter") {
                                    event.preventDefault();
                                    inviteUnregisteredUserByEmail();
                                  }
                                }}
                                style={styles.groupInviteInput}
                                autoComplete="off"
                            />

                            <button
                                type="button"
                                style={styles.groupInviteButton}
                                onClick={
                                  inviteUnregisteredUserByEmail
                                }
                            >
                              Create
                            </button>
                          </div>
                        </div>

                        {emailRegistrationLink && (
                            <div
                                style={
                                  styles.emailRegistrationLinkBox
                                }
                            >
                              <p
                                  style={
                                    styles.emailRegistrationLinkText
                                  }
                              >
                                Secure registration link created.
                                It expires in seven days.
                              </p>

                              <button
                                  type="button"
                                  style={
                                    styles.copyRegistrationLinkButton
                                  }
                                  onClick={
                                    copyEmailRegistrationLink
                                  }
                              >
                                Copy registration link
                              </button>
                            </div>
                        )}
                      </div>
                  )}

                  <p style={styles.membersLabel}>
                    Members ({members.length})
                  </p>

                  <div
                      className="memberList"
                      style={styles.memberPills}
                  >

                    {members.map((member) => (
                        <div
                            key={member.username}
                            style={styles.memberControl}
                        >
      <span style={styles.memberPill}>
  <span style={styles.memberNameRow}>
    <span
        style={{
          ...styles.presenceDot,
          ...(onlineUsernames.includes(member.username)
              ? styles.presenceDotOnline
              : styles.presenceDotOffline),
        }}
        aria-hidden="true"
    />

    <span>{member.username}</span>
  </span>

  <span style={styles.memberAdminLabel}>
    {member.role === "OWNER"
        ? "Owner"
        : member.role === "ADMIN"
            ? "Admin"
            : "Member"}
  </span>

  <span style={styles.memberPresenceLabel}>
    {onlineUsernames.includes(member.username)
        ? "Online"
        : "Offline"}
  </span>
</span>
                          {member.username !== currentUsername && (
                              <>
                                {currentGroupRole === "OWNER" && (
                                    <div style={styles.memberActions}>
                                      {member.role === "MEMBER" && (
                                          <button
                                              type="button"
                                              style={styles.roleMemberButton}
                                              onClick={() =>
                                                  promoteGroupMember(member.username)
                                              }
                                          >
                                            Promote
                                          </button>
                                      )}

                                      {member.role === "ADMIN" && (
                                          <button
                                              type="button"
                                              style={styles.roleMemberButton}
                                              onClick={() =>
                                                  demoteGroupAdmin(member.username)
                                              }
                                          >
                                            Demote
                                          </button>
                                      )}

                                      <button
                                          type="button"
                                          style={styles.removeMemberButton}
                                          onClick={() =>
                                              removeGroupMember(member.username)
                                          }
                                      >
                                        Remove
                                      </button>
                                    </div>
                                )}

                                {currentGroupRole === "ADMIN" &&
                                    member.role === "MEMBER" && (
                                        <div style={styles.memberActions}>
                                          <button
                                              type="button"
                                              style={styles.removeMemberButton}
                                              onClick={() =>
                                                  removeGroupMember(member.username)
                                              }
                                          >
                                            Remove
                                          </button>
                                        </div>
                                    )}
                              </>
                          )}
                        </div>
                    ))}
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
                {decisionMessage && (
                    <section
                        style={styles.decisionGovernancePanel}
                        aria-label="Create group decision"
                    >
                      <div style={styles.decisionGovernanceHeader}>
                        <div>
                          <p style={styles.decisionGovernanceTitle}>
                            Create Decision
                          </p>

                          <p style={styles.decisionGovernanceMessage}>
                            {decisionMessage.sender === currentUsername
                                ? "Your message"
                                : `${decisionMessage.sender}'s message`}
                            : {decisionMessage.message}
                          </p>
                        </div>

                        <button
                            type="button"
                            style={styles.decisionGovernanceCloseButton}
                            onClick={closeDecisionGovernancePanel}
                            disabled={creatingDecision}
                            aria-label="Close decision panel"
                        >
                          Close
                        </button>
                      </div>

                      <label style={styles.decisionGovernanceLabel}>
                        Governance mode

                        <select
                            value={decisionGovernanceMode}
                            onChange={(event) =>
                                setDecisionGovernanceMode(
                                    event.target.value
                                )
                            }
                            style={styles.decisionGovernanceSelect}
                            disabled={creatingDecision}
                        >
                          <option value="OWNER_REVIEW">
                            Owner Review
                          </option>

                          <option value="MEMBER_VOTE">
                            Member Vote
                          </option>

                          {currentGroupRole === "OWNER" && (
                              <option value="OWNER_LED">
                                Owner Led
                              </option>
                          )}
                        </select>
                      </label>

                      <p style={styles.decisionGovernanceDescription}>
                        {decisionGovernanceMode === "OWNER_LED" &&
                            "The group owner initiates and makes the decision directly."}

                        {decisionGovernanceMode === "OWNER_REVIEW" &&
                            "A member submits a proposal for the group owner to approve or reject."}

                        {decisionGovernanceMode === "MEMBER_VOTE" &&
                            "Eligible group members vote on the proposed decision."}
                      </p>

                      <div style={styles.decisionGovernanceActions}>
                        <button
                            type="button"
                            style={styles.decisionGovernanceCancelButton}
                            onClick={closeDecisionGovernancePanel}
                            disabled={creatingDecision}
                        >
                          Cancel
                        </button>

                        <button
                            type="button"
                            style={{
                              ...styles.decisionGovernanceCreateButton,
                              ...(creatingDecision
                                  ? styles.decisionGovernanceButtonDisabled
                                  : {}),
                            }}
                            onClick={createGroupDecision}
                            disabled={creatingDecision}
                        >
                          {creatingDecision
                              ? "Creating..."
                              : "Create Decision"}
                        </button>
                      </div>
                    </section>
                )}

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

                        const messageDecision =
                            getDecisionForMessage(msg.id);

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
                                    marginTop: sameSenderAsPrevious ? "6px" : "14px",
                                    marginBottom: "0",
                                  }}
                              >
                                <div
                                    style={
                                      msg.sender === currentUsername
                                          ? styles.myMessageBubble
                                          : styles.otherMessageBubble
                                    }
                                    role={!isEditingThisMessage ? "button" : undefined}
                                    tabIndex={!isEditingThisMessage ? 0 : undefined}
                                    aria-label={!isEditingThisMessage ? "Open message actions" : undefined}
                                    aria-expanded={
                                      !isEditingThisMessage
                                          ? openMessageActionsId === msg.id
                                          : undefined
                                    }
                                    onMouseEnter={() => setHoveredMessageActionsId(msg.id)}
                                    onMouseLeave={() => setHoveredMessageActionsId(null)}
                                    onFocus={() => setHoveredMessageActionsId(msg.id)}
                                    onBlur={(event) => {
                                      if (!event.currentTarget.contains(event.relatedTarget)) {
                                        setHoveredMessageActionsId(null);
                                      }
                                    }}
                                    onClick={(event) => {
                                      if (isEditingThisMessage) {
                                        return;
                                      }

                                      if (event.target.closest("button, textarea, input, a")) {
                                        return;
                                      }

                                      setOpenMessageActionsId(
                                          openMessageActionsId === msg.id ? null : msg.id
                                      );
                                    }}
                                    onKeyDown={(event) => {
                                      if (
                                          !isEditingThisMessage &&
                                          (event.key === "Enter" || event.key === " ")
                                      ) {
                                        event.preventDefault();

                                        setOpenMessageActionsId(
                                            openMessageActionsId === msg.id ? null : msg.id
                                        );
                                      }
                                    }}
                                >
                                  {msg.pinned && (
                                      <p style={styles.pinnedMessageLabel}>
                                        Pinned by {msg.pinnedBy || "group member"}
                                      </p>
                                  )}

                                  <p style={styles.sender}>
                                    {msg.sender === currentUsername ? "You" : msg.sender} -{" "}
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
                                      <>
                                        {!(
                                            messageAttachments.length > 0 &&
                                            msg.message?.startsWith("Attachment:")
                                        ) && (
                                            <p style={styles.messageText}>
                                              {msg.message}
                                            </p>
                                        )}
                                        {messageDecision && (
                                            <div style={styles.messageDecisionContainer}>
                                              <div style={styles.messageDecisionSummary}>
                                                <span style={styles.messageDecisionBadge}>
                                                  {formatDecisionGovernanceMode(
                                                      messageDecision.governanceMode
                                                  )}
                                                </span>

                                                <span style={styles.messageDecisionStatus}>
                                                  {formatDecisionStatus(
                                                      messageDecision.status
                                                  )}
                                                </span>

                                                {currentGroupRole === "OWNER" &&
                                                    messageDecision.governanceMode ===
                                                        "OWNER_REVIEW" &&
                                                    messageDecision.status ===
                                                        "PROPOSED" && (
                                                        <div
                                                            style={{
                                                              display: "flex",
                                                              flexDirection: "column",
                                                              alignItems: "flex-end",
                                                              gap: "6px",
                                                              marginLeft: "auto",
                                                            }}
                                                        >
                                                          <button
                                                              type="button"
                                                              aria-label="Toggle decision actions"
                                                              aria-expanded={
                                                                openDecisionActionsId ===
                                                                messageDecision.decisionId
                                                              }
                                                              style={{
                                                                ...styles.messageActionMenuButton,
                                                                padding: "4px 9px",
                                                              }}
                                                              onClick={(event) => {
                                                                event.stopPropagation();
                                                                setOpenMessageActionsId(null);

                                                                setOpenDecisionActionsId(
                                                                    openDecisionActionsId ===
                                                                    messageDecision.decisionId
                                                                        ? null
                                                                        : messageDecision.decisionId
                                                                );
                                                              }}
                                                          >
                                                            Decision actions
                                                          </button>

                                                          {openDecisionActionsId ===
                                                              messageDecision.decisionId && (
                                                              <div
                                                                  style={{
                                                                    display: "flex",
                                                                    alignItems: "center",
                                                                    gap: "8px",
                                                                    padding: "5px",
                                                                    border: "1px solid #475569",
                                                                    borderRadius: "10px",
                                                                    backgroundColor: "#020617",
                                                                    boxShadow:
                                                                        "0 8px 20px rgba(0, 0, 0, 0.30)",
                                                                  }}
                                                              >
                                                                <button
                                                                    type="button"
                                                                    style={
                                                                      styles.approveDecisionButton
                                                                    }
                                                                    onClick={(event) => {
                                                                      event.stopPropagation();
                                                                      setOpenDecisionActionsId(null);

                                                                      resolveOwnerReviewDecision(
                                                                          messageDecision,
                                                                          "approve"
                                                                      );
                                                                    }}
                                                                    disabled={
                                                                      resolvingDecisionId ===
                                                                      messageDecision.decisionId
                                                                    }
                                                                >
                                                                  {resolvingDecisionId ===
                                                                  messageDecision.decisionId
                                                                      ? "Processing..."
                                                                      : "Approve"}
                                                                </button>

                                                                <button
                                                                    type="button"
                                                                    style={
                                                                      styles.rejectDecisionButton
                                                                    }
                                                                    onClick={(event) => {
                                                                      event.stopPropagation();
                                                                      setOpenDecisionActionsId(null);

                                                                      resolveOwnerReviewDecision(
                                                                          messageDecision,
                                                                          "reject"
                                                                      );
                                                                    }}
                                                                    disabled={
                                                                      resolvingDecisionId ===
                                                                      messageDecision.decisionId
                                                                    }
                                                                >
                                                                  {resolvingDecisionId ===
                                                                  messageDecision.decisionId
                                                                      ? "Processing..."
                                                                      : "Reject"}
                                                                </button>
                                                              </div>
                                                          )}
                                                        </div>
                                                    )}
                                              </div>

                                              {currentGroupRole === "OWNER" &&
                                                  messageDecision.governanceMode ===
                                                      "MEMBER_VOTE" &&
                                                  messageDecision.status ===
                                                      "PROPOSED" && (
                                                      <div
                                                          style={
                                                            styles.memberVotingOpenPanel
                                                          }
                                                      >
                                                        <label
                                                            style={
                                                              styles.memberVotingDeadlineLabel
                                                            }
                                                        >
                                                          Voting deadline

                                                          <input
                                                              type="datetime-local"
                                                              value={
                                                                votingDeadlineInputs[
                                                                    messageDecision.decisionId
                                                                ] || ""
                                                              }
                                                              onChange={(event) =>
                                                                setVotingDeadlineInputs(
                                                                    (
                                                                        currentInputs
                                                                    ) => ({
                                                                      ...currentInputs,
                                                                      [messageDecision.decisionId]:
                                                                          event.target.value,
                                                                    })
                                                                )
                                                              }
                                                              style={
                                                                styles.memberVotingDeadlineInput
                                                              }
                                                              disabled={
                                                                openingVotingDecisionId ===
                                                                messageDecision.decisionId
                                                              }
                                                          />
                                                        </label>

                                                        <button
                                                            type="button"
                                                            style={{
                                                              ...styles.openVotingButton,
                                                              ...(openingVotingDecisionId ===
                                                              messageDecision.decisionId
                                                                  ? styles.decisionGovernanceButtonDisabled
                                                                  : {}),
                                                            }}
                                                            onClick={() =>
                                                              openMemberVoting(
                                                                  messageDecision
                                                              )
                                                            }
                                                            disabled={
                                                              openingVotingDecisionId ===
                                                              messageDecision.decisionId
                                                            }
                                                        >
                                                          {openingVotingDecisionId ===
                                                          messageDecision.decisionId
                                                              ? "Opening..."
                                                              : "Open Voting"}
                                                        </button>
                                                      </div>
                                                  )}
                                              {messageDecision.governanceMode ===
                                                  "MEMBER_VOTE" &&
                                                  messageDecision.status ===
                                                      "VOTING_OPEN" && (
                                                      <div
                                                          style={
                                                            styles.memberVotingBallotPanel
                                                          }
                                                      >
                                                        <div
                                                            style={
                                                              styles.memberVotingBallotText
                                                            }
                                                        >
                                                          <strong>
                                                            Secret ballot
                                                          </strong>

                                                          <span>
                                                            Choose Approve,
                                                            Reject, or Abstain.
                                                          </span>

                                                          {recordedBallotDecisionIds[
                                                              messageDecision.decisionId
                                                          ] && (
                                                              <span
                                                                  style={
                                                                    styles.memberVotingRecordedText
                                                                  }
                                                              >
                                                                Your secret ballot
                                                                has been recorded.
                                                                Select another
                                                                option to change it.
                                                              </span>
                                                          )}
                                                        </div>

                                                        <div
                                                            style={
                                                              styles.memberVotingBallotActions
                                                            }
                                                        >
                                                          {[
                                                            "APPROVE",
                                                            "REJECT",
                                                            "ABSTAIN",
                                                          ].map(
                                                              (
                                                                  voteChoice
                                                              ) => (
                                                                  <button
                                                                      key={
                                                                        voteChoice
                                                                      }
                                                                      type="button"
                                                                      style={{
                                                                        ...styles.memberVotingChoiceButton,
                                                                        ...(castingVoteDecisionId ===
                                                                        messageDecision.decisionId
                                                                            ? styles.decisionGovernanceButtonDisabled
                                                                            : {}),
                                                                      }}
                                                                      onClick={() =>
                                                                        castMemberVote(
                                                                            messageDecision,
                                                                            voteChoice
                                                                        )
                                                                      }
                                                                      disabled={
                                                                        castingVoteDecisionId ===
                                                                        messageDecision.decisionId
                                                                      }
                                                                  >
                                                                    {castingVoteDecisionId ===
                                                                    messageDecision.decisionId
                                                                        ? "Recording..."
                                                                        : voteChoice ===
                                                                          "APPROVE"
                                                                          ? "Approve"
                                                                          : voteChoice ===
                                                                            "REJECT"
                                                                            ? "Reject"
                                                                            : "Abstain"}
                                                                  </button>
                                                              )
                                                          )}
                                                        </div>
                                                      </div>
                                                  )}

                                              {currentGroupRole === "OWNER" &&
                                                  messageDecision.governanceMode ===
                                                      "MEMBER_VOTE" &&
                                                  messageDecision.status ===
                                                      "VOTING_OPEN" && (
                                                      <div
                                                          style={
                                                            styles.memberVotingBallotActions
                                                          }
                                                      >
                                                        <button
                                                            type="button"
                                                            style={{
                                                              ...styles.openVotingButton,
                                                              ...(resolvingDecisionId ===
                                                              messageDecision.decisionId
                                                                  ? styles.decisionGovernanceButtonDisabled
                                                                  : {}),
                                                            }}
                                                            onClick={() =>
                                                              resolveMemberVoting(
                                                                  messageDecision
                                                              )
                                                            }
                                                            disabled={
                                                              resolvingDecisionId ===
                                                              messageDecision.decisionId
                                                            }
                                                        >
                                                          {resolvingDecisionId ===
                                                          messageDecision.decisionId
                                                              ? "Resolving..."
                                                              : "Resolve Voting"}
                                                        </button>
                                                      </div>
                                                  )}

                                              {currentGroupRole === "OWNER" &&
                                                  messageDecision.governanceMode ===
                                                      "MEMBER_VOTE" &&
                                                  messageDecision.status ===
                                                      "WAITING_FOR_TIE_BREAK" && (
                                                      <div
                                                          style={
                                                            styles.memberVotingBallotPanel
                                                          }
                                                      >
                                                        <div
                                                            style={
                                                              styles.memberVotingBallotText
                                                            }
                                                        >
                                                          <strong>
                                                            Owner tie-break
                                                          </strong>

                                                          <span>
                                                            Cast the public
                                                            deciding vote.
                                                          </span>
                                                        </div>

                                                        <div
                                                            style={
                                                              styles.memberVotingBallotActions
                                                            }
                                                        >
                                                          <button
                                                              type="button"
                                                              style={{
                                                                ...styles.memberVotingChoiceButton,
                                                                ...(resolvingDecisionId ===
                                                                messageDecision.decisionId
                                                                    ? styles.decisionGovernanceButtonDisabled
                                                                    : {}),
                                                              }}
                                                              onClick={() =>
                                                                resolveMemberVoteTieBreak(
                                                                    messageDecision,
                                                                    "APPROVE"
                                                                )
                                                              }
                                                              disabled={
                                                                resolvingDecisionId ===
                                                                messageDecision.decisionId
                                                              }
                                                          >
                                                            {resolvingDecisionId ===
                                                            messageDecision.decisionId
                                                                ? "Resolving..."
                                                                : "Tie-Break Approve"}
                                                          </button>

                                                          <button
                                                              type="button"
                                                              style={{
                                                                ...styles.memberVotingChoiceButton,
                                                                ...(resolvingDecisionId ===
                                                                messageDecision.decisionId
                                                                    ? styles.decisionGovernanceButtonDisabled
                                                                    : {}),
                                                              }}
                                                              onClick={() =>
                                                                resolveMemberVoteTieBreak(
                                                                    messageDecision,
                                                                    "REJECT"
                                                                )
                                                              }
                                                              disabled={
                                                                resolvingDecisionId ===
                                                                messageDecision.decisionId
                                                              }
                                                          >
                                                            {resolvingDecisionId ===
                                                            messageDecision.decisionId
                                                                ? "Resolving..."
                                                                : "Tie-Break Reject"}
                                                          </button>
                                                        </div>
                                                      </div>
                                                  )}

                                            </div>
                                        )}
                                      </>
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

                                  {!isEditingThisMessage && openMessageActionsId === msg.id && (
                                      <div
                                          style={{
                                            ...styles.messageActionsMenu,
                                            ...(msg.sender === currentUsername
                                                ? {
                                                  right: "calc(100% + 8px)",
                                                }
                                                : {
                                                  left: "calc(100% + 8px)",
                                                }),
                                          }}
                                      >
                                        <button
                                            type="button"
                                            style={styles.messageActionMenuButton}
                                            onClick={() => {
                                              setOpenMessageActionsId(null);
                                              togglePinGroupMessage(msg);
                                            }}
                                        >
                                          {msg.pinned ? "Unpin" : "Pin"}
                                        </button>

                                        {!messageDecision && (
                                            <button
                                                type="button"
                                                style={styles.messageActionMenuButton}
                                                onClick={() => {
                                                  openDecisionGovernancePanel(msg);
                                                }}
                                            >
                                              Decision
                                            </button>
                                        )}

                                        {canModifyMessage && (
                                            <>
                                              <button
                                                  type="button"
                                                  style={styles.messageActionMenuButton}
                                                  onClick={() => {
                                                    setOpenMessageActionsId(null);
                                                    startEditingGroupMessage(msg);
                                                  }}
                                              >
                                                Edit
                                              </button>

                                              <button
                                                  type="button"
                                                  style={styles.messageActionMenuButton}
                                                  onClick={() => {
                                                    setOpenMessageActionsId(null);
                                                    deleteGroupMessage(msg);
                                                  }}
                                              >
                                                Delete
                                              </button>
                                            </>
                                        )}
                                      </div>
                                  )}

                                  {(
                                      (!isEditingThisMessage &&
                                          (
                                              hoveredMessageActionsId === msg.id ||
                                              openMessageActionsId === msg.id
                                          )) ||
                                      (msg.seenCount !== undefined && msg.memberCount !== undefined)
                                  ) && (
                                      <div style={styles.messageMetadataRow}>
                                        {!isEditingThisMessage &&
                                            (
                                                hoveredMessageActionsId === msg.id ||
                                                openMessageActionsId === msg.id
                                            ) && (
                                                <span
                                                    style={styles.messageActionsHint}
                                                    aria-hidden="true"
                                                >
                                                  ⋯
                                                </span>
                                            )}

                                        {msg.seenCount !== undefined && msg.memberCount !== undefined && (
                                            <p style={styles.seenStatus}>
                                              Seen by {msg.seenCount} of {msg.memberCount}
                                              {msg.editedAt && " · edited"}
                                            </p>
                                        )}
                                      </div>
                                  )}
                                </div>
                              </div>
                            </div>
                        );
                      })
                  )}

                  <div ref={messagesEndRef}/>
                </div>

                <div
                    style={{
                      minHeight: "22px",
                      padding: "2px 4px 4px",
                      color: "#d6c6a5",
                      fontSize: "13px",
                      fontStyle: "italic",
                    }}
                    aria-live="polite"
                >
                  {typingUsernames.length === 1 &&
                      `${typingUsernames[0]} is typing...`}

                  {typingUsernames.length === 2 &&
                      `${typingUsernames[0]} and ${typingUsernames[1]} are typing...`}

                  {typingUsernames.length > 2 &&
                      `${typingUsernames.length} people are typing...`}
                </div>

                <div style={styles.messageInputRow}>
  <textarea
      placeholder="Write a group message"
      value={message}
      onChange={handleGroupMessageChange}
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
    fontSize: "clamp(26px, 2.5vw, 34px)",
    marginBottom: "4px",
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

  pendingInvitationList: {
    display: "flex",
    flexDirection: "column",
    gap: "10px",
  },

  pendingInvitationCard: {
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
    gap: "14px",
    flexWrap: "wrap",
    padding: "12px 14px",
    borderRadius: "12px",
    border: "1px solid #334155",
    backgroundColor: "#020617",
  },

  pendingInvitationDetails: {
    minWidth: 0,
  },

  pendingInvitationGroupName: {
    color: "#e0f2fe",
    fontSize: "15px",
    fontWeight: "700",
  },

  pendingInvitationMeta: {
    marginTop: "4px",
    color: "#d6c6a5",
    fontSize: "13px",
    fontWeight: "700",
  },

  pendingInvitationTime: {
    marginTop: "4px",
    color: "#94a3b8",
    fontSize: "12px",
  },

  pendingInvitationActions: {
    display: "flex",
    alignItems: "center",
    gap: "8px",
    flexWrap: "wrap",
  },

  acceptInvitationButton: {
    padding: "8px 13px",
    borderRadius: "9px",
    border: "1px solid #38bdf8",
    backgroundColor: "#1e3a8a",
    color: "#ffffff",
    fontSize: "12px",
    fontWeight: "700",
    cursor: "pointer",
  },

  declineInvitationButton: {
    padding: "8px 13px",
    borderRadius: "9px",
    border: "1px solid #d6c6a5",
    backgroundColor: "transparent",
    color: "#d6c6a5",
    fontSize: "12px",
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
    flex: 1,
    minHeight: 0,
    overflowY: "auto",
    scrollbarWidth: "none",
    msOverflowStyle: "none",
    padding: "18px 14px 22px",
    marginBottom: "6px",
    display: "flex",
    flexDirection: "column",
    gap: 0,
    scrollPaddingTop: "24px",
    scrollPaddingBottom: "28px",
    scrollBehavior: "smooth",
    scrollSnapType: "y proximity",
  },

  memberBox: {
    marginBottom: "14px",
    textAlign: "center",
    display: "flex",
    flexDirection: "column",
    minHeight: 0,
    flex: 1,
  },
  groupInviteBox: {
    marginBottom: "10px",
    paddingBottom: "10px",
    borderBottom: "1px solid #1e293b",
    width: "100%",
    maxWidth: "100%",
    boxSizing: "border-box",
  },

  groupInviteSection: {
    marginBottom: "9px",
  },

  groupInviteLabel: {
    color: "#bfdbfe",
    fontSize: "12px",
    fontWeight: "700",
    lineHeight: "1.25",
    margin: "0 0 6px 0",
  },

  groupInviteRow: {
    display: "flex",
    alignItems: "stretch",
    gap: "5px",
    width: "100%",
  },

  groupInviteInput: {
    flex: 1,
    minWidth: 0,
    height: "34px",
    padding: "5px 8px",
    borderRadius: "7px",
    border: "1px solid #38bdf8",
    backgroundColor: "#020617",
    color: "#ffffff",
    fontSize: "12px",
    outline: "none",
    boxSizing: "border-box",
  },

  groupInviteButton: {
    height: "34px",
    border: "1px solid #d6c6a5",
    borderRadius: "7px",
    padding: "5px 8px",
    backgroundColor: "#0f172a",
    color: "#d6c6a5",
    fontSize: "11px",
    fontWeight: "700",
    cursor: "pointer",
    whiteSpace: "nowrap",
    flexShrink: 0,
  },

  emailRegistrationLinkBox: {
    marginTop: "4px",
    padding: "10px",
    borderRadius: "9px",
    border: "1px solid rgba(214, 198, 165, 0.65)",
    backgroundColor: "rgba(30, 41, 59, 0.7)",
    boxSizing: "border-box",
  },

  emailRegistrationLinkText: {
    margin: "0 0 9px 0",
    color: "#d6c6a5",
    fontSize: "12px",
    lineHeight: "1.4",
    textAlign: "center",
  },

  copyRegistrationLinkButton: {
    width: "100%",
    border: "1px solid #38bdf8",
    borderRadius: "8px",
    padding: "8px 10px",
    backgroundColor: "#0f172a",
    color: "#bfdbfe",
    fontSize: "12px",
    fontWeight: "700",
    cursor: "pointer",
    boxSizing: "border-box",
  },

  membersLabel: {
    color: "#bfdbfe",
    fontSize: "14px",
    fontWeight: "700",
    margin: "2px 0 7px 0",
  },

  memberPills: {
    display: "flex",
    justifyContent: "center",
    alignContent: "flex-start",
    columnGap: "5px",
    rowGap: "5px",
    flexWrap: "wrap",
    width: "100%",
    maxWidth: "100%",
    flex: 1,
    minHeight: 0,
    overflowY: "auto",
    overflowX: "hidden",
    paddingRight: "4px",
    paddingBottom: "4px",
    boxSizing: "border-box",
    scrollbarWidth: "thin",
  },

  memberPill: {
    backgroundColor: "#020617",
    color: "#e0f2fe",
    border: "1px solid #38bdf8",
    borderRadius: "999px",
    padding: "8px 14px",
    fontSize: "14px",
    fontWeight: "700",
    maxWidth: "100%",
    minWidth: 0,
    boxSizing: "border-box",
    whiteSpace: "normal",
    overflowWrap: "anywhere",
    textAlign: "center",
  },
  memberNameRow: {
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    gap: "6px",
  },

  presenceDot: {
    width: "8px",
    height: "8px",
    borderRadius: "50%",
    flexShrink: 0,
  },

  presenceDotOnline: {
    backgroundColor: "#38bdf8",
    boxShadow: "0 0 0 2px rgba(56, 189, 248, 0.18)",
  },

  presenceDotOffline: {
    backgroundColor: "#64748b",
  },

  memberPresenceLabel: {
    display: "block",
    marginTop: "3px",
    color: "#94a3b8",
    fontSize: "11px",
    fontWeight: "600",
  },
  memberControl: {
    display: "flex",
    alignItems: "center",
    gap: "5px",
    justifyContent: "center",
    flexWrap: "wrap",
    width: "100%",
    maxWidth: "100%",
    minWidth: 0,
    boxSizing: "border-box",
  },

  memberAdminLabel: {
    marginLeft: "6px",
    color: "#d6c6a5",
    fontSize: "11px",
    fontWeight: "700",
  },

  memberActions: {
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    gap: "5px",
    flexWrap: "wrap",
    maxWidth: "100%",
    minWidth: 0,
  },

  roleMemberButton: {
    border: "1px solid #d6c6a5",
    borderRadius: "7px",
    padding: "4px 7px",
    backgroundColor: "transparent",
    color: "#d6c6a5",
    fontSize: "11px",
    fontWeight: "700",
    cursor: "pointer",
    flexShrink: 0,
  },

  removeMemberButton: {
    border: "1px solid #d6c6a5",
    borderRadius: "7px",
    padding: "4px 7px",
    backgroundColor: "transparent",
    color: "#d6c6a5",
    fontSize: "11px",
    fontWeight: "700",
    cursor: "pointer",
    flexShrink: 0,
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
    position: "relative",
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
    position: "relative",
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
    height: "clamp(620px, 78vh, 760px)",
    alignItems: "stretch",
  },
  chatSidebar: {
    width: "260px",
    minWidth: "260px",
    height: "100%",
    justifyContent: "flex-start",
    backgroundColor: "#0f172a",
    border: "1px solid #1e293b",
    borderRadius: "16px",
    padding: "16px",
    display: "flex",
    flexDirection: "column",
    gap: "12px",
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

  pinnedMessageLabel: {
    color: "#d6c6a8",
    backgroundColor: "rgba(214, 198, 168, 0.10)",
    border: "1px solid rgba(214, 198, 168, 0.35)",
    borderRadius: "999px",
    display: "inline-block",
    padding: "3px 9px",
    fontSize: "11px",
    fontWeight: "800",
    margin: "0 0 8px 0",
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
  messageDecisionContainer: {
    marginTop: "7px",
  },
  messageDecisionSummary: {
    display: "flex",
    alignItems: "center",
    flexWrap: "wrap",
    gap: "6px",
  },
  messageDecisionBadge: {
    display: "inline-flex",
    alignItems: "center",
    border: "1px solid #475569",
    borderRadius: "999px",
    padding: "3px 8px",
    backgroundColor: "#0f172a",
    color: "#d6c6a8",
    fontSize: "11px",
    fontWeight: "800",
  },
  messageDecisionStatus: {
    color: "#bfdbfe",
    fontSize: "11px",
    fontWeight: "700",
  },
  messageDecisionActions: {
    display: "flex",
    alignItems: "center",
    flexWrap: "wrap",
    gap: "8px",
    marginTop: "8px",
  },
  memberVotingBallotPanel: {
    display: "flex",
    flexDirection: "column",
    gap: "9px",
    marginTop: "8px",
    padding: "10px",
    border: "1px solid #334155",
    borderRadius: "8px",
    backgroundColor: "#111c2e",
  },

  memberVotingBallotText: {
    display: "flex",
    flexDirection: "column",
    gap: "3px",
    color: "#cbd5e1",
    fontSize: "12px",
  },

  memberVotingRecordedText: {
    marginTop: "3px",
    color: "#d6c6a8",
    fontWeight: "700",
  },

  memberVotingBallotActions: {
    display: "flex",
    flexWrap: "wrap",
    gap: "7px",
  },

  memberVotingChoiceButton: {
    border: "1px solid #64748b",
    borderRadius: "8px",
    padding: "7px 12px",
    backgroundColor: "#1e293b",
    color: "#e2e8f0",
    fontSize: "12px",
    fontWeight: "800",
    cursor: "pointer",
  },

  memberVotingOpenPanel: {
    display: "flex",
    flexWrap: "wrap",
    alignItems: "flex-end",
    gap: "8px",
    marginTop: "8px",
    padding: "10px",
    border: "1px solid #334155",
    borderRadius: "8px",
    backgroundColor: "#111c2e",
  },

  memberVotingDeadlineLabel: {
    display: "flex",
    flexDirection: "column",
    gap: "5px",
    color: "#d6c6a8",
    fontSize: "12px",
    fontWeight: "700",
  },

  memberVotingDeadlineInput: {
    minWidth: "210px",
    padding: "7px 9px",
    border: "1px solid #64748b",
    borderRadius: "7px",
    backgroundColor: "#0f172a",
    color: "#e2e8f0",
    fontSize: "12px",
    outline: "none",
  },

  openVotingButton: {
    border: "1px solid #60a5fa",
    borderRadius: "8px",
    padding: "7px 12px",
    backgroundColor: "#1e3a5f",
    color: "#dbeafe",
    fontSize: "12px",
    fontWeight: "800",
    cursor: "pointer",
  },

  approveDecisionButton: {
    border: "1px solid #60a5fa",
    borderRadius: "8px",
    padding: "6px 12px",
    backgroundColor: "#1e3a5f",
    color: "#dbeafe",
    fontSize: "12px",
    fontWeight: "800",
    cursor: "pointer",
  },
  rejectDecisionButton: {
    border: "1px solid #d6c6a8",
    borderRadius: "8px",
    padding: "6px 12px",
    backgroundColor: "#0f172a",
    color: "#d6c6a8",
    fontSize: "12px",
    fontWeight: "800",
    cursor: "pointer",
  },
  messageActionsHint: {
    flexShrink: 0,
    color: "#ffffff",
    backgroundColor: "rgba(2, 6, 23, 0.92)",
    border: "1px solid rgba(214, 198, 168, 0.90)",
    borderRadius: "999px",
    padding: "0 7px 1px",
    fontSize: "15px",
    lineHeight: "1",
    fontWeight: "800",
    pointerEvents: "none",
    opacity: 1,
    boxShadow: "0 3px 10px rgba(0, 0, 0, 0.30)",
  },
  decisionGovernancePanel: {
    marginBottom: "12px",
    padding: "14px",
    border: "1px solid #475569",
    borderRadius: "12px",
    backgroundColor: "#0f172a",
    boxShadow: "0 8px 20px rgba(0, 0, 0, 0.24)",
  },
  decisionGovernanceHeader: {
    display: "flex",
    alignItems: "flex-start",
    justifyContent: "space-between",
    gap: "12px",
    marginBottom: "12px",
  },
  decisionGovernanceTitle: {
    margin: "0 0 5px 0",
    color: "#bfdbfe",
    fontSize: "14px",
    fontWeight: "800",
  },
  decisionGovernanceMessage: {
    margin: 0,
    maxWidth: "700px",
    color: "#d6c6a8",
    fontSize: "12px",
    lineHeight: "1.45",
    overflowWrap: "anywhere",
  },
  decisionGovernanceCloseButton: {
    flexShrink: 0,
    border: "1px solid #475569",
    borderRadius: "8px",
    padding: "5px 10px",
    backgroundColor: "#020617",
    color: "#d6c6a8",
    fontSize: "12px",
    fontWeight: "700",
    cursor: "pointer",
  },
  decisionGovernanceLabel: {
    display: "flex",
    flexDirection: "column",
    gap: "6px",
    color: "#bfdbfe",
    fontSize: "12px",
    fontWeight: "700",
  },
  decisionGovernanceSelect: {
    width: "100%",
    maxWidth: "340px",
    boxSizing: "border-box",
    border: "1px solid #475569",
    borderRadius: "8px",
    padding: "9px 10px",
    backgroundColor: "#020617",
    color: "#e2e8f0",
    fontSize: "13px",
    outline: "none",
  },
  decisionGovernanceDescription: {
    margin: "9px 0 0 0",
    color: "#94a3b8",
    fontSize: "12px",
    lineHeight: "1.45",
  },
  decisionGovernanceActions: {
    display: "flex",
    justifyContent: "flex-end",
    gap: "8px",
    marginTop: "12px",
  },
  decisionGovernanceCancelButton: {
    border: "1px solid #475569",
    borderRadius: "8px",
    padding: "7px 12px",
    backgroundColor: "#020617",
    color: "#d6c6a8",
    fontSize: "12px",
    fontWeight: "700",
    cursor: "pointer",
  },
  decisionGovernanceCreateButton: {
    border: "1px solid #d6c6a8",
    borderRadius: "8px",
    padding: "7px 12px",
    backgroundColor: "#1e3a5f",
    color: "#f5ead5",
    fontSize: "12px",
    fontWeight: "800",
    cursor: "pointer",
  },
  decisionGovernanceButtonDisabled: {
    opacity: 0.6,
    cursor: "not-allowed",
  },
  messageActionsMenu: {
    position: "absolute",
    top: "6px",
    zIndex: 20,
    display: "flex",
    alignItems: "center",
    gap: "6px",
    padding: "5px",
    borderRadius: "10px",
    border: "1px solid #475569",
    backgroundColor: "#020617",
    boxShadow: "0 8px 20px rgba(0, 0, 0, 0.40)",
    whiteSpace: "nowrap",
  },
  messageActionMenuButton: {
    border: "1px solid #475569",
    borderRadius: "8px",
    padding: "5px 10px",
    backgroundColor: "#0f172a",
    color: "#d6c6a8",
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
  messageMetadataRow: {
    display: "flex",
    alignItems: "center",
    justifyContent: "flex-end",
    gap: "6px",
    marginTop: "8px",
    minWidth: 0,
  },
  seenStatus: {
    color: "#94a3b8",
    fontSize: "12px",
    margin: 0,
    textAlign: "right",
    whiteSpace: "nowrap",
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

