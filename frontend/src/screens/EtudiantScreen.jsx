import { useState, useEffect } from 'react';
import { api } from '../services/api';
import { useEtudiants } from '../hooks/useEtudiants';

export default function EtudiantScreen() {
  const [promotionId, setPromotionId] = useState('');
  const [selectedEtudiantId, setSelectedEtudiantId] = useState('');
  const [code, setCode] = useState('');
  const [sessionId, setSessionId] = useState('');
  const [lien, setLien] = useState('');
  const [exercices, setExercices] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const { etudiants, loading: loadingEtudiants } = useEtudiants(promotionId);

  const clearMessages = () => { setError(''); setSuccess(''); };

  // Load exercices when student selected
  useEffect(() => {
    if (!selectedEtudiantId) { setExercices([]); return; }
    setLoading(true);
    api.getResultats(selectedEtudiantId)
      .then(setExercices)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [selectedEtudiantId]);

  const handleMarkPresence = async (e) => {
    e.preventDefault(); clearMessages();
    if (!code || !selectedEtudiantId) return setError('Code et étudiant requis');
    setLoading(true);
    try {
      await api.markPresence(code, Number(selectedEtudiantId));
      setSuccess('Présence marquée !');
      setCode('');
    } catch (e) { setError(e.body?.message || e.message); }
    finally { setLoading(false); }
  };

  const handleDepositExercice = async (e) => {
    e.preventDefault(); clearMessages();
    if (!sessionId || !selectedEtudiantId || !lien) return setError('Tous les champs requis');
    setLoading(true);
    try {
      await api.depositExercice(Number(sessionId), Number(selectedEtudiantId), lien);
      setSuccess('Exercice déposé !');
      setLien('');
    } catch (e) { setError(e.body?.message || e.message); }
    finally { setLoading(false); }
  };

  const handleReplaceLien = async (exerciceId) => {
    clearMessages();
    const newLien = prompt('Nouveau lien :');
    if (!newLien) return;
    setLoading(true);
    try {
      await api.replaceExerciceLien(exerciceId, Number(selectedEtudiantId), newLien);
      setSuccess('Lien remplacé !');
    } catch (e) { setError(e.body?.message || e.message); }
    finally { setLoading(false); }
  };

  return (
    <div className="screen">
      <header><h1>Espace Étudiant</h1></header>
      {error && <div className="alert error">{error}</div>}
      {success && <div className="alert success">{success}</div>}

      <section className="card">
        <h2>Choisir la promotion et l'étudiant</h2>
        <div className="field-row">
          <div className="field">
            <label>Promotion ID</label>
            <input type="number" value={promotionId} onChange={(e) => setPromotionId(e.target.value)} required />
          </div>
        </div>
        <div className="field">
          <label>Étudiant</label>
          <select value={selectedEtudiantId} onChange={(e) => setSelectedEtudiantId(e.target.value)} disabled={loadingEtudiants || !promotionId}>
            <option value="">— Sélectionner —</option>
            {etudiants.map((e) => <option key={e.etudiantId} value={e.etudiantId}>{e.nom}</option>)}
          </select>
        </div>
      </section>

      {selectedEtudiantId && (
        <section className="card">
          <h2>Marquer sa présence</h2>
          <form onSubmit={handleMarkPresence} className="presence-form">
            <div className="field">
              <label>Code de présence</label>
              <input type="text" value={code} onChange={(e) => setCode(e.target.value.toUpperCase())} maxLength={6} required />
            </div>
            <button type="submit" disabled={loading}>{loading ? 'Validation...' : 'Valider ma présence'}</button>
          </form>
        </section>
      )}

      {selectedEtudiantId && (
        <section className="card">
          <h2>Déposer son exercice</h2>
          <form onSubmit={handleDepositExercice}>
            <div className="field">
              <label>Session ID</label>
              <input type="number" value={sessionId} onChange={(e) => setSessionId(e.target.value)} required />
            </div>
            <div className="field">
              <label>Lien (GitHub, GitLab, etc.)</label>
              <input type="url" value={lien} onChange={(e) => setLien(e.target.value)} placeholder="https://github.com/..." required />
            </div>
            <button type="submit" disabled={loading}>{loading ? 'Dépôt...' : 'Déposer l\'exercice'}</button>
          </form>
        </section>
      )}

      {selectedEtudiantId && (
        <section className="card">
          <h2>Mes exercices et résultats</h2>
          {loading ? <p>Chargement...</p> : exercices.length === 0 ? <p>Aucun exercice déposé.</p> : (
            <table>
              <thead>
                <tr><th>Session</th><th>Lien</th><th>Statut</th><th>Note</th><th>Commentaire</th><th>Action</th></tr>
              </thead>
              <tbody>
                {exercices.map((ex) => (
                  <tr key={ex.exerciceId}>
                    <td>{ex.sessionTitre}</td>
                    <td><a href={ex.lien} target="_blank" rel="noopener">{ex.lien}</a></td>
                    <td><span className={`statut ${ex.statut.toLowerCase()}`}>{ex.statut}</span></td>
                    <td>{ex.note !== null ? ex.note : '—'}</td>
                    <td>{ex.commentaire || '—'}</td>
                    <td>
                      {ex.statut === 'EN_ATTENTE' && (
                        <button onClick={() => handleReplaceLien(ex.exerciceId)}>Remplacer</button>
                      )}
                    </td>
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