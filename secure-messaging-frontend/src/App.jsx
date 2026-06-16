import React from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";

import Welcome from "./pages/Welcome";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import SendMessage from "./pages/SendMessage";
import Inbox from "./pages/Inbox";
import GroupChat from "./pages/GroupChat";
import ProtectedRoute from "./ProtectedRoute";

function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Welcome />} />
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />
                <Route
                    path="/dashboard"
                    element={
                        <ProtectedRoute>
                            <Dashboard />
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/send"
                    element={
                        <ProtectedRoute>
                            <SendMessage />
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/inbox"
                    element={
                        <ProtectedRoute>
                            <Inbox />
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/groups"
                    element={
                        <ProtectedRoute>
                            <GroupChat />
                        </ProtectedRoute>
                    }
                />
            </Routes>
        </BrowserRouter>
    );
}

export default App;
