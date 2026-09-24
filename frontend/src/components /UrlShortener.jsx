
import { useEffect, useState } from "react";

function UrlShortener() {
    const [longUrl, setLongUrl] = useState("");
    const [shortUrl, setShortUrl] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [stats, setStats] = useState(null);


    useEffect(() => {
        if (!shortUrl) {
            return;
        }

        const shortCode = shortUrl
            .split("/")
            .filter(Boolean)
            .pop();

        const interval = setInterval(async () => {
            try {
                const response = await fetch(
                    `http://localhost:8080/api/urls/${encodeURIComponent(shortCode)}/stats`
                );

                if (response.ok) {
                    const data = await response.json();
                    setStats(data);
                }
            } catch (error) {
                console.error("Could not refresh stats:", error);
            }
        }, 2000);

        return () => {
            clearInterval(interval);
        };
    }, [shortUrl]);

    const handleShorten = async () => {
        if (!longUrl.trim()) {
            setError("Please enter a URL.");
            return;
        }

        setLoading(true);
        setError("");
        setShortUrl("");
        setStats(null);

        try {
            // =========================
            // 1. CREATE SHORT URL
            // =========================
            const response = await fetch("http://localhost:8080/api/urls", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    longUrl: longUrl.trim(),
                }),
            });

            if (!response.ok) {
                const errorText = await response.text();

                console.log("Backend status:", response.status);
                console.log("Backend error:", errorText);

                throw new Error(errorText);
            }

            const data = await response.json();

            console.log("BACKEND RESPONSE:", data);
            console.log("SHORT URL:", data.shortUrl);

            setShortUrl(data.shortUrl);

            // =========================
            // 2. EXTRACT SHORT CODE
            // =========================
            const shortCode = data.shortUrl
                .split("/")
                .filter(Boolean)
                .pop();

            console.log("EXTRACTED SHORT CODE:", shortCode);

            // Safety check
            if (!shortCode) {
                throw new Error("Could not extract short code");
            }

            // =========================
            // 3. GET STATS
            // =========================
            const statsUrl =
                `http://localhost:8080/api/urls/${encodeURIComponent(shortCode)}/stats`;

            console.log("STATS URL:", statsUrl);

            const statsResponse = await fetch(statsUrl);

            if (!statsResponse.ok) {
                console.log(
                    "Stats request failed:",
                    statsResponse.status
                );

                return;
            }

            const statsData = await statsResponse.json();

            console.log("STATS RESPONSE:", statsData);

            setStats(statsData);

        } catch (error) {
            console.error("ERROR:", error);

            setError(
                "Could not shorten the URL. Make sure the backend is running."
            );
        } finally {
            setLoading(false);
        }
    };


    return (
        <div
            style={{
                minHeight: "100vh",
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
                background: "#f5f7fb",
                fontFamily: "Arial, sans-serif",
            }}
        >
            <div
                style={{
                    width: "500px",
                    padding: "40px",
                    background: "white",
                    borderRadius: "15px",
                    boxShadow: "0 10px 30px rgba(0,0,0,0.1)",
                }}
            >
                <h1
                    style={{
                        textAlign: "center",
                        marginBottom: "10px",
                    }}
                >
                    URL Shortener
                </h1>

                <p
                    style={{
                        textAlign: "center",
                        color: "#666",
                        marginBottom: "30px",
                    }}
                >
                    Turn your long URLs into short links
                </p>

                <input
                    type="text"
                    placeholder="Enter your long URL"
                    value={longUrl}
                    onChange={(e) => setLongUrl(e.target.value)}
                    onKeyDown={(e) => {
                        if (e.key === "Enter") {
                            handleShorten();
                        }
                    }}
                    style={{
                        width: "100%",
                        boxSizing: "border-box",
                        padding: "14px",
                        fontSize: "16px",
                        border: "1px solid #ccc",
                        borderRadius: "8px",
                        marginBottom: "15px",
                    }}
                />

                <button
                    onClick={handleShorten}
                    disabled={loading}
                    style={{
                        width: "100%",
                        padding: "14px",
                        fontSize: "16px",
                        border: "none",
                        borderRadius: "8px",
                        cursor: loading ? "not-allowed" : "pointer",
                        background: "#111827",
                        color: "white",
                    }}
                >
                    {loading ? "Shortening..." : "Shorten URL"}
                </button>

                {error && (
                    <p
                        style={{
                            color: "red",
                            marginTop: "20px",
                            textAlign: "center",
                        }}
                    >
                        {error}
                    </p>
                )}

                {shortUrl && (
                    <div
                        style={{
                            marginTop: "30px",
                            padding: "20px",
                            background: "#f0fdf4",
                            borderRadius: "10px",
                        }}
                    >
                        <p
                            style={{
                                marginTop: 0,
                                fontWeight: "bold",
                            }}
                        >
                            Your short URL:
                        </p>

                        <a
                            href={shortUrl}
                            target="_blank"
                            rel="noreferrer"
                            style={{
                                wordBreak: "break-all",
                                fontSize: "16px",
                            }}
                        >
                            {shortUrl}
                        </a>

                        {stats && (
                            <div
                                style={{
                                    marginTop: "20px",
                                    paddingTop: "15px",
                                    borderTop: "1px solid #ddd",
                                }}
                            >
                                <p>
                                    <strong>Clicks:</strong> {stats.clickCount}
                                </p>

                                <p>
                                    <strong>Created:</strong>{" "}
                                    {new Date(stats.createdAt).toLocaleString()}
                                </p>

                                <p>
                                    <strong>Expires:</strong>{" "}
                                    {new Date(stats.expiresAt).toLocaleString()}
                                </p>
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
}

export default UrlShortener;