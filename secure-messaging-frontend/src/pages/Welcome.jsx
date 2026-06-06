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
	minHeight: "760px",
        background: "rgba(15, 42, 74, 0.96)",
        borderRadius: "22px",
        padding: "70px 55px",
        textAlign: "center",
        boxShadow: "0 20px 45px rgba(0, 0, 0, 0.35)",
        border: "1px solid rgba(255, 255, 255, 0.12)",
    },
    
logo: {
    width: "165px",
    height: "165px",
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
    fontSize: "28px",
    marginBottom: "24px",
    fontWeight: "600",
},

title: {
    fontSize: "52px",
    fontWeight: "800",
    lineHeight: "1.25",
    marginTop: "0",
    marginBottom: "24px",
    color: "#f5efe6",
},

    tagline: {
        color: "#e0f2fe",
        fontSize: "24px",
        fontWeight: "700",
        marginTop: "14px",
        marginBottom: "40px",
    },

description: {
    color: "#e8dfd1",
    fontSize: "18px",
    lineHeight: "2",
    maxWidth: "500px",
    margin: "0 auto 50px auto",
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
