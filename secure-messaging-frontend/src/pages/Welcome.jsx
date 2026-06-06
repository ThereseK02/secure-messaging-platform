import { useNavigate } from "react-router-dom";
import logo from "../assets/branding/secure-messaging-logo.png";

function Welcome() {
    const navigate = useNavigate();

    return (
        <div style={styles.page}>
            <div style={styles.card}>
                <img src={logo} alt="Secure Messaging Logo" style={styles.logo} />

                <h1 style={styles.welcome}>Welcome to</h1>

                <h2 style={styles.title}>Secure Messaging Platform</h2>

                <p style={styles.tagline}>Think secure. Stay ahead.</p>

                <p style={styles.description}>
                    A full-stack secure messaging application built with Spring Boot,
                    React, PostgreSQL, JWT authentication, Docker, Nginx, GitHub Actions,
                    and AWS EC2 deployment.
                </p>

                <div style={styles.buttons}>
                    <button style={styles.loginButton} onClick={() => navigate("/login")}>
                        Login
                    </button>

                    <button style={styles.registerButton} onClick={() => navigate("/register")}>
                        Register
                    </button>
                </div>
            </div>
        </div>
    );
}

const styles = {
    page: {
        minHeight: "100vh",
        background: "radial-gradient(circle at top, #123c69, #071a33 55%, #020617)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        fontFamily: '"Inter", "Segoe UI", Arial, sans-serif',
        padding: "30px",
    },
    card: {
        width: "100%",
        maxWidth: "980px",
        background: "rgba(5, 38, 89, 0.96)",
        borderRadius: "22px",
        padding: "55px 60px",
        textAlign: "center",
        boxShadow: "0 20px 45px rgba(0, 0, 0, 0.35)",
        border: "1px solid rgba(255, 255, 255, 0.12)",
    },
    logo: {
        width: "82px",
        height: "82px",
        objectFit: "contain",
        marginBottom: "22px",
    },
    welcome: {
        color: "#cbd5e1",
        fontSize: "28px",
        margin: "0 0 8px 0",
        fontWeight: "600",
    },
    title: {
        color: "#ffffff",
        fontSize: "44px",
        margin: "0",
        fontWeight: "800",
    },
    tagline: {
        color: "#e0f2fe",
        fontSize: "24px",
        fontWeight: "700",
        marginTop: "14px",
        marginBottom: "28px",
    },
    description: {
        color: "#f8fafc",
        fontSize: "18px",
        lineHeight: "1.8",
        maxWidth: "820px",
        margin: "0 auto 38px auto",
    },
    buttons: {
        display: "flex",
        justifyContent: "center",
        gap: "22px",
        flexWrap: "wrap",
    },
    loginButton: {
        background: "linear-gradient(135deg, #ec4899, #8b5cf6)",
        color: "white",
        border: "none",
        padding: "15px 38px",
        borderRadius: "12px",
        fontSize: "17px",
        fontWeight: "700",
        cursor: "pointer",
    },
    registerButton: {
        background: "transparent",
        color: "white",
        border: "2px solid white",
        padding: "13px 36px",
        borderRadius: "12px",
        fontSize: "17px",
        fontWeight: "700",
        cursor: "pointer",
    },
};

export default Welcome;