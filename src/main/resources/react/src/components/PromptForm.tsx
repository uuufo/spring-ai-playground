import React, { useState } from "react";

const PromptForm: React.FC = () => {
  const [prompt, setPrompt] = useState("");
  const [response, setResponse] = useState<string>("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!prompt.trim()) {
      setError("Please enter a prompt");
      return;
    }

    setLoading(true);
    setError("");
    setResponse("");

    try {
      const eventSource = new EventSource(
        `/api/prompt?prompt=${encodeURIComponent(prompt)}`
      );

      eventSource.onmessage = (event) => {
        setResponse((prevResponse) => prevResponse + event.data);
      };

      eventSource.addEventListener("end", () => {
        setLoading(false);
        eventSource.close();
      });

      eventSource.onerror = () => {
        setError("Error streaming response, please try again.");
        setLoading(false);
        eventSource.close();
      };
    } catch (err) {
      setError("Error sending prompt, please try again.");
      setLoading(false);
    }
  };

  return (
    <div className="mt-4">
      <h2>Prompt</h2>
      <form onSubmit={handleSubmit}>
        <div>
          <textarea
            placeholder="Enter your prompt here"
            value={prompt}
            onChange={(e) => setPrompt(e.target.value)}
            rows={4}
            style={{ width: "100%", marginBottom: "10px" }}
            disabled={loading}
          />
          {error && <p>{error}</p>}
        </div>
        <button type="submit" disabled={loading || !prompt.trim()}>
          {loading ? "Processing..." : "Submit"}
        </button>
      </form>
      {response && (
        <div style={{ whiteSpace: "pre-wrap", wordBreak: "break-word" }}>
          <h3>Response:</h3>
          <p>{response}</p>
        </div>
      )}
    </div>
  );
};

export default PromptForm;
