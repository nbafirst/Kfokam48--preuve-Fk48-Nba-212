import { useState, useEffect } from 'react';
import { api } from '../services/api';

export default function FormateurScreen() {
  const [activeTab, setActiveTab] = useState('session');
  const [promotionId, setPromotionId] = useState('');
  const [sessionTitre, setSessionTitre] = useState('');
  const [sessionCode, setSessionCode] = useState('');
  const [expirationAt, setExpirationAt] = useState('');
  const [clotureAt, setClotureAt] = useState('');
  const [sessionId, setSessionId] = useState('');
  const [tableau, setTableau] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const clearMessages = () => { setError(''); setSuccess(''); };

  const handleCreateSession = async (e) => {
    e.preventDefault(); clearMessages();
    if (!sessionTitre || !promotionId) return setError('Titre et promotion requis');
    setLoading(true);
    try {
      const data = await api.createSession(sessionTitre, Number(promotionId));
      setSessionCode(data.code);
      setExpirationAt(new Date(data.expirationAt).toLocaleString());
      setSessionId(data.id);
      setSuccess('Session créée avec succès !');
    } catch (e) { setError(e.body?.message || e.message); }
    finally { setLoading(false); }
  };

  const handleCloseSession = async () => {
    if (!sessionId) return;
    clearMessages(); setLoading(true);
    try {
      const data = await api.closeSession(sessionId);
      setClotureAt(new Date(data.clotureAt).toLocaleString());
      setSuccess('Session clôturée');
    } catch (e) { setError(e.body?.message || e.message); }
    finally { setLoading(false); }
  };

  const handleLoadTableau = async () => {
    if (!promotionId) return setError('Promotion requise');
    clearMessages(); setLoading(true);
    try {
      const data = await api.getTableau(Number(promotionId));
      setTableau(data);
      setSuccess('Tableau chargé');
    } catch (e) { setError(e.body?.message || e.message); }
    finally { setLoading(false); }
  };

  return (
    <div className="screen">
      <header><h1>Espace Formateur</h1></header>
      <nav className="tabs">
        <button className={activeTab === 'session' ? 'active' : ''} onClick={() => setActiveTab('session')}>Session</button>
        <button className={activeTab === 'tableau' ? 'active' : ''} onClick={() => setActiveTab('tableau')}>Tableau</button>
      </nav>

      {error && <div className="alert error">{error}</div>}
      {success && <div className="alert success">{success}</div>}

      {activeTab === 'session' && (
        <section className="card">
          <h2>Créer une session</h2>
          <form onSubmit={handleCreateSession}>
            <div className="field">
              <label>Promotion ID</label>
              <input type="number" value={promotionId} onChange={(e) => setPromotionId(e.target.value)} required />
            </div>
            <div className="field">
              <label>Titre de la session</label>
              <input type="text" value={sessionTitre} onChange={(e) => setSessionTitre(e.target.value)} required />
            </div>
            <button type="submit" disabled={loading}>{loading ? 'Création...' : 'Créer la session'}</button>
          </form>

          {sessionCode && (
            <div className="result-card">
              <h3>Session créée</h3>
              <p><strong>ID :</strong> {sessionId}</p>
              <p><strong>Code de présence :</strong> <code>{sessionCode}</code></p>
              <p><strong>Expire le :</strong> {expirationAt}</p>
              <button onClick={handleCloseSession} disabled={loading || clotureAt}>
                {clotureAt ? 'Déjà clôturée' : 'Clôturer la session'}
              </button>
              {clotureAt && <p className="cloture">Clôturée le : {clotureAt}</p>}
            </div>
          )}
        </section>
      )}

      {activeTab === 'tableau' && (
        <section className="card">
          <h2>Tableau récapitulatif</h2>
          <div className="field">
            <label>Promotion ID</label>
            <input type="number" value={promotionId} onChange={(e) => setPromotionId(e.target.value)} />
            <button onClick={handleLoadTableau} disabled={loading}>{loading ? 'Chargement...' : 'Charger'}</button>
          </div>

          {tableau.length > 0 && (
            <table>
              <thead>
                <tr>
                  <th>Étudiant</th>
                  <th>Présences</th>
                  <th>Exercices déposés</th>
                  <th>Moyenne</th>
                  <th>Relectures en attente</th>
                </tr>
              </thead>
              <tbody>
                {tableau.map((row) => (
                  <tr key={row.etudiantId}>
                    <td>{row.nom}</td>
                    <td>{row.presences}</td>
                    <td>{row.exercicesDeposes}</td>
                    <td>{row.moyenne !== null ? row.moyenne.toFixed(2) : '—'}</td>
                    <td>{row.relecturesEnAttente}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>
      )}
    </div>
  );
}