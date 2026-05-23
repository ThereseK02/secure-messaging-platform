import { useState } from "react";
import api from "../services/api";

export default function Register() {

    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");

    async function handleRegister(e) {

        e.preventDefault();

        try {

            await api.post("/users/register", {
                username,
                password
            });

            alert("Registration successful");

        } catch (error) {

            console.error(error);
            alert("Registration failed");
        }
    }

    return (

        <div
            style={{
                backgroundColor: "#021024",
                height: "100vh",
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
                fontFamily: "Arial"
            }}
        >

            <div
                style={{
                    backgroundColor: "#052659",
                    padding: "40px",
                    borderRadius: "12px",
                    width: "350px"
                }}
            >

                <h1
                    style={{
                        color: "#00d4ff",
                        textAlign: "center"
                    }}
                >
                    Register
                </h1>

                <form onSubmit={handleRegister}>

                    <input
                        type="text"
                        placeholder="Username"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        style={{
                            width: "100%",
                            padding: "12px",
                            marginBottom: "20px",
                            borderRadius: "8px"
                        }}
                    />

                    <input
                        type="password"
                        placeholder="Password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        style={{
                            width: "100%",
                            padding: "12px",
                            marginBottom: "20px",
                            borderRadius: "8px"
                        }}
                    />

                    <button
                        type="submit"
                        style={{
                            width: "100%",
                            padding: "12px",
                            backgroundColor: "#00d4ff",
                            border: "none",
                            borderRadius: "8px",
                            fontWeight: "bold"
                        }}
                    >
                        Register
                        <p style={{ color: "#94a3b8", fontSize: "12px", textAlign: "center" }}>
                            Development test account: testuser / Test123!
                        </p>
                    </button>

                </form>

            </div>

        </div>
    );
}