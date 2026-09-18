import { useState } from "react";

function UrlShortener() {
    const [longUrl, setLongUrl] = useState("");
    const [shortUrl, setShortUrl] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const handleShorten = async () => {
        if (!longUrl.trim()) {
            setError("Please enter a URL");
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
                    longUrl: longUrl,
                }),
            });

            if (!response.ok) {
                throw new Error("Failed to shorten URL");
            }

            const data = await response.json();

            setShortUrl(data.shortUrl);
        } catch (err) {
            console.error(err);
            setError("Something went wrong. Make sure the backend is running.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div>
            <h1>URL Shortener</h1>

            <input
                type="text"
                placeholder="Enter your long URL"
                value={longUrl}
                onChange={(e) => setLongUrl(e.target.value)}
            />

            <button onClick={handleShorten} disabled={loading}>
                {loading ? "Shortening..." : "Shorten URL"}
            </button>

            {shortUrl && (
                <div>
                    <p>Your short URL:</p>

                    <a
                        href={shortUrl}
                        target="_blank"
                        rel="noreferrer"
                    >
                        {shortUrl}
                    </a>
                </div>
            )}

            {error && <p>{error}</p>}
        </div>
    );
}

export default UrlShortener;