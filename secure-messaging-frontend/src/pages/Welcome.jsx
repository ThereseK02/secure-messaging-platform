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

                  A modern messaging platform designed to keep conversations private,
    secure, and easy to manage.

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
        maxWidth: "620px",
	minHeight: "auto",
        background: "rgba(15, 42, 74, 0.96)",
        borderRadius: "22px",
        padding:  "clamp(18px, 3vw, 45px)",
        textAlign: "center",
        boxShadow: "0 20px 45px rgba(0, 0, 0, 0.35)",
        border: "1px solid rgba(255, 255, 255, 0.12)",
    },
    
logo: {
    width: "165px",
    height: "clamp(90px, 14vw, 135px)",
    objectFit: "cover",
    objectPosition: "center",
    borderRadius: "50%",
    marginBottom: "22px",
    boxShadow: "0 0 22px rgba(56, 189, 248, 0.35)",
    backgroundColor: "#020617",
    padding: "6px"
},
    welcome: {
        color: "#cbd5e1",
        fontSize: "clamp(20px, 3vw, 28px)",
        marginBottom: "18px",
        fontWeight: "600",
    },
    title: {
        fontSize: "clamp(36px, 6vw, 52px)",
        fontWeight: "800",
        lineHeight: "1.15",
        marginTop: "0",
        marginBottom: "18px",
        color: "#f5efe6",
    },

    tagline: {
        color: "#e0f2fe",
        fontSize: "clamp(18px, 3vw, 24px)",
        fontWeight: "700",
        marginTop: "10px",
        marginBottom: "18px",
    },

    description: {
        color: "#e8dfd1",
        fontSize: "clamp(15px, 2vw, 18px)",
        lineHeight: "1.7",
        maxWidth: "500px",
        margin: "0 auto 24px auto",
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
    color: "#f5efe6",
    border: "2px solid #f5efe6",
    padding: "13px 36px",
    borderRadius: "12px",
    fontSize: "17px",
    fontWeight: "700",
    cursor: "pointer",
},
};
export default Welcome;
