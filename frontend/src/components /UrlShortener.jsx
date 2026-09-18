import { useState } from "react";

function UrlShortener() {
    const [longUrl, setLongUrl] = useState("");
    const [shortUrl, setShortUrl] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const handleShorten = async () => {
        if (!longUrl.trim()) {
            setError("Please enter a URL.");
            return;
        }

        setLoading(true);
        setError("");
        setShortUrl("");

        try {
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
                throw new Error("Could not shorten URL");
            }

            const data = await response.json();

            setShortUrl(data.shortUrl);
        } catch (error) {
            console.error(error);
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
                    </div>
                )}
            </div>
        </div>
    );
}

export default UrlShortener;