import { BrowserRouter, Routes, Route } from "react-router-dom";
import Welcome from "./pages/Welcome";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import SendMessage from "./pages/SendMessage";
import Inbox from "./pages/Inbox";
import GroupChat from "./pages/GroupChat";

function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Welcome />} />
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />
                <Route path="/dashboard" element={<Dashboard />} />
                <Route path="/send" element={<SendMessage />} />
                <Route path="/inbox" element={<Inbox />} />
                <Route path="/groups" element={<GroupChat />} />
            </Routes>
        </BrowserRouter>
    );
}

export default App;