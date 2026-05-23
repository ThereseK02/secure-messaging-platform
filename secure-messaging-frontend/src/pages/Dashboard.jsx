import { useNavigate } from "react-router-dom";

export default function Dashboard() {

    const navigate = useNavigate();

    const token = localStorage.getItem("token");

    function handleLogout() {

        localStorage.removeItem("token");

        navigate("/");
    }

    if (!token) {

        navigate("/");

        return null;
    }

    return (

        <div
            style={{
                minHeight: "100vh",
                backgroundColor: "#020617",
                color: "white",
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
                fontFamily: "Arial, sans-serif"
            }}
        >

            <div
                style={{
                    backgroundColor: "#0f172a",
                    padding: "40px",
                    borderRadius: "14px",
                    width: "500px",
                    textAlign: "center",
                    border: "1px solid #1e293b",
                    boxShadow: "0 0 20px rgba(56,189,248,0.08)"
                }}
            >

                <h1
                    style={{
                        color: "#38bdf8",
                        fontSize: "52px",
                        lineHeight: "1.15",
                        marginBottom: "36px",
                        textAlign: "center",
                        fontWeight: "bold"
                    }}
                >
                    <div>Secure Messaging</div>

                    <div>Dashboard</div>
                </h1>

                <p
                    style={{
                        color: "#cbd5e1",
                        marginBottom: "10px"
                    }}
                >
                    Authenticated successfully.
                </p>

                <p
                    style={{
                        color: "#94a3b8",
                        marginBottom: "30px"
                    }}
                >
                    Your JWT session is active.
                </p>

                <div
                    style={{
                        display: "flex",
                        flexDirection: "column",
                        gap: "14px"
                    }}
                >

                    <button
                        onClick={() => navigate("/send")}
                        style={{
                            padding: "12px",
                            borderRadius: "8px",
                            border: "none",
                            backgroundColor: "#38bdf8",
                            fontWeight: "bold",
                            cursor: "pointer"
                        }}
                    >
                        Send Message
                    </button>

                    <button
                        onClick={() => navigate("/inbox")}
                        style={{
                            padding: "12px",
                            borderRadius: "8px",
                            border: "none",
                            backgroundColor: "#1e293b",
                            color: "white",
                            fontWeight: "bold",
                            cursor: "pointer"
                        }}
                    >
                        Inbox
                    </button>

                    <button
                        onClick={handleLogout}
                        style={{
                            padding: "12px",
                            borderRadius: "8px",
                            border: "none",
                            backgroundColor: "#ef4444",
                            color: "white",
                            fontWeight: "bold",
                            cursor: "pointer"
                        }}
                    >
                        Logout
                    </button>

                </div>

            </div>

        </div>
    );
}