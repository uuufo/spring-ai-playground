import React, { useState } from "react";
import axios from "axios";

const ImageGenerator: React.FC = () => {
  const [description, setDescription] = useState("");
  const [imageUrl, setImageUrl] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleGenerate = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!description.trim()) {
      setError("Please enter an image description");
      return;
    }

    setLoading(true);
    setError("");

    try {
      const { data } = await axios.post("/api/generate-image", { description });
      setImageUrl(data.imageUrl);
      setError("");
    } catch (error) {
      setImageUrl("");
      setError("Error generating image, please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mt-4">
      <h2>Image Generator</h2>
      <form onSubmit={handleGenerate}>
        <input
          type="text"
          placeholder="Describe the image"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          style={{ width: "100%", marginBottom: "10px" }}
        />
        {error && <p>{error}</p>}

        <button type="submit" disabled={loading || !description.trim()}>
          {loading ? "Generating..." : "Generate Image"}
        </button>
      </form>

      {!loading && imageUrl && (
        <div>
          <h3>Generated Image:</h3>
          <img
            src={imageUrl}
            alt="Generated"
            style={{ width: "100%" }}
            onError={() => {
              setImageUrl("");
              setError("Failed to load image");
            }}
          />
        </div>
      )}
    </div>
  );
};

export default ImageGenerator;
