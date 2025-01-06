import React from 'react';
import PromptForm from './components/PromptForm';
import ImageGenerator from './components/ImageGenerator';

const App: React.FC = () => {
  return (
    <div style={{ padding: '20px' }}>
      <header style={{ textAlign: 'center', marginBottom: '20px' }}>
        <h1>AI Playground</h1>
      </header>
      <PromptForm />
      <ImageGenerator />
    </div>
  );
};

export default App;