import { BrowserRouter, Routes, Route } from "react-router-dom";

import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import SendMessage from "./pages/SendMessage";
import Inbox from "./pages/Inbox";

function App() {

    return (
        <BrowserRouter>

            <Routes>

                <Route path="/" element={<Login />} />

                <Route path="/register" element={<Register />} />

                <Route path="/dashboard" element={<Dashboard />} />

                <Route path="/send" element={<SendMessage />} />

                <Route path="/inbox" element={<Inbox />} />

            </Routes>

        </BrowserRouter>
    );
}

export default App;
