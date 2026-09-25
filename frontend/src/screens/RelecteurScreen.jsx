import { useState, useEffect } from 'react';
import { api } from '../services/api';

export default function RelecteurScreen() {
  const [etudiantId, setEtudiantId] = useState('');
  const [relectures, setRelectures] = useState([]);
  const [selectedRelecture, setSelectedRelecture] = useState(null);
  const [note, setNote] = useState('');
  const [commentaire, setCommentaire] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const clearMessages = () => { setError(''); setSuccess(''); };

  const loadRelectures = async () => {
    if (!etudiantId) return;
    clearMessages(); setLoading(true);
    try {
      const data = await api.getRelecturesEnAttente(Number(etudiantId));
      setRelectures(data);
      setSuccess(`${data.length} relecture(s) en attente`);
    } catch (e) { setError(e.body?.message || e.message); }
    finally { setLoading(false); }
  };

  const handleSelect = (r) => {
    setSelectedRelecture(r);
    setNote('');
    setCommentaire('');
  };

  const handleRender = async (e) => {
    e.preventDefault(); clearMessages();
    if (!selectedRelecture || note === '' || !commentaire) return setError('Note et commentaire requis');
    setLoading(true);
    try {
      await api.renderRelecture(selectedRelecture.relectureId, Number(etudiantId), Number(note), commentaire);
      setSuccess('Relecture rendue !');
      setSelectedRelecture(null);
      loadRelectures();
    } catch (e) { setError(e.body?.message || e.message); }
    finally { setLoading(false); }
  };

  const handleCorrect = async (e) => {
    e.preventDefault(); clearMessages();
    if (!selectedRelecture || note === '' || !commentaire) return setError('Note et commentaire requis');
    setLoading(true);
    try {
      await api.correctRelecture(selectedRelecture.relectureId, Number(etudiantId), Number(note), commentaire);
      setSuccess('Note corrigée !');
      setSelectedRelecture(null);
      loadRelectures();
    } catch (e) { setError(e.body?.message || e.message); }
    finally { setLoading(false); }
  };

  return (
    <div className="screen">
      <header><h1>Espace Relecteur</h1></header>
      {error && <div className="alert error">{error}</div>}
      {success && <div className="alert success">{success}</div>}

      <section className="card">
        <h2>S'identifier</h2>
        <div className="field-row">
          <div className="field">
            <label>Votre ID étudiant</label>
            <input type="number" value={etudiantId} onChange={(e) => setEtudiantId(e.target.value)} required />
          </div>
          <button onClick={loadRelectures} disabled={loading || !etudiantId} className="btn-secondary">
            {loading ? 'Chargement...' : 'Voir mes relectures'}
          </button>
        </div>
      </section>

      {relectures.length > 0 && (
        <section className="card">
          <h2>Relectures en attente</h2>
          <table>
            <thead>
              <tr><th>Session</th><th>Lien exercice</th><th>Action</th></tr>
            </thead>
            <tbody>
              {relectures.map((r) => (
                <tr key={r.relectureId}>
                  <td>{r.sessionTitre}</td>
                  <td><a href={r.lien} target="_blank" rel="noopener">{r.lien}</a></td>
                  <td><button onClick={() => handleSelect(r)}>Relire</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      )}

      {selectedRelecture && (
        <section className="card form-card">
          <h2>Rendre la relecture</h2>
          <p>Session : <strong>{selectedRelecture.sessionTitre}</strong></p>
          <p>Lien : <a href={selectedRelecture.lien} target="_blank" rel="noopener">{selectedRelecture.lien}</a></p>
          <form onSubmit={handleRender}>
            <div className="field">
              <label>Note (0-20)</label>
              <input type="number" min="0" max="20" value={note} onChange={(e) => setNote(e.target.value)} required />
            </div>
            <div className="field">
              <label>Commentaire</label>
              <textarea value={commentaire} onChange={(e) => setCommentaire(e.target.value)} required rows="4" />
            </div>
            <div className="btn-group">
              <button type="submit" disabled={loading}>{loading ? 'Envoi...' : 'Rendre la relecture'}</button>
              <button type="button" onClick={() => setSelectedRelecture(null)} className="btn-secondary">Annuler</button>
            </div>
          </form>
        </section>
      )}
    </div>
  );
}