import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";

export default function ChangePassword() {

    const navigate = useNavigate();

    const [currentPassword, setCurrentPassword] =
        useState("");

    const [newPassword, setNewPassword] =
        useState("");

    const [confirmPassword, setConfirmPassword] =
        useState("");

    const [notification, setNotification] =
        useState(null);

    const [isSubmitting, setIsSubmitting] =
        useState(false);

    function showNotification(type, text) {
        setNotification({ type, text });
    }

    async function handleChangePassword(event) {

        event.preventDefault();

        if (!currentPassword) {
            showNotification(
                "error",
                "Current password is required."
            );
            return;
        }

        if (!newPassword) {
            showNotification(
                "error",
                "New password is required."
            );
            return;
        }

        if (!confirmPassword) {
            showNotification(
                "error",
                "Please confirm your new password."
            );
            return;
        }

        const passwordLength =
            Array.from(newPassword).length;

        const passwordBytes =
            new TextEncoder()
                .encode(newPassword)
                .length;

        if (passwordLength < 15) {
            showNotification(
                "error",
                "New password must be at least 15 characters."
            );
            return;
        }

        if (passwordBytes > 72) {
            showNotification(
                "error",
                "New password must not exceed 72 UTF-8 bytes."
            );
            return;
        }

        if (newPassword !== confirmPassword) {
            showNotification(
                "error",
                "New passwords do not match."
            );
            return;
        }

        if (newPassword === currentPassword) {
            showNotification(
                "error",
                "New password must be different from the current password."
            );
            return;
        }

        try {
            setIsSubmitting(true);
            setNotification(null);

            await api.put(
                "/api/users/change-password",
                {
                    currentPassword,
                    newPassword
                }
            );

            showNotification(
                "success",
                "Password changed successfully. Redirecting to login..."
            );

            setCurrentPassword("");
            setNewPassword("");
            setConfirmPassword("");

            setTimeout(() => {
                localStorage.removeItem("token");
                localStorage.removeItem("username");
                navigate("/login", { replace: true });
            }, 1500);

        } catch (error) {
            console.error(error);

            showNotification(
                "error",
                error.response?.data?.error ||
                "Password change failed."
            );

        } finally {
            setIsSubmitting(false);
        }
    }

    const inputStyle = {
        width: "100%",
        boxSizing: "border-box",
        padding: "14px",
        marginBottom: "18px",
        borderRadius: "10px",
        border: "1px solid #1e40af",
        backgroundColor: "#0f172a",
        color: "#ffffff",
        fontSize: "16px",
        outline: "none"
    };

    return (
        <div
            style={{
                minHeight: "100vh",
                background:
                    "radial-gradient(circle at center, #0f2a4a 0%, #071a33 45%, #020617 100%)",
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
                fontFamily: "Arial, sans-serif",
                padding: "clamp(16px, 3vw, 24px)"
            }}
        >
            <div
                style={{
                    width: "100%",
                    maxWidth: "500px",
                    backgroundColor: "rgba(15, 23, 42, 0.94)",
                    border: "1px solid #1e3a8a",
                    borderRadius: "18px",
                    padding: "34px",
                    boxShadow:
                        "0 0 35px rgba(59, 130, 246, 0.18)"
                }}
            >
                <h1
                    style={{
                        color: "#38bdf8",
                        textAlign: "center",
                        fontSize: "clamp(30px, 5vw, 42px)",
                        marginTop: 0,
                        marginBottom: "10px"
                    }}
                >
                    Change Password
                </h1>

                <p
                    style={{
                        color: "#cbd5e1",
                        textAlign: "center",
                        lineHeight: "1.5",
                        marginTop: 0,
                        marginBottom: "26px"
                    }}
                >
                    Enter your current password and choose a secure
                    new password.
                </p>

                {notification && (
                    <div
                        style={{
                            backgroundColor:
                                notification.type === "success"
                                    ? "rgba(196, 167, 125, 0.16)"
                                    : "rgba(148, 163, 184, 0.14)",
                            border:
                                notification.type === "success"
                                    ? "1px solid #c4a77d"
                                    : "1px solid #64748b",
                            color:
                                notification.type === "success"
                                    ? "#e7d7bd"
                                    : "#e2e8f0",
                            padding: "12px",
                            borderRadius: "10px",
                            marginBottom: "20px",
                            textAlign: "center",
                            fontWeight: "bold"
                        }}
                    >
                        {notification.text}
                    </div>
                )}

                <form
                    onSubmit={handleChangePassword}
                    noValidate
                >
                    <input
                        type="password"
                        autoComplete="current-password"
                        placeholder="Current password"
                        value={currentPassword}
                        onChange={(event) =>
                            setCurrentPassword(event.target.value)
                        }
                        disabled={isSubmitting}
                        style={inputStyle}
                    />

                    <input
                        type="password"
                        autoComplete="new-password"
                        placeholder="New password"
                        value={newPassword}
                        onChange={(event) =>
                            setNewPassword(event.target.value)
                        }
                        minLength={15}
                        maxLength={72}
                        disabled={isSubmitting}
                        style={{
                            ...inputStyle,
                            marginBottom: "10px"
                        }}
                    />

                    <p
                        style={{
                            color: "#94a3b8",
                            fontSize: "13px",
                            lineHeight: "1.45",
                            marginTop: 0,
                            marginBottom: "16px"
                        }}
                    >
                        Use at least 15 characters. Passphrases and
                        spaces are allowed.
                    </p>

                    <input
                        type="password"
                        autoComplete="new-password"
                        placeholder="Confirm new password"
                        value={confirmPassword}
                        onChange={(event) =>
                            setConfirmPassword(event.target.value)
                        }
                        minLength={15}
                        maxLength={72}
                        disabled={isSubmitting}
                        style={inputStyle}
                    />

                    <button
                        type="submit"
                        disabled={isSubmitting}
                        style={{
                            width: "100%",
                            padding: "14px",
                            borderRadius: "10px",
                            border: "none",
                            backgroundColor: isSubmitting
                                ? "#334155"
                                : "#1e3a8a",
                            color: "#ffffff",
                            fontWeight: "bold",
                            fontSize: "16px",
                            cursor: isSubmitting
                                ? "not-allowed"
                                : "pointer",
                            marginBottom: "14px"
                        }}
                    >
                        {isSubmitting
                            ? "Changing Password..."
                            : "Change Password"}
                    </button>

                    <button
                        type="button"
                        onClick={() => navigate("/dashboard")}
                        disabled={isSubmitting}
                        style={{
                            width: "100%",
                            padding: "14px",
                            borderRadius: "10px",
                            border: "1px solid #c4a77d",
                            backgroundColor: "#0f172a",
                            color: "#e7d7bd",
                            fontWeight: "bold",
                            fontSize: "16px",
                            cursor: isSubmitting
                                ? "not-allowed"
                                : "pointer"
                        }}
                    >
                        Back to Dashboard
                    </button>
                </form>
            </div>
        </div>
    );
}
