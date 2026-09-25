import { useState } from 'react';
import FormateurScreen from './screens/FormateurScreen';
import EtudiantScreen from './screens/EtudiantScreen';
import RelecteurScreen from './screens/RelecteurScreen';
import './App.css';

function App() {
  const [role, setRole] = useState('formateur');

  return (
    <div className="app">
      <nav className="role-nav">
        <button className={role === 'formateur' ? 'active' : ''} onClick={() => setRole('formateur')}>Formateur</button>
        <button className={role === 'etudiant' ? 'active' : ''} onClick={() => setRole('etudiant')}>Étudiant</button>
        <button className={role === 'relecteur' ? 'active' : ''} onClick={() => setRole('relecteur')}>Relecteur</button>
      </nav>
      <main>
        {role === 'formateur' && <FormateurScreen />}
        {role === 'etudiant' && <EtudiantScreen />}
        {role === 'relecteur' && <RelecteurScreen />}
      </main>
    </div>
  );
}

export default App;