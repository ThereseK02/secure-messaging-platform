
import { useState } from "react";
import {
    useNavigate,
    useSearchParams
} from "react-router-dom";
import api from "../services/api";
import logo from "../assets/branding/secure-messaging-logo.png";

export default function Register() {

    const navigate = useNavigate();
    const [searchParams] = useSearchParams();

    const invitationToken =
        searchParams.get("invitationToken") || "";

    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [notification, setNotification] = useState(null);

function showNotification(type, text) {
    setNotification({ type, text });

    setTimeout(() => {
        setNotification(null);
    }, 3500);
}

    async function handleRegister(e) {

        e.preventDefault();
        if (!username.trim()) {
            showNotification(
                "error",
                "Username is required."
            );
            return;
        }

        if (!email.trim()) {
            showNotification(
                "error",
                "Email is required."
            );
            return;
        }

        if (!password) {
            showNotification(
                "error",
                "Password is required."
            );
            return;
        }

        if (!confirmPassword) {
            showNotification(
                "error",
                "Please confirm your password."
            );
            return;
        }
        const passwordLength =
            Array.from(password).length;

        const passwordBytes =
            new TextEncoder().encode(password).length;

        if (passwordLength < 15) {
            showNotification(
                "error",
                "Password must be at least 15 characters."
            );
            return;
        }

        if (passwordBytes > 72) {
            showNotification(
                "error",
                "Password must not exceed 72 UTF-8 bytes."
            );
            return;
        }

        if (password !== confirmPassword) {
            showNotification(
                "error",
                "Passwords do not match."
            );
            return;
        }

        try {
            await api.post("/users/register", {
                username,
                email,
                password,
                invitationToken
            });
showNotification("success", "Registration successful. Redirecting to login...");

setTimeout(() => {
    navigate("/login");
}, 1500);

        } catch (error) {

            console.error(error);
            showNotification(
                "error",
                error.response?.data?.message ||
                error.response?.data?.error ||
                "Registration failed"
            );
        }
    }

    return (

        <div
            style={{
                backgroundColor: "#021024",
                minHeight: "100vh",
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
                fontFamily: "Arial, sans-serif",
                padding: "clamp(12px, 3vw, 20px)",
                width: "100%"
            }}
        >

            <div
                style={{
                    backgroundColor: "#052659",
                    padding: "4px 36px",
                    borderRadius: "20px",
                    width: "100%",
                    maxWidth: "462px",
                    display: "flex",
                    flexDirection: "column",
                    boxShadow: "0 0 25px rgba(0,0,0,0.4)"
                }}
            >

                <img
                    src={logo}
                    alt="Secure Messaging"
                    style={{
                        width: "165px",
                        height: "165px",
                        objectFit: "cover",
                        objectPosition: "center",
                        borderRadius: "50%",
                        display: "block",
                        margin: "0 auto 18px auto",
                        boxShadow: "0 0 22px rgba(56, 189, 248, 0.35)"
                    }}
                />

                <h1
                    style={{
                        color: "white",
                        textAlign: "center",
                        marginBottom: "10px",
                        fontSize: "42px"
                    }}
                >
                    Register
                </h1>

                <p
                    style={{
                        color: "#38bdf8",
                        textAlign: "center",
                        marginBottom: invitationToken
                            ? "14px"
                            : "30px"
                    }}
                >
                    Create your secure account
                </p>

                {invitationToken && (
                    <div
                        style={{
                            backgroundColor: "#172554",
                            border: "1px solid #c4a77d",
                            color: "#e7d7bd",
                            padding: "12px",
                            borderRadius: "10px",
                            marginBottom: "24px",
                            textAlign: "center",
                            fontSize: "14px"
                        }}
                    >
                        Complete registration with the email address
                        that received this group invitation.
                    </div>
                )}

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
            textAlign: "center",
            fontWeight: "bold"
        }}
    >
        {notification.text}
    </div>
)}

                <form onSubmit={handleRegister} noValidate>

                    <input
                        type="text"
                        autoComplete="username"
                        required
                        placeholder="Username"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        style={{
                            width: "100%",
                            padding: "14px",
                            marginBottom: "24px",
                            borderRadius: "10px",
                            border: "1px solid #2563eb",
                            backgroundColor: "#0f172a",
                            color: "white",
                            fontSize: "16px",
                            boxSizing: "border-box"
                        }}
                    />

                    <input
                        type="email"
                        autoComplete="email"
                        required
                        placeholder="Email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        style={{
                            width: "100%",
                            padding: "14px",
                            marginBottom: "24px",
                            borderRadius: "10px",
                            border: "1px solid #2563eb",
                            backgroundColor: "#0f172a",
                            color: "white",
                            fontSize: "16px",
                            boxSizing: "border-box"
                        }}
                    />

                    <input
                        type="password"
                        placeholder="Password"
                        value={password}
                        onChange={(e) =>
                            setPassword(e.target.value)}
                        autoComplete="new-password"
                        minLength={15}
                        maxLength={72}
                        required
                        style={{
                            width: "100%",
                            padding: "14px",
                            marginBottom: "10px",
                            borderRadius: "10px",
                            border: "1px solid #2563eb",
                            backgroundColor: "#0f172a",
                            color: "white",
                            fontSize: "16px",
                            boxSizing: "border-box"
                        }}
                    />

                    <p
                        style={{
                            color: "#94a3b8",
                            fontSize: "13px",
                            lineHeight: "1.45",
                            marginTop: "0",
                            marginBottom: "16px"
                        }}
                    >
                        Use at least 15 characters. Passphrases and spaces
                        are allowed.
                    </p>

                    <input
                        type="password"
                        placeholder="Confirm password"
                        value={confirmPassword}
                        onChange={(e) =>
                            setConfirmPassword(e.target.value)}
                        autoComplete="new-password"
                        minLength={15}
                        maxLength={72}
                        required
                        style={{
                            width: "100%",
                            padding: "14px",
                            marginBottom: "30px",
                            borderRadius: "10px",
                            border: "1px solid #2563eb",
                            backgroundColor: "#0f172a",
                            color: "white",
                            fontSize: "16px",
                            boxSizing: "border-box"
                        }}
                    />

                    <button
                        type="submit"
                        style={{
                            width: "100%",
                            padding: "14px",
                            background: "linear-gradient(to right, #2563eb, #7c3aed)",
                            border: "none",
                            borderRadius: "10px",
                            color: "white",
                            fontWeight: "bold",
                            fontSize: "16px",
                            cursor: "pointer",
                            transition: "0.3s ease",
                            boxShadow: "0 4px 15px rgba(124,58,237,0.35)"
                        }}
                    >
                        Register
                    </button>

                    <div
                        style={{
                            marginTop: "24px",
                            textAlign: "center",
                            color: "#64748b"
                        }}
                    >

                        <hr
                            style={{
                                border: "0.5px solid rgba(59,130,246,0.25)",
                                marginBottom: "10px"
                            }}
                        />

                        <p
                            style={{
                                color: "#64748b",
                                marginBottom: "8px",
                                fontWeight: "500",
                                fontSize: "12px"
                            }}
                        >
                            Connect
                        </p>

                        <div
                            style={{
                                display: "flex",
                                justifyContent: "center",
                                alignItems: "flex-start",
                                gap: "28px",
                                fontSize: "11px"
                            }}
                        >

                            <div style={{ minWidth: "100px" }}>
                                <div style={{ color: "white", marginBottom: "2px" }}>
                                    GitHub
                                </div>

                                <a
                                    href="https://github.com"
                                    target="_blank"
                                    rel="noreferrer"
                                    style={{
                                        color: "#38bdf8",
                                        textDecoration: "none",
                                        whiteSpace: "nowrap"
                                    }}
                                >
                                    github.com
                                </a>
                            </div>

                            <div style={{ minWidth: "100px" }}>
                                <div style={{ color: "white", marginBottom: "2px" }}>
                                    LinkedIn
                                </div>

                                <a
                                    href="https://linkedin.com"
                                    target="_blank"
                                    rel="noreferrer"
                                    style={{
                                        color: "#38bdf8",
                                        textDecoration: "none",
                                        whiteSpace: "nowrap"
                                    }}
                                >
                                    linkedin.com
                                </a>
                            </div>

                            <div style={{ minWidth: "140px" }}>
                                <div style={{ color: "white", marginBottom: "2px" }}>
                                    Website
                                </div>

                                <a
                                    href="https://brain-secure-messaging.com"
                                    target="_blank"
                                    rel="noreferrer"
                                    style={{
                                        color: "#38bdf8",
                                        textDecoration: "none",
                                        whiteSpace: "nowrap"
                                    }}
                                >
                                    brain-secure-messaging.com
                                </a>
                            </div>

                        </div>

                        <p
                            style={{
                                marginTop: "12px",
                                fontSize: "11px",
                                color: "#64748b"
                            }}
                        >
                            © 2026 Secure Messaging System
                        </p>

                    </div>

                </form>

            </div>

        </div>
    );
}
